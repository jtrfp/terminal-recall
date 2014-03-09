package org.jtrfp.trcl.core;

import java.awt.Color;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import org.apache.commons.io.IOUtils;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TriangleList;
import org.jtrfp.trcl.gpu.GLFragmentShader;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GLVertexShader;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.obj.WorldObject;

public class Renderer {
    private final Camera camera;
    private final GLProgram primaryProgram, deferredProgram;
    private final GLUniform fogStart, fogEnd, fogColor;
    private boolean initialized = false;
    private final GPU gpu;
    private final RenderList[] renderList = new RenderList[2];
    private boolean renderListToggle = false;
    private RenderableSpacePartitioningGrid rootGrid;
    private boolean active = false;// TODO: Remove when conversion is complete

    private int frameNumber;
    private long lastTimeMillis;

    public Renderer(GPU gpu) {
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
		    .getResourceAsStream("/vertexShader.glsl")));
	    fragmentShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/fragShader.glsl")));
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
	
	fogStart = primaryProgram.getUniform("fogStart");
	fogEnd = primaryProgram.getUniform("fogEnd");
	fogColor = primaryProgram.getUniform("fogColor");

	//DEFERRED PROGRAM
	vertexShader = gpu.newVertexShader();
	fragmentShader = gpu.newFragmentShader();
	deferredProgram = gpu.newProgram();
	try {
	    vertexShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/deferredVertexShader.glsl")));
	    fragmentShader.setSource(IOUtils.toString(getClass()
		    .getResourceAsStream("/deferredFragShader.glsl")));
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
	deferredProgram.getUniform("primaryRendering").set((int) 1);
	primaryProgram.use();
	
	System.out.println("Initializing RenderList...");
	final TR tr = gpu.getTr();
	renderList[0] = new RenderList(gl, primaryProgram,deferredProgram, tr);
	renderList[1] = new RenderList(gl, primaryProgram,deferredProgram, tr);
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
	    primaryProgram.getUniform("textureMap").set((int) 0);// Texture unit
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
	    final int fps = (1000 / (int) (System.currentTimeMillis() - lastTimeMillis));
	    gpu.getTr().getReporter()
		    .report("org.jtrfp.trcl.core.Renderer.FPS", "" + fps);
	}
	lastTimeMillis = System.currentTimeMillis();
    }

    public void render() {
	if (!active)
	    return;
	fpsTracking();
	// Update GPU
	PrimitiveList.tickAnimators();
	ensureInit();
	final GL3 gl = gpu.getGl();
	setFogColor(gpu.getTr().getWorld().getFogColor());
	gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
	final double cameraViewDepth = camera.getViewDepth();
	fogStart.set((float) (cameraViewDepth * 1.2) / 5f);
	fogEnd.set((float) (cameraViewDepth * 1.5) * 1.3f);
	int renderListIndex = 0;
	renderListIndex = renderListToggle ? 0 : 1;
	renderList[renderListIndex].sendToGPU(gl);
	renderList[renderListIndex].render(gl);
    }

    public void activate() {
	active = true;
    }// TODO: Remove this when paged conversion is complete.

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
	fogColor.set((float) c.getRed() / 255f, (float) c.getGreen() / 255f,
		(float) c.getBlue() / 255f);
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
