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
package com.ritolaaudio.trcl.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.SelfParsingFile;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;
import com.ritolaaudio.trcl.SpecialRAWDimensions;

public class RAWFile extends SelfParsingFile
	{
	byte [] rawBytes;
	private BufferedImage [] segImg;
	private double scaleWidth=1,scaleHeight=1;
	private Dimension dims;
	private int segWidth,segHeight;
	private Color [] palette;
	
	SegmentDirection dir;
	int numSegs=-1;
	private enum SegmentDirection {VERTICAL,HORIZONTAL}

	public RAWFile(InputStream inputStream) throws IllegalAccessException, IOException
		{
		super(inputStream);
		}

	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{Parser.bytesEndingWith(null,Parser.property("rawBytes",byte[].class),false);}

	/**
	 * @return the rawBytes
	 */
	public byte[] getRawBytes()
		{
		return rawBytes;
		}

	/**
	 * @param rawBytes the rawBytes to set
	 */
	public void setRawBytes(byte[] rawBytes)
		{
		this.rawBytes = rawBytes;
		}
	
	public int getSideLength()
		{
		return (int)Math.sqrt(rawBytes.length);
		}
	
	public int valueAt(int x,int y)
		{return (int)rawBytes[x+(y*getSideLength())] & 0xFF;}
	
	public void setPalette(Color [] palette)
		{if(palette==null)throw new NullPointerException("Palette must be non-null.");
		this.palette=palette;
		}
	
	public BufferedImage [] asSegments(int upScalePowerOfTwo)
		{
		if(palette==null) throw new RuntimeException("Palette property must be set before extracting Image.");
		if(segImg==null)
			{
			System.out.println("Raw len: "+getRawBytes().length);
			dims=SpecialRAWDimensions.getSpecialDimensions(getRawBytes().length);
			System.out.println("Special dims: "+dims);
			int lesserDim,greaterDim;
			if(dims.getHeight()>dims.getWidth()){dir=SegmentDirection.VERTICAL;lesserDim=(int)dims.getWidth();greaterDim=(int)dims.getHeight();}
			else {dir=SegmentDirection.HORIZONTAL;lesserDim=(int)dims.getHeight();greaterDim=(int)dims.getWidth();}
			int newLDim=lesserDim,newGDim=greaterDim;
			//If non-square
			if(lesserDim!=greaterDim)
				{
				System.out.println("Detected as non-square.");
				if(!SpecialRAWDimensions.isPowerOfTwo(lesserDim))
					{newLDim=nextPowerOfTwo(lesserDim);System.out.println("Lesser dim is non-power-of-two.");}
				else{System.out.println("Lesser dim is power-of-two.");}
				newGDim=nextMultiple(greaterDim,lesserDim);
				}//end if(non-square)
			else
				{//Square, make sure they are a power-of-two
				System.out.println("Detected as square.");
				if(!SpecialRAWDimensions.isPowerOfTwo(lesserDim))
					{newGDim=nextPowerOfTwo(greaterDim);
					newLDim=nextPowerOfTwo(lesserDim);
					System.out.println("Non-power-of-two square");
					}
				else{System.out.println("Power-of-two square.");}
				}//end if(square)
			newGDim*=Math.pow(2, upScalePowerOfTwo);
			newLDim*=Math.pow(2, upScalePowerOfTwo);
			int newWidth=0,newHeight=0;
			if(dir==SegmentDirection.VERTICAL){newWidth=newLDim;newHeight=newGDim;}
			else{newWidth=newGDim;newHeight=newLDim;}//Horizontal
			scaleWidth=(double)newWidth/dims.getWidth();
			scaleHeight=(double)newHeight/dims.getHeight();
			System.out.println("newWidth="+newWidth+" newHeight="+newHeight);
			System.out.println("scaleWidth="+scaleWidth+" scaleHeight="+scaleHeight);
			//Break into segments
			numSegs=newGDim/newLDim;
			//Square so height and width are the same.
			segHeight=newLDim;
			segWidth=newLDim;
			System.out.println("Segwidth="+segWidth+" segHeight="+segHeight);
			segImg = new BufferedImage[numSegs];
			for(int seg=0; seg<numSegs;seg++)
				{BufferedImage segBuf = new BufferedImage(segWidth,segHeight,BufferedImage.TYPE_INT_ARGB);
				//System.out.println("Created new seg buffer of size "+segWidth+"x"+segHeight);
				segImg[seg]=segBuf;
				//Generate the bitmap
				for(int y=0; y<segHeight; y++)
					{for(int x=0; x<segWidth;x++)
						{Color c=getFilteredSegmentedPixelAt(x,y,seg);
						segBuf.setRGB(x,y,c.getRGB());
						}//end for(x)
					}//end for(y)
				}//end (create rgb888)
			}//end if(segImg==null)
		return segImg;
		}
	
	private int nextMultiple(int originalValue, int multiplicand)
		{return (int)Math.ceil((double)originalValue/(double)multiplicand)*multiplicand;}
	
	private int nextPowerOfTwo(double v)
		{return (int)Math.pow(2,Math.floor(Math.sqrt(v)));}
	
	private Color getFilteredSegmentedPixelAt(double u, double v, int segment)
		{
		//if(u<0)throw new RuntimeException("u is "+u);
		//if(v<0)throw new RuntimeException("v is "+v);
		switch(dir)
			{
			case HORIZONTAL:
				return getFilteredScaledGlobalPixelAt(u+segment*segWidth,v);
			case VERTICAL:
				return getFilteredScaledGlobalPixelAt(u,v+segment*segHeight);
			}//end switch(dir)
		throw new RuntimeException("Invalid segment direction: "+dir);
		}//end getFilteredSegmentedPixelAt(...)
	
	private Color getFilteredScaledGlobalPixelAt(double u, double v)
		{
		return getFilteredGlobalPixelAt(u/scaleWidth,v/scaleHeight);
		}
	
	/**
	 * Takes a normalized value in range of [0,1] and sharpens it.
	 * @param val
	 * @param sharp
	 * @return
	 * @since May 15, 2013
	 */
	private static double sharp(double val, double sharp)
		{return (Math.tanh((val*2.-1.)*sharp)+1)/2.;}
	
	private Color getFilteredGlobalPixelAt(double u, double v)
		{
		//System.out.println("ACCESS: x="+u+" y="+v);
		//if(u<1)u+=dims.getWidth()-1;
		//if(v<1)v+=dims.getHeight()-1;
		u -= .5;
		v -= .5;
		int x = (int)Math.floor(u);
		int y = (int)Math.floor(v);
		
		Color tl=getPixelAt(x,y+1);
		Color tr=getPixelAt(x+1,y+1);
		Color bl=getPixelAt(x,y);
		Color br=getPixelAt(x+1,y);
		
		//Some experimental attempts at selective blurring/sharpening
		double sharpU=Math.pow(Math.pow((tl.getRed()+bl.getRed())-(tr.getRed()+br.getRed()),2.)+
				Math.pow((tl.getGreen()+bl.getGreen())-(tr.getGreen()+br.getGreen()),2.)+
				Math.pow((tl.getBlue()+bl.getBlue())-(tr.getBlue()+br.getBlue()),2.)+
				Math.pow((tl.getAlpha()+bl.getAlpha())-(tr.getAlpha()+br.getAlpha()),2.),2);
		double sharpV=Math.pow(Math.pow((tl.getRed()+tr.getRed())-(bl.getRed()+br.getRed()),2.)+
				Math.pow((tl.getGreen()+tr.getGreen())-(bl.getGreen()+br.getGreen()),2.)+
				Math.pow((tl.getBlue()+tr.getBlue())-(bl.getBlue()+br.getBlue()),2.)+
				Math.pow((tl.getAlpha()+tr.getAlpha())-(bl.getAlpha()+br.getAlpha()),2.),2);
		double uRatio = (u - x);
		double vRatio = (v - y);
		//uRatio=sharp(uRatio,sharpU);
		//vRatio=sharp(vRatio,sharpV);
		
		double uInverse=1 - uRatio;
		double vInverse=1 - vRatio;
		
		double r = ((double)bl.getRed()   * uInverse  + (double)br.getRed()   * uRatio) * vInverse + 
				((double)tl.getRed() * uInverse  + (double)tr.getRed() * uRatio) * vRatio;
		double g = ((double)bl.getGreen()   * uInverse  + (double)br.getGreen()   * uRatio) * vInverse + 
				((double)tl.getGreen() * uInverse  + (double)tr.getGreen() * uRatio) * vRatio;
		double b = ((double)bl.getBlue()   * uInverse  + (double)br.getBlue()   * uRatio) * vInverse + 
				((double)tl.getBlue() * uInverse  + (double)tr.getBlue() * uRatio) * vRatio;
		double a = ((double)bl.getAlpha()   * uInverse  + (double)br.getAlpha()   * uRatio) * vInverse + 
				((double)tl.getAlpha() * uInverse  + (double)tr.getAlpha() * uRatio) * vRatio;
		//System.out.println("r="+r+" g="+g+" b="+b+" a="+a+" uRatio="+uRatio+" vRatio="+vRatio+" uInverse="+uInverse+" vInverse="+vInverse);
		   return new Color((int)r,(int)g,(int)b,(int)a);
		}
	
	private final Color getPixelAt(int x, int y)
		{
		if(x<0)return palette[0];
		if(y<0)return palette[0];
		if(x>=dims.getWidth())return palette[0];
		if(y>=dims.getHeight())return palette[0];
		return palette[getRawBytes()[x+y*(int)dims.getWidth()]&0xFF];
		}
	}//end RAWFile
