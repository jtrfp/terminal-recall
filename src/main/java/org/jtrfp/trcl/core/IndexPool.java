package org.jtrfp.trcl.core;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class IndexPool
	{
	private final Queue<Integer> freeIndices = new LinkedBlockingQueue<Integer>();
	private int maxCapacity=1;
	private int highestIndex=0;
	private GrowthBehavior growthBehavior=new GrowthBehavior()
		{public int grow(int index){return index*2;}};//Default is to double each time.
	
	public IndexPool(){
	    freeIndices.add(0);
	}
		
	public int pop()
		{if(!freeIndices.isEmpty())
			{return freeIndices.remove();}
		else if(highestIndex+1<maxCapacity)
			{return ++highestIndex;}
		else//Need to allocate a new block of indices
			{maxCapacity = growthBehavior.grow(maxCapacity);
			return pop();//Try again.
			}
		}//end pop()
	
	public int free(int index)
		{freeIndices.add(index);return index;}
	
	static interface GrowthBehavior
		{int grow(int previousMaxCapacity);}
	void setGrowthBehavior(GrowthBehavior gb){growthBehavior=gb;}
	
	public static void main(String [] args){
	    System.out.println("Testing index pool");
	    final IndexPool ip = new IndexPool();
	    for(int i=0; i<200; i++){
		System.out.println("pop(): "+ip.pop());
	    }for(int i=0; i<100; i++){
		System.out.println("free(): "+ip.free(i));
	    }//end for(i)
	    System.out.println("free: "+ip.freeIndices.size()+" highest: "+ip.highestIndex+" max: "+ip.maxCapacity);
	}
	}//end IndexPool
