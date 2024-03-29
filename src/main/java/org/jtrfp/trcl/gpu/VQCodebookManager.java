/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.OutOfIndicesException;

import com.jogamp.opengl.GL3;

public class VQCodebookManager {
    private final 	IndexPool 	codebook256Indices = new IndexPool().setHardLimit(CODE256_PER_PAGE*NUM_CODE_PAGES);
    private GLTexture 			rgbaTexture,
    					esTuTvTexture;
    private final	Collection<TileUpdate>tileUpdates	       = new ArrayList<TileUpdate>(1024);
    private final	GPU		gpu;
    private final	UncaughtExceptionHandler handler;
    public static final int 		CODE_PAGE_SIDE_LENGTH_TEXELS	=512;
    public static final int 		CODE_SIDE_LENGTH		=4;
    public static final int 		NUM_CODE_PAGES			=512;
    public static final int		MIP_DEPTH			=1;
    public static final int 		NUM_CODES_PER_AXIS		=CODE_PAGE_SIDE_LENGTH_TEXELS/CODE_SIDE_LENGTH;
    public static final int 		CODES_PER_PAGE 			=NUM_CODES_PER_AXIS*NUM_CODES_PER_AXIS;
    public static final int 		CODE256_PER_PAGE 		= CODES_PER_PAGE/256;
    public static final int 		CODE256_HEIGHT_CODES		= 256 / NUM_CODES_PER_AXIS;

