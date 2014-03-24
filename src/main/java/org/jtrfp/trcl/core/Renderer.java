package org.jtrfp.trcl.core;

import java.awt.Color;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import org.apache.commons.io.IOUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TriangleList;
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
    private 		RenderableSpacePartitioningGrid rootGrid;
    private final 	Camera			camera;
    private final 	GLProgram 		primaryProgram, deferredProgram;
    private 		boolean 		initialized = false;
    private		boolean 		active = false;// TODO: Remove when conversion is complete
    private 		boolean 		renderListToggle = false;
    private final 	GPU 			gpu;
    private final 	RenderList[] 		renderList = new RenderList[2];
    private final 	GLUniform	    	screenWidth, 
    /*    */	    				screenHeight,
    /*    */					fogColor,
    /*    */					sunVector;
    private final	GLTexture 		intermediateColorTexture,intermediateDepthTexture,intermediateNormTexture;
    private final	GLFrameBuffer 		intermediateFrameBuffer;
    private 		int			frameNumber;
    private 		long			lastTimeMillis;

    public Renderer(GPU gpu) {
	final TR tr = gpu.getTr();
	this.gpu = gpu;
	this.camera = new Camera(gpu);
	final GL3 gl = gpu.getGl();
	// Fixed pipeline behavior
	gl.glEnable(GL2.GL_DEPTH_TEST);
	gl.glDepthFunc(GL2.GL_LESS);
	gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
	gl.glClearColor(0f, 0f, 0f, 0f);

	// Generate shader program
	GLVertexShader vertexShader = gpu.newVertexShader();
	GLFragmentShader fragmentShader = gpu.newFragmentShader();
	primaryProgram = gpu.newProgram();
	try {// Apache Commons to the rescue again. (:
	    vertexShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/shader/vertexShader.glsl")));
	    fragmentShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/shader/fragShader.glsl")));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	primaryProgram.attachShader(vertexShader);
	primaryProgram.attachShader(fragmentShader);
	primaryProgram.link();
	if(!primaryProgram.validate()){
	    System.out.println("PRIMARY PROGRAM VALIDATION FAILED:");
	    System.out.println(primaryProgram.getInfoLog());
	}
	primaryProgram.use();
	primaryProgram.getUniform("texturePalette").set((int)0);
	
	//DEFERRED PROGRAM
	vertexShader = gpu.newVertexShader();
	fragmentShader = gpu.newFragmentShader();
	deferredProgram = gpu.newProgram();
	try {
	    vertexShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/shader/deferredVertexShader.glsl")));
	    fragmentShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/shader/deferredFragShader.glsl")));
	} catch (Exception e) {
	    e.printStackTrace();
	}
	deferredProgram.attachShader(vertexShader);
	deferredProgram.attachShader(fragmentShader);
	deferredProgram.link();
	if(!deferredProgram.validate()){
	    System.out.println("DEFERRED PROGRAM VALIDATION FAILED:");
	    System.out.println(deferredProgram.getInfoLog());
	}
	deferredProgram.use();
	screenWidth = deferredProgram.getUniform("screenWidth");
	screenHeight = deferredProgram.getUniform("screenHeight");
	fogColor = deferredProgram.getUniform("fogColor");
	sunVector = deferredProgram.getUniform("sunVector");
	deferredProgram.getUniform("texturePalette").set((int) 0);
	deferredProgram.getUniform("primaryRendering").set((int) 1);
	deferredProgram.getUniform("depthTexture").set((int) 2);
	deferredProgram.getUniform("normTexture").set((int) 3);
	sunVector.set(.5774f,.5774f,.5774f);
	intermediateColorTexture = gpu
		.newTexture()
		.bind()
		.setImage(GL3.GL_RG16, 1024, 768, GL3.GL_RGB,
			GL3.GL_FLOAT, null)
		.setMagFilter(GL3.GL_NEAREST)
		.setMinFilter(GL3.GL_NEAREST);
	intermediateNormTexture = gpu
		.newTexture()
		.bind()
		.setImage(GL3.GL_RGB8, 1024, 768, GL3.GL_RGB, GL3.GL_FLOAT, null)
		.setMagFilter(GL3.GL_NEAREST)
		.setMinFilter(GL3.GL_NEAREST);
	intermediateDepthTexture = gpu
		.newTexture()
		.bind()
		.setImage(GL3.GL_DEPTH_COMPONENT24, 1024, 768, 
			GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null)
		.setMagFilter(GL3.GL_NEAREST)
		.setMinFilter(GL3.GL_NEAREST)
		.setWrapS(GL3.GL_CLAMP_TO_EDGE)
		.setWrapT(GL3.GL_CLAMP_TO_EDGE);
	intermediateFrameBuffer = gpu
		.newFrameBuffer()
		.bindToDraw()
		.attachDrawTexture(intermediateColorTexture,
			GL3.GL_COLOR_ATTACHMENT0)
		.attachDrawTexture(intermediateNormTexture, 
			GL3.GL_COLOR_ATTACHMENT1)
		.attachDepthTexture(intermediateDepthTexture)
		.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0,GL3.GL_COLOR_ATTACHMENT1);
	if(gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
	    System.out.println("Framebuffer setup failure. OpenGL code "+gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
	    System.exit(1);
	}
	primaryProgram.use();
	
	gpu.addGLEventListener(new GLEventListener() {
	    @Override
	    public void init(GLAutoDrawable drawable) {
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
		tr.getRenderer().getDeferredProgram().use();
		intermediateColorTexture.bind().setImage(GL3.GL_RG16, width,
			height, GL3.GL_RGB, GL3.GL_FLOAT, null);
		intermediateDepthTexture.bind().setImage(GL3.GL_DEPTH_COMPONENT24, width, height, 
			GL3.GL_DEPTH_COMPONENT, GL3.GL_FLOAT, null);
		intermediateNormTexture.bind().setImage(GL3.GL_RGB8, width, height, GL3.GL_RGB, GL3.GL_FLOAT, null);
		screenWidth.setui(width);
		screenHeight.setui(height);
		tr.getRenderer().getPrimaryProgram().use();
	    }
	});
	
	System.out.println("Initializing RenderList...");
	renderList[0] = new RenderList(gl, primaryProgram,deferredProgram, intermediateFrameBuffer, 
		    intermediateColorTexture,intermediateDepthTexture, intermediateNormTexture, tr);
	renderList[1] = new RenderList(gl, primaryProgram,deferredProgram,intermediateFrameBuffer, 
		    intermediateColorTexture,intermediateDepthTexture, intermediateNormTexture, tr);
    }//end constructor

    private void ensureInit() {
	if (initialized)
	    return;
	final GL3 gl = gpu.getGl();

	gpu.getMemoryManager().map();

	System.out.println("Uploading vertex data to GPU...");
	TriangleList.uploadAllListsToGPU(gl);
	System.out.println("...Done.");
	System.out.println("Uploading object definition data to GPU...");
	WorldObject.uploadAllObjectDefinitionsToGPU();
	System.out.println("...Done.");
	System.out.println("\t...World.init() complete.");

	try {
	    gpu.getMemoryManager().bindToUniform(1, primaryProgram,
		    primaryProgram.getUniform("rootBuffer"));
	    //primaryProgram.getUniform("textureMap").set((int) 0);// Texture unit
								// 0 mapped to
								// textureMap
	} catch (RuntimeException e) {
	    e.printStackTrace();
	}
	GLTexture.specifyTextureUnit(gl, 0);
	Texture.getGlobalTexture().bind(gl);
	if (!primaryProgram.validate()) {
	    System.out.println(primaryProgram.getInfoLog());
	    System.exit(1);
	}
	System.out.println("...Done.");
	gpu.getMemoryManager().map();
	initialized = true;
    }// end ensureInit()

    private void fpsTracking() {
	frameNumber++;
	if ((frameNumber %= 20) == 0) {
	    final int dT = (int) (System.currentTimeMillis() - lastTimeMillis);
	    if(dT<=0)return;
	    final int fps = (1000 / dT);
	    gpu.getTr().getReporter()
		    .report("org.jtrfp.trcl.core.Renderer.FPS", "" + fps);
	}
	lastTimeMillis = System.currentTimeMillis();
    }

    public void render() {
	if (!active)
	    return;
	final GL3 gl = gpu.getGl();
	gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
	ensureInit();
	int renderListIndex = 0;
	renderListIndex = renderListToggle ? 0 : 1;
	renderList[renderListIndex].render(gl);
	fpsTracking();
	// Update GPU
	PrimitiveList.tickAnimators();
	setFogColor(gpu.getTr().getWorld().getFogColor());
	renderList[renderListIndex].sendToGPU(gl);
	//gpu.getMemoryManager().unmap();
    }

    public void activate() {
	active = true;
    }// TODO: Remove this when paged conversion is complete.
    
    public void temporarilyMakeImmediatelyVisible(PositionedRenderable pr){
	renderList[0].getSubmitter().submit(pr);
	renderList[1].getSubmitter().submit(pr);
    }
    
    public void updateVisibilityList() {
	renderListToggle = !renderListToggle;
	renderList[renderListToggle ? 0 : 1].reset();
	rootGrid.itemsWithinRadiusOf(
		camera.getCameraPosition().add(
			camera.getLookAtVector().scalarMultiply(
				getCamera().getViewDepth() / 2.1)),
		renderList[renderListToggle ? 0 : 1].getSubmitter());
    }// end updateVisibilityList()

    public void setFogColor(Color c) {
	deferredProgram.use();
	fogColor.set((float) c.getRed() / 255f, (float) c.getGreen() / 255f,
		(float) c.getBlue() / 255f);
	primaryProgram.use();
    }
    
    public void setSunVector(Vector3D sv){
	deferredProgram.use();
	sunVector.set((float)sv.getX(),(float)sv.getY(),(float)sv.getZ());
	primaryProgram.use();
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
    }

    /**
     * @return the primaryProgram
     */
    GLProgram getPrimaryProgram() {
        return primaryProgram;
    }

    /**
     * @return the deferredProgram
     */
    GLProgram getDeferredProgram() {
        return deferredProgram;
    }
}//end Renderer
