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

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.GLFutureTask;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.dbg.StateBeanBridgeGL3;
import org.jtrfp.trcl.ext.tr.GPUResourceFinalizer;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.mem.MemoryManager;
import org.jtrfp.trcl.mem.MemoryWindow;
import org.jtrfp.trcl.tools.Util;

import com.jogamp.opengl.DebugGL3;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.awt.GLCanvas;

public class GPU implements GLExecutor<GL3>{//TODO: Can we remove GLExecutor? submitToGL returns GL Task whree executor returns TRFutureTask
    	public static final int 			GPU_VERTICES_PER_BLOCK = 96;
    	public static final int 			BYTES_PER_VEC4 = 16;
    	private TRFutureTask<Integer>			defaultTIU;
	
	private ByteOrder 				byteOrder;
	//private final TR 				tr;
	private GL3 					gl;
	public TRFutureTask<MemoryManager> 	        memoryManager;
	public TRFutureTask<TextureManager>        	textureManager;
	private GPUVendor				vendor=null;
	public TRFutureTask<RendererFactory>      	rendererFactory;
	private GLExecutor		         	glExecutor;
	private GLCanvas				canvas;
	public         TRFutureTask<MatrixWindow> 	matrixWindow;
	public    TRFutureTask<ObjectListWindow> 	objectListWindow;
	public      TRFutureTask<ObjectDefinitionWindow>objectDefinitionWindow;
	private       ThreadManager                     threadManager;
	private GPUResourceFinalizer                    gpuResourceFinalizer;
	private final ArrayList<TRFuture<? extends MemoryWindow>>
	                                                memoryWindows = new ArrayList<TRFuture<? extends MemoryWindow>>();
	private ExecutorService                         executorService;
	private World                                   world;
	private UncaughtExceptionHandler                uncaughtExceptionHandler;
	private boolean                                 initialized = false;
	private Reporter                                reporter;
	
	public GPU() {
	}//end constructor
	
