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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.pool.IndexPool;

public class VQCodebookManager {
    private final 	IndexPool 	codebook256Indices = new IndexPool();
    private final 	GLTexture 	rgbaTexture,esTuTvTexture,indentationTexture;
    private final	ByteBuffer[]	codePageBuffers	   = new ByteBuffer[NUM_CODE_PAGES];
    private final	long[]		pageBufTimeStamp   = new long[NUM_CODE_PAGES];
    private final	ConcurrentSkipListSet<Integer>	
    					staleCodePages	   = new ConcurrentSkipListSet<Integer>();
    private TRFutureTask<Void>		codePageDiscardTask;
    private final	GPU		gpu;
    public static final int 		CODE_PAGE_SIDE_LENGTH_TEXELS	=128;
    public static final int 		CODE_SIDE_LENGTH		=4;
    public static final int 		NUM_CODES_PER_AXIS		=CODE_PAGE_SIDE_LENGTH_TEXELS/CODE_SIDE_LENGTH;
    public static final int 		NUM_CODE_PAGES			=2048;
    public static final int 		CODES_PER_PAGE 			=NUM_CODES_PER_AXIS*NUM_CODES_PER_AXIS;
    public static final int		MIP_DEPTH			=1;
    public static final int		PAGE_BUFFER_TIMEOUT		=5000; //5s then remove the buffer.

    public VQCodebookManager(TR tr) {
	gpu = tr.gpu.get();
	rgbaTexture = gpu.
		newTexture().
		setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY).
		bind().
		setInternalColorFormat(GL3.GL_RGBA4).
		configure(new int[]{CODE_PAGE_SIDE_LENGTH_TEXELS,CODE_PAGE_SIDE_LENGTH_TEXELS,NUM_CODE_PAGES}, 1).
		setMagFilter(GL3.GL_LINEAR).
		setMinFilter(GL3.GL_LINEAR).
		setWrapS(GL3.GL_CLAMP_TO_EDGE).
		setWrapT(GL3.GL_CLAMP_TO_EDGE);
	esTuTvTexture = gpu.
		newTexture().
		setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY).
		bind().
		setInternalColorFormat(GL3.GL_RGBA4).
		configure(new int[]{CODE_PAGE_SIDE_LENGTH_TEXELS,CODE_PAGE_SIDE_LENGTH_TEXELS,NUM_CODE_PAGES}, 1).
		setMagFilter(GL3.GL_LINEAR).
		setMinFilter(GL3.GL_LINEAR).
		setWrapS(GL3.GL_CLAMP_TO_EDGE).
		setWrapT(GL3.GL_CLAMP_TO_EDGE);
	indentationTexture = gpu.
		newTexture().
		setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY).
		bind().
		setInternalColorFormat(GL3.GL_RGBA4).
		configure(new int[]{CODE_PAGE_SIDE_LENGTH_TEXELS,CODE_PAGE_SIDE_LENGTH_TEXELS,NUM_CODE_PAGES}, 1).
		setMagFilter(GL3.GL_LINEAR).
		setMinFilter(GL3.GL_LINEAR).
		setWrapS(GL3.GL_CLAMP_TO_EDGE).
		setWrapT(GL3.GL_CLAMP_TO_EDGE).
		unbind();
    }//end constructor

    public VQCodebookManager setRGBA(int codeID, RasterRowWriter rowWriter) {
	try{subImage(codeID,rowWriter,rgbaTexture,4);}
	catch(OutOfMemoryError e){
	    System.err.println("Warning: Codepages running low. Attemping a nuclear GC. Hold on to your hats...");
	    TR.nuclearGC();
	    System.err.println("Still alive. Attempting RGBA write again...");
	    subImage(codeID,rowWriter,rgbaTexture,4);
	    System.err.println("Success.");
	}
	return this;
    }// end setRGBA(...)
    
    private static final int [] codeDims 	= new int[] { CODE_SIDE_LENGTH, CODE_SIDE_LENGTH, 1 };
    private static final int [] codePageDims 	= new int[] { CODE_PAGE_SIDE_LENGTH_TEXELS, CODE_PAGE_SIDE_LENGTH_TEXELS, 1 };
    
    public static interface RasterRowWriter{
	public void applyRow(int row, ByteBuffer dest);
    }//end RasterRowWriter
    
    private void subImage(final int codeID, final RasterRowWriter texels,
	    final GLTexture tex, int mipLevel) throws OutOfMemoryError {
	final int x = (codeID % NUM_CODES_PER_AXIS)*CODE_SIDE_LENGTH;
	final int z = codeID / CODES_PER_PAGE;
	final int y = ((codeID % CODES_PER_PAGE) / NUM_CODES_PER_AXIS)*CODE_SIDE_LENGTH;
	
	if(z >= NUM_CODE_PAGES){
	    throw new OutOfMemoryError("Ran out of codebook pages. Requested index to write: "+z+" max: "+NUM_CODE_PAGES);
	}
	if(x>=CODE_PAGE_SIDE_LENGTH_TEXELS || y >= CODE_PAGE_SIDE_LENGTH_TEXELS ){
	    throw new RuntimeException("One or more texel coords intolerably out of range: x="+x+" y="+y);
	}
	if(codePageBuffers[z]==null){
	    codePageBuffers[z] = ByteBuffer
		    .allocateDirect(CODE_PAGE_SIDE_LENGTH_TEXELS
			    * CODE_PAGE_SIDE_LENGTH_TEXELS * 4);
	    pageBufTimeStamp[z]=System.currentTimeMillis();
	}
	final ByteBuffer codePageBuffer = codePageBuffers[z];
	staleCodePages.add(z);
	synchronized (codePageBuffer) {
	synchronized (texels) {
	    for (int row = 0; row < CODE_SIDE_LENGTH; row++) {
		codePageBuffer
			.position((x + (y+row) * CODE_PAGE_SIDE_LENGTH_TEXELS) * 4);
		texels.applyRow(row, codePageBuffer);
		//codePageBuffer.put(texels);
	    }// end for(rows)
	}}// end sync(codePageBuffers[z],texels)
    }// end subImage(...)
    
    public void refreshStaleCodePages(){
	final long currTime = System.currentTimeMillis();
	while(!staleCodePages.isEmpty()){
	    final int codePageID = staleCodePages.pollFirst();
	    final ByteBuffer codePageBuffer=codePageBuffers[codePageID];
	    synchronized(codePageBuffer){
	    codePageBuffer.clear();
	    rgbaTexture.bind().subImage(
		    new int[]{0,0,codePageID},
		    codePageDims,
		    GL3.GL_RGBA, 0, codePageBuffer);
	    pageBufTimeStamp[codePageID]=currTime;
	    }//end sync(codePageBuffer)
	}//end for(staleCodePages
	//TODO: Only cleanup when whole page no longer in use
	//cleanupOldCodePageBuffers(currTime);//Cannot use yet.
    }//end refreshStaleCodePages()
