/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionUnpacker;
import org.jtrfp.trcl.coll.CollectionThreadDecoupler;
import org.jtrfp.trcl.coll.ObjectTallyCollection;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.GLProgram.ValidationHandler;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.ObjectPool;
import org.jtrfp.trcl.pool.ObjectPool.GenerativeMethod;
import org.jtrfp.trcl.pool.ObjectPool.PreparationMethod;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.ochafik.util.listenable.Pair;

public class RendererFactory {
    public static final int			PRIMITIVE_BUFFER_WIDTH  = 512;
    public static final int			PRIMITIVE_BUFFER_HEIGHT = 512;
    public static final int			NUM_PORTALS = 4;
    public static final int			PRIMITIVE_BUFFER_OVERSAMPLING = 4;
    public static final int			OBJECT_BUFFER_WIDTH = 4*Renderer.NUM_BLOCKS_PER_PASS*Renderer.NUM_RENDER_PASSES;
    
    private final GPU gpu;
    private final World world;
    private Reporter reporter;
    private final ObjectTallyCollection<Positionable>allRelevant = new ObjectTallyCollection<Positionable>(new ArrayList<Positionable>());
    private final CollectionThreadDecoupler<Positionable> allRelevantDecoupled = new CollectionThreadDecoupler<Positionable>(allRelevant, Executors.newSingleThreadExecutor());
    //private final CollisionManager              collisionManager;
    private final	boolean			backfaceCulling;
    private 	 	GLUniform	    	sunVector;
    private 	 	GLUniform	    	fogScalar;
    private 		GLTexture 		opaqueDepthTexture,
    /*					*/	opaquePrimitiveIDTexture,
    /*					*/	primitiveUVZWTexture,primitiveNormLODTexture,
    /*					*/	layerAccumulatorTexture0,
    /*					*/	layerAccumulatorTexture1,
    /*					*/	portalTexture;
    private 		GLFrameBuffer 		opaqueFrameBuffer,
    /*					*/	depthQueueFrameBuffer,
    /*					*/	objectFrameBuffer,
    /*					*/	vertexFrameBuffer,
    /*					*/	primitiveFrameBuffer;
    private final	GLFrameBuffer[]		portalFrameBuffers = new GLFrameBuffer[NUM_PORTALS];
    
    private            GLProgram 		
    /*					*/	opaqueProgram, 
    /*					*/	deferredProgram, 
    /*					*/	depthQueueProgram, 
    /*                                  */      primitiveProgram,
    /*					*/	skyCubeProgram;
    private final ThreadManager			threadManager;
    private  ObjectProcessingStage              objectProcessingStage;
    private VertexProcessingStage               vertexProcessingStage;
    private final ObjectPool<Renderer>          rendererPool;
    private final IndexPool                     portalFrameBufferIndexPool = new IndexPool().setHardLimit(NUM_PORTALS);
    private final HashMap<Renderer,Integer>     rendererPortalIndexMap = new HashMap<Renderer,Integer>();
    private final ObjectListWindow              objectListWindow;
    private final GLExecutor			glExecutor;
    
