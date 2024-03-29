/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gpu;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.core.ThreadManager;


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
    //public final Future<VQCodebookManager>vqCodebookManager;
    public final VQCodebookManager vqCodebookManager;
    private Texture			fallbackTexture;
    private final ConcurrentHashMap<Integer,VQTexture>
    						colorCache = new ConcurrentHashMap<Integer,VQTexture>();
    private  SolidColorTextureFactory solidColorTextureFactory;
    private  UncompressedVQTextureFactory uncompressedVQTextureFactory;
    
    public TextureManager(final GPU gpu, ThreadManager threadManager, final UncaughtExceptionHandler exceptionHandler){
	this.gpu                = gpu;
	this.threadManager      = threadManager;
	subTextureWindow 	= new SubTextureWindow(gpu);
	tocWindow 		= new TextureTOCWindow(gpu);
	/*
	vqCodebookManager=gpu.getGlExecutor().submitToGL(new GLExecutable<VQCodebookManager,GL3>(){
	    @Override
	    public VQCodebookManager execute(GL3 gl) throws Exception {
		return new VQCodebookManager(gpu, exceptionHandler, gl);
	    }});*/
	vqCodebookManager = new VQCodebookManager(gpu, exceptionHandler);
    }//end constructor
    
    public VQTexture newTexture(ByteBuffer imageRGB8, ByteBuffer imageESTuTv, String debugName, boolean uvWrapping, boolean generateMipMaps){
	return getUncompressedVQTextureFactory().newUncompressedVQTexture(imageRGB8,imageESTuTv,debugName, uvWrapping, generateMipMaps);
    }
    public VQTexture newTexture(BufferedImage imgRGBA,BufferedImage imgESTuTv, String debugName, boolean uvWrapping, boolean generateMipMaps){
	return getUncompressedVQTextureFactory().newUncompressedVQTexture(imgRGBA,imgESTuTv,debugName,uvWrapping, generateMipMaps);
    }
    public SubTextureWindow getSubTextureWindow(){
	return subTextureWindow;
    }
    
    private Texture defaultTriPipeTexture;
    
    public Texture getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	    InputStream is=null;
	 try{
	  defaultTriPipeTexture = 
		  newTexture(ImageIO.read(is = LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),null,
	    			"Default TriPipe Texture (grayNoise)",true,true);}
	 catch(IOException e){throw new RuntimeException("Failure to load default tripipe texture.",e);}
	finally{if(is!=null)
	    try{is.close();}catch(Exception e){e.printStackTrace();}}
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
    
    public synchronized Texture getFallbackTexture(){
	if(fallbackTexture!=null)return fallbackTexture;
	VQTexture t;
	InputStream is=null;
	try{
	 t = newTexture(
		ImageIO.read(is = VQTexture.class
			.getResourceAsStream("/fallbackTexture.png")),null,
		"Fallback",true,true);}
	catch(IOException e){throw new RuntimeException("Failure to load fallback texture. Is everything right with the resources directory?",e);}
	finally{try{if(is!=null)is.close();}catch(Exception e){e.printStackTrace();}}
	fallbackTexture = t;
	return fallbackTexture;
    }//end getFallbackTexture()
    
    public synchronized Texture solidColor(Color color) {
	final int	key 	= color.hashCode();
	VQTexture		result 	= colorCache.get(key);
	if(result!=null)
	    return result;
	result = getSolidColorTextureFactory().newSolidColorTexture(color);
	colorCache.put(key,result);
	return result;
    }//end solidColor(...)
    
    public TextureTOCWindow getTOCWindow(){
	return tocWindow;
    }

    public SolidColorTextureFactory getSolidColorTextureFactory() {
	if(solidColorTextureFactory == null)
	    solidColorTextureFactory= new SolidColorTextureFactory(gpu, threadManager);//Temporary until they are fully separated
        return solidColorTextureFactory;
    }

    public void setSolidColorTextureFactory(
    	SolidColorTextureFactory solidColorTextureFactory) {
        this.solidColorTextureFactory = solidColorTextureFactory;
    }

    public UncompressedVQTextureFactory getUncompressedVQTextureFactory() {
	if(uncompressedVQTextureFactory == null)
	    uncompressedVQTextureFactory= new UncompressedVQTextureFactory(gpu, threadManager, "TextureManager");
        return uncompressedVQTextureFactory;
    }

    public void setUncompressedVQTextureFactory(
    	UncompressedVQTextureFactory uncompressedVQTextureFactory) {
        this.uncompressedVQTextureFactory = uncompressedVQTextureFactory;
    }
}//end TextureSystem
