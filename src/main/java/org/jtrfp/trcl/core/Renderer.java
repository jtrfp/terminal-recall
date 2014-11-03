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
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.GridCubeProximitySorter;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

public final class Renderer {
    public static final int			VERTEX_BUFFER_WIDTH     = 1024;
    public static final int			VERTEX_BUFFER_HEIGHT    = 4096;
    public static final int			PRIMITIVE_BUFFER_WIDTH  = 1024;
    public static final int			PRIMITIVE_BUFFER_HEIGHT = 4096;
    public static final int			DEPTH_QUEUE_SIZE = 8;
    public static final int			OBJECT_BUFFER_WIDTH = 4*RenderList.NUM_BLOCKS_PER_PASS*RenderList.NUM_RENDER_PASSES;
    private 		RenderableSpacePartitioningGrid rootGrid;
    private final	GridCubeProximitySorter proximitySorter = new GridCubeProximitySorter();
    private final 	Camera			camera;
    			GLProgram 		objectProgram,
    /*					*/	opaqueProgram, 
    /*					*/	deferredProgram, 
    /*					*/	depthQueueProgram, 
    /*					*/	depthErasureProgram,
    /*					*/	vertexProgram,
    /*                                  */      primitiveProgram;
    private 		boolean 		initialized = false;
    private volatile	AtomicBoolean 		renderListToggle = new AtomicBoolean(false);
    private final 	GPU 			gpu;
    public final 	TRFutureTask<RenderList>[]renderList = new TRFutureTask[2];
    private 	 	GLUniform	    	fogColor,
    /*    */					sunVector;
    private 		GLTexture 		opaqueUVTexture,
    /*					*/	opaqueDepthTexture,
    /*					*/	opaqueNormTexture,
    /*					*/	opaqueTextureIDTexture,
    /*					*/	depthQueueTexture,
    /*					*/	depthQueueStencil,
    /*					*/	camMatrixTexture,noCamMatrixTexture,
    /*					*/	vertexXYTexture,vertexUVTexture,vertexWTexture,vertexZTexture,vertexTextureIDTexture,
    /*					*/	vertexNormXYTexture,vertexNormZTexture,
    /*					*/	primitiveUVZWTexture,primitiveNormTexture;
    private 		GLFrameBuffer 		opaqueFrameBuffer,
    /*					*/	depthQueueFrameBuffer,
    /*					*/	objectFrameBuffer,
    /*					*/	vertexFrameBuffer,
    /*					*/	primitiveFrameBuffer;
    private 		int			frameNumber;
    private 		long			lastTimeMillis;
    private final	boolean			backfaceCulling;
    private		double			meanFPS;
    private		float[]			cameraMatrixAsFlatArray		= new float[16];
    private		TRFutureTask<Void>	visibilityUpdateFuture;

