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

import java.awt.Color;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

public class World implements GLEventListener
	{
	double sizeX,sizeY,sizeZ, cameraViewDepth;
	private final int blockGranularity=7;//Dim-Segments per diameter. should exceed 2.
	double gridBlockSize;
	RenderingCube [][][] renderGrid;
	int gridSizeX,gridSizeY,gridSizeZ;
	double cameraX=50000,cameraY,cameraZ=50000;
	Color fogColor = Color.black;
	//DoubleBuffer inanimateVertexBuffer;
	
	public World(double sizeX, double sizeY, double sizeZ, double cameraViewDepth)
		{
		this.sizeX=sizeX;
		this.sizeY=sizeY;
		this.sizeZ=sizeZ;
		this.cameraY=sizeY/3;
		this.cameraViewDepth=cameraViewDepth;
		//Create the grid
		gridBlockSize=cameraViewDepth/blockGranularity;
		gridSizeX=(int)(sizeX/gridBlockSize)+1;
		gridSizeY=(int)(sizeY/gridBlockSize)+1;
		gridSizeZ=(int)(sizeZ/gridBlockSize)+1;
		System.out.println("sizeX "+gridSizeX);
		System.out.println("sizeY "+gridSizeY);
		System.out.println("sizeZ "+gridSizeZ);
		renderGrid = new RenderingCube[gridSizeX][gridSizeY][gridSizeZ];
		for(int z=0; z<gridSizeZ; z++)
			{
			for(int y=0; y<gridSizeY; y++)
				{
				for(int x=0; x<gridSizeX; x++)
					{
					renderGrid[x][y][z]=new RenderingCube();
					}//end for(x)
				}//end for(y)
			}//end for(z)
		}//end constructor
	
	public void pushTriangle(Triangle t, GL2 gl)
		{
		if(t==null)throw new NullPointerException();
		//Average of the coordinates to determine approximate location of the triangle as a whole.
		final double x=(t.x[0]+t.x[1]+t.x[2])/3.;
		final double y=(t.y[0]+t.y[1]+t.y[2])/3.;
		final double z=(t.z[0]+t.z[1]+t.z[2])/3.;
		
		renderGrid
			[(int)(x/gridBlockSize)]
			[(int)(y/gridBlockSize)]
			[(int)(z/gridBlockSize)].add(t, gl);
		}//end pushRenderable()

	@Override
	public void display(GLAutoDrawable drawable)
		{
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glPushMatrix();
		final float fogRed=(float)fogColor.getRed()/255f;
		final float fogGreen=(float)fogColor.getGreen()/255f;
		final float fogBlue=(float)fogColor.getBlue()/255f;
		
		//BACKGROUND
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDepthFunc(GL2.GL_ALWAYS);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		
		gl.glBegin(GL2.GL_QUADS);
			gl.glColor3f(fogRed,fogGreen,fogBlue);
			gl.glVertex2d(-1, -1);
			gl.glVertex2d(1, -1);
			gl.glVertex2d(1, 1);
			gl.glVertex2d(-1, 1);
			
			gl.glColor3d(1, 1, 1);
		gl.glEnd();
		
		gl.glPopMatrix();
		
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glDepthFunc(GL2.GL_LESS);
		
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		
		//FOG
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
		gl.glFogfv(GL2.GL_FOG_COLOR, FloatBuffer.wrap(new float[]{fogRed,fogGreen,fogBlue}));
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_DONT_CARE);
		gl.glFogf(GL2.GL_FOG_START, (float)TR.mapSquareSize*4);
		gl.glFogf(GL2.GL_FOG_END, (float)(TR.visibilityDiameterInMapSquares*TR.mapSquareSize)/2.3f);
		gl.glEnable(GL2.GL_FOG);
		
		TR.glu.gluLookAt(cameraX, cameraY, cameraZ, cameraX, cameraY, cameraZ+1, 0, 1, 0);
		
		cameraX+=1000;
		cameraZ+=1000;
		
		int gridPosZ=(int)(cameraZ/gridBlockSize);
		int gridPosY=(int)(cameraY/gridBlockSize);
		int gridPosX=(int)(cameraX/gridBlockSize);
		//System.out.println("x="+gridPosX+" z="+gridPosZ);
		renderGrid[TR.bidiMod(gridPosX,gridSizeX)][TR.bidiMod(gridPosY,gridSizeY)][TR.bidiMod(gridPosZ,gridSizeZ)].renderRadius(gl, this);
		
		gl.glPopMatrix();
		}//end display()

	@Override
	public void dispose(GLAutoDrawable arg0)
		{
		// TODO Auto-generated method stub
		
		}

	@Override
	public void init(GLAutoDrawable drawable)
		{
		GL2 gl = drawable.getGL().getGL2();
		TR.canvas.setGL(new DebugGL2(gl));
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glClearColor(0f, 0f, 0f, 0f);
		TR.glu.gluPerspective(45.0f, (float)TR.frame.getWidth()/(float)TR.frame.getHeight(), TR.mapSquareSize/10, cameraViewDepth);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glShadeModel(GL2.GL_FLAT);
		gl.glTexParameteri( GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_NEAREST_MIPMAP_LINEAR );
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		}

	@Override
	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4)
		{
		// TODO Auto-generated method stub
		
		}
	}//World
