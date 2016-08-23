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
package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Texture;


public class BillboardSprite extends WorldObject{
	private Dimension dim;
	private String debugName;
	private RotationDelegate rotationDelegate;
	
	public BillboardSprite(){
	    super();
	    rotationDelegate = new StaticRotationDelegate(getTr());
	    }
	@Override
	protected void recalculateTransRotMBuffer(){
	    	rotationDelegate.updateRotation(this);
		super.recalculateTransRotMBuffer();
		}//end recalculateTransRotMBuffer()
	
	public void setBillboardSize(Dimension dim)
		{this.dim=dim;}
	public Dimension getBillboardSize(){return this.dim;}
	
	public void setTexture(Texture desc, boolean useAlpha){
		if(dim==null)throw new NullPointerException("Billboard size must be non-null. (did you forget to set it?)");
		Triangle[] tris= Triangle.quad2Triangles(
				new double[]{-.5*dim.getWidth(),.5*dim.getWidth(),.5*dim.getWidth(),-.5*dim.getWidth()}, //X
				new double[]{-.5*dim.getHeight(),-.5*dim.getHeight(),.5*dim.getHeight(),.5*dim.getHeight()}, 
				new double[]{0,0,0,0}, 
				new double[]{0,1,1,0}, //U
				new double[]{0,0,1,1}, 
				desc, 
				RenderMode.DYNAMIC,true,Vector3D.ZERO,"BillboardSprite");
		GL33Model m = new GL33Model(false,getTr(), "BillboardSprite."+getDebugName());
		m.addTriangles(tris);
		setModel(m);
		}
	
	@Override
	protected boolean recalcMatrixWithEachFrame(){
	    return true;
	}
	public String getDebugName() {
	    return debugName;
	}
	
	public interface RotationDelegate{
	    public void updateRotation(WorldObject target);
	}
	
	public static class StaticRotationDelegate implements RotationDelegate{
	    private double rotationAngleRadians;
	    private final TR tr;
	    
	    public StaticRotationDelegate(TR tr){
		this.tr=tr;
	    }

	    @Override
	    public void updateRotation(WorldObject target) {
		final Camera camera = tr.mainRenderer.getCamera();
	    	final Vector3D cLookAt = camera.getLookAtVector();
	    	final Rotation rot = new Rotation(cLookAt,rotationAngleRadians);
		target.setHeading(rot.applyTo(cLookAt.negate()));
		target.setTop(rot.applyTo(camera.getUpVector()));
	    }//end updateRotation(...)

	    protected double getRotationAngleRadians() {
	        return rotationAngleRadians;
	    }

	    protected void setRotationAngleRadians(double rotationAngleRadians) {
	        this.rotationAngleRadians = rotationAngleRadians;
	    }
	}//end StaticRotationDelegate
	
	public static class UpAlwaysCameraTopDelegate implements RotationDelegate {
	    private final Camera camera;
	    
	    public UpAlwaysCameraTopDelegate(Camera camera){
		this.camera = camera;
	    }

	    @Override
	    public void updateRotation(WorldObject target) {
	    	final Vector3D cLookAt = camera.getLookAtVector();
		target.setHeading(cLookAt.negate());
		final Vector3D cHeading = camera.getHeading();
		final Vector3D side = cHeading.crossProduct(Vector3D.PLUS_J);
		final Vector3D newTop = side.crossProduct(cHeading);
		target.setTop(newTop);
	    }
	}//end UpAlwaysCameraTopDelegate

	protected RotationDelegate getRotationDelegate() {
	    return rotationDelegate;
	}
	protected void setRotationDelegate(RotationDelegate rotationDelegate) {
	    this.rotationDelegate = rotationDelegate;
	}
	public void setDebugName(String debugName) {
	    this.debugName = debugName;
	}
}//end BillboardSprite
