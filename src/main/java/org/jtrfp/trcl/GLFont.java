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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Future;

public class GLFont
	{
	private final Future<Texture>[] textures;
	private double maxAdvance=-1;
	private final int [] widths = new int[256];
	private final double [] glWidths=new double[256];
	private final int sideLength;
	private static final Color TEXT_COLOR=new Color(80,200,180);
	
	public GLFont(ByteBuffer []indexedPixels, Color [] palette, int imgHeight, List<Integer> widths, int asciiOffset){
	    this(Texture.indexed2RGBA8888(indexedPixels,palette),imgHeight,widths,asciiOffset);
	}
	
	public GLFont(ByteBuffer []rgba8888, int imgHeight, List<Integer> widths, int asciiOffset){
	    final int numChars = widths.size();
	    int maxDim=0;// xOffset=0;
	    for(int i=0; i<widths.size(); i++){
		if(widths.get(i)>maxDim)maxDim=widths.get(i);
		this.widths[i+asciiOffset]=widths.get(i);
	    }if(imgHeight>maxDim)maxDim=imgHeight;
	    sideLength = (int)Math.pow(2, Math.ceil(Math.log(maxDim)/Math.log(2)));
	    maxAdvance=imgHeight;
	    textures = new Future[256];
	    DummyFuture<Texture> empty = new DummyFuture<Texture>(new Texture(ByteBuffer.allocateDirect(sideLength*sideLength*4),"GLFont rgba buf empty"));
	    for(int i=0; i<widths.size();i++){
		glWidths[i+asciiOffset]=(double)widths.get(i)/((double)getTextureSideLength()*1.2);
	    }
	    for(int i=0; i<asciiOffset;i++){
		textures[i] = empty;
	    }//end for(i:asciiOffset)
	    for(int i=0; i<numChars; i++){
		ByteBuffer texBuf = ByteBuffer.allocateDirect(sideLength*sideLength*4);
		for(int row=0; row<imgHeight; row++){
		    rgba8888[i].clear();
		    rgba8888[i].position(4*row*widths.get(i));
		    rgba8888[i].limit(rgba8888[i].position()+4*widths.get(i));
		    texBuf.clear();
		    texBuf.position(row*sideLength*4);
		    texBuf.limit(texBuf.position()+sideLength*4);
		    texBuf.put(rgba8888[i]);
		}//end for(imgHeight)
		textures[i+asciiOffset]=new DummyFuture<Texture>(new Texture(texBuf,"GLFont rgba buf char="+(char)i));
	    }//end for(i:numChars)
	    for(int i=asciiOffset+numChars; i<256;i++){
		textures[i] = empty;
	    }//end for(i:asciiOffset)
	}//end GLFont
	
	public GLFont(Font realFont)
		{
	    	sideLength=64;
		final Font font=realFont.deriveFont((float)sideLength).deriveFont(Font.BOLD);
		//Generate the textures
		textures = new Future[256];
		Texture empty=renderToTexture(' ',realFont);
		for(int c=0; c<256; c++)
			{textures[c]=new DummyFuture<Texture>(realFont.canDisplay(c)?renderToTexture(c,font):empty);}
		}//end constructor
	public Future<Texture>[] getTextures()
		{return textures;}
	
	private Texture renderToTexture(int c, Font font)
		{
		BufferedImage img = new BufferedImage(sideLength, sideLength, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g=img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(font);
		FontMetrics metrics=g.getFontMetrics();
		maxAdvance=metrics.getMaxAdvance();
		if(metrics.charWidth(c)>=getTextureSideLength())
			{int size=g.getFont().getSize();
			g.setFont(font.deriveFont((float)(size*size)/(float)metrics.charWidth(c)*.9f));
			metrics=g.getFontMetrics();
			}//end if(too big to fit)
		g.setColor(TEXT_COLOR);
		g.drawChars(new char [] {(char)c}, 0, 1, (int)((double)getTextureSideLength()*.05), (int)((double)getTextureSideLength()*.95));
		widths[c]=metrics.charWidth(c);
		glWidths[c]=(double)widths[c]/(double)getTextureSideLength();
		g.dispose();
		return new Texture(img,"GLFont "+(char)c);
		}
	
	public double getTextureSideLength(){return sideLength;}
	public double glWidthOf(char currentChar)
		{return glWidths[currentChar];}
	public double height(){return maxAdvance;}
	}//end GLFont
