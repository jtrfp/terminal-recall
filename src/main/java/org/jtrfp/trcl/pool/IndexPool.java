/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.PriorityBlockingQueue;

public class IndexPool{
        public static final String     NUM_UNUSED_INDICES = "numUnusedIndices",
        	                       NUM_USED_INDICES   = "numUsedIndices";
    
	private final PriorityBlockingQueue<Integer>
	                                freeIndices     = new PriorityBlockingQueue<Integer>();
	private final PriorityBlockingQueue<Integer>
                                        usedIndices     = new PriorityBlockingQueue<Integer>();
	private volatile int 		maxCapacity	= 0;
	private volatile int 		highestIndex	= -1;
	private volatile int            numUnusedIndices= 0;
	private volatile int            numUsedIndices  = 0;
	private GrowthBehavior 		growthBehavior	= new GrowthBehavior(){
	    public int grow(int index)//CAUTION: Never let this return zero.
	     {return index==0?1:index*2;}
	    public int shrink(int minDesiredSize)
	     {return minDesiredSize;}
	    };//Default is to double each time, and shrink to exact minimum.
	private int hardLimit=Integer.MAX_VALUE;//Basically no hard limit by default
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	public IndexPool(){
	}
	
	private void updateNumUnusedIndices(){
	    setNumUnusedIndices(freeIndices.size());
	}
	
	private void setNumUnusedIndices(int numUnusedIndices){
	    pcs.firePropertyChange(NUM_UNUSED_INDICES, this.numUnusedIndices, numUnusedIndices);
	    this.numUnusedIndices=numUnusedIndices;
	}
	/**
	 * Discard the trailing free indices of this pool. This is not defragmentation. Fragmentation will 
	 * persist except for all unused indices between the greatest used index and the maxCapacity.
	 * @return Number of indices removed.
	 * @since Mar 19, 2015
	 */
	public synchronized int compact(){
	    //Used low to high
	    ArrayList<Integer> used = new ArrayList<Integer>();
	    for(Integer i:usedIndices)
		used.add(i);
	    Collections.sort(used);
	    //Unused high to low
	    ArrayList<Integer> unused = new ArrayList<Integer>();
	    for(Integer i:freeIndices)
		unused.add(i);
	    Collections.sort(unused,Collections.reverseOrder());
	    
	    //Checks
	    HashSet<Integer> redundancyChecker  = new HashSet<Integer>();
	    for(Integer i:usedIndices)
		assert redundancyChecker.add(i):"Duplicate entry: "+i;
	    redundancyChecker.clear();
	    for(Integer i:freeIndices)
		assert redundancyChecker.add(i):"Duplicate entry: "+i;
	    
	    int item, removalTally=0;
	     final int greatestUsed = !used.isEmpty()?used.get(used.size()-1):-1;
	     
	     final Iterator<Integer> unusedIterator = unused.iterator();
	     boolean run = true;
	     while(run){
		if(unusedIterator.hasNext()){
		    item = unusedIterator.next();
		    if(item>greatestUsed){
			freeIndices.remove(item);
			removalTally++;
			}
		    else run=false;
		}else run=false;
	     }//end while(run)
	    final int proposedNewMaxCapacity = Math.max(0,greatestUsed+1);//+1 since size()==maxIndex+1
	    assert proposedNewMaxCapacity<=maxCapacity:"proposedNewMaxCapacity="+proposedNewMaxCapacity+" maxCapacity="+maxCapacity;
	    assert proposedNewMaxCapacity>=usedIndices.size():"proposedNewMaxCapacity="+proposedNewMaxCapacity+" usedIndices.size()="+usedIndices.size();
	    maxCapacity  = growthBehavior.shrink(proposedNewMaxCapacity);
	    highestIndex = greatestUsed;
	    updateNumUnusedIndices();
	    updateNumUsedIndices();
	    return removalTally;
	}//end compact()
	
    public synchronized int pop(){
	final int result = innerPop();
	if(result!=-1){
	    usedIndices.add(result);
	    updateNumUnusedIndices();
	    updateNumUsedIndices();
	}
	return result;
    }
    private int innerPop(){
	try{return innerPopOrException();}
	catch(OutOfIndicesException e){
	    return -1;
	    }//Shouldn't happen.
    }//end pop()
    
    public synchronized int popOrException() throws OutOfIndicesException{
	final int index = innerPopOrException();
	usedIndices.add(index);
	updateNumUnusedIndices();
	updateNumUsedIndices();
	return index;
    }
    
    private int innerPopOrException() throws OutOfIndicesException{
	final int index = pop(true);
	return index;
    }
    
    public synchronized int pop(Collection<Integer> dest, int count){
	final ArrayList<Integer> temp = new ArrayList<Integer>();
	final int remaining = innerPop(temp,count);
	dest       .addAll(temp);
	usedIndices.addAll(temp);
	updateNumUnusedIndices();
	updateNumUsedIndices();
	return remaining;
    }
    
    private int innerPop(Collection<Integer> dest, int count){
	try{popOrException(dest,count);return 0;}
	catch(OutOfIndicesException e){
	    return count;
	    }
    }//end pop(...)
    
    public synchronized void popOrException(Collection<Integer> dest, int count) throws OutOfIndicesException{
	pop(dest,count,true);//Narrow point
    }//end pop(...)
    
    private int pop(Collection<Integer> dest, int count, boolean throwException) throws OutOfIndicesException{
	 count-=freeIndices.drainTo(dest,count);
	 if(count>0) {
	    synchronized (this) {
		if (highestIndex + count >= hardLimit){
		    if(throwException)throw new OutOfIndicesException();
		    else {
			return count;}
		    }//end if()
		if (highestIndex + count < maxCapacity){
		     int remaining = availablePop(dest,count);
		     return remaining;
		     }
		else {
		    int remaining = growthPop(dest,count); 
		    return remaining;
		}
	    }//end sync(this)
	}//end catch{no element}
	assert count>=0;
	return count;
    }//end pop(...)
    
