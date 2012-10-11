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
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;

public class Texture
	{
	private int textureID;
	private Color [][] colorGrid;
	
	public Texture(int id)
		{
		this.setTextureID(id);
		}
	
	public Texture(BufferedImage image)
		{
		this(toColorGrid(image));
		}//end constructor
	
	private static Color [][] toColorGrid(BufferedImage image)
		{
		Color [][] result = new Color[image.getWidth()][image.getHeight()];
		for(int y=0; y<image.getHeight(); y++)
			{
			for(int x=0; x<image.getWidth(); x++)
				{
				result[x][y]=new Color(image.getRGB(x, y));
				}//end for(x)
			}//end for(y)
		return result;
		}//end constructor
	
	public Texture(Color [][] image)
		{
		ByteBuffer buf = ByteBuffer.allocateDirect(image.length*image[0].length*3);
		colorGrid=image;
		
		for(int z=image[0].length-1; z>=0;z--)
			{
			for(int x=0; x<image.length; x++)
				{
				buf.put((byte)image[x][z].getRed());
				buf.put((byte)image[x][z].getGreen());
				buf.put((byte)image[x][z].getBlue());
				}//end for(x)
			}//end for(z)
		
		buf.rewind();
		GL2 gl = TR.canvas.getGL().getGL2();
		gl.getContext().makeCurrent();
		IntBuffer ib= IntBuffer.allocate(1);
		gl.glGenTextures(1, ib);
		textureID=ib.get();
		//System.out.println("ID: "+textureID+" width: "+width);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, 
		                   GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, 
		                   GL2.GL_LINEAR);
		TR.glu.gluBuild2DMipmaps(GL2.GL_TEXTURE_2D, GL2.GL_RGB, image.length, image[0].length, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
		/*gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, 
				image.length,image[0].length, 
				0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);*/
		gl.getContext().release();
		}//end constructor
	
	public Texture(byte [] rawBytes, int width, Color [] palette)
		{
		ByteBuffer buf = ByteBuffer.allocateDirect(rawBytes.length*3);
		for(byte b:rawBytes)
			{
			buf.put((byte)palette[(int)b&0xFF].getRed());
			buf.put((byte)palette[(int)b&0xFF].getGreen());
			buf.put((byte)palette[(int)b&0xFF].getBlue());
			}
		buf.rewind();
		//TR.canvas.getContext().makeCurrent();
		GL2 gl = TR.canvas.getGL().getGL2();
		gl.getContext().makeCurrent();
		IntBuffer ib= IntBuffer.allocate(1);
		gl.glGenTextures(1, ib);
		textureID=ib.get();
		//System.out.println("ID: "+textureID+" width: "+width);
		gl.glBindTexture(GL2.GL_TEXTURE_2D, textureID);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MAG_FILTER, 
		                   GL2.GL_LINEAR);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_MIN_FILTER, 
		                   GL2.GL_LINEAR);
		gl.glTexImage2D(GL2.GL_TEXTURE_2D, 0, GL2.GL_RGB, 
				width,rawBytes.length/(width), 
				0, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, buf);
		gl.getContext().release();
		}//end constructor
	
	public boolean isValid()
		{
		return textureID>=0;
		}
	
	public void free()
		{
		//TR.canvas.getContext().makeCurrent();
		GL2 gl = TR.canvas.getGL().getGL2();
		gl.getContext().makeCurrent();
		gl.glDeleteTextures(1, IntBuffer.wrap(new int []{textureID}));
		textureID=-1;
		gl.getContext().release();
		}
	/**
	 * @return the textureID
	 */
	public int getTextureID()
		{
		return textureID;
		}
	/**
	 * @param textureID the textureID to set
	 */
	public void setTextureID(int textureID)
		{
		this.textureID = textureID;
		}
	
	public static final Color [] GREYSCALE;
	static
		{
		GREYSCALE=new Color[256];
		for(int i=0; i<256; i++)
			{
			GREYSCALE[i]=new Color(i,i,i);
			}
		}//end static{}
	
	public Color getAverageColor()
		{
		int height=colorGrid.length;
		int width=colorGrid[0].length;
		int r=0,g=0,b=0;
		for(int v=0; v<height; v++)
			{
			for(int u=0; u<height; u++)
				{
				r+=colorGrid[u][v].getRed();
				g+=colorGrid[u][v].getGreen();
				b+=colorGrid[u][v].getBlue();
				}//end for(u)
			}//end for(v)
		int t=width*height;
		return new Color(r/t,g/t,b/t);
		}//end getAverageColor
	}//end Texture
