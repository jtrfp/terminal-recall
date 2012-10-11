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

import java.util.ArrayList;

import javax.media.opengl.GL2;

/**
 * 3-dimensional chunk of partitioned space to improve rendering time.
 * @author Chuck Ritola
 *
 */
public class RenderingCube
	{
	//SingleTextureTriangleList [] staticTriangles = new SingleTextureTriangleList[256];
	MultiTextureTriangleList staticList = new MultiTextureTriangleList();
	MultiTextureTriangleList staticRadiusList = new MultiTextureTriangleList();
	boolean radiusValidated=false;

	public void add(Triangle t, GL2 gl)
		{
		if(t.renderMode==RenderMode.STATIC)
			{
			//staticTriangles[t.texture.getTextureID()].add(t);
			staticList.add(t, gl);
			}
		}//end addRenderable
	
	public void render(GL2 gl)
		{
		staticList.render(gl);
		}//end render(...)
	
	public void renderRadius(GL2 gl, World w)
		{
		if(!radiusValidated)validateRadius(gl,w);
		staticRadiusList.render(gl);
		}
	
	public void validateRadius(GL2 gl, World w)
		{
		//Create the grid
		int gridPosZ=(int)(w.cameraZ/w.gridBlockSize);
		int gridPosY=(int)(w.cameraY/w.gridBlockSize);
		int gridPosX=(int)(w.cameraX/w.gridBlockSize);
		int gridRadius = (int)(w.cameraViewDepth/w.gridBlockSize);
		
		int gridStartX=gridPosX-gridRadius;
		int gridStartY=gridPosY-gridRadius;
		int gridStartZ=gridPosZ-gridRadius;
		
		int gridEndX=gridPosX+gridRadius;
		int gridEndY=gridPosY+gridRadius;
		int gridEndZ=gridPosZ+gridRadius;
		
		if(staticRadiusList!=null)staticRadiusList.free(gl);
		staticRadiusList = new MultiTextureTriangleList();
		//Load the vertices
		for(int z=gridStartZ; z<gridEndZ; z++)
			{
			for(int y=gridStartY; y<gridEndY; y++)
				{
				for(int x=gridStartX; x<gridEndX; x++)
					{
					for(Triangle t:
							w.renderGrid[TR.bidiMod(x,w.gridSizeX)][TR.bidiMod(y,w.gridSizeY)]
							[TR.bidiMod(z,w.gridSizeZ)].staticList.dumpTriangles())
						{
						staticRadiusList.add(t, gl);
						}//end for(triangles)
					}//end for(x)
				}//end for(y)
			}//end for(z)
		}//end validateRadius
	}//end RenderingCube
