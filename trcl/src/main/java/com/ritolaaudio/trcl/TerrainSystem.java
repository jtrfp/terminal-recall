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

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public final class TerrainSystem extends RenderableSpacePartitioningGrid
	{
	final double gridSquareSize;
	final double heightScalar;
	final ArrayList<TerrainChunk> renderingCubes = new ArrayList<TerrainChunk>();
	public TerrainSystem(final AltitudeMap altitude, final TextureMesh textureMesh, final double gridSquareSize, final double heightScalar, final World world)
		{
		super(world.getRootGrid());
		final int width=(int)altitude.getWidth(); int height=(int)altitude.getHeight();
		this.gridSquareSize=gridSquareSize;
		this.heightScalar=heightScalar;
		
		final int chunkSideLength=4;//Keep at power of two for now. 4x4 = 16. 16x6 = 96. 96 vertices per GLSL block means 1 chunk per block.
		
		final double u[] = {0,1,1,0};
		final double v[] = {1,1,0,0};
		//For each chunk
		for(int gZ=0; gZ<height; gZ+=chunkSideLength)
			{
			for(int gX=0; gX<width; gX+=chunkSideLength)
				{
				final double objectX=Math.round(((double)gX+((double)chunkSideLength/2.))*gridSquareSize);
				final double objectZ=Math.round(((double)gZ+((double)chunkSideLength/2.))*gridSquareSize);
				final double objectY=Math.round(altitude.heightAt(gX, gZ)*heightScalar);
				
				final Model m = new Model(false);
				//m.setDebugName("TerrainChunk z="+gZ+" x="+gX);
				//for each square
				for(int cZ=gZ; cZ<gZ+chunkSideLength; cZ++)//TODO: affix to origin and apply matrix.
					{
					for(int cX=gX; cX<gX+chunkSideLength; cX++)
						{
						final double hTL=altitude.heightAt(cX, cZ)*heightScalar;
						final double hTR=altitude.heightAt((cX+1),cZ)*heightScalar;
						final double hBR=altitude.heightAt((cX+1),(cZ+1))*heightScalar;
						final double hBL=altitude.heightAt(cX,(cZ+1))*heightScalar;
						final double xPos=cX*gridSquareSize;
						final double zPos=cZ*gridSquareSize;
						
						Triangle [] tris = Triangle.quad2Triangles(// CLOCKWISE
								new double [] {xPos-objectX,xPos+gridSquareSize-objectX,xPos+gridSquareSize-objectX,xPos-objectX}, //x
								new double [] {hTL-objectY,hTR-objectY,hBR-objectY,hBL-objectY}, 
								new double [] {zPos-objectZ,zPos-objectZ,zPos+gridSquareSize-objectZ,zPos+gridSquareSize-objectZ}, 
								u,
								v,
								textureMesh.textureAt(cX, cZ), RenderMode.STATIC);
						
						m.addTriangle(tris[0]);
						m.addTriangle(tris[1]);
						}//end for(cX)
					}//end for(cZ)
				//Add to grid
				//System.out.println("TerrainSystem: addToGrid ...");
				if(m.finalizeModel().getTriangleList()!=null)
					{
					final TerrainChunk chunkToAdd = new TerrainChunk(world,m);
					chunkToAdd.setPosition(new Vector3D(objectX, objectY, objectZ));
					add(chunkToAdd);
					}
				else {System.out.println("Rejected chunk: "+m.getDebugName());}
				//System.out.println("TerrainSystem: END addToGrid ...");
				}//end for(gX)
			}//end for(gZ)
		//System.out.println("TerrainSystem.constructor finish");
		}//end constructor
	/**
	 * @return the gridSquareSize
	 */
	public double getGridSquareSize()
		{
		return gridSquareSize;
		}
	/**
	 * @return the heightScalar
	 */
	public double getHeightScalar()
		{
		return heightScalar;
		}
	/**
	 * @return the renderingCubes
	 */
	public ArrayList<TerrainChunk> getRenderingCubes()
		{
		return renderingCubes;
		}
	}//end TerrainSystem
