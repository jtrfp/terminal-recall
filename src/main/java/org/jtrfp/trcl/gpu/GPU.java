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
package org.jtrfp.trcl.gpu;

import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.media.opengl.DebugGL3;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureManager;
import org.jtrfp.trcl.dbg.StateBeanBridgeGL3;
import org.jtrfp.trcl.mem.MemoryManager;

public class GPU{
    	public static final int 			GPU_VERTICES_PER_BLOCK = 96;
    	public static final int 			BYTES_PER_VEC4 = 16;
    	private TRFutureTask<Integer>			defaultTIU;
	
	private ByteOrder 				byteOrder;
	private final TR 				tr;
	private GL3 					gl;
	public final TRFutureTask<MemoryManager> 	memoryManager;
	public final TRFutureTask<TextureManager> 	textureManager;
	private GPUVendor				vendor=null;
	
	public GPU(final TR tr){
	    this.tr=tr;
	    memoryManager = new TRFutureTask<MemoryManager>(tr,new Callable<MemoryManager>(){

		@Override
		public MemoryManager call() throws Exception {
		    return new MemoryManager(GPU.this);
		}
		
	    });
	    tr.getThreadManager().threadPool.submit(memoryManager);
	    textureManager = new TRFutureTask<TextureManager>(tr,new Callable<TextureManager>(){

		@Override
		public TextureManager call() throws Exception {
		    return new TextureManager(tr);
		}
		
	    });tr.getThreadManager().threadPool.submit(textureManager);
	    defaultTIU = tr.getThreadManager().submitToGL(new Callable<Integer>(){
		@Override
		public Integer call() throws Exception {
		    return glGet(GL3.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS) - 1;
		}});
	}//end constructor
	
	public int glGet(int key){
		IntBuffer buf = IntBuffer.wrap(new int[1]);
		getGl().glGetIntegerv(key, buf);
		return buf.get(0);
		}
	
	public String glGetString(int key)
		{return getGl().glGetString(key);}
	
	public ByteOrder getByteOrder(){
		if(byteOrder==null)
			{byteOrder = System.getProperty("sun.cpu.endian").contentEquals("little")?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN;}
		return byteOrder;
		}
	public GLTexture newTexture()
		{return new GLTexture(this);}
	public int newTextureID(){
		IntBuffer ib= IntBuffer.allocate(1);
		getGl().glGenTextures(1, ib);
		ib.clear();
		return ib.get();
		}
	public GLFragmentShader newFragmentShader()
		{return new GLFragmentShader(this);}
	public GLVertexShader newVertexShader()
		{return new GLVertexShader(this);}
	public GLProgram newProgram()
		{return new GLProgram(this);}
	public GL3 getGl(){
		if(gl==null)
			{GL gl1;
			//In case GL is not ready, wait and try again.
			final GLCanvas canvas = tr.getRootWindow().getCanvas();
			try{for(int i=0; i<10; i++){gl1=canvas.getGL();if(gl1!=null)
				{gl=gl1.getGL3();
				canvas.setGL(gl=new StateBeanBridgeGL3(new DebugGL3(gl)));
				break;
				} Thread.sleep(2000);}}
			catch(InterruptedException e){e.printStackTrace();}
			}//end if(!null)
		return gl;
		}
	
	public TR getTr() {
	    	return tr;
		}
	public GLFrameBuffer newFrameBuffer() {
	    return new GLFrameBuffer(gl);
	}
	public GLRenderBuffer newRenderBuffer() {
	    return new GLRenderBuffer(gl);
	}

	public void defaultProgram() {
	    getGl().glUseProgram(0);
	}

	public void defaultTIU() {
	    getGl().glActiveTexture(GL3.GL_TEXTURE0+defaultTIU.get());
	}

	public void defaultFrameBuffers() {
	    getGl().glBindFramebuffer(GL3.GL_DRAW_FRAMEBUFFER, 0);
	    getGl().glBindFramebuffer(GL3.GL_READ_FRAMEBUFFER, 0);
	}

	public void defaultTexture() {
	    getGl().glBindTexture(GL3.GL_TEXTURE_CUBE_MAP, 0);
	    getGl().glBindTexture(GL3.GL_TEXTURE_2D_MULTISAMPLE, 0);
	    getGl().glBindTexture(GL3.GL_TEXTURE_2D, 0);
	    getGl().glBindTexture(GL3.GL_TEXTURE_1D, 0);
	}

	public void defaultViewport() {
	    getGl().glViewport(0, 0, tr.getRootWindow().getCanvas().getWidth(), tr
			.getRootWindow().getCanvas().getHeight());
	}
	
	public GPUVendor getGPUVendor(){
	    if(vendor==null){
		final String vString = gl.glGetString(GL3.GL_VENDOR).toUpperCase();
		if(vString.contains("AMD")||
			vString.contains("ATI "))//The space is important because NVIDIA CORPORATION has 'ATI'
		    vendor=GPUVendor.AMD;
		else if(vString.contains("INTEL"))
		    vendor=GPUVendor.Intel;
		else if(vString.contains("NVIDIA"))
		    vendor=GPUVendor.nVIDIA;
		else vendor=GPUVendor.Unknown;
	    }//end if(null)
	    return vendor;
	}//end getGPUVendor()
	
	public enum GPUVendor{
	    Intel,
	    AMD,
	    nVIDIA,
	    Mali,
	    Unknown
	    }//end GPUVendor
	}//end GPU
