package org.jtrfp.trcl.math;

import org.jtrfp.trcl.IntTransferFunction;

public class IntRandomTransferFunction implements IntTransferFunction {
 static final int ORDER_SIZE=256;
 static final int [] lookupTable = new int[ORDER_SIZE];
 static{
     for(int i=0; i<ORDER_SIZE; i++){
	 lookupTable[i]=(int)(Math.random()*Integer.MAX_VALUE*(Math.random()>.5?1:-1));}
 }//end static
 
    @Override
    public int transfer(int input) {
	return lookupTable[input%256];
    }//end transfer(...)
}//end IntRandomTransferFunction
