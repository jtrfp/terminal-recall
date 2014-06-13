/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureManager;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.WorldObject2DVisibleEverywhere;

public class BackdropSystem extends RenderableSpacePartitioningGrid{
    private WorldObject overworldBackdrop,tunnelBackdrop;
	public BackdropSystem(World world){
		super(world);
		final TR tr = world.getTr();
		final TextureManager tm = tr.gpu.get().textureManager.get();
		//Backdrop
		Model backdropModel=new Model(false,tr);
		backdropModel.setDebugName("Backdrop model: Flat quad.");
		backdropModel.addTriangles(
				Triangle.quad2Triangles(
						new double[]{-1,1,1,-1}, new double[]{-1,-1,1,1}, new double[]{.9999999,.9999999,.9999999,.9999999}, 
						new double[]{0,1,1,0}, new double[]{0,0,1,1}, tm.solidColor(world.getFogColor()), RenderMode.DYNAMIC,Vector3D.ZERO,"backdropSystem"));
		overworldBackdrop = new Backdrop(tr,backdropModel.finalizeModel());
		overworldBackdrop.setRenderFlags((byte)1);
		add(overworldBackdrop);
		
		backdropModel=new Model(false,tr);
		backdropModel.addTriangles(
				Triangle.quad2Triangles(
						new double[]{-1,1,1,-1}, new double[]{-1,-1,1,1}, new double[]{.9999999,.9999999,.9999999,.9999999}, 
						new double[]{0,1,1,0}, new double[]{0,0,1,1}, tm.solidColor(Color.black), RenderMode.DYNAMIC,Vector3D.ZERO,"BackdropSystem"));
		tunnelBackdrop = new Backdrop(tr,backdropModel.finalizeModel());
		tunnelBackdrop.setRenderFlags((byte)1);
		tunnelBackdrop.setVisible(false);
		add(tunnelBackdrop);
		}
	
	public void overworldMode(){overworldBackdrop.setVisible(true);tunnelBackdrop.setVisible(false);}
	public void tunnelMode(){overworldBackdrop.setVisible(false);tunnelBackdrop.setVisible(true);}
	
	public static class Backdrop extends WorldObject2DVisibleEverywhere{

	    public Backdrop(TR tr) {
		super(tr);
	    }

	    public Backdrop(TR tr, Model model) {
		super(tr,model);
	    }
	    
	}//end Backdrop
}//end BackdropSystem
