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
import java.util.concurrent.Future;

public class GLFont
	{
	private final Font realFont;
	private final Future<Texture>[] textures;
	private Graphics2D g;
	private FontMetrics metrics;
	private final int [] widths = new int[256];
	private final double [] glWidths=new double[256];
	private static final int sideLength=64;
	private static final Color TEXT_COLOR=new Color(80,200,180);
	
	public GLFont(Font realFont)
		{
		this.realFont=realFont.deriveFont((float)sideLength).deriveFont(Font.BOLD);
		//Generate the textures
		textures = new Future[256];
		Texture empty=renderToTexture(' ');
		for(int c=0; c<256; c++)
			{textures[c]=new DummyFuture<Texture>(realFont.canDisplay(c)?renderToTexture(c):empty);}
		}//end constructor
	public Future<Texture>[] getTextures()
		{return textures;}
	
	private Texture renderToTexture(int c)
		{
		BufferedImage img = new BufferedImage(sideLength, sideLength, BufferedImage.TYPE_INT_ARGB);
		g=img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(realFont);
		metrics=g.getFontMetrics();
		if(metrics.charWidth(c)>=getTextureSideLength())
			{int size=g.getFont().getSize();
			g.setFont(realFont.deriveFont((float)(size*size)/(float)metrics.charWidth(c)*.9f));
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
	public double height(){return metrics.getMaxAdvance();}
	}//end GLFont
