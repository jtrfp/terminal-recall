package org.jtrfp.trcl.math;

public class Misc {
    public static void scalarMultiply(float [] vals, float scalar){
	for(int i=0; i<vals.length; i++){
	    vals[i]*=scalar;
	}
    }
}
