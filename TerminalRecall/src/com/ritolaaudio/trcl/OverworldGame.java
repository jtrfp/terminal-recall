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


import java.io.IOException;

import javax.media.opengl.GL2;

import jtrfp.common.FileLoadException;

import com.ritolaaudio.trcl.file.LVLFile;

public class OverworldGame
	{
	final double heightMultiplier=10;
	
	AltitudeMap heightMap;
	LVLFile lvlFile;
	Texture [][] textureGrid = new Texture[256][256];
	TerrainSystem terrain;
	CloudSystem cloudSystem;
	World world;
	
	final int visibleTerrainGridDiameter=(int)TR.visibilityDiameterInMapSquares;
	
	public OverworldGame(LVLFile lvl) throws IllegalAccessException, FileLoadException, IOException
		{
		this.lvlFile=lvl;
		this.heightMap=		TR.resources.getRAWAltitude(lvl.getHeightMapOrTunnelFile());
		textureGrid = TR.resources.getTerrainTextureGrid
				(lvl.getTexturePlacementFile(),TR.resources.getTextures(lvl.getLevelTextureListFile(), 
						lvl.getGlobalPaletteFile(),GammaCorrectingColorProcessor.singleton));
		world = new World(256*TR.mapSquareSize,16.*TR.mapSquareSize,256*TR.mapSquareSize,TR.mapSquareSize*visibleTerrainGridDiameter/2);
		
		terrain = new TerrainSystem(heightMap, textureGrid, TR.mapSquareSize, TR.mapSquareSize*heightMultiplier);
		GL2 gl = TR.canvas.getGL().getGL2();
		gl.getContext().makeCurrent();
		terrain.addToWorld(world,gl);
		if(!lvl.getCloudTextureFile().contentEquals("stars.vox"))
			{
			cloudSystem = new CloudSystem(
					lvlFile.getCloudTextureFile(),
					TR.resources.getPalette(lvlFile.getBackgroundGradientPaletteFile()),
					TR.mapSquareSize*10,
					(int)(TR.mapWidth/(TR.mapSquareSize*10)),
					gl,
					world.sizeY/2);
			cloudSystem.addToWorld(world, gl);
			}
		gl.getContext().release();
		TR.canvas.addGLEventListener(world);
		}//end OverworldGame
	/*
	private int uploadCloudRAWToTexture(final RAWFile rawFile, final ACTFile actFile)
		{
		return uploadRGBTexture(indexedCloudToRGB(rawFile,actFile),rawFile.getSideLength());
		}
	
	private int uploadRAWToTexture(final RAWFile rawFile, final ACTFile actFile)
		{
		return uploadRGBTexture(indexedToRGB(rawFile,actFile),rawFile.getSideLength());
		}
	
	private ByteBuffer indexedToRGB(RAWFile texture, ACTFile actFile)
		{
		final ByteBuffer buffer = ByteBuffer.allocateDirect(texture.getSideLength()*texture.getSideLength()*3);//RGB * pixels
		for(int bi=0; bi<texture.getRawBytes().length;bi++)
			{
			int index = ((int)texture.getRawBytes()[bi] & 0xFF);
			final TRColor color = actFile.getColorTable()[index];
			buffer.put(ag(color.getRed()));
			buffer.put(ag(color.getGreen()));
			buffer.put(ag(color.getBlue()));
			}//end for(texture.getRawBytes)
		
		buffer.rewind();//Avoiding a trainwreck just in case JOGL randomly decides to read from the current position onward.
		return buffer;
		}
	
	private ByteBuffer indexedCloudToRGB(RAWFile texture, ACTFile actFile)
		{
		final ByteBuffer buffer = ByteBuffer.allocateDirect(texture.getSideLength()*texture.getSideLength()*3);//RGB * pixels
		double avgR=0,avgG=0,avgB=0;
		final double length=texture.getRawBytes().length;
		for(int bi=0; bi<length;bi++)
			{
			final int index = ((int)texture.getRawBytes()[bi] & 0xFF)-48;//Palette is apparently offset for clouds?
			final TRColor color = actFile.getColorTable()[index];
			buffer.put(ag(color.getRed()));		avgR+=(int)color.getRed()&0xFF;
			buffer.put(ag(color.getGreen()));	avgG+=(int)color.getGreen()&0xFF;
			buffer.put(ag(color.getBlue()));	avgB+=(int)color.getBlue()&0xFF;
			//System.out.println("index="+index+" red="+((int)color.getRed()&0xFF)+" green="+((int)color.getGreen()&0xFF)+" blue="+((int)color.getBlue()&0xFF));
			}//end for(texture.getRawBytes)
		//Take the average of this texture and make it the fog
		fogColor = new Color((int)(avgR/length),(int)(avgG/length),(int)(avgB/length));
		buffer.rewind();//Avoiding a trainwreck just in case JOGL randomly decides to read from the current position onward.
		return buffer;
		}
	
	private int uploadRGBTexture(final ByteBuffer buffer,final int sideLength)
		{
		GL2 gl = TR.canvas.getGL().getGL2();
		final IntBuffer iBuf=IntBuffer.allocate(1);
		gl.glGenTextures(1, iBuf);
		final int result =iBuf.get();
		//Upload the texture to the card
		gl.glBindTexture(GL2.GL_TEXTURE_2D, result);
		gl.glPixelStorei(GL2.GL_UNPACK_ALIGNMENT, 1);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, 
		                   GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, 
		                   GL2.GL_LINEAR);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, 
				sideLength,sideLength, 
				0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buffer);
		//TODO: Register this index to a list so we can release them afterward
		return result;
		}
	
	@Override
	public void render(GL2 gl)
		{
		final float fogRed=(float)ag(fogColor.getRed())/255f;
		final float fogGreen=(float)ag(fogColor.getGreen())/255f;
		final float fogBlue=(float)ag(fogColor.getBlue())/255f;
		//BACKGROUND
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl.glDisable(GL2.GL_LIGHTING);
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
		gl.glEnd();
		
		gl.glPopMatrix();
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glDepthFunc(GL2.GL_LESS);
		
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		
		//FOG
		gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
		gl.glFogfv(GL2.GL_FOG_COLOR, FloatBuffer.wrap(new float[]{fogRed,fogGreen,fogBlue}));
		gl.glHint(GL2.GL_FOG_HINT, GL2.GL_DONT_CARE);
		gl.glFogf(GL2.GL_FOG_START, (float)TR.mapSquareSize*4);
		gl.glFogf(GL2.GL_FOG_END, (float)(TR.visibilityDiameterInMapSquares*TR.mapSquareSize));
		gl.glEnable(GL2.GL_FOG);
		gl.glPushMatrix();
		
		TR.glu.gluLookAt(locationX, locationY, locationZ, locationX, locationY, locationZ+1, 0, 1, 0);
		
		//CLOUD LAYER
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,GL2.GL_DECAL);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, cloudTextureIndex);
		final int cloudscapeWidthInTiles=4*(int)(visibleTerrainGridDiameter*TR.mapSquareSize/cloudTileSize);
		final int cloudscapeCenterTileOffset=cloudscapeWidthInTiles/2+1;
		int cloudOffsetX=0, cloudOffsetZ=(int)(cloudTileSize*cloudscapeWidthInTiles/2);// Depending on direction we are looking. Saves work, deepens FOV
		final int cloudCornerX=cloudOffsetX+(int)(locationX/cloudTileSize)*(int)cloudTileSize-(int)cloudTileSize*cloudscapeCenterTileOffset;
		final int cloudCornerZ=cloudOffsetZ+(int)(locationZ/cloudTileSize)*(int)cloudTileSize-(int)cloudTileSize*cloudscapeCenterTileOffset;
		
		for(int cz=cloudCornerZ; cz<cloudCornerZ+cloudscapeWidthInTiles*cloudTileSize; cz+=cloudTileSize)
			{
			for(int cx=cloudCornerX; cx<cloudCornerX+cloudscapeWidthInTiles*cloudTileSize; cx+=cloudTileSize)
				{
				gl.glBegin(GL2.GL_QUADS);
				gl.glTexCoord2d(0, 0);gl.glVertex3d(cx, cloudHeight,cz);
				gl.glTexCoord2d(1, 0);gl.glVertex3d(cx+cloudTileSize, cloudHeight,cz);
				gl.glTexCoord2d(1, 1);gl.glVertex3d(cx+cloudTileSize, cloudHeight,cz+cloudTileSize);
				gl.glTexCoord2d(0, 1);gl.glVertex3d(cx, cloudHeight, cz+cloudTileSize);
				gl.glEnd();
				}//end for(cx)
			}//end for(cz)
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		//SUN
		gl.glEnable(GL2.GL_LIGHTING);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, FloatBuffer.wrap(new float[]{1f,.8f,.9f}));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, FloatBuffer.wrap(new float[]{.6f,.5f,.6f}));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, FloatBuffer.wrap(new float[]{.2f,.3f,.25f}));
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, FloatBuffer.wrap(new float[]{0,(float)TR.mapSquareSize*15f,0}));
		gl.glEnable(GL2.GL_LIGHT0);
		
		//TERRAIN
		int gridOffsetX=0, gridOffsetY=20; //Dependent on direction we are looking. Saves work for polys which are behind.
		for(int y=-visibleTerrainGridDiameter/2; y<visibleTerrainGridDiameter; y++)
			{
			for(int x=-visibleTerrainGridDiameter/2; x<visibleTerrainGridDiameter; x++)
				{
				renderGrid((int)(locationX/TR.mapSquareSize)+x+gridOffsetX,(int)(locationZ/TR.mapSquareSize)+y+gridOffsetY,gl);
				}//end for(x)
			}//end for(y)
		
		locationX+=TR.mapSquareSize/20;
		locationZ+=TR.mapSquareSize/20;
		
	    gl.glPopMatrix();
		}*/
	/*
	public class TerrainRenderer implements Renderable
		{
		double h0,h1,h2,h3; //clockwise quad coords, starting top-left.
		int x,y;
		public TerrainRenderer(int x, int y, double [][] heightMap)
			{
			h0=heightMap[x][y];
			h1=heightMap[(x+1)%heightMap.length][y];
			h2=heightMap[(x+1)%heightMap.length][(y+1)%heightMap[0].length];
			h3=heightMap[x][(y+1)%heightMap[0].length];
			this.x=x;
			this.y=y;
			}
		@Override
		public void render(GL2 gl)
			{
			//System.out.println("Rendering "+x+" "+y+" "+h0+" "+h1+" "+h2+" "+h3 );
			//System.out.println(terrainTextures[x][y]);
			gl.glPushMatrix();
			gl.glEnable(GL2.GL_LIGHTING);
			gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE, FloatBuffer.wrap(new float[]{1,1,1,1}));
			gl.glFrontFace(GL2.GL_CW);
			gl.glNormal3d(.1, .1, .1);//TODO
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,GL2.GL_DECAL);
			gl.glBindTexture(GL2.GL_TEXTURE_2D, textureGrid[x][y].getTextureID());
			
			gl.glTranslated(x*TR.mapSquareSize, 0, y*TR.mapSquareSize);
			
			gl.glBegin(GL2.GL_QUADS);
			
			gl.glTexCoord2d(0, 0);
			gl.glVertex3d(0,h0,0);
			
			gl.glTexCoord2d(0, 1);
			gl.glVertex3d(TR.mapSquareSize,h1, 0);
			
			gl.glTexCoord2d(1, 1);
			gl.glVertex3d(TR.mapSquareSize,h2, TR.mapSquareSize);
			
			gl.glTexCoord2d(1, 0);
			gl.glVertex3d(0,h3, TR.mapSquareSize);
			gl.glEnd();
			gl.glDisable(GL2.GL_TEXTURE_2D);
			
			//TR.glut.glutSolidTeapot(TR.mapSquareSize/2);
			gl.glPopMatrix();
			//System.out.println("error "+gl.glGetError());
			}
		}//end DefaultTerrainRenderer
	
	public interface Renderable
		{
		public void render(GL2 gl);
		}
	
	public class RenderingList implements Renderable
		{
		Renderable [] renderables;
		public RenderingList(Renderable [] renderables)
			{
			this.renderables=renderables;
			}
		@Override
		public void render(GL2 gl)
			{
			for(Renderable r:renderables){r.render(gl);}
			}
		}//end HeightMapRenderingList
	*/
	}//end OverworldGame
