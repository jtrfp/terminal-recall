/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;

public class GLFont{
	private final 		Texture[] textures;
	private double 		maxAdvance=-1;
	private final int [] 	widths = new int[256];
	private final double [] glWidths=new double[256];
	private final int 	sideLength;
	private static final Color TEXT_COLOR=new Color(80,200,180);
	private final TR	tr;
	
	public GLFont(ByteBuffer []indexedPixels, Color [] palette, int imgHeight, List<Integer> widths, int asciiOffset, TR tr){
	    this(Texture.indexed2RGBA8888(indexedPixels,palette),imgHeight,widths,asciiOffset,tr);
	}
	
	public GLFont(ByteBuffer []rgba8888, int imgHeight, List<Integer> widths, int asciiOffset, TR tr){
	    this.tr=tr;
	    final int numChars = widths.size();
	    int maxDim=0;// xOffset=0;
	    //Determine the biggest dimension and push to maxDim, also load the intrinsic widths
	    for(int i=0; i<widths.size(); i++){
		if(widths.get(i)>maxDim)maxDim=widths.get(i);
		this.widths[i+asciiOffset]=widths.get(i);
	    }if(imgHeight>maxDim)maxDim=imgHeight;
	    //Scale maxDim up to next power of 2 and use as sideLength
	    sideLength = (int)Math.pow(2, Math.ceil(Math.log(maxDim)/Math.log(2)));
	    maxAdvance=imgHeight;
	    textures = new Texture[256];
	Texture empty = (tr
		.gpu.get()
		.textureManager.get()
		.newTexture(
			ByteBuffer.allocateDirect(sideLength * sideLength * 4),null,
			"GLFont rgba buf empty",false));
	    //Load the gl-specific widths
	    for(int i=0; i<widths.size();i++){
		glWidths[i+asciiOffset]=(double)widths.get(i)/((double)getTextureSideLength()*1.2);
	    }//Load empties to the left side of the ASCII textures.
	    for(int i=0; i<asciiOffset;i++){
		textures[i] = empty;
	    }//end for(i:asciiOffset)
	    //Create the actual valid ASCII textures.
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
	    textures[i + asciiOffset] = tr.gpu.get().textureManager.get().newTexture(
		    texBuf, null,"GLFont rgba buf char=" + (char) i,false);
	    }//end for(i:numChars)
	    //Load empties to the right side of the ASCII textures.
	    for(int i=asciiOffset+numChars; i<256;i++){
		textures[i] = empty;
	    }//end for(i:asciiOffset)
	}//end GLFont
	
	public GLFont(Font realFont, TR tr){
	    	this.tr=tr;
	    	sideLength=64;
		final Font font=realFont.deriveFont((float)sideLength)/*.deriveFont(Font.BOLD)*/;
		//Generate the textures
		textures = new Texture[256];
		Texture empty=renderToTexture(' ',realFont);
		for(int c=0; c<256; c++)
			{textures[c]=realFont.canDisplay(c)?renderToTexture(c,font):empty;}
		}//end constructor
	public Texture[] getTextures()
		{return textures;}
	
	private Texture renderToTexture(int c, Font font){
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
		return tr.gpu.get().textureManager.get().newTexture(img,null,"GLFont "+(char)c,false);
		}//end renderToTexture(...)
	
	public double getTextureSideLength(){return sideLength;}
	public double glWidthOf(char currentChar)
		{return glWidths[currentChar];}
	public double height(){return maxAdvance;}
	}//end GLFont
