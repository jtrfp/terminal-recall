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
package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.jtrfp.trcl.LineSegment;


/**
 * 
 * @author Chuck Ritola
 *
 */

public class TextureManager {
    private final TR 				tr;
    private final SubTextureWindow 		subTextureWindow;
    private final TextureTOCWindow 		tocWindow;
    public final TRFutureTask<VQCodebookManager>vqCodebookManager;
    private TextureDescription			fallbackTexture;
    private final ConcurrentHashMap<Integer,Texture>
    						colorCache = new ConcurrentHashMap<Integer,Texture>();
    public TextureManager(final TR tr){
	this.tr			= tr;
	subTextureWindow 	= new SubTextureWindow(tr);
	tocWindow 		= new TextureTOCWindow(tr);
	vqCodebookManager=tr.getThreadManager().submitToGL(new Callable<VQCodebookManager>(){
	    @Override
	    public VQCodebookManager call() throws Exception {
		return new VQCodebookManager(tr);
	    }});
    }//end constructor
    
    public Texture newTexture(ByteBuffer imageRGB8, String debugName, boolean uvWrapping){
	return new Texture(imageRGB8,debugName,tr, uvWrapping);
    }
    public Texture newTexture(BufferedImage img, String debugName, boolean uvWrapping){
	return new Texture(img,debugName,tr, uvWrapping);
    }
    public SubTextureWindow getSubTextureWindow(){
	return subTextureWindow;
    }
    
    private TextureDescription defaultTriPipeTexture;
    
    public TextureDescription getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	 defaultTriPipeTexture
	    	= 
	    		new Texture(Texture.RGBA8FromPNG(LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),
	    			"Default TriPipe Texture (grayNoise)",tr,true);
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
    
    public synchronized TextureDescription getFallbackTexture(){
	if(fallbackTexture!=null)return fallbackTexture;
	Texture t;
	t = new Texture(
		Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/fallbackTexture.png")),
		"Fallback",tr,true);
	fallbackTexture = t;
	return fallbackTexture;
    }//end getFallbackTexture()
    
    public synchronized TextureDescription solidColor(Color color) {
	final int	key 	= color.getRed()+color.getGreen()*256+color.getBlue()*65536;
	Texture		result 	= colorCache.get(key);
	if(result!=null)
	    return result;
	result = new Texture(color,tr);
	colorCache.put(key,result);
	return result;
    }//end solidColor(...)
    
    public TextureTOCWindow getTOCWindow(){
	return tocWindow;
    }
}//end TextureSystem