    private int pop(boolean throwException) throws OutOfIndicesException {
	try {final int index = freeIndices.remove();
	     assert index>=0:"Popped index from freeIndices is interolerably negative.";
	     return index;
	} catch (NoSuchElementException e) {
	    synchronized (this) {
		if (highestIndex + 1 >= hardLimit)
		    if(throwException)throw new OutOfIndicesException();
		    else try{
			     final int index = freeIndices.take();
			     assert !usedIndices.contains(index):index;
			     return index;}
			catch(InterruptedException ex){
			    ex.printStackTrace(); 
			    assert false:"Unexpected interruption.";
			    return -1;
			    }
		if (highestIndex + 1 < maxCapacity){
		    final int index = availablePop();
		    assert !usedIndices.contains(index):index;
		    return index;
		}
		else {
		    final int index = growthPop();
		    assert !usedIndices.contains(index):index;
		    return index;
		    }
	    }//end sync(this)
	}//end catch{no element}
    }// end pop()
    
    	private int availablePop(Collection<Integer>dest, int count){
    	    while(count-->0)
    		dest.add(availablePop());
    	    return 0;
    	}
    	
    	private int growthPop(Collection<Integer>dest, int count){
    	    final int origMaxCapacity = maxCapacity;
    	    maxCapacity = growthBehavior.grow(maxCapacity);
    	    if(maxCapacity==origMaxCapacity)
    		throw new RuntimeException("GrowthBehavior will not grow past "+origMaxCapacity);
    	    return innerPop(dest,count);
    	}
	
	private int availablePop(){
	    int result = ++highestIndex;
	    assert !usedIndices.contains(result):result;
	    return result;
	    }
	
	private int growthPop(){
	    maxCapacity = growthBehavior.grow(maxCapacity);
	    return innerPop();//Try again.
	}//end grothPop()
	
	public PriorityBlockingQueue<Integer> getFreeIndices(){
	    return freeIndices;
	}
	
	public PriorityBlockingQueue<Integer> getUsedIndices(){
	    return usedIndices;
	}
	
	public synchronized int free(int index){
	    if(freeIndices.contains(index))
		throw new IllegalArgumentException("Double-release of resources: "+index);
	    if(index<0){
		throw new IllegalArgumentException("Index is intolerably negative: "+index);}
	    if(!usedIndices.remove(index))
		throw new IllegalArgumentException("Cannot free an index which is not in use: "+index);
	    freeIndices.add(index);
	    //usedIndices.remove(index);
	    updateNumUnusedIndices();
	    updateNumUsedIndices();
	    return index;
	}
	
	public static interface GrowthBehavior{
	    int grow(int previousMaxCapacity);
	    /**
	     * Requests the backing implementation to shrink to an arbitrary size no smaller than the specified capacity.
	     * The behavior may choose to not shrink at all.
	     * @param minDesiredCapacity The minimum size allowable resulting from this shrink.
	     * @return Positive quantity of the new size, no smaller than minDesiredCapacity.
	     * @since Mar 19, 2015
	     */
	    int shrink(int minDesiredCapacity);
	    }
	public void setGrowthBehavior(GrowthBehavior gb){growthBehavior=gb;}
/*
	public synchronized int popConsecutive(int numNewItems) {
	    //TODO This should use the freed pool instead of always allocating new
	    int result;
	    if(highestIndex+numNewItems<maxCapacity)
		{result = highestIndex+1; highestIndex+=numNewItems;
		updateNumUnusedIndices();
		updateNumUsedIndices();
		return result;}
	    else//Need to allocate a new block of indices
		{maxCapacity = growthBehavior.grow(maxCapacity);
		result = popConsecutive(numNewItems);
		updateNumUnusedIndices();
		updateNumUsedIndices();
		return result;//Try again.
		}
	}//end popConsecutive(...)
*/
	/**
	 * @return the maxCapacity
	 */
	public int getMaxCapacity() {
	    return maxCapacity;
	}
	
	public static class OutOfIndicesException extends Exception {}
	/**
	 * @return the hardLimit
	 */
	public int getHardLimit() {
	    return hardLimit;
	}

	/**
	 * @param hardLimit the hardLimit to set
	 */
	public IndexPool setHardLimit(int hardLimit) {
	    this.hardLimit = hardLimit;
	    return this;
	}

	public synchronized void free(Collection<Integer> indicesToFree) {
	    freeIndices.addAll(indicesToFree);
	    usedIndices.removeAll(indicesToFree);
	    updateNumUnusedIndices();
	    updateNumUsedIndices();
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}
	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}
	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}
	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
	 */
	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}
	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}
	/**
	 * @return the numUnusedIndices
	 */
	public int getNumUnusedIndices() {
	    return numUnusedIndices;
	}

	/**
	 * @return the numUsedIndices
	 */
	public int getNumUsedIndices() {
	    return numUsedIndices;
	}

	private void updateNumUsedIndices(){
	    setNumUsedIndices(usedIndices.size());
	}
	private void setNumUsedIndices(int numUsedIndices) {
	    pcs.firePropertyChange(NUM_USED_INDICES, this.numUsedIndices, numUsedIndices);
	    this.numUsedIndices = numUsedIndices;
	}

	public synchronized void freeAll() {//TODO: compactFreeAll() with clear() instead of drain()
	    usedIndices.drainTo(freeIndices);
	    updateNumUnusedIndices();
	    updateNumUsedIndices();
	    //growthBehavior.shrink(0);
	}
}//end IndexPool
