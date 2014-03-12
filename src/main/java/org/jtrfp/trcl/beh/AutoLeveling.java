package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class AutoLeveling extends Behavior {
    private double [] retainmentCoeff=new double[]{.988,.988,.988};
    private double [] inverseRetainmentCoeff = new double[]{1-.988,1-.988,1-.988};
    private LevelingAxis levelingAxis = LevelingAxis.TOP;
    private Vector3D levelingVector=new Vector3D(0,1,0);
    @Override
    public void _tick(long timeInMillis){
	final WorldObject 	parent 		= getParent();
	final Vector3D 		oldHeading 	= parent.getHeading();
	final Vector3D 		oldTop 		= parent.getTop();
	
	final Vector3D newHeading = 
		levelingAxis==LevelingAxis.TOP?	new Vector3D(oldHeading.getX(),oldHeading.getY(),oldHeading.getZ()).normalize():
		    		/*heading*/	new Vector3D(
		    	oldHeading.getX()*retainmentCoeff[0]+levelingVector.getX()*inverseRetainmentCoeff[0],
		    	oldHeading.getY()*retainmentCoeff[1]+levelingVector.getY()*inverseRetainmentCoeff[1],
		    	oldHeading.getZ()*retainmentCoeff[2]+levelingVector.getZ()*inverseRetainmentCoeff[2])
		    	    .normalize();
	
	final Vector3D newTop = 
		levelingAxis==LevelingAxis.HEADING?new Vector3D(oldTop.getX(),oldTop.getY(),oldTop.getZ()).normalize():
		    		/*heading*/	new Vector3D(
			oldTop.getX()*retainmentCoeff[0]+levelingVector.getX()*inverseRetainmentCoeff[0],
			oldTop.getY()*retainmentCoeff[1]+levelingVector.getY()*inverseRetainmentCoeff[1],
			oldTop.getZ()*retainmentCoeff[2]+levelingVector.getZ()*inverseRetainmentCoeff[2])
			    .normalize();
	
	final Rotation topDelta=new Rotation(oldTop,newTop);
	final Rotation headingDelta=new Rotation(oldHeading,newHeading);
	parent.setHeading(headingDelta.applyTo(topDelta.applyTo(oldHeading)));
	parent.setTop(headingDelta.applyTo(topDelta.applyTo(oldTop)));
    }//end _tick(...)
    
    public static enum LevelingAxis{
	TOP,
	HEADING
    }//end LevelingAxis

    /**
     * @return the retainmentCoeff
     */
    public double [] getRetainmentCoeff() {
        return retainmentCoeff;
    }

    /**
     * @param retainmentCoeff the retainmentCoeff to set
     */
    public AutoLeveling setRetainmentCoeff(double x, double y, double z) {
	retainmentCoeff[0]=x;
	retainmentCoeff[1]=y;
	retainmentCoeff[2]=z;
	inverseRetainmentCoeff[0]=1.-x;
	inverseRetainmentCoeff[1]=1.-y;
	inverseRetainmentCoeff[2]=1.-z;
        return this;
    }

    /**
     * @return the levelingAxis
     */
    public LevelingAxis getLevelingAxis() {
        return levelingAxis;
    }

    /**
     * @param levelingAxis the levelingAxis to set
     */
    public AutoLeveling setLevelingAxis(LevelingAxis levelingAxis) {
        this.levelingAxis = levelingAxis;
        return this;
    }

    /**
     * @return the levelingVector
     */
    public Vector3D getLevelingVector() {
        return levelingVector;
    }

    /**
     * @param levelingVector the levelingVector to set
     */
    public AutoLeveling setLevelingVector(Vector3D levelingVector) {
        this.levelingVector = levelingVector;
        return this;
    }
}//end AutoLeveling
