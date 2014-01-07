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
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.WorldObject2DVisibleEverywhere;

public class BackdropSystem extends RenderableSpacePartitioningGrid{
    private WorldObject overworldBackdrop,tunnelBackdrop;
	public BackdropSystem(World world)
		{
		super(world);
		//Backdrop
		Model backdropModel=new Model(false);
		backdropModel.addTriangles(
				Triangle.quad2Triangles(
						new double[]{-1,1,1,-1}, new double[]{-1,-1,1,1}, new double[]{.9999999,.9999999,.9999999,.9999999}, 
						new double[]{0,1,1,0}, new double[]{0,0,1,1}, Texture.solidColor(world.getFogColor()), RenderMode.DYNAMIC));
		overworldBackdrop = new WorldObject2DVisibleEverywhere(world.getTr(),backdropModel.finalizeModel());
		overworldBackdrop.setPosition(new Vector3D(0,0,0));
		add(overworldBackdrop);
		//addAlwaysVisible(overworldBackdrop);
		
		backdropModel=new Model(false);
		backdropModel.addTriangles(
				Triangle.quad2Triangles(
						new double[]{-1,1,1,-1}, new double[]{-1,-1,1,1}, new double[]{.9999999,.9999999,.9999999,.9999999}, 
						new double[]{0,1,1,0}, new double[]{0,0,1,1}, Texture.solidColor(Color.black), RenderMode.DYNAMIC));
		tunnelBackdrop = new WorldObject2DVisibleEverywhere(world.getTr(),backdropModel.finalizeModel());
		tunnelBackdrop.setPosition(new Vector3D(0,0,0));
		tunnelBackdrop.setActive(false);
		//addAlwaysVisible(tunnelBackdrop);
		}
	
	public void overworldMode(){overworldBackdrop.setVisible(true);tunnelBackdrop.setVisible(false);}
	public void tunnelMode(){overworldBackdrop.setVisible(false);tunnelBackdrop.setVisible(true);}
	}//end BackdropSystem
