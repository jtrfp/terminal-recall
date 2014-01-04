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
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.obj.DebrisFactory;
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
		// DEBRIS
		tr.getResourceManager().setDebrisFactory(new DebrisFactory(tr));
		//SETUP PROJECTILE FACTORIES
		Weapon [] w = Weapon.values();
		ProjectileFactory [] pf = new ProjectileFactory[w.length];
		for(int i=0; i<w.length;i++){
		    pf[i]=new ProjectileFactory(tr, w[i], ExplosionType.Blast);
		}//end for(weapons)
		tr.getResourceManager().setProjectileFactories(pf);
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
		if(startX!=null && startY!=null&&startZ!=null){
		    System.out.println("Using user-specified start point");
		    final int sX=Integer.parseInt(startX);
		    final int sY=Integer.parseInt(startY);
		    final int sZ=Integer.parseInt(startZ);
		    player.setPosition(new Vector3D(sX,sY,sZ));
		}else {player.setPosition(Tunnel.TUNNEL_START_POS);}
		player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
		tr.getWorld().add(player);
		tr.setPlayer(player);
		
		tr.setOverworldSystem(new OverworldSystem(tr.getWorld(), lvl));
		backdropSystem = new BackdropSystem(tr.getWorld());
		
		//TODO: Uncomment for tunnel
		TunnelInstaller tunnelInstaller = new TunnelInstaller(tr.getResourceManager().getTDFData(lvl.getTunnelDefinitionFile()),tr.getWorld());
		GPU gpu = tr.getGPU();
		//gpu.takeGL();//Remove if tunnels are put back in. TunnelInstaller takes the GL for us.
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
