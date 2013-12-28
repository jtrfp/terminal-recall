package org.jtrfp.trcl.core;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import org.apache.commons.io.IOUtils;
import org.jtrfp.jfdt.Parser;
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
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.obj.WorldObject;

import com.jogamp.common.util.IOUtil;

public class Renderer
	{
	private final Camera camera;
	private final GLProgram shaderProgram;
	private final GLUniform fogStart,fogEnd,fogColor;
	private boolean initialized=false;
	private final GPU gpu;
	private final RenderList [] renderList = new RenderList[2];
	private boolean renderListToggle=false;
	private RenderableSpacePartitioningGrid rootGrid;
	
	private int frameNumber;
	private long lastTimeMillis;
	
	public Renderer(GPU gpu)
		{this.gpu=gpu;this.camera=new Camera(gpu);
		final GL3 gl = gpu.getGl();
		//Fixed pipeline behavior
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LESS);
		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		gl.glClearColor(0f, 0f, 0f, 0f);
		
		//Generate shader program
		GLVertexShader vertexShader = gpu.newVertexShader();
		GLFragmentShader fragmentShader = gpu.newFragmentShader();
		shaderProgram = gpu.newProgram();
		try{//Apache Commons to the rescue again. (:
		    vertexShader.setSource(IOUtils.toString(getClass().getResourceAsStream("/vertexShader.glsl")));
		    fragmentShader.setSource(IOUtils.toString(getClass().getResourceAsStream("/fragShader.glsl")));
		}
		catch(Exception e){e.printStackTrace();}
		shaderProgram.attachShader(vertexShader);
		shaderProgram.attachShader(fragmentShader);
		shaderProgram.link();
		shaderProgram.use();
		
		fogStart=shaderProgram.getUniform("fogStart");
		fogEnd=shaderProgram.getUniform("fogEnd");
		fogColor=shaderProgram.getUniform("fogColor");
		
		System.out.println("Initializing RenderList...");
		renderList[0] = new RenderList(gl, shaderProgram,gpu.getTr());
		renderList[1] = new RenderList(gl, shaderProgram,gpu.getTr());
		}
	
	private void ensureInit()
		{if(initialized) return;
		final GL3 gl = gpu.getGl();
		
		GlobalDynamicTextureBuffer.getTextureBuffer().map();
		System.out.println("Uploading vertex data to GPU...");
		TriangleList.uploadAllListsToGPU(gl);
		System.out.println("...Done.");
		System.out.println("Uploading object defintion data to GPU...");
		WorldObject.uploadAllObjectDefinitionsToGPU();
		System.out.println("...Done.");
		System.out.println("\t...World.init() complete.");
		GlobalDynamicTextureBuffer.getTextureBuffer().unmap();
		
		try{
		GlobalDynamicTextureBuffer.getTextureBuffer().bindToUniform(1, shaderProgram, shaderProgram.getUniform("rootBuffer"));
		shaderProgram.getUniform("textureMap").set((int)0);//Texture unit 0 mapped to textureMap
		}
	catch (RuntimeException e)
		{e.printStackTrace();}
	GLTexture.specifyTextureUnit(gl, 0);
	Texture.getGlobalTexture().bind(gl);
	if(!shaderProgram.validate())
		{
		System.out.println(shaderProgram.getInfoLog());
		System.exit(1);
		}
	System.out.println("...Done.");
		initialized=true;
		}//end ensureInit()
	
	private void fpsTracking()
		{frameNumber++;
		if ((frameNumber %= 20) == 0){
		    	final int fps = (1000 / (int) (System.currentTimeMillis() - lastTimeMillis));
		    	gpu.getTr().getReporter().report("org.jtrfp.trcl.core.Renderer.FPS", ""+fps);
			}
		lastTimeMillis = System.currentTimeMillis();
		}
	
	public void render()
		{fpsTracking();
		// Update GPU
		GlobalDynamicTextureBuffer.getTextureBuffer().map();
		PrimitiveList.tickAnimators();
		ensureInit();
		final GL3 gl = gpu.getGl();
		setFogColor(gpu.getTr().getWorld().getFogColor());
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT);
		final double cameraViewDepth = camera.getViewDepth();
		fogStart.set((float) (cameraViewDepth * 1.2) / 5f);
		fogEnd.set((float) (cameraViewDepth * 1.5) * 1.3f);
		int renderListIndex=0;
		synchronized(ThreadManager.GAME_OBJECT_MODIFICATION_LOCK)
			{renderListIndex=renderListToggle?0:1;renderList[renderListIndex].sendToGPU(gl);}
		GlobalDynamicTextureBuffer.getTextureBuffer().unmap();
		// Render objects
		renderList[renderListIndex].render(gl);
		}
	
	public void updateVisibilityList()
		{synchronized(ThreadManager.GAME_OBJECT_MODIFICATION_LOCK){
			renderListToggle=!renderListToggle;
			renderList[renderListToggle?0:1].reset();
			rootGrid.itemsWithinRadiusOf(
				camera.getCameraPosition().add(
					camera.getLookAtVector().scalarMultiply(getCamera().getViewDepth() / 2.1)),
					renderList[renderListToggle?0:1].getSubmitter());
			}//end sync()
		}//end updateVisibilityList()
	
	public void setFogColor(Color c)
		{
	    	fogColor.set(
			(float) c.getRed() / 255f, 
			(float) c.getGreen() / 255f, 
			(float)c.getBlue() / 255f);
		}
	/**
	 * @return the camera
	 */
	public Camera getCamera()
		{return camera;}

	/**
	 * @return the rootGrid
	 */
	public RenderableSpacePartitioningGrid getRootGrid()
		{return rootGrid;}

	/**
	 * @param rootGrid the rootGrid to set
	 */
	public void setRootGrid(RenderableSpacePartitioningGrid rootGrid)
		{
		this.rootGrid = rootGrid;
		}
	}
