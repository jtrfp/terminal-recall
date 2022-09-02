/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.pool;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.NoSuchElementException;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;

/**
 * Pool of objects for re-use later with modularly-specified pooling method, preparation method, and generative method.
 * Pooling methods give behavior on how to organize and manage the pool, such as recycling in a flat round-robin (forced deactivation)
 * or creating new objects as needed if depleted using the generativeMethod.
 * <br>
 * Generative method takes the task of creating new objects to be pooled at initialization, mid-runtime, or both.
 * <br>
 * Preparation method takes the task of activating pooled objects when needed and deactivating them when they expire.
 * @author Chuck Ritola
 *
 * @param <TYPE> The type of object being pooled.
 */
public class ObjectPool<TYPE> {
    private final PoolingMethod<TYPE> poolingMethod;
    private final PreparationMethod<TYPE> preparationMethod;
    //private final GenerativeMethod<TYPE> generativeMethod;
    public ObjectPool(PoolingMethod<TYPE> poolingMethod, PreparationMethod<TYPE> preparationMethod, GenerativeMethod<TYPE> generativeMethod){
	this.poolingMethod=poolingMethod;
	this.preparationMethod=preparationMethod;
	//this.generativeMethod=generativeMethod;
	this.poolingMethod.initialize(this,preparationMethod,generativeMethod);
    }//end constructor
    
    /**
     * Report that the specified object has expired. This method will use the preparation method's deactivation facilities
     * on the supplied object.
     * @param obj
     * @return Monad of obj.
     * @since Feb 5, 2014
     */
    public TYPE expire(TYPE obj){
	return poolingMethod.notifyExpiration(preparationMethod.deactivate(obj));}
    /**
     * Pop an unused object off the pool, which may be recycled or brand new, depending on the pooling method supplied at instantiation.
     * @return An unused, activated object from the pool.
     * @since Feb 5, 2014
     */
    public TYPE pop(){
	return preparationMethod.reactivate(poolingMethod.pop());}
    public static interface PoolingMethod<TYPE>{
	/**
	 * Called by the ObjectPool constructor for the PoolingMethod to initialize, typically to build the initial pool
	 * but could be used for other things if need be.
	 * @param parent
	 * @param preparationMethod
	 * @param generativeMethod
	 * @return "this" Monad.
	 * @since Feb 5, 2014
	 */
	PoolingMethod<TYPE> initialize(ObjectPool<TYPE> parent, PreparationMethod<TYPE> preparationMethod, GenerativeMethod<TYPE> generativeMethod);
	/**
	 * Notifies this PoolingMethod that the specified object has expired.
	 * @param obj
	 * @return Monad of obj.
	 * @since Feb 5, 2014
	 */
	TYPE notifyExpiration(TYPE obj);
	/**
	 * Requests that an unused object the popped form this PoolingMethod by a means specified by the PoolingMethod itself..
	 * @return Unused, activated object from the pool.
	 * @since Feb 5, 2014
	 */
	TYPE pop();
	/**
	 * Requests that several unused objects be popped from this PoolingMethod by a means specified by the PoolingMethod itself.
	 * The submitted objects are not guaranteed to be consecutive.
	 * @param target
	 * @param numItems
	 * @return Monad of target.
	 * @since Feb 5, 2014
	 */
	Submitter<TYPE> pop(Submitter<TYPE> target, int numItems);
	/**
	 * Requests that several unused objects be popped from this PoolingMethod by a means specified by the PoolingMethod itself.
	 * The submitted objects are guaranteed to be consecutive. If consecutive relations are irrelevant to the implementation, this
	 * method shall behave identically to pop(Submitter<TYPE>,int)
	 * @param target
	 * @param numItems
	 * @return Monad of target.
	 * @since Feb 5, 2014
	 */
	Submitter<TYPE> popConsective(Submitter<TYPE> target, int numItems);
	/**
	 * Returns the Submitter used to populate this PoolingMethod in an in-order non-indexed fashion.
	 * @return
	 * @since Feb 5, 2014
	 */
	Submitter<TYPE> getPopulationTarget();
    }
    public static interface PreparationMethod<TYPE>{
	/**
	 * Invokes implementation necessary to deactivate the supplied object to put it into its dormant state in the pool.
	 * @param obj	Object to be deactivated.
	 * @return Monad of obj.
	 * @since Feb 5, 2014
	 */
	public TYPE deactivate(TYPE obj);
	/**
	 * Invokes implementation necessary to activate/initialize the supplied object to take it from the pool and put into active use.
	 * @param obj	Object to be activated.
	 * @return Monad of obj.
	 * @since Feb 5, 2014
	 */
	public TYPE reactivate(TYPE obj);
    }
    
