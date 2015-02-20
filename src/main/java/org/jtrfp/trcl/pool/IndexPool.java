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

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexPool{
	private final BlockingQueue<Integer> freeIndices= new LinkedBlockingQueue<Integer>();
	private volatile int 		maxCapacity	= 1;
	private volatile int 		highestIndex	= -1;
	private GrowthBehavior 		growthBehavior	= new GrowthBehavior()
		{public int grow(int index){return index*2;}};//Default is to double each time.
	private int hardLimit=Integer.MAX_VALUE;//Basically no hard limit by default
	
	public IndexPool(){
	}
	
    public int pop(){
	try{return pop(false);}
	catch(OutOfIndicesException e){
	    e.printStackTrace();
	    assert false;
	    return -1;
	    }//Shouldn't happen.
    }//end pop()
    
    public int popOrException() throws OutOfIndicesException{
	return pop(true);
    }
    
    public int pop(Collection<Integer> dest, int count){
	try{return pop(dest,count,false);}
	catch(OutOfIndicesException e){
	    e.printStackTrace();
	    assert false;
	    return count;
	    }//Shouldn't happen.
    }//end pop(...)
    
    public void popOrException(Collection<Integer> dest, int count) throws OutOfIndicesException{
	pop(dest,count,true);
    }//end pop(...)
    
    private int pop(Collection<Integer> dest, int count, boolean throwException) throws OutOfIndicesException{
	 count-=freeIndices.drainTo(dest,count);
	 if(count>0) {
	    synchronized (this) {
		if (highestIndex + count >= hardLimit){
		    if(throwException)throw new OutOfIndicesException();
		    else return count;
		    }//end if()
		if (highestIndex + count < maxCapacity)
		     return availablePop(dest,count);
		else return growthPop(dest,count);
	    }//end sync(this)
	}//end catch{no element}
	assert count>=0;
	return count;
    }//end pop(...)
    
    private int pop(boolean throwException) throws OutOfIndicesException {
	try {
	    return freeIndices.remove();
	} catch (NoSuchElementException e) {
	    synchronized (this) {
		if (highestIndex + 1 >= hardLimit)
		    if(throwException)throw new OutOfIndicesException();
		    else try{return freeIndices.take();}
			catch(InterruptedException ex){
			    ex.printStackTrace(); 
			    assert false:"Unexpected interruption.";
			    return -1;
			    }
		if (highestIndex + 1 < maxCapacity)
		    return availablePop();
		else return growthPop();
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
    	    return pop(dest,count);
    	}
	
	private int availablePop()
	    {return (++highestIndex);}
	
	private int growthPop(){
	    maxCapacity = growthBehavior.grow(maxCapacity);
	    return pop();//Try again.
	}//end grothPop()
	
	public int free(int index){
	    	if(freeIndices.contains(index)){
		    throw new RuntimeException("Double-release of resources: "+index);
		}
		    freeIndices.add(index);return index;}
	
	public static interface GrowthBehavior
		{int grow(int previousMaxCapacity);}
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
	}
}//end IndexPool
