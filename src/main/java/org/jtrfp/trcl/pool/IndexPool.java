package org.jtrfp.trcl.pool;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexPool{
	private final Queue<Integer> freeIndices = new LinkedBlockingQueue<Integer>();
	private int maxCapacity=1;
	private int highestIndex=-1;
	private GrowthBehavior growthBehavior=new GrowthBehavior()
		{public int grow(int index){return index*2;}};//Default is to double each time.
	
	public IndexPool(){
	}
		
	public synchronized int pop()
		{if(!freeIndices.isEmpty())
			{return freeIndices.remove();}
		else if(highestIndex+1<maxCapacity)
			{return (++highestIndex);}
		else//Need to allocate a new block of indices
			{maxCapacity = growthBehavior.grow(maxCapacity);
			return pop();//Try again.
			}
		}//end pop()
	
	public synchronized int free(int index)
		{freeIndices.add(index);return index;}
	
	public static interface GrowthBehavior
		{int grow(int previousMaxCapacity);}
	public void setGrowthBehavior(GrowthBehavior gb){growthBehavior=gb;}
	
	public static void main(String [] args){
	    System.out.println("Testing index pool");
	    final IndexPool ip = new IndexPool();
	    /*
	    for(int i=0; i<20; i++){
		System.out.println("pop(): "+ip.pop());
	    }for(int i=0; i<10; i++){
		System.out.println("free(): "+ip.free(i));
	    }//end for(i)
	    */
	   /*for(int i=0; i<5; i++){
		System.out.println(ip.popConsecutive(5)+"");
	    }
	    System.out.println("free: "+ip.freeIndices.size()+" highest: "+ip.highestIndex+" max: "+ip.maxCapacity);*/
	}

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
}//end IndexPool
