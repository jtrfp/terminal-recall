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
package com.ritolaaudio.trcl;


import java.awt.Color;
import java.io.IOException;

import javax.media.opengl.GL3;

import jtrfp.common.FileLoadException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import com.ritolaaudio.trcl.file.LVLFile;
import com.ritolaaudio.trcl.file.Location3D;
import com.ritolaaudio.trcl.file.NAVFile;
import com.ritolaaudio.trcl.file.NAVFile.NAVSubObject;

public class OverworldGame
	{
	private AltitudeMap heightMap;
	private LVLFile lvlFile;
	private TextureMesh textureGrid;
	//private TerrainSystem terrain;
	//private CloudSystem cloudSystem;
	private World world;
	//private ObjectSystem objectSystem;
	private OverworldSystem overworldSystem;
	private HUDSystem hudSystem;
	private BackdropSystem backdropSystem;
	
	final int visibleTerrainGridDiameter=(int)TR.visibilityDiameterInMapSquares;
	
	public OverworldGame(LVLFile lvl, TR tr) throws IllegalAccessException, FileLoadException, IOException
		{
		world = new World(256*TR.mapSquareSize,14.*TR.mapSquareSize,256*TR.mapSquareSize,TR.mapSquareSize*visibleTerrainGridDiameter/2., tr);
		//Set up palette
		Color [] globalPalette = tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile());
		globalPalette[0]=new Color(0,0,0,0);//index zero is transparent
		tr.setGlobalPalette(globalPalette);
		
		hudSystem = new HUDSystem(world);
		
		//MAV targets
		NAVFile nav = tr.getResourceManager().getNAVData(lvl.getNavigationFile());
		for(NAVSubObject nObj:nav.getNavObjects())
			{
			if(nObj instanceof NAVFile.START)
				{
				NAVFile.START start = (NAVFile.START)nObj;
				Location3D loc = start.getLocationOnMap();
				world.setCameraPosition(Tunnel.TUNNEL_START_POS);
				world.setCameraDirection(Tunnel.TUNNEL_START_DIRECTION);
				//TODO: Uncomment to enable player locale
				//world.setCameraDirection(new ObjectDirection(start.getRoll(),start.getPitch(),start.getYaw()));
				//Y is nudged up because some levels put player disturbingly close to the ground
				//world.setCameraPosition(new Vector3D(TR.legacy2Modern(loc.getZ()),TR.legacy2Modern(loc.getY())+TR.mapSquareSize/2,TR.legacy2Modern(loc.getX())));
				}
			}//end for(nav.getNavObjects())
		
		//TODO: Tunnel activators
		
		overworldSystem = new OverworldSystem(world, lvl);
		backdropSystem = new BackdropSystem(world);
		
		TunnelInstaller tunnelInstaller = new TunnelInstaller(tr.getResourceManager().getTDFData(lvl.getTunnelDefinitionFile()),world);
		
		GL3 gl = tr.getGl();
		//gl.getContext().makeCurrent();
		System.out.println("Building master texture...");
		Texture.finalize(gl);
		System.out.println("\t...Done.");
		System.out.println("Finalizing GPU memory allocation...");
		GlobalDynamicTextureBuffer.finalizeAllocation(gl);
		gl.getContext().release();
		//////// NO GL BEYOND THIS POINT ////////
		System.out.println("\t...Done.");
		System.out.println("Invoking JVM's garbage collector...");
		System.gc();
		System.out.println("\t...Ahh, that felt good.");
		System.out.println("Attaching to GL Canvas...");
		tr.getCanvas().addGLEventListener(world);
		System.out.println("\t...Done.");
		System.out.println("Starting animator...");
		tr.getAnimator().start();
		System.out.println("\t...Done.");
		}//end OverworldGame
	}//end OverworldGame
