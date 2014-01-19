package org.jtrfp.trcl.math;

public class Mat4x4 {
    public static double [] set(int row, int col, double val, double[] dest){
	dest[row*4+col]=val;
	return dest;
    }//end set(...)
    public static double [] mul(double [] l, double [] r, double [] dest){
	for(int i=0; i<16; i++){
	    final int col=i%4;
	    final int row=i/4;
	    dest[i]=0;
	    for(int k=0; k<4; k++){
		dest[i]+=l[row*4+k]*r[k*4+col];
	    }
	}//end for(index:16)
	return dest;
    }//end mul(...)
}//end Mat4x4
