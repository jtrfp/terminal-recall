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
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.Reporter;


/**
 * 
 * @author Chuck Ritola
 *
 */

public class TextureManager {
    private final GPU				gpu;
    private final ThreadManager			threadManager;
    private final SubTextureWindow 		subTextureWindow;
    private final TextureTOCWindow 		tocWindow;
    public final TRFutureTask<VQCodebookManager>vqCodebookManager;
    private TextureDescription			fallbackTexture;
    private final ConcurrentHashMap<Integer,Texture>
    						colorCache = new ConcurrentHashMap<Integer,Texture>();
    public TextureManager(final GPU gpu, Reporter reporter, ThreadManager threadManager, final UncaughtExceptionHandler exceptionHandler){
	this.gpu                = gpu;
	this.threadManager      = threadManager;
	subTextureWindow 	= new SubTextureWindow(gpu);
	tocWindow 		= new TextureTOCWindow(gpu);
	vqCodebookManager=gpu.submitToGL(new Callable<VQCodebookManager>(){
	    @Override
	    public VQCodebookManager call() throws Exception {
		return new VQCodebookManager(gpu, exceptionHandler);
	    }});
    }//end constructor
    
    public Texture newTexture(ByteBuffer imageRGB8, ByteBuffer imageESTuTv, String debugName, boolean uvWrapping){
	return new Texture(gpu,threadManager,imageRGB8,imageESTuTv,debugName, uvWrapping);
    }
    public Texture newTexture(BufferedImage imgRGBA,BufferedImage imgESTuTv, String debugName, boolean uvWrapping){
	return new Texture(gpu,threadManager,imgRGBA,imgESTuTv,debugName,uvWrapping);
    }
    public SubTextureWindow getSubTextureWindow(){
	return subTextureWindow;
    }
    
    private TextureDescription defaultTriPipeTexture;
    
    public TextureDescription getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	 try{
	  defaultTriPipeTexture = 
	    		new Texture(gpu,threadManager,ImageIO.read(LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),null,
	    			"Default TriPipe Texture (grayNoise)",true);}
	 catch(IOException e){throw new RuntimeException("Failure to load default tripipe texture.",e);}
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
    
    public synchronized TextureDescription getFallbackTexture(){
	if(fallbackTexture!=null)return fallbackTexture;
	Texture t;
	try{
	 t = new Texture(gpu,threadManager,
		ImageIO.read(Texture.class
			.getResourceAsStream("/fallbackTexture.png")),null,
		"Fallback",true);}
	catch(IOException e){throw new RuntimeException("Failure to load fallback texture. Is everything right with the resources directory?",e);}
	fallbackTexture = t;
	return fallbackTexture;
    }//end getFallbackTexture()
    
    public synchronized TextureDescription solidColor(Color color) {
	final int	key 	= color.hashCode();
	Texture		result 	= colorCache.get(key);
	if(result!=null)
	    return result;
	result = new Texture(gpu,threadManager,color);
	colorCache.put(key,result);
	return result;
    }//end solidColor(...)
    
    public TextureTOCWindow getTOCWindow(){
	return tocWindow;
    }
}//end TextureSystem
