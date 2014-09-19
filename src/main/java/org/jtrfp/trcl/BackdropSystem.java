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
package org.jtrfp.trcl;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.core.TextureManager;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;

public class BackdropSystem extends RenderableSpacePartitioningGrid{
    private WorldObject overworldBackdrop,tunnelBackdrop;
	public BackdropSystem(World world){
		super(world);
		final TR tr = world.getTr();
		final TextureManager tm = tr.gpu.get().textureManager.get();
		TextureDescription td;
		td = tm.getFallbackTexture();
		overworldBackdrop = new Backdrop(tr,td);
		overworldBackdrop.setRenderFlags((byte)1);
		add(overworldBackdrop);
		td = tm.solidColor(Color.black);
		tunnelBackdrop = new Backdrop(tr,td);
		tunnelBackdrop.setRenderFlags((byte)1);
		tunnelBackdrop.setVisible(false);
		add(tunnelBackdrop);
		}
	
	public void overworldMode(){overworldBackdrop.setVisible(true);tunnelBackdrop.setVisible(false);}
	public void tunnelMode(){overworldBackdrop.setVisible(false);tunnelBackdrop.setVisible(true);}
	public void loadingMode(){overworldBackdrop.setVisible(false);tunnelBackdrop.setVisible(true);}
	
	public static class Backdrop extends Sprite2D{

	    public Backdrop(TR tr, TextureDescription tex) {
		super(tr,.9999999,2,2,tex,false);
	    }
	}//end Backdrop
}//end BackdropSystem
