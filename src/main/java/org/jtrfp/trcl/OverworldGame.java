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
import java.io.IOException;

import javax.media.opengl.GL3;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class OverworldGame
	{
	//private World world;
	private OverworldSystem overworldSystem;
	private HUDSystem hudSystem;
	private BackdropSystem backdropSystem;
	
	final int visibleTerrainGridDiameter=(int)TR.visibilityDiameterInMapSquares;
	
	public OverworldGame(LVLFile lvl, TR tr) throws IllegalAccessException, FileLoadException, IOException
		{
		//Set up palette
		Color [] globalPalette = tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile());
		globalPalette[0]=new Color(0,0,0,0);//index zero is transparent
		tr.setGlobalPalette(globalPalette);
		
		hudSystem = new HUDSystem(tr.getWorld());
		hudSystem.activate();
		
		//MAV targets
		NAVFile nav = tr.getResourceManager().getNAVData(lvl.getNavigationFile());
		for(NAVSubObject nObj:nav.getNavObjects())
			{
			if(nObj instanceof NAVFile.START)
				{
				NAVFile.START start = (NAVFile.START)nObj;
				Location3D loc = start.getLocationOnMap();
				tr.getRenderer().getCamera().setCameraPosition(Tunnel.TUNNEL_START_POS);
				tr.getWorld().setCameraDirection(Tunnel.TUNNEL_START_DIRECTION);
				//TODO: Uncomment to enable player locale
				//world.setCameraDirection(new ObjectDirection(start.getRoll(),start.getPitch(),start.getYaw()));
				//Y is nudged up because some levels put player disturbingly close to the ground
				//world.setCameraPosition(new Vector3D(TR.legacy2Modern(loc.getZ()),TR.legacy2Modern(loc.getY())+TR.mapSquareSize/2,TR.legacy2Modern(loc.getX())));
				}
			}//end for(nav.getNavObjects())
		
		//TODO: Tunnel activators
		
		overworldSystem = new OverworldSystem(tr.getWorld(), lvl);
		backdropSystem = new BackdropSystem(tr.getWorld());
		
		TunnelInstaller tunnelInstaller = new TunnelInstaller(tr.getResourceManager().getTDFData(lvl.getTunnelDefinitionFile()),tr.getWorld());
		GPU gpu = tr.getGPU();
		System.out.println("Building master texture...");
		Texture.finalize(gpu);
		System.out.println("\t...Done.");
		System.out.println("Finalizing GPU memory allocation...");
		GlobalDynamicTextureBuffer.finalizeAllocation(gpu);
		gpu.releaseGL();
		//////// NO GL BEYOND THIS POINT ////////
		System.out.println("\t...Done.");
		System.out.println("Invoking JVM's garbage collector...");
		System.gc();
		System.out.println("\t...Ahh, that felt good.");
		System.out.println("Attaching to GL Canvas...");
		gpu.addGLEventListener(tr.getWorld());
		
		System.out.println("\t...Done.");
		System.out.println("Starting animator...");
		gpu.startAnimator();
		System.out.println("\t...Done.");
		}//end OverworldGame
	}//end OverworldGame
