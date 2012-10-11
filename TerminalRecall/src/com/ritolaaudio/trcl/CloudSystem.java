/*******************************************************************************
 * Copyright (c) 2012 chuck.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.awt.Color;
import java.io.IOException;

import javax.media.opengl.GL2;

import jtrfp.common.FileLoadException;

public class CloudSystem
	{
	Color fogColor;
	double ceilingHeight;
	Texture cloudTexture;
	double cloudTileSideSize;
	int gridSideSizeInTiles;
	public CloudSystem(String cloudTextureFileName, Color [] palette, double cloudTileSideSize, int gridSideSizeInTiles, GL2 gl, double ceilingHeight) throws IllegalAccessException, FileLoadException, IOException
		{
		final int transpose=48;
		this.ceilingHeight=ceilingHeight;
		this.cloudTileSideSize=cloudTileSideSize;
		this.gridSideSizeInTiles=gridSideSizeInTiles;
		Color [] newPalette = new Color[256];
		//Transpose palette by 48
		for(int i=0; i<256; i++)
			{
			newPalette[TR.bidiMod((i+transpose),256)]=palette[i];
			}
		
		cloudTexture = TR.resources.getRAWAsTexture(cloudTextureFileName, newPalette, GammaCorrectingColorProcessor.singleton);
		}//end constructor
	
	public void addToWorld(World worldToAddTo, GL2 gl)
		{
		//Set fog
		worldToAddTo.fogColor=cloudTexture.getAverageColor();
		//Create a grid
		for(int z=0; z<gridSideSizeInTiles; z++)
			{
			for(int x=0; x<gridSideSizeInTiles; x++)
				{
				double xPos=x*cloudTileSideSize;
				double zPos=z*cloudTileSideSize;
				
				Triangle t=new Triangle();
				t.setTexture(cloudTexture);
				t.setRenderMode(RenderMode.STATIC);
				
				int vtx=0;
				//TOP LEFT
				t.x[vtx]=xPos;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos;
				t.u[vtx]=0; t.v[vtx]=1;
				//BOTTOM LEFT
				vtx++;
				t.x[vtx]=xPos;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos+cloudTileSideSize;
				t.u[vtx]=0; t.v[vtx]=0;
				//BOTTOM RIGHT
				vtx++;
				t.x[vtx]=xPos+cloudTileSideSize;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos+cloudTileSideSize;
				t.u[vtx]=1; t.v[vtx]=0;
				
				worldToAddTo.pushTriangle(t, gl);
				
				t = new Triangle();
				t.setTexture(cloudTexture);
				t.setRenderMode(RenderMode.STATIC);
				
				vtx=0;
				//TOP LEFT
				t.x[vtx]=xPos;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos;
				t.u[vtx]=0; t.v[vtx]=1;
				//TOP RIGHT
				vtx++;
				t.x[vtx]=xPos+cloudTileSideSize;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos;
				t.u[vtx]=1; t.v[vtx]=1;
				//BOTTOM RIGHT
				vtx++;
				t.x[vtx]=xPos+cloudTileSideSize;t.y[vtx]=ceilingHeight;t.z[vtx]=zPos+cloudTileSideSize;
				t.u[vtx]=1; t.v[vtx]=0;
				worldToAddTo.pushTriangle(t, gl);
				}//end for(x)
			}//end for(z)
		}//end addToWorld
	}//end CloudSystem