    public static final class LazyAllocate<TYPE> implements PoolingMethod<TYPE>{
	private final Deque<TYPE> unused      = new ArrayDeque<TYPE>();
	private final Deque<TYPE> used        = new ArrayDeque<TYPE>();
	private GenerativeMethod <TYPE> generativeMethod;
	private final PopulationTarget populationTarget = new PopulationTarget();
	private int maxSize = Integer.MAX_VALUE;
	
	@Override
	public PoolingMethod<TYPE> initialize(ObjectPool<TYPE> parent,
		PreparationMethod<TYPE> preparationMethod,
		GenerativeMethod<TYPE> generativeMethod) {
	    this.generativeMethod  = generativeMethod;
	    return this;
	}//end initialize()

	@Override
	public TYPE notifyExpiration(TYPE obj) {
	    if(!used.contains(obj))
		throw new IllegalStateException("Cannot expire; records show this isn't checked out: "+obj);
	    assert !unused.contains(obj);
	    unused.add(obj);
	    used.remove(obj);
	    return obj;
	}

	@Override
	public TYPE pop() {
	    TYPE result;
	    //First try unused pool
	    try{result = unused.pop();
		used.push(result);
	    }catch(NoSuchElementException e){
		//Not available. Create new
		generateBlock();
		result = pop();
	    }
	    return result;
	}//end pop()
	
	private void generateBlock(){
	    final int size = used.size()+unused.size();
	    if(size+generativeMethod.getAtomicBlockSize() > getMaxSize())
		throw new IllegalStateException("Reached max size. Cannot generate more. Size="+size+" Blocksize="+generativeMethod.getAtomicBlockSize());
	    generativeMethod.generateConsecutive(1, getPopulationTarget());
	}

	@Override
	public Submitter<TYPE> pop(Submitter<TYPE> target, int numItems) {
	    for(int i=0; i<numItems; i++)
		target.submit(pop());
	    return target;
	}

	@Override
	public Submitter<TYPE> popConsective(Submitter<TYPE> target,
		int numItems) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public Submitter<TYPE> getPopulationTarget() {
	    return populationTarget;
	}
	
	private class PopulationTarget extends AbstractSubmitter<TYPE>{
	    @Override
	    public void submit(TYPE item) {
		unused.push(item);
	    }
	}//end PopulationTarget

	public int getMaxSize() {
	    return maxSize;
	}

	public LazyAllocate<TYPE> setMaxSize(int maxSize) {
	    this.maxSize = maxSize;
	    return this;
	}
	
    }//end LazyAllocate
    
    public static final class FillThenDrain<TYPE> implements PoolingMethod<TYPE>{
	private final ArrayList<TYPE> pool = new ArrayList<TYPE>();
	private int counter=0;
	private final int initialSizeInElements;
	private PreparationMethod<TYPE> preparationMethod;
	//private GenerativeMethod<TYPE> generativeMethod;
	
	public FillThenDrain(int initialSize){
	    this.initialSizeInElements=initialSize;
	}//end constructor
	@Override
	public PoolingMethod<TYPE> initialize(ObjectPool<TYPE> parent,
		PreparationMethod<TYPE> preparationMethod,
		GenerativeMethod<TYPE> generativeMethod) {
	    this.preparationMethod=preparationMethod;
	    //this.generativeMethod=generativeMethod;
	    generativeMethod.generateConsecutive(initialSizeInElements, populationTarget);
	    return this;
	}//end initialize()
	private final Submitter<TYPE> populationTarget = new AbstractSubmitter<TYPE>(){
	    @Override
	    public void submit(TYPE item) {
		pool.add(item);
	    }//end submit(...)
	};
	@Override
	public TYPE notifyExpiration(TYPE obj) {
	    throw new RuntimeException("Feature not supported.");
	}
	@Override
	public TYPE pop() {
	    final TYPE result= pool.get(counter++);
	    counter=counter>=pool.size()?pool.size()-1:counter;
	    return result;}
	@Override
	public Submitter<TYPE> pop(Submitter<TYPE> target, int numItems) {
	    for(int i=0; i<numItems; i++){
		target.submit(pop());
	    }//end for(items)
	    return target;
	}
	@Override
	public Submitter<TYPE> popConsective(Submitter<TYPE> target,
		int numItems) {
	    return pop(target,numItems);
	}
	@Override
	public Submitter<TYPE> getPopulationTarget() {
	    return populationTarget;
	}
	public PoolingMethod<TYPE> drain(){
	    for(int i=counter; i>=0; i--){
		preparationMethod.deactivate(pool.get(counter));
	    }//end for(counter)
	    counter=0;
	    return this;
	}//end drain()
	
    }//end FillThenDrain<TYPE>
    
