package org.jtrfp.trcl.core;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.gpu.GPU;

public class Camera
	{
	private Vector3D lookAtVector = new Vector3D(0, 0, 1);
	private Vector3D upVector = new Vector3D(0, 1, 0);
	private Vector3D cameraPosition = new Vector3D(50000, 0, 50000);
	private RealMatrix cameraMatrix;
	private double viewDepth;
	private RealMatrix projectionMatrix;
	private final GPU gpu;
	private int updateDebugStateCounter;
	public Camera(GPU gpu)
		{this.gpu=gpu;}
	
	private void updateProjectionMatrix(){
		final float fov = 70f;// In degrees
		final float aspect = (float) gpu.getComponent().getWidth()
				/ (float) gpu.getComponent().getHeight();
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
		{return lookAtVector;}
	/**
	 * @param lookAtVector the lookAtVector to set
	 */
	public void setLookAtVector(Vector3D lookAtVector){
		this.lookAtVector = lookAtVector;
		cameraMatrix=null;
		}
	/**
	 * @return the upVector
	 */
	public Vector3D getUpVector()
		{return upVector;}
	/**
	 * @param upVector the upVector to set
	 */
	public void setUpVector(Vector3D upVector){
		this.upVector = upVector;
		cameraMatrix=null;
		}
	/**
	 * @return the cameraPosition
	 */
	public Vector3D getCameraPosition()
		{return cameraPosition;}
	/**
	 * @param cameraPosition the cameraPosition to set
	 */
	public void setPosition(Vector3D cameraPosition){
		this.cameraPosition = cameraPosition;
		cameraMatrix=null;
		}
	
	private void applyMatrix(){
		Vector3D eyeLoc = getCameraPosition();
		Vector3D aZ = getLookAtVector().negate();
		Vector3D aX = getUpVector().crossProduct(aZ).normalize();
		Vector3D aY = /*aZ.crossProduct(aX)*/getUpVector();

		RealMatrix rM = new Array2DRowRealMatrix(new double[][]
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
		
		cameraMatrix = getProjectionMatrix().multiply(rM.multiply(tM));
		}//end applyMatrix()
	public void setViewDepth(double cameraViewDepth){
	    	this.viewDepth=cameraViewDepth;
		cameraMatrix=null;
		projectionMatrix=null;
		}

	private RealMatrix getProjectionMatrix(){
		if(projectionMatrix==null)updateProjectionMatrix();
		return projectionMatrix;
		}
	
	public double getViewDepth()
		{return viewDepth;}
	
	public RealMatrix getMatrix()
		{if(cameraMatrix==null){
		    applyMatrix();
		    if(updateDebugStateCounter++ % 30 ==0){
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.position", cameraPosition);
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.lookAt", lookAtVector);
			    gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Camera.up", upVector);
			}}
		return cameraMatrix;
		}
	}//end Camera