    private class RendererPreparationMethod implements PreparationMethod<Renderer>{
	@Override
	public Renderer deactivate(final Renderer obj) {
	    final ThreadManager tm = threadManager;
	    getGlExecutor().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    obj.setEnabled(false);
		    obj.setRenderingTarget(null);
		    obj.getCamera().setRootGrid(null);
		    return null;
		}}).get();
	    return obj;
	}

	@Override
	public Renderer reactivate(final Renderer obj) {
	    final ThreadManager tm = threadManager;
	    getGlExecutor().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    obj.setEnabled(true);
		    return null;
		}}).get();
	    return obj;
	}
    }//end RendererPoolingMethod
    
    private class RendererGenerativeMethod implements GenerativeMethod<Renderer>{
	@Override
	public int getAtomicBlockSize() {
	    return 1;
	}

	@Override
	public Submitter<Renderer> generateConsecutive(int numBlocks,
		Submitter<Renderer> populationTarget) {
	    for(int i=0; i<numBlocks; i++){
		final Renderer renderer = RendererFactory.this.newRenderer("RendererFactory.rendererPool");
		renderer.getCamera().setRelevanceRadius(TRFactory.visibilityDiameterInMapSquares*TRFactory.mapSquareSize/4.);
		populationTarget.submit(renderer);}
	    return populationTarget;
	}
    }// end RendererGenerativeMethod
    
    public RendererFactory(final GPU gpu, final ThreadManager threadManager, 
	    final GLCanvas canvas, final World world, 
	    /*CollisionManager collisionManager, */ObjectListWindow objectListWindow){
	this.gpu=gpu;
	this.threadManager = threadManager;
	this.world=world;
	this.objectListWindow = objectListWindow;
	this.glExecutor = gpu.getGlExecutor();
	//this.collisionManager=collisionManager;
	if(world == null)
	    throw new NullPointerException("World intolerably null.");
	final GL3 gl = gpu.getGl();
	
	getGlExecutor().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		// Fixed pipeline behavior
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
		gl.glDepthRange(0, 1);
		gl.glEnable(GL3.GL_DEPTH_CLAMP);
		
		final ValidationHandler vh = new RFValidationHandler();
		objectProcessingStage      = new ObjectProcessingStage(gpu,vh);
		vertexProcessingStage      = new VertexProcessingStage(gpu,objectProcessingStage,vh);
		
		// VERTEX SHADERS
		GLVertexShader		
					traditionalVertexShader		= gpu.newVertexShader(),
					fullScreenQuadVertexShader	= gpu.newVertexShader(),
					primitiveVertexShader		= gpu.newVertexShader(),
					skyCubeVertexShader		= gpu.newVertexShader(),
					fullScreenTriangleShader	= gpu.newVertexShader();
		GLFragmentShader	
					opaqueFragShader		= gpu.newFragmentShader(),
					deferredFragShader		= gpu.newFragmentShader(),
					depthQueueFragShader		= gpu.newFragmentShader(),
					vertexFragShader		= gpu.newFragmentShader(),
					primitiveFragShader		= gpu.newFragmentShader(),
					skyCubeFragShader		= gpu.newFragmentShader();
		fullScreenTriangleShader  .setSourceFromResource("/shader/fullScreenTriangleVertexShader.glsl");
		traditionalVertexShader	  .setSourceFromResource("/shader/traditionalVertexShader.glsl");
		fullScreenQuadVertexShader.setSourceFromResource("/shader/fullScreenQuadVertexShader.glsl");
		opaqueFragShader	  .setSourceFromResource("/shader/opaqueFragShader.glsl");
		deferredFragShader	  .setSourceFromResource("/shader/deferredFragShader.glsl");
		depthQueueFragShader	  .setSourceFromResource("/shader/depthQueueFragShader.glsl");
		vertexFragShader	  .setSourceFromResource("/shader/vertexFragShader.glsl");
		primitiveFragShader	  .setSourceFromResource("/shader/primitiveFragShader.glsl");
		primitiveVertexShader	  .setSourceFromResource("/shader/primitiveVertexShader.glsl");
		skyCubeFragShader	  .setSourceFromResource("/shader/skyCubeFragShader.glsl");
		skyCubeVertexShader	  .setSourceFromResource("/shader/skyCubeVertexShader.glsl");
		
		
		opaqueProgram		=gpu.newProgram().setValidationHandler(vh).attachShader(traditionalVertexShader)	  .attachShader(opaqueFragShader).link();
		deferredProgram		=gpu.newProgram().setValidationHandler(vh).attachShader(skyCubeVertexShader)  	  .attachShader(deferredFragShader).link();
		depthQueueProgram	=gpu.newProgram().setValidationHandler(vh).attachShader(traditionalVertexShader)	  .attachShader(depthQueueFragShader).link();
		primitiveProgram	=gpu.newProgram().setValidationHandler(vh).attachShader(primitiveVertexShader)     .attachShader(primitiveFragShader).link();
		skyCubeProgram		=gpu.newProgram().setValidationHandler(vh).attachShader(skyCubeVertexShader)       .attachShader(skyCubeFragShader).link();
		
		skyCubeProgram.use();
		skyCubeProgram.getUniform("cubeTexture").set((int)0);
		
		opaqueProgram.use();
		opaqueProgram.getUniform("xyBuffer").set((int)1);
		/// 2 UNUSED
		/// 3 UNUSED
		opaqueProgram.getUniform("zBuffer").set((int)4);
		opaqueProgram.getUniform("wBuffer").set((int)5);
		/// 6 UNUSED
		/// 7 UNUSED
		
		primitiveProgram.use();
		primitiveProgram.getUniform("xyVBuffer").set((int)0);
		primitiveProgram.getUniform("wVBuffer").set((int)1);
		primitiveProgram.getUniform("zVBuffer").set((int)2);
		primitiveProgram.getUniform("uvVBuffer").set((int)3);
		primitiveProgram.getUniform("nXnYnZVBuffer").set((int)4);
		primitiveProgram.getUniform("nZVBuffer").set((int)5);
		
		depthQueueProgram.use();
		/// ... zero?
		/// 1 UNUSED
		depthQueueProgram.getUniform("xyBuffer").set((int)2);
		/// 3 UNUSED
		/// 4 UNUSED
		depthQueueProgram.getUniform("zBuffer").set((int)5);
		depthQueueProgram.getUniform("wBuffer").set((int)6);
		deferredProgram.use();
		sunVector 	= deferredProgram	.getUniform("sunVector");
		fogScalar	= deferredProgram	.getUniform("fogScalar");
		deferredProgram.getUniform("rootBuffer").set((int) 0);
		deferredProgram.getUniform("cubeTexture").set((int)1);
		deferredProgram.getUniform("portalTexture").set((int)2);
		deferredProgram.getUniform("ESTuTvTiles").set((int) 3);
		deferredProgram.getUniform("rgbaTiles").set((int) 4);
		deferredProgram.getUniform("primitiveIDTexture").set((int) 5);
		deferredProgram.getUniform("layerAccumulator0").set((int)6);
		deferredProgram.getUniform("vertexTextureIDTexture").set((int) 7);
		deferredProgram.getUniform("primitiveUVZWTexture").set((int) 8);
		deferredProgram.getUniform("primitivenXnYnZLTexture").set((int) 9);
		deferredProgram.getUniform("layerAccumulator1").set((int)10);
		deferredProgram.getUniform("ambientLight").set(.4f, .5f, .7f);
		sunVector.set(.5774f,-.5774f,.5774f);
		fogScalar.set(1f);
		final int width  = canvas.getWidth();
		final int height = canvas.getHeight();
		gpu.defaultProgram();
		gpu.defaultTIU();
		
		/////// PRIMITIVE
		primitiveNormLODTexture = gpu  //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F,
				PRIMITIVE_BUFFER_WIDTH * PRIMITIVE_BUFFER_OVERSAMPLING, 
				PRIMITIVE_BUFFER_HEIGHT * PRIMITIVE_BUFFER_OVERSAMPLING, 
				GL3.GL_RGBA,
				GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_LINEAR)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("primitiveNormTexture")
			.unbind();
		primitiveUVZWTexture = gpu  //Does not need to be in reshape() since it is off-screen.
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, 
				PRIMITIVE_BUFFER_WIDTH * PRIMITIVE_BUFFER_OVERSAMPLING, 
				PRIMITIVE_BUFFER_HEIGHT * PRIMITIVE_BUFFER_OVERSAMPLING, 
				GL3.GL_RGBA,
				GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_LINEAR)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("primitiveUVZWTexture");
		primitiveFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(primitiveUVZWTexture,
				GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(primitiveNormLODTexture,
				GL3.GL_COLOR_ATTACHMENT1)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Primitive framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// PORTALS
		allocatePortals(width,height);
		/////// INTERMEDIATE
		opaqueDepthTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_DEPTH_COMPONENT16, width, height, 
				GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setDebugName("opaqueDepthTexture");
		opaquePrimitiveIDTexture = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_R32F, width, height, 
				GL3.GL_RED, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setExpectedMaxValue(.1, .1, .1, .1)
			.setDebugName("opaquePrimitiveIDTexture")
			.unbind();
		opaqueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDepthTexture(opaqueDepthTexture)
			.attachDrawTexture(opaquePrimitiveIDTexture, 
				GL3.GL_COLOR_ATTACHMENT0)
			.attachDepthTexture(opaqueDepthTexture)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0)
			.unbindFromDraw();
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Intermediate framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		/////// LAYER ACCUMULATOR
		layerAccumulatorTexture0 = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, width, height, GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setExpectedMaxValue(65536, 65536, 65536, 65536)
			.setDebugName("layerAccumulatorTexture0")
			.unbind();
		layerAccumulatorTexture1 = gpu
			.newTexture()
			.bind()
			.setImage(GL3.GL_RGBA32F, width, height, GL3.GL_RGBA, GL3.GL_FLOAT, null)
			.setMagFilter(GL3.GL_NEAREST)
			.setMinFilter(GL3.GL_NEAREST)
			.setWrapS(GL3.GL_CLAMP_TO_EDGE)
			.setWrapT(GL3.GL_CLAMP_TO_EDGE)
			.setExpectedMaxValue(65536, 65536, 65536, 65536)
			.setDebugName("layerAccumulatorTexture1")
			.unbind();
		depthQueueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(layerAccumulatorTexture0, GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(layerAccumulatorTexture1, GL3.GL_COLOR_ATTACHMENT1)
			.attachDepthTexture(opaqueDepthTexture)
			/*
			.attachDrawTexture2D(depthQueueTexture, 
				GL3.GL_COLOR_ATTACHMENT0,GL3.GL_TEXTURE_2D_MULTISAMPLE)
			.attachDepthTexture2D(depthQueueStencil)
			.attachStencilTexture2D(depthQueueStencil)
			*/
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0, GL3.GL_COLOR_ATTACHMENT1);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Depth queue framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		gpu.defaultProgram();
		gpu.defaultTIU();
		gpu.defaultTexture();
		gpu.defaultFrameBuffers();
		return null;
	    }
	}).get();
	
	canvas.addGLEventListener(new GLEventListener() {
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
		// SHAPE-DEPENDENT UNIFORMS
		primitiveProgram.use();
		primitiveProgram.getUniform("screenWidth" ).set((float)width);//TODO: replace with vec2
		primitiveProgram.getUniform("screenHeight").set((float)height);
		deferredProgram.use();
		deferredProgram.getUniform("screenDims").set(width, height);
		gpu.defaultProgram();
		gpu.defaultFrameBuffers();
		gpu.defaultTIU();
		opaqueFrameBuffer    .bindToDraw().destroy();
		depthQueueFrameBuffer.bindToDraw().destroy();
		gpu.defaultFrameBuffers();
		
		opaqueDepthTexture.
		        bind().
		        setImage(GL3.GL_DEPTH_COMPONENT16, width, height,GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null).
			unbind();
		opaquePrimitiveIDTexture.
		    bind().
		    setImage(GL3.GL_R32F, width, height, GL3.GL_RED, GL3.GL_FLOAT, null).
		    unbind();
		opaqueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDepthTexture(opaqueDepthTexture)
			.attachDrawTexture(opaquePrimitiveIDTexture, 
				GL3.GL_COLOR_ATTACHMENT0)
			.attachDepthTexture(opaqueDepthTexture)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0)
			.unbindFromDraw();
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Intermediate framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		
		layerAccumulatorTexture0.
		 bind().
		 setImage(GL3.GL_RGBA32F, width, height, GL3.GL_RGBA, GL3.GL_FLOAT, null).
		 unbind();
		layerAccumulatorTexture1.
		 bind().
		 setImage(GL3.GL_RGBA32F, width, height, GL3.GL_RGBA, GL3.GL_FLOAT, null).
		 unbind();
		depthQueueFrameBuffer = gpu
			.newFrameBuffer()
			.bindToDraw()
			.attachDrawTexture(layerAccumulatorTexture0, GL3.GL_COLOR_ATTACHMENT0)
			.attachDrawTexture(layerAccumulatorTexture1, GL3.GL_COLOR_ATTACHMENT1)
			.attachDepthTexture(opaqueDepthTexture)
			.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
		if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Depth queue framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
		
		for(int i=0; i<NUM_PORTALS; i++){
		    portalFrameBuffers[i].
		    bindToDraw().
		    //attachDrawTexture(null, i, GL3.GL_COLOR_ATTACHMENT0).
		    setDrawBufferList()//Empty
		    //portalFrameBuffers[i].destroy();
		    .unbindFromDraw();}
		portalTexture.delete();
		portalTexture = null;
		gpu.defaultTexture();
		allocatePortals(width,height);
		gpu.defaultFrameBuffers();
		gpu.defaultTexture();
	    }//end reshape(...)
	});
	
	if(System.getProperties().containsKey("org.jtrfp.trcl.core.RenderList.backfaceCulling")){
	    backfaceCulling = System.getProperty("org.jtrfp.trcl.core.RenderList.backfaceCulling").toUpperCase().contains("TRUE");
	}else backfaceCulling = true;
	
	rendererPool = new ObjectPool<Renderer>(
                new ObjectPool.LazyAllocate<Renderer>().setMaxSize(NUM_PORTALS), new RendererPreparationMethod(), new RendererGenerativeMethod());
    }//end constructor
    
    private void allocatePortals(int width, int height){
	if(portalTexture == null)
	 portalTexture = gpu.
		newTexture().
		setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY);
	portalTexture.
		bind().
		setInternalColorFormat(GL3.GL_RGB565).
		configure(new int[]{width,height,NUM_PORTALS}, 1).
		setMagFilter(GL3.GL_NEAREST).
		setMinFilter(GL3.GL_NEAREST).
		setWrapS(GL3.GL_CLAMP_TO_EDGE).
		setWrapT(GL3.GL_CLAMP_TO_EDGE).
		unbind();
	for(int i=0; i<NUM_PORTALS; i++){
	    if(portalFrameBuffers[i]==null)
	      portalFrameBuffers[i]=gpu.
	      newFrameBuffer();
	    portalFrameBuffers[i].
	      bindToDraw().
	      attachDrawTexture(portalTexture, i, GL3.GL_COLOR_ATTACHMENT0).
	      setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0);
	    final GL3 gl = gpu.getGl();
	    if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
		    throw new RuntimeException("Portal framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
		}
	    }//end for(NUM_PORTALS)
	gpu.defaultFrameBuffers();
    }//end allocatePortals()
    
    public Renderer newRenderer(String debugName){
	//Renderer result = new Renderer(this,world,threadManager ,objectListWindow,debugName);
	Renderer result = new Renderer();
	result.setRendererFactory(this);
	result.setWorld(world);
	result.setThreadManager(threadManager);
	result.setObjectListWindow(objectListWindow);
	result.setDebugName(debugName);
	result.setGpu(getGPU());
	result.setReporter(getReporter());
	result.ensureInit();
	//Need a buffer because else the unpacker will cause redundant adds!
	final CollectionActionDispatcher<Positionable>
	 buffer = new CollectionActionDispatcher<Positionable>(new ArrayList<Positionable>());
	final CollectionActionUnpacker<Positionable>
         relevantUnpacker = new CollectionActionUnpacker<Positionable>(buffer);
	result.getCamera().getRelevanceCollections().addTarget(relevantUnpacker, true);
	buffer.addTarget(allRelevantDecoupled, true);
	return result;
    }
    
    public void addRelevanceTallyListener(Positionable pos, PropertyChangeListener l){
	allRelevant.addObjectTallyListener(pos, l);
    }
    
    public void removeRelevanceTallyListener(Positionable pos, PropertyChangeListener l){
	allRelevant.removeObjectTallyListener(pos, l);
    }

    /**
     * @return the gpu
     */
    public GPU getGPU() {
        return gpu;
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
    
    public GLTexture getOpaqueDepthTexture() {
        return opaqueDepthTexture;
    }
    
    public GLTexture getOpaquePrimitiveIDTexture() {
        return opaquePrimitiveIDTexture;
    }
    
    public GLFrameBuffer getVertexFrameBuffer() {
        return vertexFrameBuffer;
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
    public GLTexture getPrimitiveNormLODTexture() {
        return primitiveNormLODTexture;
    }

    /**
     * @return the primitiveFrameBuffer
     */
    public GLFrameBuffer getPrimitiveFrameBuffer() {
        return primitiveFrameBuffer;
    }

    public GLUniform getSunVectorUniform() {
	return sunVector;
    }
    
    public GLUniform getFogScalarUniform() {
	return fogScalar;
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
     * @return the depthQueueProgram
     */
    public GLProgram getDepthQueueProgram() {
        return depthQueueProgram;
    }


    /**
     * @return the primitiveProgram
     */
    public GLProgram getPrimitiveProgram() {
        return primitiveProgram;
    }
    
    public GLTexture getLayerAccumulatorTexture0() {
        return layerAccumulatorTexture0;
    }
    
    public GLTexture getLayerAccumulatorTexture1() {
        return layerAccumulatorTexture1;
    }

    /**
     * @return the skyCubeProgram
     */
    public GLProgram getSkyCubeProgram() {
        return skyCubeProgram;
    }
    
    private class RFValidationHandler implements ValidationHandler{
	@Override
	public void invalidProgram(GLProgram p) {
	    //Ignore.
	}
    }//end RFValidationHandler

    /**
     * @return the portalTexture
     */
    public GLTexture getPortalTexture() {
        return portalTexture;
    }

    public ObjectProcessingStage getObjectProcessingStage() {
	return objectProcessingStage;
    }

    public VertexProcessingStage getVertexProcessingStage() {
	return vertexProcessingStage;
    }

    /**
     * @return the portalFrameBuffers
     */
    public GLFrameBuffer[] getPortalFrameBuffers() {
        return portalFrameBuffers;
    }
    
    private void printPortalState(){
	System.out.println("Active portals: "+portalFrameBufferIndexPool.getNumUsedIndices());
	Set<Integer> used = new HashSet<Integer>();
	used.addAll(portalFrameBufferIndexPool.getUsedIndices());
	System.out.print("PORTAL STATUS:\n    ");
	for(int i=0; i<NUM_PORTALS; i++)
	    System.out.print(""+(used.contains(i)?"X":""+i));
	System.out.println();
    }//end printPortalState()

    public Renderer acquireRenderer() {
	return rendererPool.pop();
    }

    public void releaseRenderer(Renderer cameraToRelease) {
	rendererPool.expire(cameraToRelease);
    }
    
    public synchronized Pair<Renderer,Integer> acquirePortalRenderer() throws PortalNotAvailableException {
	final int index = portalFrameBufferIndexPool.pop();
	if(index == -1)
	    throw new PortalNotAvailableException();
	final Renderer result = acquireRenderer();
	assert !rendererPortalIndexMap.containsKey(result);
	rendererPortalIndexMap.put(result, index);
	result.setRenderingTarget(portalFrameBuffers[index]);
	printPortalState();
	return new Pair<Renderer,Integer>(result,index);
    }//end acquirePortalRenderer()
    
    public synchronized void releasePortalRenderer(Renderer rendererToRelease) {
	portalFrameBufferIndexPool.free(getPortalTextureIDOf(rendererToRelease));
	releaseRenderer(rendererToRelease);
	rendererPortalIndexMap.remove(rendererToRelease);
	printPortalState();
    }//end releasePortalRenderer(...)
    
    public synchronized int getPortalTextureIDOf(Renderer renderer){
	if(renderer == null)
	    throw new NullPointerException("Supplied renderer argument intolerably null.");
	final Integer result = rendererPortalIndexMap.get(renderer);
	if(result == null)
	    throw new IllegalArgumentException("Specified renderer "+renderer+" not found in rendererPortalIndexMap.");
	return result;
    }//end getPortalTextureIDOf(...)

    public synchronized int getRelevanceTallyOf(Positionable positionableToTally) {
	return allRelevant.getTallyOf(positionableToTally);
    }
    
    public static class PortalNotAvailableException extends IllegalStateException {
	public PortalNotAvailableException(){
	    super();
	}
    }

    public ObjectTallyCollection<Positionable> getAllRelevant() {
        return allRelevant;
    }

    public Reporter getReporter() {
        return reporter;
    }

    public void setReporter(Reporter reporter) {
        this.reporter = reporter;
    }

    public GLExecutor getGlExecutor() {
        return glExecutor;
    }
}//end RendererFactory
