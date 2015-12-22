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
package org.jtrfp.trcl.math;

public final class Vect3D {
    private static final boolean CATCH_NAN = true;
    private static final boolean CATCH_POS_INF = false;
    private static final boolean CATCH_NEG_INF = false;

    public static void subtract(double[] l, double[] r, double[] dest) {
	dest[0]=l[0]-r[0];
	dest[1]=l[1]-r[1];
	dest[2]=l[2]-r[2];
	checkValues(l);
	checkValues(r);
	checkValues(dest);
    }//end subtract
    
    public static boolean isAnyEqual(double [] valuesToCheck, double compare){
	boolean result = false;
	for(double d:valuesToCheck)
	    result |= d==compare;
	return result;
    }//end isAnyNaN()
    
    public static boolean isAnyEqual(float [] valuesToCheck, float compare){
	boolean result = false;
	for(float f:valuesToCheck)
	    result |= f==compare;
	return result;
    }//end isAnyEqual()
    
    public static boolean isAnyNaN(float... valuesToCheck) {
	boolean result = false;
	for(float f:valuesToCheck)
	    result |= Float.isNaN(f);
	return result;
    }
    
    public static boolean isAnyNaN(double... valuesToCheck) {
	boolean result = false;
	for(double d:valuesToCheck)
	    result |= Double.isNaN(d);
	return result;
    }
    
    // If all switches are false, this should completely compile out in JIT
    private static void checkValues(double [] vals){
	if(CATCH_NAN)
	    assert !isAnyNaN(vals);
	if(CATCH_POS_INF)
	    assert !isAnyEqual(vals,Double.POSITIVE_INFINITY);
	if(CATCH_NEG_INF)
	    assert !isAnyEqual(vals,Double.NEGATIVE_INFINITY);
    }
    
    private static void checkValues(float [] vals){
	if(CATCH_NAN)
	    assert !isAnyNaN(vals);
	if(CATCH_POS_INF)
	    assert !isAnyEqual(vals,Float.POSITIVE_INFINITY);
	if(CATCH_NEG_INF)
	    assert !isAnyEqual(vals,Float.NEGATIVE_INFINITY);
    }

    public static double[] scalarMultiply(double [] src,
	    double scalar, double[] dest) {
	dest[0]=src[0]*scalar;
	dest[1]=src[1]*scalar;
	dest[2]=src[2]*scalar;
	checkValues(src);
	checkValues(dest);
	return dest;
    }
    
    public static double[] normalize(double[] src){
	return normalize(src,new double[3]);
    }
    
    public static double[] normalize(double[] src, double [] dest) {
	final double norm = norm(src);
	if(norm==0)
	    throw new IllegalArgumentException("Cannot normalize zero-norm vector.");
	checkValues(src);
	dest[0]=src[0]/norm;
	dest[1]=src[1]/norm;
	dest[2]=src[2]/norm;
	checkValues(dest);
	return dest;
    }

    public static double norm(double[] src) {
	final double x=src[0];
	final double y=src[1];
	final double z=src[2];
	checkValues(src);
	return Math.sqrt(x*x+y*y+z*z);
    }

    public static double [] add(double[] l, double rx, double ry,
	    double rz, double[] dest) {
	dest[0]=l[0]+rx;
	dest[1]=l[1]+ry;
	dest[2]=l[2]+rz;
	checkValues(l);checkValues(dest);
	return dest;
    }

    public static double[] add(double[] l, double[] r,
	    double[] dest) {
	dest[0]=l[0]+r[0];
	dest[1]=l[1]+r[1];
	dest[2]=l[2]+r[2];
	checkValues(l);
	checkValues(r);
	checkValues(dest);
	return dest;
    }

    public static double distance(double[] l, double[] r) {
	checkValues(l);
	checkValues(r);
	final double dx=l[0]-r[0];
	final double dy=l[1]-r[1];
	final double dz=l[2]-r[2];
	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    public static double taxicabDistance(double [] l, double [] r){
	checkValues(l);
	checkValues(r);
	final double dx=Math.abs(l[0]-r[0]);
	final double dy=Math.abs(l[1]-r[1]);
	final double dz=Math.abs(l[2]-r[2]);
	return dx+dy+dz;
    }

    public static double[] negate(double[] in) {
	in[0]=-in[0];
	in[1]=-in[1];
	in[2]=-in[2];
	return in;
    }
    
    public static double [] cross(double [] l, double [] r, double []dest){
	checkValues(l);
	checkValues(r);
	dest[0]=l[1]*r[2]-l[2]*r[1];
	dest[1]=l[2]*r[0]-l[0]*r[2];
	dest[2]=l[0]*r[1]-l[1]*r[0];
	return dest;
    }

    public static double distanceXZ(double[] l, double[] r) {
	checkValues(l);checkValues(r);
	final double dx=l[0]-r[0];
	final double dz=l[2]-r[2];
	return Math.sqrt(dx*dx + dz*dz);
    }

    public static float [] normalize(float[] val) {
	checkValues(val);
	final double norm = norm(val);
	val[0]/=norm;
	val[1]/=norm;
	val[2]/=norm;
	return val;
    }

    private static double norm(float[] val) {
	checkValues(val);
	int a=0;
	for(float v:val)
	    a+=v*v;
	return Math.sqrt(a);
    }

    public static float[] scalarMultiply(float[] src, int scalar,
	    float[] dest) {
	dest[0]=src[0]*scalar;
	dest[1]=src[1]*scalar;
	dest[2]=src[2]*scalar;
	checkValues(src);
	checkValues(dest);
	return dest;
    }//end scalarMultiply()

    public static double dot3(double[] l, double[] r) {
	return l[0]*r[0]+l[1]*r[1]+l[2]*r[2];
    }
    
}//end Vect3D
