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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Random;

import javax.media.opengl.GL3;

import junit.framework.Assert;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.GLProgram.ValidationHandler;
import org.jtrfp.trcl.obj.WorldObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RenderingIT{
    protected static final String NO_CAM_MATRIX_REF  = "noCamMatrixRef.dat";
    protected static final String YES_CAM_MATRIX_REF = "yesCamMatrixRef.dat";
    protected static final int MATRIX_ERROR_THRESH = 0;
    protected TR tr;
    private static final ValidationHandler vh = new ValidationHandler(){
	@Override
	public void invalidProgram(GLProgram p) {
	    //fail("Program validation failed."); //Ignore for now
	}
    };
    
    @Before
    public void setUp() throws Exception{
	tr = new TR();
	tr.getDefaultGrid().removeAll();
	tr.getThreadManager().setPaused(false);
	final Renderer renderer = tr.mainRenderer.get();
	renderer.setAmbientLight(Color.gray);
	renderer.setSunVector(new Vector3D(1,2,.25).normalize());
	Camera c = tr.mainRenderer.get().getCamera();
	c.setHeading(Vector3D.PLUS_K);
	c.setTop(Vector3D.PLUS_J);
	c.setPosition(new double[]{0,0,-50000});
	c.notifyPositionChange();
	c.setActive(true);
    }
    
    private void spawnCubes(){//Ordering not guaranteed so we will keep it at one cube per primitive type
	final TextureDescription texture = tr.gpu.get().textureManager.get().getFallbackTexture();
	//OPAQUE
	WorldObject obj;
	obj = new WorldObject(tr);
	obj.setModel(Model.buildCube(10000, 10000, 10000, texture, new double[]{5000,5000,5000}, false, tr));
	obj.setHeading(new Vector3D(1,1,1).normalize());
	obj.setTop(new Vector3D(-1,1,1).normalize());
	obj.setPosition(new double[]
		{-10000,0,0});
	obj.notifyPositionChange();
	obj.setVisible(true);
	obj.setActive (true);
	tr.getDefaultGrid().add(obj);
	//TRANSPARENT
	obj = new WorldObject(tr);
	obj.setModel(Model.buildCube(10000, 10000, 10000, texture, new double[]{5000,5000,5000}, true, tr));
	obj.setHeading(new Vector3D(1,1,1).normalize());
	obj.setTop(new Vector3D(-1,1,1).normalize());
	obj.setPosition(new double[]
		{10000,0,0});
	obj.notifyPositionChange();
	obj.setVisible(true);
	obj.setActive (true);
	tr.getDefaultGrid().add(obj);
    }//end spawnCubes()
    
    @After
    public void tearDown() throws Exception{
	tr.getRootWindow().setVisible(false);
    }
    
    @Test
    public void emptyRendererTest() throws Exception {
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getNoCamMatrixTexture().
	 getTextureImage(bb,GL3.GL_RGBA,GL3.GL_FLOAT);
	bb.clear();
	
	int matrixError=0;
	while(bb.hasRemaining()){
	    float res = bb.getFloat();
	    //if(res!=0)System.out.println("res="+res+" pos="+bb.position());
	    matrixError+=res;
	    }
	assertEquals(0,matrixError);
    }//end emptyRendereTest
    
    
    private void dumpContentsToFile(ByteBuffer bb, String file) throws Exception{
	bb.clear();
	final File f = new File(file);
	if(f.exists())f.delete();
	FileOutputStream fos = new FileOutputStream(file);
	FileChannel channel = fos.getChannel();
	channel.write(bb);
	channel.close();
	fos    .close();
	System.out.println("Dumped contents to "+file);
    }
    
    /**
     * The target ByteBuffer's position is cleared after writing.
     * @param bb
     * @param file
     * @throws Exception
     * @since Jun 14, 2015
     */
    private void readContentsFromResource(ByteBuffer bb, String file) throws Exception{
	InputStream is = Class.class.getResourceAsStream("/"+file);
	assertNotNull("Failed to load resource /"+file,is);
	int val;
	while((val=is.read())!=-1)
	    bb.put((byte)val);
	is.close();
	bb.clear();
    }
    
    private boolean testResults(ByteBuffer bb, ByteBuffer refBB){
	int matrixError=0;
	while(bb.hasRemaining()){
	    float ref = refBB.getFloat();
	    float res = bb.getFloat();
	    if(ref!=0) System.out.println("ref="+ref+" res="+res+" pos="+bb.position());
	    matrixError+=Math.abs(ref-res);
	    }
	return matrixError<=MATRIX_ERROR_THRESH;
    }
    
    private void clearOldResult(String file){
	File f = new File(file+".result");
	if(f.exists())f.delete();
    }
    
    @Test
    public void cubeRendererMatrixTestNoCam() throws Exception {
	clearOldResult(NO_CAM_MATRIX_REF);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	// NO CAM
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getNoCamMatrixTexture().
	 getTextureImage(bb,GL3.GL_RGBA,GL3.GL_FLOAT);
	bb.clear();
	/*
	dumpContentsToFile(bb,NO_CAM_MATRIX_REF);
	*/
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,NO_CAM_MATRIX_REF);
	
	if(!testResults(bb,refBB))
	    {dumpContentsToFile(bb,NO_CAM_MATRIX_REF+".result");fail("");}
    }//end cubeRendererTest
    
    @Test
    public void cubeRendererMatrixTestYesCam() throws Exception {
	clearOldResult(YES_CAM_MATRIX_REF);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	// YES CAM
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getCamMatrixTexture().
	 getTextureImage(bb,GL3.GL_RGBA,GL3.GL_FLOAT);
	bb.clear();
	/*
	dumpContentsToFile(bb,YES_CAM_MATRIX_REF);
	*/
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,YES_CAM_MATRIX_REF);
	
	if(!testResults(bb,refBB))
	    {dumpContentsToFile(bb,YES_CAM_MATRIX_REF+".result");fail("");}
    }//end cubeRendererTest

}//end RendererIT
