package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class AutoLeveling extends Behavior {
    private double retainmentCoeff=.988;
    private double inverseRetainmentCoeff=1.-retainmentCoeff;
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
		    	oldHeading.getX()*retainmentCoeff+levelingVector.getX()*inverseRetainmentCoeff,
		    	oldHeading.getY()*retainmentCoeff+levelingVector.getY()*inverseRetainmentCoeff,
		    	oldHeading.getZ()*retainmentCoeff+levelingVector.getZ()*inverseRetainmentCoeff)
		    	    .normalize();
	
	final Vector3D newTop = 
		levelingAxis==LevelingAxis.HEADING?new Vector3D(oldTop.getX(),oldTop.getY(),oldTop.getZ()).normalize():
		    		/*heading*/	new Vector3D(
			oldTop.getX()*retainmentCoeff+levelingVector.getX()*inverseRetainmentCoeff,
			oldTop.getY()*retainmentCoeff+levelingVector.getY()*inverseRetainmentCoeff,
			oldTop.getZ()*retainmentCoeff+levelingVector.getZ()*inverseRetainmentCoeff)
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
    public double getRetainmentCoeff() {
        return retainmentCoeff;
    }

    /**
     * @param retainmentCoeff the retainmentCoeff to set
     */
    public AutoLeveling setRetainmentCoeff(double retainmentCoeff) {
        this.retainmentCoeff = retainmentCoeff;
        inverseRetainmentCoeff=1.-retainmentCoeff;
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