    public Renderer(final GPU gpu) {
	final TR tr = gpu.getTr();
	this.gpu = gpu;
	this.camera = new Camera(gpu);
	final GL3 gl = gpu.getGl();
	tr.getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		// Fixed pipeline behavior
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClearColor(0f, 0f, 0f, 0f);
		
		// VERTEX SHADERS
		GLVertexShader		objectVertexShader		= gpu.newVertexShader(),
					traditionalVertexShader		= gpu.newVertexShader(),
					fullScreenQuadVertexShader	= gpu.newVertexShader(),
					primitiveVertexShader		= gpu.newVertexShader();
		GLFragmentShader	objectFragShader		= gpu.newFragmentShader(),
					opaqueFragShader		= gpu.newFragmentShader(),
					deferredFragShader		= gpu.newFragmentShader(),
					depthQueueFragShader		= gpu.newFragmentShader(),
					erasureFragShader		= gpu.newFragmentShader(),
					vertexFragShader		= gpu.newFragmentShader(),
					primitiveFragShader		= gpu.newFragmentShader();
		objectVertexShader	  .setSourceFromResource("/shader/objectVertexShader.glsl");
		objectFragShader	  .setSourceFromResource("/shader/objectFragShader.glsl");
		traditionalVertexShader	  .setSourceFromResource("/shader/traditionalVertexShader.glsl");
		fullScreenQuadVertexShader.setSourceFromResource("/shader/fullScreenQuadVertexShader.glsl");
		opaqueFragShader	  .setSourceFromResource("/shader/opaqueFragShader.glsl");
		deferredFragShader	  .setSourceFromResource("/shader/deferredFragShader.glsl");
		erasureFragShader	  .setSourceFromResource("/shader/erasureFragShader.glsl");
		depthQueueFragShader	  .setSourceFromResource("/shader/depthQueueFragShader.glsl");
		vertexFragShader	  .setSourceFromResource("/shader/vertexFragShader.glsl");
		primitiveFragShader	  .setSourceFromResource("/shader/primitiveFragShader.glsl");
		primitiveVertexShader	  .setSourceFromResource("/shader/primitiveVertexShader.glsl");
		
		objectProgram		=gpu.newProgram().attachShader(objectFragShader)	  .attachShader(objectVertexShader).link();
		vertexProgram		=gpu.newProgram().attachShader(fullScreenQuadVertexShader).attachShader(vertexFragShader).link();
		opaqueProgram		=gpu.newProgram().attachShader(traditionalVertexShader)	  .attachShader(opaqueFragShader).link();
		deferredProgram		=gpu.newProgram().attachShader(fullScreenQuadVertexShader).attachShader(deferredFragShader).link();
		depthQueueProgram	=gpu.newProgram().attachShader(traditionalVertexShader)	  .attachShader(depthQueueFragShader).link();
		depthErasureProgram	=gpu.newProgram().attachShader(fullScreenQuadVertexShader).attachShader(erasureFragShader).link();
		primitiveProgram	=gpu.newProgram().attachShader(primitiveVertexShader)     .attachShader(primitiveFragShader).link();
		
		vertexProgram.use();
		vertexProgram.getUniform("rootBuffer").set((int)0);
		vertexProgram.getUniform("camMatrixBuffer").set((int)1);
		vertexProgram.getUniform("noCamMatrixBuffer").set((int)2);
		
		opaqueProgram.use();
		opaqueProgram.getUniform("xyBuffer").set((int)1);
		opaqueProgram.getUniform("uvBuffer").set((int)2);
		opaqueProgram.getUniform("texIDBuffer").set((int)3);
		opaqueProgram.getUniform("zBuffer").set((int)4);
		opaqueProgram.getUniform("wBuffer").set((int)5);
		opaqueProgram.getUniform("normXYBuffer").set((int)6);
		opaqueProgram.getUniform("normZBuffer").set((int)7);
		
		objectProgram.use();
		objectProgram.getUniform("rootBuffer").set((int)0);
		
		primitiveProgram.use();
		primitiveProgram.getUniform("xyVBuffer").set((int)0);
		primitiveProgram.getUniform("wVBuffer").set((int)1);
		primitiveProgram.getUniform("zVBuffer").set((int)2);
		primitiveProgram.getUniform("uvVBuffer").set((int)3);
		primitiveProgram.getUniform("nXnYnZVBuffer").set((int)4);
		
		depthQueueProgram.use();
		depthQueueProgram.getUniform("depthTexture").set((int)1);
		depthQueueProgram.getUniform("xyBuffer").set((int)2);
		depthQueueProgram.getUniform("uvBuffer").set((int)3);
		depthQueueProgram.getUniform("texIDBuffer").set((int)4);
		depthQueueProgram.getUniform("zBuffer").set((int)5);
		depthQueueProgram.getUniform("wBuffer").set((int)6);
		deferredProgram.use();
		fogColor 	= deferredProgram	.getUniform("fogColor");
		sunVector 	= deferredProgram	.getUniform("sunVector");
		deferredProgram.getUniform("rootBuffer").set((int) 0);
		deferredProgram.getUniform("primaryRendering").set((int) 1);
		deferredProgram.getUniform("depthTexture").set((int) 2);
		deferredProgram.getUniform("normTexture").set((int) 3);
		deferredProgram.getUniform("rgbaTiles").set((int) 4);
		deferredProgram.getUniform("textureIDTexture").set((int) 5);
		deferredProgram.getUniform("depthQueueTexture").set((int) 6);
		sunVector.set(.5774f,.5774f,.5774f);
		final int width = tr.getRootWindow().getWidth();
		final int height = tr.getRootWindow().getHeight();
		/////// OBJECT
		camMatrixTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, 1024, 128, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		noCamMatrixTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, 1024, 128, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		objectFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(camMatrixTexture, GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(noCamMatrixTexture, GL3.GL_COLOR_ATTACHMENT1)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Object frame buffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// VERTEX
		vertexXYTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexNormXYTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG16F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexNormZTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG16F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexUVTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexZTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexWTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()//// This is actually W-reciprocal.
			.bind()
			.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexTextureIDTexture = gpu //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_R32F, VERTEX_BUFFER_WIDTH, VERTEX_BUFFER_HEIGHT, 
				GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMinFilter(GL3.GL_NEAREST)
			.setMagFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		vertexFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(vertexXYTexture, GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(vertexUVTexture, GL3.GL_COLOR_ATTACHMENT1)
			.attachDrawTexture(vertexZTexture, GL3.GL_COLOR_ATTACHMENT2)
			.attachDrawTexture(vertexWTexture, GL3.GL_COLOR_ATTACHMENT3)
			.attachDrawTexture(vertexTextureIDTexture, GL3.GL_COLOR_ATTACHMENT4)
			.attachDrawTexture(vertexNormXYTexture, GL3.GL_COLOR_ATTACHMENT5)
			.attachDrawTexture(vertexNormZTexture, GL3.GL_COLOR_ATTACHMENT6)
			.setDrawBufferList(
				GL3.GL_COLOR_ATTACHMENT0,
				GL3.GL_COLOR_ATTACHMENT1,
				GL3.GL_COLOR_ATTACHMENT2,
				GL3.GL_COLOR_ATTACHMENT3,
				GL3.GL_COLOR_ATTACHMENT4,
				GL3.GL_COLOR_ATTACHMENT5,
				GL3.GL_COLOR_ATTACHMENT6);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Vertex frame buffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// PRIMITIVE
		primitiveNormTexture = gpu  //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG32F, PRIMITIVE_BUFFER_WIDTH, PRIMITIVE_BUFFER_HEIGHT, GL3.GL_RGBA,
				GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST);
		primitiveUVZWTexture = gpu  //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, PRIMITIVE_BUFFER_WIDTH, PRIMITIVE_BUFFER_HEIGHT, GL3.GL_RGBA,
				GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST);
		primitiveFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(primitiveUVZWTexture,
				GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(primitiveNormTexture,
				GL3.GL_COLOR_ATTACHMENT1)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Primitive framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// INTERMEDIATE
		opaqueUVTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RG16, width, height, GL3.GL_RGB,
				GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST);
		opaqueNormTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGB8, width, height, GL3.GL_RGB, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST);
		opaqueDepthTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_DEPTH_COMPONENT24, width, height, 
				GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		opaqueTextureIDTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_R32F, width, height, 
				GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE);
		opaqueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(opaqueUVTexture,
				GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(opaqueNormTexture, 
				GL3.GL_COLOR_ATTACHMENT1)
			.attachDrawTexture(opaqueTextureIDTexture, 
				GL3.GL_COLOR_ATTACHMENT2)
			.attachDepthTexture(opaqueDepthTexture)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1,GL3.GL_COLOR_ATTACHMENT2);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Intermediate framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// DEPTH QUEUE
		depthQueueTexture = gpu
			.newTexture()
			.setBindingTarget(GL3.GL_TEXTURE_2D_MULTISAMPLE)
			.bind()
			.setImage2DMultisample(DEPTH_QUEUE_SIZE, GL3.GL_RGBA32F,width,height,false);
		depthQueueStencil = gpu
			.newTexture()
			.setBindingTarget(GL3.GL_TEXTURE_2D_MULTISAMPLE)
			.bind()
			.setImage2DMultisample(DEPTH_QUEUE_SIZE, GL3.GL_DEPTH24_STENCIL8,width,height,false);
		depthQueueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture2D(depthQueueTexture, 
				GL3.GL_COLOR_ATTACHMENT0,GL3.GL_TEXTURE_2D_MULTISAMPLE)
			.attachDepthTexture2D(depthQueueStencil)
			.attachStencilTexture2D(depthQueueStencil)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Depth queue framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		opaqueProgram.use();
		return null;
	    }
	}).get();
	
	System.out.println("Renderer adding GLEventListener");
	tr.getRootWindow().getCanvas().addGLEventListener(new GLEventListener() {
	    @Override
	    public void init(GLAutoDrawable drawable) {
		drawable.getGL().setSwapInterval(0);
	    }

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
	    }

	    @Override
	    public void display(GLAutoDrawable drawable) {
	    }

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
		Renderer.this.getDeferredProgram().use();
		opaqueUVTexture.bind().setImage(GL3.GL_RG16, width,
			height, GL3.GL_RGB, GL3.GL_FLOAT, null);
		opaqueDepthTexture.bind().setImage(GL3.GL_DEPTH_COMPONENT24, width, height, 
			GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null);
		opaqueNormTexture.bind().setImage(GL3.GL_RGB8, width, height, GL3.GL_RGB, GL3.GL_FLOAT, null);
		opaqueTextureIDTexture.bind().setImage(GL3.GL_R32F, width, height, GL3.GL_RED, GL3.GL_FLOAT, null);
		depthQueueStencil.bind().setImage2DMultisample(DEPTH_QUEUE_SIZE, GL3.GL_DEPTH24_STENCIL8,width,height,false);
		depthQueueTexture.bind().setImage2DMultisample(DEPTH_QUEUE_SIZE, GL3.GL_RGBA32F,width,height,false);// Doesn't like RGBA32UI for some reason.
		depthQueueProgram.use();
		Renderer.this.getOpaqueProgram().use();
	    }
	});
	System.out.println("...Done.");
	System.out.println("Initializing RenderList...");
	renderList[0] = new TRFutureTask<RenderList>(tr,new Callable<RenderList>(){
	    @Override
	    public RenderList call() throws Exception {
		return new RenderList(gl, Renderer.this, tr);
	    }});tr.getThreadManager().threadPool.submit(renderList[0]);
	    renderList[1] = new TRFutureTask<RenderList>(tr,new Callable<RenderList>(){
		    @Override
		    public RenderList call() throws Exception {
			return new RenderList(gl,Renderer.this, tr);
		    }});tr.getThreadManager().threadPool.submit(renderList[1]);
	if(System.getProperties().containsKey("org.jtrfp.trcl.core.RenderList.backfaceCulling")){
	    backfaceCulling = System.getProperty("org.jtrfp.trcl.core.RenderList.backfaceCulling").toUpperCase().contains("TRUE");
	}else backfaceCulling = true;
    }//end constructor

    private void ensureInit() {
	if (initialized)
	    return;
	gpu.memoryManager.get().map();
	initialized = true;
    }// end ensureInit()

    private void fpsTracking() {
	frameNumber++;
	final long dT = (long) (System.currentTimeMillis() - lastTimeMillis);
	if(dT<=0)return;
	final int fps = (int)(1000L / dT);
	meanFPS = meanFPS*.9+(double)fps*.1;
	if ((frameNumber %= 20) == 0) {
	    gpu.getTr().getReporter()
		    .report("org.jtrfp.trcl.core.Renderer.FPS", "" + meanFPS);
	    final List<WorldObject> list = renderList[renderListToggle.get() ? 0 : 1].get().getVisibleWorldObjectList();
	    synchronized(list){
	    gpu.getTr().getReporter()
	    	.report("org.jtrfp.trcl.core.Renderer.numVisibleObjects", list.size());}
	}
	lastTimeMillis = System.currentTimeMillis();
    }//end fpsTracking()
    
    public void render() {
	final GL3 gl = gpu.getGl();
	try{
	    gpu.textureManager.getRealtime().vqCodebookManager.getRealtime().refreshStaleCodePages();
		ensureInit();
		//Make sure memory on the GPU is sync'ed by flushing stale pages to GPU mem.
		//gpu.memoryManager.get().flushStalePages();
		final RenderList renderList = currentRenderList().getRealtime();
		deferredProgram.use();
		renderList.render(gl,cameraMatrixAsFlatArray);
		// Update GPU
		cameraMatrixAsFlatArray = renderList.sendToGPU(gl);
		fpsTracking();
		final World world = gpu.getTr().getWorld();
		if(world==null)return;
		setFogColor(world.getFogColor());
	}catch(NotReadyException e){}
    }//end render()
    
    public void temporarilyMakeImmediatelyVisible(final PositionedRenderable pr){
	if(pr instanceof WorldObject)
	    gpu.getTr().getCollisionManager().getCurrentlyActiveCollisionList().add((WorldObject)pr);
	
	gpu.getTr().getThreadManager().submitToGPUMemAccess(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		final Submitter<PositionedRenderable> s = Renderer.this.currentRenderList().get().getSubmitter();
		synchronized(s){
		s.submit(pr);
		return null;}
	      }
	});
    }//end temporarilyMakeImmediatelyVisible(...)
    
    public void updateVisibilityList(boolean mandatory) {
	if(visibilityUpdateFuture!=null){
	    if(!visibilityUpdateFuture.isDone()){
		if(!mandatory){System.out.println("Renderer.updateVisibilityList() !done");return;}
		else {}
		}
	    visibilityUpdateFuture.get();
	    }//end if(visibilityUpdateFuture!=null)
	if(!getBackRenderList().isDone())return;//Not ready.
	visibilityUpdateFuture = gpu.getTr().getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() {
		try{
		proximitySorter.setCenter(camera.getCameraPosition().toArray());
		rootGrid.cubesWithinRadiusOf(
			camera.getCameraPosition().add(
				camera.getLookAtVector().scalarMultiply(
					getCamera().getViewDepth() / 2.1)),
					proximitySorter
			);
		Renderer.this.gpu.getTr().getThreadManager().submitToGPUMemAccess(new Callable<Void>(){
		    @Override
		    public Void call() {
			final RenderList rl = getBackRenderList().get();
			rl.reset();
			final Submitter<PositionedRenderable> s = rl.getSubmitter();
			synchronized(s){
			 proximitySorter.dumpPositionedRenderables(s);}
			toggleRenderList();
			return null;
		    }//end gl call()
		}).get();
		proximitySorter.reset();
		}catch(Exception e){e.printStackTrace();}
		return null;
	    }//end pool run()
	});
    }// end updateVisibilityList()
    
    public TRFutureTask<RenderList> currentRenderList(){
	return renderList[renderListToggle.get() ? 0 : 1];
    }
    public TRFutureTask<RenderList> getBackRenderList(){
	return renderList[renderListToggle.get() ? 1 : 0];
    }
    private synchronized void toggleRenderList(){
	renderListToggle.set(!renderListToggle.get());
    }

    public void setFogColor(Color c) {
	deferredProgram.use();
	fogColor.set((float) c.getRed() / 255f, (float) c.getGreen() / 255f,
		(float) c.getBlue() / 255f);
	opaqueProgram.use();
    }
    
    public void setSunVector(Vector3D sv){
	deferredProgram.use();
	sunVector.set((float)sv.getX(),(float)sv.getY(),(float)sv.getZ());
	opaqueProgram.use();
    }

    /**
     * @return the camera
     */
    public Camera getCamera() {
	return camera;
    }

    /**
     * @return the rootGrid
     */
    public RenderableSpacePartitioningGrid getRootGrid() {
	return rootGrid;
    }

    /**
     * @param rootGrid
     *            the rootGrid to set
     */
    public void setRootGrid(RenderableSpacePartitioningGrid rootGrid) {
	this.rootGrid = rootGrid;
	if(camera.getContainingGrid()!=null)
	    camera.getContainingGrid().remove(camera);
	rootGrid.add(camera);
    }

    /**
     * @return the primaryProgram
     */
    GLProgram getOpaqueProgram() {
        return opaqueProgram;
    }

    /**
     * @return the deferredProgram
     */
    GLProgram getDeferredProgram() {
        return deferredProgram;
    }

    /**
     * @return the backfaceCulling
     */
    protected boolean isBackfaceCulling() {
        return backfaceCulling;
    }

    /**
     * @return the objectProgram
     */
    public GLProgram getObjectProgram() {
        return objectProgram;
    }

    /**
     * @return the depthQueueProgram
     */
    public GLProgram getDepthQueueProgram() {
        return depthQueueProgram;
    }

    /**
     * @return the depthErasureProgram
     */
    public GLProgram getDepthErasureProgram() {
        return depthErasureProgram;
    }

    /**
     * @return the depthQueueTexture
     */
    public GLTexture getDepthQueueTexture() {
        return depthQueueTexture;
    }

    /**
     * @return the depthQueueStencil
     */
    public GLTexture getDepthQueueStencil() {
        return depthQueueStencil;
    }

    /**
     * @return the objectTexture
     */
    public GLTexture getCamMatrixTexture() {
        return camMatrixTexture;
    }

    /**
     * @return the depthQueueFrameBuffer
     */
    public GLFrameBuffer getDepthQueueFrameBuffer() {
        return depthQueueFrameBuffer;
    }

    /**
     * @return the objectFrameBuffer
     */
    public GLFrameBuffer getObjectFrameBuffer() {
        return objectFrameBuffer;
    }
    
    public GLFrameBuffer getOpaqueFrameBuffer() {
        return opaqueFrameBuffer;
    }
    
    public GLTexture getOpaqueUVTexture() {
        return opaqueUVTexture;
    }
    
    public GLTexture getOpaqueDepthTexture() {
        return opaqueDepthTexture;
    }
    
    public GLTexture getOpaqueNormTexture() {
        return opaqueNormTexture;
    }
    
    public GLTexture getOpaqueTextureIDTexture() {
        return opaqueTextureIDTexture;
    }
    
    public GLProgram getVertexProgram() {
        return vertexProgram;
    }
    
    public GLTexture getVertexXYTexture() {
        return vertexXYTexture;
    }
    
    public GLTexture getVertexUVTexture() {
        return vertexUVTexture;
    }
    
    public GLTexture getVertexWTexture() {
        return vertexWTexture;
    }
    
    public GLTexture getVertexZTexture() {
        return vertexZTexture;
    }
    
    public GLTexture getTextureIDTexture() {
        return vertexTextureIDTexture;
    }
    
    public GLFrameBuffer getVertexFrameBuffer() {
        return vertexFrameBuffer;
    }

    public GLTexture getVertexNormXYTexture() {
        return vertexNormXYTexture;
    }
    
    public GLTexture getVertexNormZTexture() {
        return vertexNormZTexture;
    }
    
    public GLTexture getNoCamMatrixTexture() {
        return noCamMatrixTexture;
    }

    /**
     * @return the primitiveUVZWTexture
     */
    public GLTexture getPrimitiveUVZWTexture() {
        return primitiveUVZWTexture;
    }

    /**
     * @return the primitiveNormTexture
     */
    public GLTexture getPrimitiveNormTexture() {
        return primitiveNormTexture;
    }

    /**
     * @return the primitiveFrameBuffer
     */
    public GLFrameBuffer getPrimitiveFrameBuffer() {
        return primitiveFrameBuffer;
    }
}//end Renderer
