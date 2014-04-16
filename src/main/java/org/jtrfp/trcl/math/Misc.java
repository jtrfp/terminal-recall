package org.jtrfp.trcl.math;


public class Misc {
    
    public static void main(String [] args){
	byte test = Img.compileInsertJumpPair(15,0);
	System.out.println(Img.getInsert(test));
	System.out.println(Img.getJump(test));
    }
    
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
    public static void scalarAdd(float[] vals, float scalar) {
	for(int i=0; i<vals.length; i++){
	    vals[i]+=scalar;
	}
    }
    public static void setAll(float[] vals, float value) {
	for(int i=0; i<vals.length; i++){
	    vals[i]=value;
	}
    }
    public static void normalize(float[] vals, float min, float max, boolean scale) {
	float mmin=Float.MAX_VALUE;
	float mmax=Float.MIN_VALUE;
	final float desiredAmplitude = max-min;
	final float center = (max+min)/2;
	for(int i=0; i<vals.length; i++){
	    final float val = vals[i];
	    mmin=val<mmin?val:mmin;
	    mmax=val>mmax?val:mmax;
	}
	final float currentAmplitude = mmax-mmin;
	final float mCenter=(mmax+mmin)/2;
	final float scaleAdjust = desiredAmplitude/currentAmplitude;
	scalarAdd(vals,-mCenter);//First pull down to origin 0
	if(scale)scalarMultiply(vals,scaleAdjust);
	scalarAdd(vals,center);
    }
    public static void nudge(float[] vals, int min, int max) {
	float mmin=Float.MAX_VALUE;
	float mmax=Float.MIN_VALUE;
	for(int i=0; i<vals.length; i++){
	    final float val = vals[i];
	    mmin=val<mmin?val:mmin;
	    mmax=val>mmax?val:mmax;
	}
	if(mmin<min){scalarAdd(vals,min-mmin); return;}
	if(max<mmax)scalarAdd(vals,max-mmax);
	//Nothing else to do
    }
    
    /**
     * Float variant of Apache Commons' ArrayMath.ebeDivide(...)
     * Stores to numerator
     * 
     * @since Mar 23, 2014
     */
    public static void ebeDiv(float [] numerator, float [] denominator){
	for(int i=0; i<numerator.length; i++){
	    numerator[i]/=denominator[i];
	}
    }//end ebeDiv(...)
    /**
     * Element-by-element multiplication of floats. Stores to left.
     * @param left
     * @param right
     * @since Mar 23, 2014
     */
    public static void ebeMul(float[] left, float[] right) {
	for(int i=0; i<left.length; i++){
	    left[i]*=right[i];
	}
    }
    
    public static double sigmoid(double in){
	return 1./(1+Math.pow(Math.E, -in));
    }
    public static double logit(double in){
	return Math.log(in/(1.-in));
    }
    public static double byteToNorm101(byte value){
	return (value)/128.;
    }
    public static byte norm101toByte(double norm){
	return (byte)clamp(Math.round(norm*127.),-128,127);
    }
    public static double clamp(double val, int min, int max){
	return Math.min(Math.max(val, min), max);
    }
    //public static byte

    public static void printArray(float[] work8x8, int entriesPerRow) {
	StringBuilder sb = new StringBuilder();
	sb.append("\n\nARRAY:\n");
	for(int i=0; i<work8x8.length/entriesPerRow; i++){
	    for(int ei=0; ei<entriesPerRow; ei++){
		try{sb.append(work8x8[ei+i*entriesPerRow]+" ");}
		catch(ArrayIndexOutOfBoundsException e){sb.append("\n\t");System.out.print(sb);}
	    }sb.append("\n");}
	System.out.print(sb);
    }
    public static void printArray(int[] work8x8, int entriesPerRow) {
	StringBuilder sb = new StringBuilder();
	sb.append("\n\nARRAY:\n");
	for(int i=0; i<work8x8.length/entriesPerRow; i++){
	    for(int ei=0; ei<entriesPerRow; ei++){
		try{sb.append(work8x8[ei+i*entriesPerRow]+" ");}
		catch(ArrayIndexOutOfBoundsException e){sb.append("\n\t");System.out.print(sb);}
	    }sb.append("\n");}
	System.out.print(sb);
    }
    
    /**
     * 
     * @param freq	Periods per cycle.
     * @param phase	One cycle is domain of [0,1] repeatable. No negatives.
     * @return
     * @since Mar 25, 2014
     */
    public static float cosTriangle(float freq, float phase){
	return Math.abs(1f-((phase*freq*2)%2));
    }
}//end Misc
