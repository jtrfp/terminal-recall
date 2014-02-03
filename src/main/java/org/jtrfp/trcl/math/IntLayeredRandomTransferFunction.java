package org.jtrfp.trcl.math;

import org.jtrfp.trcl.IntTransferFunction;

public class IntLayeredRandomTransferFunction implements IntTransferFunction {
 //private static final int ORDER_SIZE=256;
 //private static final int [] lookupTable = new int[ORDER_SIZE];
 
  private final int layers;
  public IntLayeredRandomTransferFunction(int layers){
      this.layers=layers;
  }
    @Override
    public int transfer(int input) {
	int result=0;
	for(int o=0; o<layers; o++){
	    result+=(IntRandomTransferFunction.lookupTable[(int)(input*Math.pow(.5, o))%256]/Math.pow(2, layers));
	}//end for(order)
	return result;
    }//end transfer(...)
}//end IntRandomTransferFunction