    public VQCodebookManager(GPU gpu, UncaughtExceptionHandler handler) {
	this.handler=handler;
	this.gpu    = gpu;

	//Check if we can make enough codepages
	final IntBuffer ib = ByteBuffer.allocateDirect(4).asIntBuffer();
	
	gpu.getGlExecutor().submitToGL(new GLExecutable<Void, GL3>() {
	    public Void execute(GL3 gl) {
		gpu.getGl().glGetIntegerv(GL3.GL_MAX_ARRAY_TEXTURE_LAYERS, ib);
		if(ib.get(0)<NUM_CODE_PAGES)
		    throw new RuntimeException("Insufficient support for number of array texture layers: "+ib.get(0));
		else System.out.println("GL implementation supports "+ib.get(0)+" array texture layers.");
		rgbaTexture = gpu.
			newTexture().
			setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY).
			bind(gl).
			setInternalColorFormat(GL3.GL_COMPRESSED_RGBA).
			configure(new int[]{CODE_PAGE_SIDE_LENGTH_TEXELS,CODE_PAGE_SIDE_LENGTH_TEXELS,NUM_CODE_PAGES}, MIP_DEPTH, gl).
			setMagFilter(GL3.GL_LINEAR, gl).
			setMinFilter(GL3.GL_LINEAR_MIPMAP_LINEAR, gl).
			setWrapS(GL3.GL_CLAMP_TO_EDGE, gl).
			setWrapT(GL3.GL_CLAMP_TO_EDGE, gl);
		esTuTvTexture = gpu.
			newTexture().
			setBindingTarget(GL3.GL_TEXTURE_2D_ARRAY).
			bind(gl).
			setInternalColorFormat(GL3.GL_COMPRESSED_RGBA).
			configure(new int[]{CODE_PAGE_SIDE_LENGTH_TEXELS,CODE_PAGE_SIDE_LENGTH_TEXELS,NUM_CODE_PAGES}, MIP_DEPTH, gl).
			setMagFilter(GL3.GL_LINEAR, gl).
			setMinFilter(GL3.GL_LINEAR_MIPMAP_LINEAR, gl).
			setWrapS(GL3.GL_CLAMP_TO_EDGE, gl).
			setWrapT(GL3.GL_CLAMP_TO_EDGE, gl).
			unbind(gl);
		return null;
	    }
	});
    }//end constructor

    public VQCodebookManager setRGBA(int codeID, RasterRowWriter []rowWriter) {
	return setNNNN(codeID,rowWriter,rgbaTexture);
    }
    private VQCodebookManager setNNNN(int codeID, RasterRowWriter []rowWriter, GLTexture texture) {
	try{subImage(codeID,rowWriter,texture,2);}
	catch(OutOfMemoryError e){
	    handler.uncaughtException(Thread.currentThread(),new RuntimeException(e));}
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

    //private static final int [] codeDims 	= new int[] { CODE_SIDE_LENGTH, CODE_SIDE_LENGTH, 1 };
    //private static final int [] codePageDims 	= new int[] { CODE_PAGE_SIDE_LENGTH_TEXELS, CODE_PAGE_SIDE_LENGTH_TEXELS, 1 };
    private static final int [] code256Dims	= new int[] {CODE_PAGE_SIDE_LENGTH_TEXELS,CODE256_HEIGHT_CODES*CODE_SIDE_LENGTH, 1};

    public static interface RasterRowWriter{
	public void applyRow(int row, ByteBuffer dest);
    }//end RasterRowWriter

    private void subImage256(final int blockID, final RasterRowWriter[][] texels, GLTexture texture, int mipLevel) throws OutOfMemoryError{
	final int y = (blockID % CODE256_PER_PAGE) * CODE256_HEIGHT_CODES * CODE_SIDE_LENGTH;
	final int page = blockID / CODE256_PER_PAGE;
	if(page >= NUM_CODE_PAGES)
	    throw new OutOfMemoryError("Ran out of codebook pages. Requested index to write: "+page+" max: "+NUM_CODE_PAGES);
	synchronized(tileUpdates){
	    tileUpdates.add(new TileUpdate(texels,0,y,page));}
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
	synchronized(tileUpdates){
	    tileUpdates.add(new TileUpdate(new RasterRowWriter[][]{texels},x,y,z));}
    }// end subImage(...)

    private final ByteBuffer workTile = ByteBuffer.allocateDirect(4*CODE_SIDE_LENGTH*CODE_SIDE_LENGTH);
    private final ByteBuffer workTile256=ByteBuffer.allocateDirect(256*4*CODE_SIDE_LENGTH*CODE_SIDE_LENGTH);

    public void refreshStaleCodePages(GL3 gl){
	synchronized(tileUpdates){
	    refreshStaleCodePages(rgbaTexture,0,gl);
	    refreshStaleCodePages(esTuTvTexture,1,gl);
	    tileUpdates.clear();}
    }

    private void refreshStaleCodePages(GLTexture texture, int channelArrayIndex, GL3 gl){
	texture.bind(gl);
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
			writeWithMip(texture, workTile, tu.getX(), tu.getY(), tu.getZ(), 
				CODE_SIDE_LENGTH, CODE_SIDE_LENGTH, 0, gl);
			/*workTile.clear();
			texture.subImage(
				new int[]{tu.getX(),tu.getY(),tu.getZ()},
				codeDims,
				GL3.GL_RGBA, 0, workTile);*/
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
			writeWithMip(texture, workTile256, tu.getX(), tu.getY(), tu.getZ(), 
				code256Dims[0], code256Dims[1], 0, gl);
			/*workTile256.clear();
			texture.subImage(
				new int[]{tu.getX(),tu.getY(),tu.getZ()},
				code256Dims,
				GL3.GL_RGBA, 0, workTile256);*/
		    }//end code256
		    else throw new RuntimeException("Unrecognized rowWriter count: "+tu.getRowWriters().length);
		}//end if(rw!=null)
	    }//end if(channelArrayIndex)
	}//end for(tileUpdates)
	gpu.defaultTexture();
    }//end refreshStaleCodePages()
    
    private void writeWithMip(GLTexture texture, ByteBuffer workTile256, int updateX, int updateY, int layer, int updateWidth, int updateHeight, int level, GL3 gl){
	if(level >= MIP_DEPTH)
	    return;
	workTile256.clear();
	texture.subImage(
		new int[]{updateX,updateY,layer},
		new int[]{updateWidth, updateHeight, 1},
		GL3.GL_RGBA, level, workTile256, gl);
	//Scale down
	final int newUpdateHeight = updateHeight / 2;
	final int newUpdateWidth  = updateWidth  / 2;
	for(int newY = 0; newY < newUpdateHeight; newY++){
	    final int oldY = newY*2;
	    for(int newX = 0; newX < newUpdateWidth; newX++){
		final int oldX = newX*2;
		int accumulatorR = 0, accumulatorG = 0, accumulatorB = 0, accumulatorA = 0;
		//X,Y
		int oldFlatIndex = (oldY * updateWidth + oldX) * 4;
		workTile256.position(oldFlatIndex);
		accumulatorR += workTile256.get() & 0xFF;
		accumulatorG += workTile256.get() & 0xFF;
		accumulatorB += workTile256.get() & 0xFF;
		accumulatorA += workTile256.get() & 0xFF;
		//X+1, Y
		oldFlatIndex = (oldY * updateWidth + (oldX+1)) * 4;
		workTile256.position(oldFlatIndex);
		accumulatorR += workTile256.get() & 0xFF;
		accumulatorG += workTile256.get() & 0xFF;
		accumulatorB += workTile256.get() & 0xFF;
		accumulatorA += workTile256.get() & 0xFF;
		
		//X+1, Y+1
		oldFlatIndex = ((oldY+1) * updateWidth + (oldX+1)) * 4;
		workTile256.position(oldFlatIndex);
		accumulatorR += workTile256.get() & 0xFF;
		accumulatorG += workTile256.get() & 0xFF;
		accumulatorB += workTile256.get() & 0xFF;
		accumulatorA += workTile256.get() & 0xFF;
		
		//X, Y+1
		oldFlatIndex = ((oldY+1) * updateWidth + oldX) * 4;
		workTile256.position(oldFlatIndex);
		accumulatorR += workTile256.get() & 0xFF;
		accumulatorG += workTile256.get() & 0xFF;
		accumulatorB += workTile256.get() & 0xFF;
		accumulatorA += workTile256.get() & 0xFF;
		
		accumulatorR /= 4;
		accumulatorG /= 4;
		accumulatorB /= 4;
		accumulatorA /= 4;
		
		final int newFlatIndex = (newY * newUpdateWidth + newX) * 4;
		workTile256.position(newFlatIndex);
		workTile256.put((byte)(accumulatorR & 0xFF));
		workTile256.put((byte)(accumulatorG & 0xFF));
		workTile256.put((byte)(accumulatorB & 0xFF));
		workTile256.put((byte)(accumulatorA & 0xFF));
	    }//end for(x)
	}//end for(y)
	writeWithMip(texture, workTile256, updateX/2, updateY/2, layer, newUpdateWidth, newUpdateHeight, level+1, gl);
    }//end writeWithMip(...)

    public synchronized void newCodebook256(List<Integer> list, int count){
	count=codebook256Indices.pop(list,count);
	while(count > 0){
	    System.err.println("Warning: Codepages running low. Attempting a nuclear GC. Hold on to your hats...");
	    System.err.println("Remaining indices needed: "+count+" list="+list);
	    TRFactory.nuclearGC();
	    gpu.compactRootBuffer();
	    System.err.println("Still alive. Waiting 250ms and reattempting pop codebook256...");
	    try{Thread.sleep(3000);}catch(InterruptedException e){}
	    count=codebook256Indices.pop(list,count);
	    System.err.println("New count="+count+" list="+list);
	}//end while(count>0)
    }

    public synchronized int newCodebook256() {
	try{return codebook256Indices.popOrException();}
	catch(OutOfIndicesException e){
	    System.err.println("Warning: Codepages running low. Attemping a nuclear GC. Hold on to your hats...");
	    TRFactory.nuclearGC();
	    gpu.compactRootBuffer();
	    System.err.println("Still alive. Attempting blocking texture codebook256...");
	    return codebook256Indices.pop();
	}//end catch()
    }// end newCodebook256()

    public void freeCodebook256(int codebook256ToRelease) {
	System.out.println("VQCodebookManager.freeCodebook256() "+codebook256ToRelease);
	codebook256Indices.free(codebook256ToRelease);
    }// end freeCodebook256(...)

    public void freeCodebook256(Collection<Integer> list) {
	System.out.println("VQCodebookManager.freeCodebook256(list) "+list.iterator().next());
	codebook256Indices.free(list);
    }// end freeCodebook256(...)

    public GLTexture getRGBATexture()		{return rgbaTexture;}
    public GLTexture getESTuTvTexture()		{return esTuTvTexture;}

    public ByteBuffer []dumpPagesToBuffer() throws IOException {
	ByteBuffer buf = ByteBuffer.allocate(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * NUM_CODE_PAGES);
	final ByteBuffer[] result = new ByteBuffer[NUM_CODE_PAGES];
	for(int pg=0; pg<NUM_CODE_PAGES; pg++){
	    buf.position(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * pg);
	    buf.limit(4 * CODE_PAGE_SIDE_LENGTH_TEXELS * CODE_PAGE_SIDE_LENGTH_TEXELS * (pg+1));
	    result[pg]=buf.slice();
	    result[pg].clear();
	}
	rgbaTexture.getTextureImage(buf,GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE);
	return result;
    }//end dumpPageToPNG(...)

    private final class TileUpdate{
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
