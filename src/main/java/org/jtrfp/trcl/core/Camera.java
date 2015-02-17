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
package org.jtrfp.trcl.core;

import java.awt.Component;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.beh.FacingObject;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.RotateAroundObject;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.beh.TriggersVisCalcWithMovement;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.RelevantEverywhere;
import org.jtrfp.trcl.obj.WorldObject;

public class Camera extends WorldObject implements RelevantEverywhere{
    	//// PROPERTIES
    	public static final String FOG_ENABLED = "fogEnabled";
    
	private volatile  RealMatrix completeMatrix;
	private volatile  double viewDepth;
	private volatile  RealMatrix projectionMatrix;
	private final	  GPU gpu;
	private volatile  int updateDebugStateCounter;
	private 	  RealMatrix rotationMatrix;
	private boolean	  fogEnabled = true;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Camera(GPU gpu) {
	super(gpu.getTr());
	this.gpu = gpu;
	addBehavior(new MatchPosition().setEnable(true));
	addBehavior(new MatchDirection()).setEnable(true);
	addBehavior(new FacingObject().setEnable(false));
	addBehavior(new RotateAroundObject().setEnable(false));
	addBehavior(new TriggersVisCalcWithMovement().setEnable(true));
	addBehavior(new SkyCubeCloudModeUpdateBehavior());
    }//end constructor

	private void updateProjectionMatrix(){
	    	final Component component = gpu.getTr().getRootWindow();
		final float fov = 70f;// In degrees
		final float aspect = (float) component.getWidth()
				/ (float) component.getHeight();
		final float zF = (float) (viewDepth * 1.5);
		final float zN = (float) (TR.mapSquareSize / 10);
		final float f = (float) (1. / Math.tan(fov * Math.PI / 360.));
		projectionMatrix = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ f / aspect, 0, 0, 0 }, new double[]
				{ 0, f, 0, 0 }, new double[]
				{ 0, 0, (zF + zN) / (zN - zF), -1f }, new double[]
				{ 0, 0, (2f * zF * zN) / (zN - zF), 0 } }).transpose();
		}
	/**
	 * @return the lookAtVector
	 */
	public Vector3D getLookAtVector()
		{return getLookAt();}
	/**
	 * @param lookAtVector the lookAtVector to set
	 */
	public synchronized void setLookAtVector(Vector3D lookAtVector){
	    	double [] heading = super.getHeadingArray();
		heading[0] = lookAtVector.getX();
		heading[1] = lookAtVector.getY();
		heading[2] = lookAtVector.getZ();
		//cameraMatrix=null;
		}
	/**
	 * @return the upVector
	 */
	public Vector3D getUpVector()
		{return super.getTop();}
	/**
	 * @param upVector the upVector to set
	 */
	public synchronized void setUpVector(Vector3D upVector){
		super.setTop(upVector);
		//cameraMatrix=null;
		}
	/**
	 * @return the cameraPosition
	 */
	public Vector3D getCameraPosition()
		{return new Vector3D(super.getPosition());}

    /**
     * @param cameraPosition
     *            the cameraPosition to set
     */
    public void setPosition(Vector3D cameraPosition) {
	this.setPosition(cameraPosition.getX(), cameraPosition.getY(),
		cameraPosition.getZ());
    }

    @Override
    public synchronized void setPosition(double x, double y, double z) {
	super.setPosition(x, y, z);
	//cameraMatrix = null;
    }
	
	private RealMatrix applyMatrix(){
	        try{
		 Vector3D eyeLoc = getCameraPosition();
		 Vector3D aZ = getLookAtVector().negate();
		 Vector3D aX = getUpVector().crossProduct(aZ).normalize();
		 Vector3D aY = getUpVector();

		 rotationMatrix = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ aX.getX(), aX.getY(), aX.getZ(), 0 }, new double[]
				{ aY.getX(), aY.getY(), aY.getZ(), 0 }, new double[]
				{ aZ.getX(), aZ.getY(), aZ.getZ(), 0 }, new double[]
				{ 0, 0, 0, 1 } });

		 RealMatrix tM = new Array2DRowRealMatrix(new double[][]
			{ new double[]
				{ 1, 0, 0, -eyeLoc.getX() }, new double[]
				{ 0, 1, 0, -eyeLoc.getY() }, new double[]
				{ 0, 0, 1, -eyeLoc.getZ() }, new double[]
				{ 0, 0, 0, 1 } });
		
		 return completeMatrix = getProjectionMatrix().multiply(rotationMatrix.multiply(tM));
	         }catch(MathArithmeticException e){}//Don't crash.
	        return completeMatrix;
		}//end applyMatrix()
	public synchronized void setViewDepth(double cameraViewDepth){
	    	this.viewDepth=cameraViewDepth;
		projectionMatrix=null;
		}
	
	private RealMatrix getProjectionMatrix(){
		if(projectionMatrix==null)updateProjectionMatrix();
		return projectionMatrix;
		}
	
	public double getViewDepth()
		{return viewDepth;}
	
	private synchronized RealMatrix getCompleteMatrix()
		{//if(cameraMatrix==null){
		    applyMatrix();
		    if(updateDebugStateCounter++ % 30 ==0){
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.position", getPosition()[0]+" "+getPosition()[1]+" "+getPosition()[2]+" ");
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.lookAt", getLookAt());
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.up", getTop());
			}//}
		return completeMatrix;
		}
	public synchronized float [] getRotationMatrixAsFlatArray(float [] dest){
	    applyMatrix();
	    RealMatrix rm = rotationMatrix;
	    for(int i=0; i<16; i++){
		dest[i]=(float)rm.getEntry(i/4, i%4);
	    }//end for(16)
	    return dest;
	}// end getRotationMatrixAsFlatArray(...)
	
	public float [] getProjectionRotationMatrixAsFlatArray(){
	    applyMatrix();//getProjectionMatrix() doesn't implicitly apply matrix since it would cause a recursion loop
	    final float [] result = new float[16];
	    final RealMatrix mat = getProjectionMatrix().multiply(rotationMatrix);
	    for(int i=0; i<16; i++){
		result[i]=(float)mat.getEntry(i/4, i%4);
	    }//end for(16)
	    return result;
	}
	
	public float [] getCompleteMatrixAsFlatArray(){
	    final float [] result = new float[16];
	    final RealMatrix mat = getCompleteMatrix();
	    for(int i=0; i<16; i++){
		result[i]=(float)mat.getEntry(i/4, i%4);
	    }//end for(16)
	    return result;
	}

	/**
	 * @return the fogEnabled
	 */
	public boolean isFogEnabled() {
	    return fogEnabled;
	}

	/**
	 * @param fogEnabled the fogEnabled to set
	 */
	public Camera setFogEnabled(boolean fogEnabled) {
	    pcs.firePropertyChange(FOG_ENABLED, this.fogEnabled, fogEnabled);
	    this.fogEnabled = fogEnabled;
	    return this;
	}
}//end Camera
