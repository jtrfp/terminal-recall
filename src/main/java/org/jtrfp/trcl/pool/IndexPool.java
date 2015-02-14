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
			    assert false;
			    return -1;
			    }
		if (highestIndex + 1 < maxCapacity)
		    return availablePop();
		else return growthPop();
	    }//end sync(this)
	}//end catch{no element}
    }// end pop()
	
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
}//end IndexPool
