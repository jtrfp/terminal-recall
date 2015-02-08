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
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.pool.IndexPool;

public class VQCodebookManager {
    private final 	IndexPool 	codebook256Indices = new IndexPool();
    private final 	GLTexture 	rgbaTexture,esTuTvTexture,indentationTexture;
    private final	Queue<TileUpdate>tileUpdates	   = new LinkedBlockingQueue<TileUpdate>();
    private final	GPU		gpu;
    private final	GLFrameBuffer	fb;
    public static final int 		CODE_PAGE_SIDE_LENGTH_TEXELS	=128;
    public static final int 		CODE_SIDE_LENGTH		=4;
    public static final int 		NUM_CODES_PER_AXIS		=CODE_PAGE_SIDE_LENGTH_TEXELS/CODE_SIDE_LENGTH;
    public static final int 		NUM_CODE_PAGES			=2048;
    public static final int 		CODES_PER_PAGE 			=NUM_CODES_PER_AXIS*NUM_CODES_PER_AXIS;
    public static final int		MIP_DEPTH			=1;
    public static final int 		CODE256_PER_PAGE 		= CODES_PER_PAGE/256;
    public static final int 		CODE256_HEIGHT_CODES		= 256 / NUM_CODES_PER_AXIS;

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
	
	final GL3 gl = gpu.getGl();
	
	fb = gpu.newFrameBuffer();
	
