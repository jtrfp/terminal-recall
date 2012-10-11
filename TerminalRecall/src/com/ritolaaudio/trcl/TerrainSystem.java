/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import javax.media.opengl.GL2;

public class TerrainSystem
	{
	Triangle [] terrainTriangles;
	double gridSquareSize;
	double heightScalar;
	public TerrainSystem(AltitudeMap altitude, Texture [][] textures, double gridSquareSize, double heightScalar)
		{
		final int width=(int)altitude.getWidth(); int height=(int)altitude.getHeight();
		this.gridSquareSize=gridSquareSize;
		this.heightScalar=heightScalar;
		
		terrainTriangles = new Triangle[width*height*2];
		int i=0;
		for(int z=0; z<height;z++)
			{
			for(int x=0; x<width; x++)
				{
				Triangle t = new Triangle();
				t.setRenderMode(RenderMode.STATIC);
				t.setTexture(textures[x][z]);
				//System.out.println("TEXTURE: "+textures[x][z].getTextureID());
				final double hTL=altitude.heightAt(x, z)*heightScalar;
				final double hTR=altitude.heightAt((x+1)%width,z)*heightScalar;
				final double hBR=altitude.heightAt((x+1)%width,(z+1)%height)*heightScalar;
				final double hBL=altitude.heightAt(x,(z+1)%height)*heightScalar;
				final double xPos=x*gridSquareSize;
				final double zPos=z*gridSquareSize;
				
				int vtx=0;
				//TOP LEFT
				t.x[vtx]=xPos;t.y[vtx]=hTL;t.z[vtx]=zPos;
				t.u[vtx]=0; t.v[vtx]=1;
				//BOTTOM LEFT
				vtx++;
				t.x[vtx]=xPos;t.y[vtx]=hBL;t.z[vtx]=zPos+gridSquareSize;
				t.u[vtx]=0; t.v[vtx]=0;
				//BOTTOM RIGHT
				vtx++;
				t.x[vtx]=xPos+gridSquareSize;t.y[vtx]=hBR;t.z[vtx]=zPos+gridSquareSize;
				t.u[vtx]=1; t.v[vtx]=0;
				
				terrainTriangles[i++]=t;
				
				t = new Triangle();
				t.setTexture(textures[x][z]);
				t.setRenderMode(RenderMode.STATIC);
				
				vtx=0;
				//TOP LEFT
				t.x[vtx]=xPos;t.y[vtx]=hTL;t.z[vtx]=zPos;
				t.u[vtx]=0; t.v[vtx]=1;
				//TOP RIGHT
				vtx++;
				t.x[vtx]=xPos+gridSquareSize;t.y[vtx]=hTR;t.z[vtx]=zPos;
				t.u[vtx]=1; t.v[vtx]=1;
				//BOTTOM RIGHT
				vtx++;
				t.x[vtx]=xPos+gridSquareSize;t.y[vtx]=hBR;t.z[vtx]=zPos+gridSquareSize;
				t.u[vtx]=1; t.v[vtx]=0;
				
				terrainTriangles[i++]=t;
				}//end for(x)
			}//end for(z)
		}//end constructor
	
	public TerrainSystem(AltitudeMap altitude,int [][] textures, Texture [] texIndexLookup, double gridSquareSize, double heightScalar)
		{
		this(altitude,int2Tex(textures,texIndexLookup),gridSquareSize,heightScalar);
		}// end constructor
	
	private static Texture [][] int2Tex(int [][] iTexGrid, Texture [] texIndexLookup)
		{
		Texture [][] tGrid = new Texture[iTexGrid.length][iTexGrid[0].length];
		for(int z=0; z<iTexGrid[0].length; z++)
			{
			for(int x=0; x<iTexGrid.length;x++)
				{
				tGrid[x][z]=texIndexLookup[iTexGrid[x][z]];
				}
			}//end for(z)
		return tGrid;
		}//end int2Tex()
	
	public void addToWorld(World w, GL2 gl)
		{
		for(Triangle t:terrainTriangles)w.pushTriangle(t, gl);
		}
	
	}//end TerrainSystem
