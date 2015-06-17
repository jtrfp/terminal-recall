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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.TRIT;
import org.jtrfp.trcl.gpu.GLProgram.ValidationHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

//@Ignore
public class RenderingIT extends TRIT{
    protected static final String NO_CAM_MATRIX_REF = "noCamMatrixRef.dat";
    protected static final String YES_CAM_MATRIX_REF= "yesCamMatrixRef.dat";
    protected static final String VERTEX_NORMXY_REF = "vertexNormXYRef.dat";
    protected static final String VERTEX_NORMZ_REF  = "vertexNormZRef.dat";
    protected static final String VERTEX_W_REF= "vertexWRef.dat";
    protected static final String VERTEX_Z_REF= "vertexZRef.dat";
    protected static final String VERTEX_XY_REF= "vertexXYRef.dat";
    protected static final String VERTEX_UV_REF= "vertexUVRef.dat";
    protected static final String VERTEX_TEXTUREID_REF= "vertexTextureIDRef.dat";
    protected static final double MATRIX_ERROR_THRESH  = 0;
    private static final ValidationHandler vh = new ValidationHandler(){
	@Override
	public void invalidProgram(GLProgram p) {
	    //fail("Program validation failed."); //Ignore for now
	}
    };
    
    @Before
    public void setUp() throws Exception{
	super.setUp();
	errorThreshold=.1;
    }
    
    
    
    @After
    public void tearDown() throws Exception{
	super.tearDown();
    }
    
    @Test
    public void populatedRendererYesCamMatrixTest() throws Exception {
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getCamMatrixTexture().
	 getTextureImage(bb,GL3.GL_RGBA,GL3.GL_FLOAT);
	bb.clear();
	
	boolean result = false;
	while(bb.hasRemaining()){
	    float res = bb.getFloat();
	    if(res!=0) result = true;
	    }
	assertTrue(result);
    }//end populatedRendererTest
    
    @Test
    public void populatedRendererNoCamMatrixTest() throws Exception {
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getNoCamMatrixTexture().
	 getTextureImage(bb,GL3.GL_RGBA,GL3.GL_FLOAT);
	bb.clear();
	
	boolean result = false;
	while(bb.hasRemaining()){
	    float res = bb.getFloat();
	    if(res!=0) result = true;
	    }
	assertTrue(result);
    }//end populatedRendererTest
    
    @Test
    public void emptyRendererUVVertexTest() throws Exception {
	ByteBuffer bb = ByteBuffer.allocateDirect(VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4)
		.order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexUVTexture().
	 getTextureImage(bb,GL3.GL_RG,GL3.GL_FLOAT);
	bb.clear();
	
	int matrixError=0;
	while(bb.hasRemaining()){
	    float res = bb.getFloat();
	    //if(res!=0)System.out.println("res="+res+" pos="+bb.position());
	    matrixError+=res;
	    }
	assertEquals(0,matrixError);
    }//end emptyRendereTest
   
    @Test
    public void emptyRendererXYVertexTest() throws Exception {
	ByteBuffer bb = ByteBuffer.allocateDirect(VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4)
		.order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexXYTexture().
	 getTextureImage(bb,GL3.GL_RG,GL3.GL_FLOAT);
	bb.clear();
	
	int matrixError=0;
	while(bb.hasRemaining()){
	    float res = bb.getFloat();
	    //if(res!=0)System.out.println("res="+res+" pos="+bb.position());
	    matrixError+=res;
	    }
	assertEquals(0,matrixError);
    }//end emptyRendereTest
    
    @Test
    public void emptyRendererYesCamMatrixTest() throws Exception {
	ByteBuffer bb = ByteBuffer.allocateDirect(1024*128*4*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	tr.mainRenderer.get().getRendererFactory().getObjectProcessingStage().getCamMatrixTexture().
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
    
    @Test
    public void emptyRendererNoCamMatrixTest() throws Exception {
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
    }//end emptyRendererTest
    
    @Ignore
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
	
	//dumpContentsToFile(bb,NO_CAM_MATRIX_REF);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,NO_CAM_MATRIX_REF);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,NO_CAM_MATRIX_REF+".result");fail("");}
    }//end cubeRendererTest
    
    @Ignore
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
	
	//dumpContentsToFile(bb,YES_CAM_MATRIX_REF);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,YES_CAM_MATRIX_REF);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,YES_CAM_MATRIX_REF+".result");fail("");}
    }//end cubeRendererTest
    
    @Ignore
    @Test
    public void cubeRendererVertexNormXYTest() throws Exception {
	clearOldResult(VERTEX_NORMXY_REF);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexNormXYTexture().
	 getTextureImage(bb,GL3.GL_RG,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,VERTEX_NORMXY_REF);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,VERTEX_NORMXY_REF);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,VERTEX_NORMXY_REF+".result");fail("");}
    }//end cubeRendererVertexTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexNormZTest() throws Exception {
	final String referenceFilename = VERTEX_NORMZ_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexNormZTexture().
	 getTextureImage(bb,GL3.GL_RED,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexNormZTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexTextureIDTest() throws Exception {
	final String referenceFilename = VERTEX_TEXTUREID_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexTextureIDTexture().
	 getTextureImage(bb,GL3.GL_RED,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexNormZTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexZTest() throws Exception {
	final String referenceFilename = VERTEX_Z_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexZTexture().
	 getTextureImage(bb,GL3.GL_RED,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexZTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexWTest() throws Exception {
	final String referenceFilename = VERTEX_W_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*1*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexWTexture().
	 getTextureImage(bb,GL3.GL_RED,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexWTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexXYTest() throws Exception {
	final String referenceFilename = VERTEX_XY_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexXYTexture().
	 getTextureImage(bb,GL3.GL_RG,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexXYTest()
    
    @Ignore
    @Test
    public void cubeRendererVertexUVTest() throws Exception {
	final String referenceFilename = VERTEX_UV_REF;
	clearOldResult(referenceFilename);
	spawnCubes();
	ByteBuffer bb = ByteBuffer.allocateDirect(
		VertexProcessingStage.VERTEX_BUFFER_WIDTH*VertexProcessingStage.VERTEX_BUFFER_HEIGHT*2*4).order(ByteOrder.nativeOrder());
	Thread.sleep(1000);
	
	tr.mainRenderer.get().getRendererFactory().getVertexProcessingStage().getVertexUVTexture().
	 getTextureImage(bb,GL3.GL_RG,GL3.GL_FLOAT);
	bb.clear();
	
	//dumpContentsToFile(bb,referenceFilename);
	
	ByteBuffer refBB = ByteBuffer.allocate(bb.capacity()).order(ByteOrder.nativeOrder());
	readContentsFromResource(refBB,referenceFilename);
	
	if(!testFloatResults(bb,refBB))
	    {dumpContentsToFile(bb,referenceFilename+".result");fail("");}
    }//end cubeRendererVertexUVTest()
    
}//end RendererIT
