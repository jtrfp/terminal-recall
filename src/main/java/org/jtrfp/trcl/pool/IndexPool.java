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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.PriorityBlockingQueue;

public class IndexPool{
	private final PriorityBlockingQueue<Integer>
	                                freeIndices     = new PriorityBlockingQueue<Integer>();
	private final PriorityBlockingQueue<Integer>
                                        usedIndices     = new PriorityBlockingQueue<Integer>();
	private volatile int 		maxCapacity	= 1;
	private volatile int 		highestIndex	= -1;
	private GrowthBehavior 		growthBehavior	= new GrowthBehavior(){
	    public int grow(int index)
	     {return index*2;}
	    public int shrink(int minDesiredSize)
	     {return minDesiredSize;}
	    };//Default is to double each time, and shrink to exact minimum.
	private int hardLimit=Integer.MAX_VALUE;//Basically no hard limit by default
	
	public IndexPool(){
	}
	/**
	 * INCOMPLETE
	 * Discard the trailing free indices of this pool. This is not defragmentation. Fragmentation will 
	 * persist except for all unused indices between the greatest used index and the maxCapacity.
	 * @return
	 * @since Mar 19, 2015
	 */
	public int compact(){
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
	    final int greatestUsed = used.get(used.size()-1);
	    final Iterator<Integer> unusedIterator = unused.iterator();
	    int item, removalTally=0;
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
	    final int proposedNewMaxCapacity = maxCapacity-removalTally;
	    maxCapacity = growthBehavior.shrink(proposedNewMaxCapacity);
	    return removalTally;
	}//end compact()
	
    public int pop(){
	final int result = innerPop();
	usedIndices.add(result);
	return result;
    }
    private int innerPop(){
	try{return innerPopOrException();}
	catch(OutOfIndicesException e){
	    e.printStackTrace();
	    assert false;
	    return -1;
	    }//Shouldn't happen.
    }//end pop()
    
    public int popOrException() throws OutOfIndicesException{
	final int index = innerPopOrException();
	usedIndices.add(index);
	return index;
    }
    
    private int innerPopOrException() throws OutOfIndicesException{
	final int index = pop(true);
	return index;
    }
    
    public int pop(Collection<Integer> dest, int count){
	final ArrayList<Integer> temp = new ArrayList<Integer>();
	final int result = innerPop(temp,count);
	assert temp.size()==count:"temp.size()="+temp.size()+" count="+count;
	dest       .addAll(temp);
	usedIndices.addAll(temp);
	return result;
    }
    
    private int innerPop(Collection<Integer> dest, int count){
	try{popOrException(dest,count);return 0;}
	catch(OutOfIndicesException e){
	    return count;
	    }//Shouldn't happen.
    }//end pop(...)
    
    private void popOrException(Collection<Integer> dest, int count) throws OutOfIndicesException{
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
		     int result = availablePop(dest,count);
		     return result;
		     }
		else {
		    int result = growthPop(dest,count); 
		    return result;
		}
	    }//end sync(this)
	}//end catch{no element}
	assert count>=0;
	return count;
    }//end pop(...)
    
    private int pop(boolean throwException) throws OutOfIndicesException {
	try {final int index = freeIndices.remove();
	     return index;
	} catch (NoSuchElementException e) {
	    synchronized (this) {
		if (highestIndex + 1 >= hardLimit)
		    if(throwException)throw new OutOfIndicesException();
		    else try{
			     final int index = freeIndices.take();
			     return index;}
			catch(InterruptedException ex){
			    ex.printStackTrace(); 
			    assert false:"Unexpected interruption.";
			    return -1;
			    }
		if (highestIndex + 1 < maxCapacity){
		    final int index = availablePop();
		    return index;
		}
		else {
		    final int index = growthPop();
		    return index;
		    }
	    }//end sync(this)
	}//end catch{no element}
    }// end pop()
    
    	private int availablePop(Collection<Integer>dest, int count){
    	    while(count-->0)
    		dest.add(++highestIndex);
    	    return 0;
    	}
    	
    	private int growthPop(Collection<Integer>dest, int count){
    	    maxCapacity = growthBehavior.grow(maxCapacity);
    	    return innerPop(dest,count);
    	}
	
	private int availablePop()
	    {return (++highestIndex);}
	
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
	
	public int free(int index){
	    	if(freeIndices.contains(index)){
		    throw new RuntimeException("Double-release of resources: "+index);
		}
		    freeIndices.add(index);
		    usedIndices.remove(index);
		    return index;}
	
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

	public synchronized int popConsecutive(int numNewItems) {
	    //TODO This should use the freed pool instead of always allocating new
	    if(highestIndex+numNewItems<maxCapacity)
		{final int result = highestIndex+1; highestIndex+=numNewItems;
		return result;}
	    else//Need to allocate a new block of indices
		{maxCapacity = growthBehavior.grow(maxCapacity);
		return popConsecutive(numNewItems);//Try again.
		}
	}//end popConsecutive(...)

	/**
	 * @return the maxCapacity
	 */
	public int getMaxCapacity() {
	    return maxCapacity;
	}
	
	public static class OutOfIndicesException extends Exception{}
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

	public void free(Collection<Integer> intArrayList) {
	    freeIndices.addAll(intArrayList);
	    usedIndices.removeAll(intArrayList);
	}
}//end IndexPool
