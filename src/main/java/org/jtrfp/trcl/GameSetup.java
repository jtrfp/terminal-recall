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
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
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
	    	final ResourceManager rm = tr.getResourceManager();
		Color [] globalPalette = rm.getPalette(lvl.getGlobalPaletteFile());
		globalPalette[0]=new Color(0,0,0,0);//index zero is transparent
		tr.setGlobalPalette(globalPalette);
		hudSystem = new HUDSystem(tr.getWorld());
		tr.setHudSystem(hudSystem);
		hudSystem.activate();
		// POWERUPS
		rm.setPluralizedPowerupFactory(new PluralizedPowerupFactory(tr));
		/// EXPLOSIONS
		rm.setExplosionFactory(new ExplosionFactory(tr));
		// DEBRIS
		rm.setDebrisFactory(new DebrisFactory(tr));
		//SETUP PROJECTILE FACTORIES
		Weapon [] w = Weapon.values();
		ProjectileFactory [] pf = new ProjectileFactory[w.length];
		for(int i=0; i<w.length;i++){
		    pf[i]=new ProjectileFactory(tr, w[i], ExplosionType.Blast);
		}//end for(weapons)
		rm.setProjectileFactories(pf);
		
		Player player =new Player(tr,tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl())); 
		tr.setPlayer(player);
		final String startX=System.getProperty("org.jtrfp.trcl.startX");
		final String startY=System.getProperty("org.jtrfp.trcl.startY");
		final String startZ=System.getProperty("org.jtrfp.trcl.startZ");
		if(startX!=null && startY!=null&&startZ!=null){
		    System.out.println("Using user-specified start point");
		    final int sX=Integer.parseInt(startX);
		    final int sY=Integer.parseInt(startY);
		    final int sZ=Integer.parseInt(startZ);
		    player.setPosition(new Vector3D(sX,sY,sZ));
		}
		tr.getWorld().add(player);
		final TDFFile tdf = rm.getTDFData(lvl.getTunnelDefinitionFile());
		tr.setOverworldSystem(new OverworldSystem(tr.getWorld(), lvl, tdf));
		tr.setBackdropSystem(new BackdropSystem(tr.getWorld()));
		// NAV SYSTEM
		tr.setNavSystem(new NAVSystem(tr.getWorld(),rm.getNAVData(lvl.getNavigationFile()).getNavObjects(), tr));
		
		TunnelInstaller tunnelInstaller = new TunnelInstaller(tdf,tr.getWorld());
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
