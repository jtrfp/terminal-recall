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
import org.jtrfp.trcl.file.LVLFile;

public class CloudSystem extends RenderableSpacePartitioningGrid
	{
	Color fogColor;
	double ceilingHeight;
	TextureDescription cloudTexture;
	double cloudTileSideSize;
	int gridSideSizeInTiles;
	public CloudSystem(RenderableSpacePartitioningGrid grid,LVLFile lvl, double cloudTileSideSize, int gridSideSizeInTiles, double ceilingHeight) throws IllegalAccessException, FileLoadException, IOException
		{
		super(grid);
		final int transpose=48;
		this.ceilingHeight=ceilingHeight;
		this.cloudTileSideSize=cloudTileSideSize;
		this.gridSideSizeInTiles=gridSideSizeInTiles;
		String cloudTextureFileName=lvl.getCloudTextureFile();
		Color[] palette = world.getTr().getResourceManager().getPalette(lvl.getBackgroundGradientPaletteFile());
		Color [] newPalette = new Color[256];
		//Transpose palette by 48
		for(int i=0; i<256; i++)
			{newPalette[TR.bidiMod((i+transpose),256)]=palette[i];}
		
		cloudTexture = world.getTr().getResourceManager().getRAWAsTexture(cloudTextureFileName, newPalette, GammaCorrectingColorProcessor.singleton,world.getTr().getGPU().takeGL());
		addToWorld();
		}//end constructor
	
	private void addToWorld()
		{
		//Set fog
		world.fogColor=cloudTexture.getAverageColor();
		//Create a grid
		for(int z=0; z<gridSideSizeInTiles; z++)
			{
			for(int x=0; x<gridSideSizeInTiles; x++)
				{
				double xPos=x*cloudTileSideSize;
				double zPos=z*cloudTileSideSize;
				
				Triangle [] tris = Triangle.quad2Triangles(
						new double[]{0,0+cloudTileSideSize,0+cloudTileSideSize,0}, 
						new double[]{ceilingHeight,ceilingHeight,ceilingHeight,ceilingHeight}, 
						new double[]{0,0,0+cloudTileSideSize,0+cloudTileSideSize}, 
						new double[]{0,1,1,0},//u 
						new double[]{1,1,0,0}, 
						cloudTexture, 
						RenderMode.STATIC);
				final Model m = new Model(false);
				m.addTriangle(tris[0]);
				m.addTriangle(tris[1]);
				final TerrainChunk rq = new TerrainChunk(world,m.finalizeModel());
				rq.setPosition(new Vector3D(xPos,ceilingHeight,zPos));
				add(rq);
				}//end for(x)
			}//end for(z)
		}//end addToWorld
	}//end CloudSystem
