/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2016 Chuck Ritola
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

/**
 * Optimized allocation-free 4x4 matrix operations.
 * @author Chuck Ritola
 *
 */

public final class Mat4x4 {
    public static double [] set(int row, int col, double val, double[] dest){
	dest[row*4+col]=val;
	return dest;
    }//end set(...)
    
    public static double [] mul4x42VectRowMajor(double [] fourXfour, double [] vect, double [] dest){
	final double x = vect[0], y = vect[1], z = vect[2], w = vect[3];
	dest[0]=x*fourXfour[0*4+0]+
		y*fourXfour[0*4+1]+
		z*fourXfour[0*4+2]+
		w*fourXfour[0*4+3];
	dest[1]=x*fourXfour[1*4+0]+
		y*fourXfour[1*4+1]+
		z*fourXfour[1*4+2]+
		w*fourXfour[1*4+3];
	dest[2]=x*fourXfour[2*4+0]+
		y*fourXfour[2*4+1]+
		z*fourXfour[2*4+2]+
		w*fourXfour[2*4+3];
	dest[3]=x*fourXfour[3*4+0]+
		y*fourXfour[3*4+1]+
		z*fourXfour[3*4+2]+
		w*fourXfour[3*4+3];
	return dest;
    }//end mul4x42Vect(...)
    
    public static double [] mul4x42VectColumnMajor(double [] fourXfour, double [] vect, double [] dest){
	final double x = vect[0], y = vect[1], z = vect[2], w = vect[3];
	dest[0]=x*fourXfour[0*4+0]+
		y*fourXfour[1*4+0]+
		z*fourXfour[2*4+0]+
		w*fourXfour[3*4+0];
	dest[1]=x*fourXfour[0*4+1]+
		y*fourXfour[1*4+1]+
		z*fourXfour[2*4+1]+
		w*fourXfour[3*4+1];
	dest[2]=x*fourXfour[0*4+2]+
		y*fourXfour[1*4+2]+
		z*fourXfour[2*4+2]+
		w*fourXfour[3*4+2];
	dest[3]=x*fourXfour[0*4+3]+
		y*fourXfour[1*4+3]+
		z*fourXfour[2*4+3]+
		w*fourXfour[3*4+3];
	return dest;
    }//end mul4x42Vect(...)
    
    public static double [] mul(double [] l, double [] r, double [] dest){
	//ROW 0
	dest[0]=l[0*4+0]*r[0*4+0]+
		l[0*4+1]*r[1*4+0]+
		l[0*4+2]*r[2*4+0]+
		l[0*4+3]*r[3*4+0];
	dest[1]=l[0*4+0]*r[0*4+1]+
		l[0*4+1]*r[1*4+1]+
		l[0*4+2]*r[2*4+1]+
		l[0*4+3]*r[3*4+1];
	dest[2]=l[0*4+0]*r[0*4+2]+
		l[0*4+1]*r[1*4+2]+
		l[0*4+2]*r[2*4+2]+
		l[0*4+3]*r[3*4+2];
	dest[3]=l[0*4+0]*r[0*4+3]+
		l[0*4+1]*r[1*4+3]+
		l[0*4+2]*r[2*4+3]+
		l[0*4+3]*r[3*4+3];
	//ROW 1
	dest[4]=l[1*4+0]*r[0*4+0]+
		l[1*4+1]*r[1*4+0]+
		l[1*4+2]*r[2*4+0]+
		l[1*4+3]*r[3*4+0];
	dest[5]=l[1*4+0]*r[0*4+1]+
		l[1*4+1]*r[1*4+1]+
		l[1*4+2]*r[2*4+1]+
		l[1*4+3]*r[3*4+1];
	dest[6]=l[1*4+0]*r[0*4+2]+
		l[1*4+1]*r[1*4+2]+
		l[1*4+2]*r[2*4+2]+
		l[1*4+3]*r[3*4+2];
	dest[7]=l[1*4+0]*r[0*4+3]+
		l[1*4+1]*r[1*4+3]+
		l[1*4+2]*r[2*4+3]+
		l[1*4+3]*r[3*4+3];
	//ROW 2
	dest[8]=l[2*4+0]*r[0*4+0]+
		l[2*4+1]*r[1*4+0]+
		l[2*4+2]*r[2*4+0]+
		l[2*4+3]*r[3*4+0];
	dest[9]=l[2*4+0]*r[0*4+1]+
		l[2*4+1]*r[1*4+1]+
		l[2*4+2]*r[2*4+1]+
		l[2*4+3]*r[3*4+1];
	dest[10]=l[2*4+0]*r[0*4+2]+
		l[2*4+1]*r[1*4+2]+
		l[2*4+2]*r[2*4+2]+
		l[2*4+3]*r[3*4+2];
	dest[11]=l[2*4+0]*r[0*4+3]+
		l[2*4+1]*r[1*4+3]+
		l[2*4+2]*r[2*4+3]+
		l[2*4+3]*r[3*4+3];
	//ROW 3
	dest[12]=l[3*4+0]*r[0*4+0]+
		l[3*4+1]*r[1*4+0]+
		l[3*4+2]*r[2*4+0]+
		l[3*4+3]*r[3*4+0];
	dest[13]=l[3*4+0]*r[0*4+1]+
		l[3*4+1]*r[1*4+1]+
		l[3*4+2]*r[2*4+1]+
		l[3*4+3]*r[3*4+1];
	dest[14]=l[3*4+0]*r[0*4+2]+
		l[3*4+1]*r[1*4+2]+
		l[3*4+2]*r[2*4+2]+
		l[3*4+3]*r[3*4+2];
	dest[15]=l[3*4+0]*r[0*4+3]+
		l[3*4+1]*r[1*4+3]+
		l[3*4+2]*r[2*4+3]+
		l[3*4+3]*r[3*4+3];
	return dest;
    }//end mul(...)
    
    public static void identity(double [] dest4x4){
	dest4x4[0]=1;
	dest4x4[1]=0;
	dest4x4[2]=0;
	dest4x4[3]=0;
	dest4x4[4]=0;
	dest4x4[5]=1;
	dest4x4[6]=0;
	dest4x4[7]=0;
	dest4x4[8]=0;
	dest4x4[9]=0;
	dest4x4[10]=1;
	dest4x4[11]=0;
	dest4x4[12]=0;
	dest4x4[13]=0;
	dest4x4[14]=0;
	dest4x4[15]=1;
    }
}//end Mat4x4