/*
    private void subImageAutoMip(final int codeID, final ByteBuffer texels,
	    final GLTexture tex, int byteSizedComponentsPerTexel) {
	ByteBuffer wb = ByteBuffer.allocate(texels.capacity());
	ByteBuffer intermediate = ByteBuffer.allocate(texels.capacity() / 4);
	texels.clear();wb.clear();
	wb.put(texels);
	int sideLen = (int)Math.sqrt(texels.capacity() / byteSizedComponentsPerTexel);
	for (int mipLevel = 0; mipLevel < MIP_DEPTH; mipLevel++) {
	    wb.clear();
	    subImage(codeID, wb, tex, mipLevel);
	    mipDown(wb, intermediate, sideLen, byteSizedComponentsPerTexel);
	    wb.clear();
	    intermediate.clear();
	    wb.put(intermediate);
	}// end for(mipLevel)
    }// end subImageAutoMip(...)
*/
    private void cleanupOldCodePageBuffers(long currTime){
	final long timeout  = currTime - PAGE_BUFFER_TIMEOUT;
	if(codePageDiscardTask!=null){
	    if(!codePageDiscardTask.isDone()){
		return;}}
	codePageDiscardTask = gpu.getTr().getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		//Check for not-recently-used codepage buffers and free them up to save memory.
		for(int i=0; i<NUM_CODE_PAGES; i++){
		    if(pageBufTimeStamp[i]<timeout && codePageBuffers[i]!=null){
			System.out.println("VQCodebookManager releasing codePageBuffer at index "+i);
			codePageBuffers[i]=null;
		    }//end if(timeout)
		}//end for(code pages)
		return null;
	    }//end call()
	});//end new Callable()
    }//end cleanupOldCodePageBuffers()
    
    private void mipDown(ByteBuffer in, ByteBuffer out, int sideLen,
	    int componentsPerTexel) {
	int outX, outY, inX, inY, inIndex, outIndex;
	final int newSideLen = sideLen / 2;
	for (int y = 0; y < newSideLen; y++)
	    for (int x = 0; x < newSideLen; x++) {
		inX = x * 2;
		inY = y * 2;
		outX = x;
		outY = y;
		int component = 0;
		for (int cIndex = 0; cIndex < componentsPerTexel; cIndex++) {
		    for (int sy = 0; sy < 2; sy++)
			for (int sx = 0; sx < 2; sx++) {
			    inIndex = ((inX + sx) + (inY + sy) * sideLen);
			    inIndex *= componentsPerTexel;
			    inIndex += cIndex;
			    component += in.get(inIndex);
			}// end for(sx)
		    outIndex = (outX + outY * newSideLen);
		    outIndex *= componentsPerTexel;
		    outIndex += cIndex;
		    component /= 4;
		    out.put(outIndex, (byte) component);
		}// end for(cIndex)
	    }// end for(x)
    }// end mipDown(...)

    public int newCodebook256() {
	return codebook256Indices.pop();
    }// end newCODE()

    public void freeCodebook256(int codebook256ToRelease) {
	System.out.println("VQCodebookManager.freeCodebook256() "+codebook256ToRelease);
	codebook256Indices.free(codebook256ToRelease);
    }// end releaseCODE(...)
    
    public GLTexture getRGBATexture()		{return rgbaTexture;}
    public GLTexture getESTuTvTexture()		{return esTuTvTexture;}
    public GLTexture getIndentationTexture()	{return indentationTexture;}

    public ByteBuffer []dumpPagesToBuffer() throws IOException {
	ByteBuffer buf = ByteBuffer.allocate(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * NUM_CODE_PAGES);
	final ByteBuffer[] result = new ByteBuffer[NUM_CODE_PAGES];
	for(int pg=0; pg<NUM_CODE_PAGES; pg++){
	    buf.position(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * pg);
	    buf.limit(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * (pg+1));
	    result[pg]=buf.slice();
	    result[pg].clear();
	}
	rgbaTexture.getTextureImageRGBA(buf);
	return result;
    }//end dumpPageToPNG(...)

}// end VQCodebookManager
