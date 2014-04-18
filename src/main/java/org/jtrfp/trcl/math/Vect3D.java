package org.jtrfp.trcl.math;

public final class Vect3D {

    public static void subtract(double[] l, double[] r, double[] dest) {
	dest[0]=l[0]-r[0];
	dest[1]=l[1]-r[1];
	dest[2]=l[2]-r[2];
    }

    public static double[] scalarMultiply(double [] src,
	    double scalar, double[] dest) {
	dest[0]=src[0]*scalar;
	dest[1]=src[1]*scalar;
	dest[2]=src[2]*scalar;
	return dest;
    }
    
    public static double[] normalize(double[] src){
	return normalize(src,new double[3]);
    }
    
    public static double[] normalize(double[] src, double [] dest) {
	final double norm = norm(src);
	dest[0]=src[0]/norm;
	dest[1]=src[1]/norm;
	dest[2]=src[2]/norm;
	return dest;
    }

    public static double norm(double[] src) {
	final double x=src[0];
	final double y=src[1];
	final double z=src[2];
	return Math.sqrt(x*x+y*y+z*z);
    }

    public static double [] add(double[] l, double rx, double ry,
	    double rz, double[] dest) {
	dest[0]=l[0]+rx;
	dest[1]=l[1]+ry;
	dest[2]=l[2]+rz;
	return dest;
    }

    public static double[] add(double[] l, double[] r,
	    double[] dest) {
	dest[0]=l[0]+r[0];
	dest[1]=l[1]+r[1];
	dest[2]=l[2]+r[2];
	return dest;
    }

    public static double distance(double[] l, double[] r) {
	final double dx=l[0]-r[0];
	final double dy=l[1]-r[1];
	final double dz=l[2]-r[2];
	return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }
    
    public static double taxicabDistance(double [] l, double [] r){
	final double dx=l[0]-r[0];
	final double dy=l[1]-r[1];
	final double dz=l[2]-r[2];
	return dx+dy+dz;
    }

    public static double[] negate(double[] in) {
	in[0]=-in[0];
	in[1]=-in[1];
	in[2]=-in[2];
	return in;
    }
    
    public static double [] cross(double [] l, double [] r, double []dest){
	dest[0]=l[1]*r[2]-l[2]*r[1];
	dest[1]=l[2]*r[0]-l[0]*r[2];
	dest[2]=l[0]*r[1]-l[1]*r[0];
	return dest;
    }

    public static double distanceXZ(double[] l, double[] r) {
	final double dx=l[0]-r[0];
	final double dz=l[2]-r[2];
	return Math.sqrt(dx*dx + dz*dz);
    }
    
}//end Vect3D
