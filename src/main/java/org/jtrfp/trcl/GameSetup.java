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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;

public class GameSetup
	{
	//private World world;
	private OverworldSystem overworldSystem;
	private HUDSystem hudSystem;
	private BackdropSystem backdropSystem;
	
	final int visibleTerrainGridDiameter=(int)TR.visibilityDiameterInMapSquares;
	
	public GameSetup(LVLFile lvl, TR tr) throws IllegalAccessException, FileLoadException, IOException{
		//Set up palette
		Color [] globalPalette = tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile());
		globalPalette[0]=new Color(0,0,0,0);//index zero is transparent
		tr.setGlobalPalette(globalPalette);
		
		hudSystem = new HUDSystem(tr.getWorld());
		hudSystem.activate();
		
		// POWERUPS
		tr.getResourceManager().setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
		
		/// EXPLOSIONS
		tr.getResourceManager().setExplosionFactory(new ExplosionFactory(tr));
		Model m;
		Triangle [] tris;
		TextureDescription t;
		final double SEG_LEN=5000;
		//RED LASERS
		m = new Model(false);
	    	 t = tr.getResourceManager().getRAWAsTexture(
	    		"BIGEX8.RAW",
	    		tr.getDarkIsClearPalette(), 
	    		GammaCorrectingColorProcessor.singleton, 
	    		tr.getGPU().getGl());
	    	tris =(Triangle.quad2Triangles(new double[]{-SEG_LEN/2.,SEG_LEN/2.,SEG_LEN/2.,0}, //X
	    		new double[]{0,0,0,0}, new double[]{-SEG_LEN*(7./2.),-SEG_LEN*(7./2.),SEG_LEN*(7/2),SEG_LEN*(7./2.)}, //YZ
	    		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
	    	 tris[0].setAlphaBlended(true);
	    	 tris[1].setAlphaBlended(true);
	    	 m.addTriangles(tris);
	    	 m.finalizeModel();
		tr.getResourceManager().setRedLaserFactory(new ProjectileFactory(tr,m,TR.mapSquareSize*12,2048,ExplosionType.Blast));
		//WHITE LASERS
		m = new Model(false);
		t = tr.getResourceManager().getRAWAsTexture(
	    		"NEWLASER.RAW", 
	    		globalPalette, 
	    		GammaCorrectingColorProcessor.singleton, 
	    		tr.getGPU().getGl());
	    	 tris =(Triangle.quad2Triangles(new double[]{-SEG_LEN/1.5,SEG_LEN/1.5,SEG_LEN/1.5,0}, //X
	    		new double[]{0,0,0,0}, new double[]{-SEG_LEN*(7./2.),-SEG_LEN*(7./2.),SEG_LEN*(7/2),SEG_LEN*(7./2.)}, //YZ
	    		new double[]{1,0,0,1}, new double[]{0,0,1,1}, t, RenderMode.STATIC));//UVtr
	    	 tris[0].setAlphaBlended(true);
	    	 tris[1].setAlphaBlended(true);
	    	 m.addTriangles(tris);
	    	 m.finalizeModel();
	    	tr.getResourceManager().setWhiteLaserFactory(new ProjectileFactory(tr,m,TR.mapSquareSize*18,4096,ExplosionType.Blast));
		//MAV targets
		NAVFile nav = tr.getResourceManager().getNAVData(lvl.getNavigationFile());
		for(NAVSubObject nObj:nav.getNavObjects())
			{
			if(nObj instanceof NAVFile.START)
				{
				NAVFile.START start = (NAVFile.START)nObj;
				Location3D loc = start.getLocationOnMap();
				tr.getRenderer().getCamera().setPosition(Tunnel.TUNNEL_START_POS);
				tr.getWorld().setCameraDirection(Tunnel.TUNNEL_START_DIRECTION);
				//TODO: Uncomment to enable player locale
				//world.setCameraDirection(new ObjectDirection(start.getRoll(),start.getPitch(),start.getYaw()));
				//Y is nudged up because some levels put player disturbingly close to the ground
				//world.setCameraPosition(new Vector3D(TR.legacy2Modern(loc.getZ()),TR.legacy2Modern(loc.getY())+TR.mapSquareSize/2,TR.legacy2Modern(loc.getX())));
				}
			}//end for(nav.getNavObjects())
		//TODO: Tunnel activators
		
		Player player =new Player(tr,tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl())); 
		final String startX=System.getProperty("org.jtrfp.trcl.startX");
		final String startY=System.getProperty("org.jtrfp.trcl.startY");
		final String startZ=System.getProperty("org.jtrfp.trcl.startZ");
		if(startX!=null && startY!=null){
		    final int sX=Integer.parseInt(startX);
		    final int sY=Integer.parseInt(startY);
		    final int sZ=Integer.parseInt(startZ);
		    player.setPosition(new Vector3D(sX,sY,sZ));
		}else {player.setPosition(Tunnel.TUNNEL_START_POS);}
		player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
		tr.getWorld().add(player);
		tr.setPlayer(player);
		
		overworldSystem = new OverworldSystem(tr.getWorld(), lvl);
		backdropSystem = new BackdropSystem(tr.getWorld());
		
		//TODO: Uncomment for tunnel
		//TunnelInstaller tunnelInstaller = new TunnelInstaller(tr.getResourceManager().getTDFData(lvl.getTunnelDefinitionFile()),tr.getWorld());
		GPU gpu = tr.getGPU();
		gpu.takeGL();//Remove if tunnels are put back in. TunnelInstaller takes the GL for us.
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
		
		System.out.println("\t...Done.");
		System.out.println("Starting animator...");
		tr.getThreadManager().start();
		System.out.println("\t...Done.");
		}//end OverworldGame
	}//end OverworldGame