	public void initialize(){
	    if(initialized)
		throw new IllegalStateException("Called initialize() but already initialized.");
	    Util.assertPropertiesNotNull(this, 
		    "executorService",
		    "glExecutor",
		    "threadManager",
		    "uncaughtExceptionHandler",
		    "canvas",
		    "world");
	    
	    final ExecutorService          executorService = getExecutorService();
	    final GLExecutor               glExecutor = getGlExecutor();
	    final ThreadManager            threadManager = getThreadManager();
	    final UncaughtExceptionHandler exceptionHandler = getUncaughtExceptionHandler();
	    final GLCanvas                 glCanvas = getCanvas();
	    final World                    world = getWorld();
	    
	    memoryManager  = new TRFutureTask<MemoryManager>(new Callable<MemoryManager>(){
		@Override
		public MemoryManager call() throws Exception {
		    return new MemoryManager(GPU.this, glExecutor);
		}
	    });
	    executorService.submit(memoryManager);
	    textureManager = new TRFutureTask<TextureManager>(new Callable<TextureManager>(){
		@Override//TODO: Set reporter in thread-agnostic manner
		public TextureManager call() throws Exception {
		    return new TextureManager(GPU.this, threadManager, exceptionHandler);
		}
	    });executorService.submit(textureManager);
	    defaultTIU = glExecutor.submitToGL(new Callable<Integer>(){
		@Override
		public Integer call() throws Exception {
		    return glGet(GL3.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS) - 1;
		}});
	    matrixWindow=new TRFutureTask<MatrixWindow>(new Callable<MatrixWindow>(){
		@Override
		public MatrixWindow call() throws Exception {
		    final MatrixWindow result = new MatrixWindow(GPU.this);
		    result.setReporter(getReporter());
		    return result;
		}//end call()
	    });threadManager.threadPool.submit(matrixWindow);
	    memoryWindows.add(matrixWindow);
	    objectListWindow=new TRFutureTask<ObjectListWindow>(new Callable<ObjectListWindow>(){
		@Override
		public ObjectListWindow call() throws Exception {
		    final ObjectListWindow result = new ObjectListWindow(GPU.this);
		    result.setReporter(getReporter());
		    return result;
		}//end call()
	    });threadManager.threadPool.submit(objectListWindow);
	    memoryWindows.add(objectListWindow);
	    objectDefinitionWindow=new TRFutureTask<ObjectDefinitionWindow>(new Callable<ObjectDefinitionWindow>(){
		@Override
		public ObjectDefinitionWindow call() throws Exception {
		    final ObjectDefinitionWindow result = new ObjectDefinitionWindow(GPU.this);
		    result.setReporter(getReporter());
		    return result;
		}//end call()
	    });threadManager.threadPool.submit(objectDefinitionWindow);
	    memoryWindows.add(objectDefinitionWindow);
	    rendererFactory = new TRFutureTask<RendererFactory>(new Callable<RendererFactory>(){
		@Override
		public RendererFactory call() throws Exception {
		    final RendererFactory rf = new RendererFactory(GPU.this, threadManager, glCanvas, world, objectListWindow.get()); 
		    rf.setReporter(getReporter());
		    return rf;
		}
	    });executorService.submit(rendererFactory);
	    
	    glExecutor.submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    System.out.println("GPU info:");
		    System.out.println("\tVendor: "+gl.glGetString(GL3.GL_VENDOR));
		    System.out.println("\tRenderer: "+gl.glGetString(GL3.GL_RENDERER));
		    System.out.println("\tVer: "+gl.glGetString(GL3.GL_VERSION));
		    System.out.println("\tGLSL: "+gl.glGetString(GL3.GL_SHADING_LANGUAGE_VERSION));
		    System.out.println("System info:");
		    System.out.println("\tVendor: "+System.getProperty("java.vendor"));
		    System.out.println("\tArch: "+System.getProperty("os.arch"));
		    System.out.println("\tOS: "+System.getProperty("os.name"));
		    System.out.println("\tVer:"+System.getProperty("os.version"));
		    return null;
		}});
	    gpuResourceFinalizer = new GPUResourceFinalizer(this);
	    initialized = true;
	}//end start()
	
	public void compactRootBuffer(){
	    for(TRFuture<? extends MemoryWindow> mw:memoryWindows)
		mw.get().compact();
	    memoryManager.get().compactRootBuffer();
	}
	
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
			try{for(int i=0; i<10; i++){
			    final GLCanvas canvas = getCanvas();
			    gl1=canvas.getGL();
			    if(gl1!=null){
				gl=gl1.getGL3();
				final String debug = System.getProperty("org.jtrfp.trcl.debugGL"); 
				if(debug!=null&&debug.toUpperCase().contentEquals("FALSE"))
				 {}//Do nothing
				else
				 canvas.setGL(gl=new StateBeanBridgeGL3(new DebugGL3(gl)));
				break;
				} Thread.sleep(2000);}}
			catch(InterruptedException e){e.printStackTrace();}
			}//end if(!null)
		return gl;
		}
	
	/*public TR getTr() {
	    	return tr;
		}*/
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
	    final GLCanvas canvas = this.canvas;
	    getGl().glViewport(0, 0, 
		    canvas.getWidth(), 
		    canvas.getHeight());
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

	@Override
	public <T> TRFutureTask<T> submitToGL(Callable<T> c) {
	    return glExecutor.submitToGL(c);
	}

	/**
	 * @return the threadManager
	 */
	public ThreadManager getThreadManager() {
	    return threadManager;
	}

	public GPUResourceFinalizer getGPUResourceFinalizer() {
	    return gpuResourceFinalizer;
	}

	public ExecutorService getExecutorService() {
	    if(executorService==null)
		executorService = Executors.newCachedThreadPool();
	    return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
	    this.executorService = executorService;
	}

	public GLExecutor getGlExecutor() {
	    return glExecutor;
	}

	public void setGlExecutor(GLExecutor glExecutor) {
	    this.glExecutor = glExecutor;
	}

	public GLCanvas getCanvas() {
	    return canvas;
	}

	public void setCanvas(GLCanvas canvas) {
	    this.canvas = canvas;
	}

	public World getWorld() {
	    return world;
	}

	public void setWorld(World world) {
	    this.world = world;
	}

	public UncaughtExceptionHandler getUncaughtExceptionHandler() {
	    return uncaughtExceptionHandler;
	}

	public void setUncaughtExceptionHandler(
		UncaughtExceptionHandler uncaughtExceptionHandler) {
	    this.uncaughtExceptionHandler = uncaughtExceptionHandler;
	}

	public void setThreadManager(ThreadManager threadManager) {
	    this.threadManager = threadManager;
	}

	@Override
	public <T> GLFutureTask<T> submitToGL(
		GLExecutable<T, GL3> executable) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void executeOnEachRefresh(
		GLExecutable<Void, GL3> executable, double orderPriority) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void executeOnResize(GLExecutable<Void,GL3> executable) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public void executeOnDispose(GLExecutable<Void,GL3> executable) {
	    throw new UnsupportedOperationException();
	}

	public Reporter getReporter() {
	    return reporter;
	}

	public void setReporter(Reporter reporter) {
	    this.reporter = reporter;
	}
	}//end GPU
