package org.jtrfp.trcl.math;

public class Misc {
    public static void scalarMultiply(float [] vals, float scalar){
	for(int i=0; i<vals.length; i++){
	    vals[i]*=scalar;
	}
    }//end scalarMutliply(...)
    public static void clamp(float []vals, float min, float max){
	for(int i=0; i<vals.length; i++){
	    vals[i]=Math.min(Math.max(vals[i], min), max);
	}
    }//end clamp(...)
    
    public static void round(float []vals){
	for(int i=0; i<vals.length; i++)
	    vals[i]=Math.round(vals[i]);
    }
}