    /**
     * Static-sized round
     * @author Chuck Ritola
     *
     * @param <TYPE>
     */
    public static final class RoundRobin<TYPE> implements PoolingMethod<TYPE>{
	private final ArrayList<TYPE> pool = new ArrayList<TYPE>();
	private int counter=0;
	private final int initialSizeInElements;
	public RoundRobin(int initialSizeInElements){
	    this.initialSizeInElements=initialSizeInElements;
	}
	@Override
	public TYPE notifyExpiration(TYPE obj) {
	    return obj;}
	@Override
	public TYPE pop() {
	    final TYPE result= pool.get(counter++);
	    counter%=pool.size();
	    return result;}
	@Override
	public Submitter<TYPE> getPopulationTarget() {
	    return populationTarget;
	}
	private final Submitter<TYPE> populationTarget = new AbstractSubmitter<TYPE>(){
	    @Override
	    public void submit(TYPE item) {
		pool.add(item);
	    }//end submit(...)
	};
	@Override
	public Submitter<TYPE> pop(Submitter<TYPE> target, int numItems) {
	    for(int i=0; i<numItems; i++){
		target.submit(pop());
	    }
	    return target;
	}//end popMultiple
	@Override
	public Submitter<TYPE> popConsective(Submitter<TYPE> target,
		int numItems) {
	    return pop(target, numItems);
	}
	@Override
	public PoolingMethod<TYPE> initialize(ObjectPool<TYPE> parent,
		PreparationMethod<TYPE> preparationMethod,
		GenerativeMethod<TYPE> generativeMethod) {
	    final int numBlocks = (int)Math.ceil((double)initialSizeInElements/(double)generativeMethod.getAtomicBlockSize());
	    generativeMethod.generateConsecutive(numBlocks,getPopulationTarget());
	    return this;
	}
    }//end RoundRobin
    /*
    public static final class Recycler<TYPE> implements PoolingMethod<TYPE>{
	private ArrayList<TYPE> pool = new ArrayList<TYPE>();
	
	public Recycler(){
	    
	}//end constructor

	@Override
	public TYPE notifyExpiration(TYPE obj) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public TYPE pop() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public TYPE populate(TYPE obj, int index) {
	    // TODO Auto-generated method stub
	    return null;
	}
    }//end Recycler
    */
    /**
     * Responsible for handling the creation of new objects in an ObjectPool. GeneartiveMethod-s work in terms of 
     * "blocks" of objects to accommodate creation systems with quantized grouping however a block size of 1 
     * can be used to bypass this trait.
     * @author Chuck Ritola
     *
     * @param <TYPE>
     */
    public static interface GenerativeMethod<TYPE>{
	/**
	 * Get the number of objects created when a single block is requested from generateConsecutive()
	 * @return	number of objects created per block.
	 * @since Feb 5, 2014
	 */
	public int getAtomicBlockSize();
	/**
	 * Generates new objects to add to a pool, consecutive if applicable. Quantity is numBlocks * getAtomicBlockSize()
	 * @param numBlocks`	Number of blocks to request.
	 * @param populationTarget	Submitter to which to write these new objects.
	 * @return Monad of populationTarget.
	 * @since Feb 5, 2014
	 */
	public Submitter<TYPE> generateConsecutive(int numBlocks, Submitter<TYPE>populationTarget);
    }//end GenerativeMethod
}//end ObjectPool
