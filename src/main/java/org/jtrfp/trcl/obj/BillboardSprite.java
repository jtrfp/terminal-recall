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
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;


public class BillboardSprite extends WorldObject{
	private Dimension dim;
	private double rotation=0;
	
	public BillboardSprite(TR tr){super(tr);}
	@Override
	protected void recalculateTransRotMBuffer(){
	    	final Camera camera = getTr().renderer.get().getCamera();
	    	final Vector3D cLookAt = camera.getLookAtVector();
	    	final Rotation rot = new Rotation(cLookAt,rotation);
		this.setHeading(rot.applyTo(cLookAt.negate()));
		this.setTop(rot.applyTo(camera.getUpVector()));
		super.recalculateTransRotMBuffer();
		}//end recalculateTransRotMBuffer()
	
	public void setBillboardSize(Dimension dim)
		{this.dim=dim;}
	public Dimension getBillboardSize(){return this.dim;}
	
	public void setTexture(TextureDescription desc, boolean useAlpha){
		if(dim==null)throw new NullPointerException("Billboard size must be non-null. (did you forget to set it?)");
		Triangle[] tris= Triangle.quad2Triangles(
				new double[]{-.5*dim.getWidth(),.5*dim.getWidth(),.5*dim.getWidth(),-.5*dim.getWidth()}, //X
				new double[]{-.5*dim.getHeight(),-.5*dim.getHeight(),.5*dim.getHeight(),.5*dim.getHeight()}, 
				new double[]{0,0,0,0}, 
				new double[]{0,1,1,0}, //U
				new double[]{0,0,1,1}, 
				desc, 
				RenderMode.DYNAMIC,true,Vector3D.MINUS_K,"BillboardSprite");
		Model m = new Model(false,getTr());
		m.addTriangles(tris);
		setModel(m.finalizeModel());
		}
	
	public void setRotation(double angle){
	    rotation=angle;
	}
}//end BillboardSprite