	for(int i=0; i<NUM_CODE_PAGES; i++){
	    fb.bindToDraw();
	    gl.glFramebufferTextureLayer(GL3.GL_DRAW_FRAMEBUFFER, GL3.GL_COLOR_ATTACHMENT0, esTuTvTexture.getId(), 0, i);
			//attachDrawTexture(esTuTvTexture, GL3.GL_COLOR_ATTACHMENT0).
	    fb.setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0);
	    gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
	    fb.unbindFromDraw();
	}//end for(i)
	
    }//end constructor
    public VQCodebookManager setRGBA(int codeID, RasterRowWriter []rowWriter) {
	return setNNNN(codeID,rowWriter,rgbaTexture);
    }
    private VQCodebookManager setNNNN(int codeID, RasterRowWriter []rowWriter, GLTexture texture) {
	try{subImage(codeID,rowWriter,texture,2);}
	catch(OutOfMemoryError e){
	    System.err.println("Warning: Codepages running low. Attemping a nuclear GC. Hold on to your hats...");
	    TR.nuclearGC();
	    System.err.println("Still alive. Attempting texture NNNN write again...");
	    subImage(codeID,rowWriter,texture,4);
	    System.err.println("Success.");
	}
	return this;
    }// end setRGBA(...)
    
    public VQCodebookManager setRGBABlock256(int blockID, RasterRowWriter [] rowWriters){
	return setNNNNBlock256(blockID, new RasterRowWriter[][]{rowWriters},rgbaTexture);
    }
    
    public VQCodebookManager setESTuTvBlock256(int blockID, RasterRowWriter [] rowWriters){
	return setNNNNBlock256(blockID, new RasterRowWriter[][]{null,rowWriters},esTuTvTexture);
    }
    
    private VQCodebookManager setNNNNBlock256(int blockID, RasterRowWriter [][] rowWriters, GLTexture texture){
	subImage256(blockID,rowWriters,texture,2);
	return this;
    }
    
    private static final int [] codeDims 	= new int[] { CODE_SIDE_LENGTH, CODE_SIDE_LENGTH, 1 };
    private static final int [] codePageDims 	= new int[] { CODE_PAGE_SIDE_LENGTH_TEXELS, CODE_PAGE_SIDE_LENGTH_TEXELS, 1 };
    private static final int [] code256Dims	= new int[] {CODE_PAGE_SIDE_LENGTH_TEXELS,CODE256_HEIGHT_CODES*CODE_SIDE_LENGTH, 1};
    
    public static interface RasterRowWriter{
	public void applyRow(int row, ByteBuffer dest);
    }//end RasterRowWriter
    
    private void subImage256(final int blockID, final RasterRowWriter[][] texels, GLTexture texture, int mipLevel){
	final int y = (blockID % CODE256_PER_PAGE) * CODE256_HEIGHT_CODES * CODE_SIDE_LENGTH;
	final int page = blockID / CODE256_PER_PAGE;
	tileUpdates.add(new TileUpdate(texels,0,y,page));
    }
    
    private void subImage(final int codeID, final RasterRowWriter []texels,
	    final GLTexture tex, int mipLevel) throws OutOfMemoryError {
	final int x = (codeID % NUM_CODES_PER_AXIS)*CODE_SIDE_LENGTH;
	final int z = codeID / CODES_PER_PAGE;
	final int y = ((codeID % CODES_PER_PAGE) / NUM_CODES_PER_AXIS)*CODE_SIDE_LENGTH;
	
	if(z >= NUM_CODE_PAGES)
	    throw new OutOfMemoryError("Ran out of codebook pages. Requested index to write: "+z+" max: "+NUM_CODE_PAGES);
	if(x>=CODE_PAGE_SIDE_LENGTH_TEXELS || y >= CODE_PAGE_SIDE_LENGTH_TEXELS )
	    throw new RuntimeException("One or more texel coords intolerably out of range: x="+x+" y="+y);
	/*
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
	*/
	tileUpdates.add(new TileUpdate(new RasterRowWriter[][]{texels},x,y,z));
    }// end subImage(...)
    
    private final ByteBuffer workTile = ByteBuffer.allocateDirect(4*CODE_SIDE_LENGTH*CODE_SIDE_LENGTH);
    private final ByteBuffer workTile256=ByteBuffer.allocateDirect(256*4*CODE_SIDE_LENGTH*CODE_SIDE_LENGTH);
    
    public void refreshStaleCodePages(){
	refreshStaleCodePages(rgbaTexture,0);
	refreshStaleCodePages(esTuTvTexture,1);
	tileUpdates.clear();
    }
    
    private void refreshStaleCodePages(GLTexture texture, int channelArrayIndex){
	texture.bind();
	for(TileUpdate tu:tileUpdates){
	    final RasterRowWriter [][]rw2 = tu.getRowWriters();
	    if(rw2.length>channelArrayIndex){
		final RasterRowWriter []rowWriters = tu.getRowWriters()[channelArrayIndex];
		    if(rowWriters!=null){
			if(rowWriters.length==1){
			    for (int row = 0; row < CODE_SIDE_LENGTH; row++) {
				workTile.position(row * 4 * CODE_SIDE_LENGTH);
				rowWriters[0].applyRow(row, workTile);
			    }// end for(rows)
			    workTile.clear();
			    texture.subImage(
				    new int[]{tu.getX(),tu.getY(),tu.getZ()},
				    codeDims,
				    GL3.GL_RGBA, 0, workTile);
			}//end if(single code)
			else if(rowWriters.length==256){
			    for (int y = 0; y < CODE256_HEIGHT_CODES; y++) {
				for (int x = 0; x < NUM_CODES_PER_AXIS; x++) {
				    final RasterRowWriter rw = rowWriters[x+y*NUM_CODES_PER_AXIS];
				    if(rw!=null){
					for (int row = 0; row < CODE_SIDE_LENGTH; row++) {
					    workTile256.position((row+y*CODE_SIDE_LENGTH) * 4
						    * CODE_PAGE_SIDE_LENGTH_TEXELS + x * 4 * CODE_SIDE_LENGTH);
					    assert workTile256!=null;
					    rw.applyRow(row, workTile256);
					}// end for(rows)
				    }//end if(!null)
				}// end for(x)
			    }// end for(y)
			    workTile256.clear();
			    texture.subImage(
				    new int[]{tu.getX(),tu.getY(),tu.getZ()},
				    code256Dims,
				    GL3.GL_RGBA, 0, workTile256);
			}//end code256
			else throw new RuntimeException("Unrecognized rowWriter count: "+tu.getRowWriters().length);
		    }//end if(rw!=null)
	    }//end if(channelArrayIndex)
	}//end for(tileUpdates)
	    gpu.defaultTexture();
	    //tileUpdates.clear();
	/*final long currTime = System.currentTimeMillis();
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
	*/
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
    */
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

    private final class TileUpdate{//TODO: 2D array, if null element then skip.
	private final RasterRowWriter [][]rowWriters;
	private final int x,y,z;
	public TileUpdate(RasterRowWriter [][]rowWriter, int x, int y, int z){
	    this.rowWriters=rowWriter;
	    this.x=x;
	    this.y=y;
	    this.z=z;
	}//end constructor
	/**
	 * @return the rowWriters
	 */
	public RasterRowWriter[][] getRowWriters() {
	    return rowWriters;
	}
	/**
	 * @return the x
	 */
	public int getX() {
	    return x;
	}
	/**
	 * @return the y
	 */
	public int getY() {
	    return y;
	}
	/**
	 * @return the z
	 */
	public int getZ() {
	    return z;
	}
	
    }//end TileUpdate
}// end VQCodebookManager
