/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.ext.tr.GPUResourceFinalizer;
import org.jtrfp.trcl.gpu.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.img.vq.BufferedImageRGBA8888VL;
import org.jtrfp.trcl.img.vq.ByteBufferVectorList;
import org.jtrfp.trcl.img.vq.CachingVectorListND;
import org.jtrfp.trcl.img.vq.ConstantVectorList;
import org.jtrfp.trcl.img.vq.MIPScalingVectorListND;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.RGBA8888VectorList;
import org.jtrfp.trcl.img.vq.RasterizedBlockVectorList;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.img.vq.VectorListND;
import org.jtrfp.trcl.img.vq.VectorListRasterizer;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;

public class UncompressedVQTextureFactory {
    private final ThreadManager threadManager;
    private final TextureManager tm;
    private final VQCodebookManager cbm;
    private final TextureTOCWindow tocWindow;
    private final SubTextureWindow stw;
    private final String debugName;
    private final GPU gpu;
    private final GPUResourceFinalizer gpuResourceFinalizer;
    
    public UncompressedVQTextureFactory(GPU gpu, ThreadManager threadManager, String debugName){
   	this.tm		  =gpu.textureManager.get();
   	this.cbm	  =tm.vqCodebookManager.get();
   	this.tocWindow	  =(TextureTOCWindow)tm.getTOCWindow();
   	this.stw	  =(SubTextureWindow)tm.getSubTextureWindow();
   	this.debugName	  =debugName.replace('.', '_');
   	this.gpu          =gpu;
   	this.threadManager=threadManager;
   	this.gpuResourceFinalizer = gpu.getGPUResourceFinalizer();
    }//end constructor
    
    VQTexture newUncompressedVQTexture(){
	return newUncompressedVQTexture("");
    }//end newUncompressedVQTexture
    
    VQTexture newUncompressedVQTexture(String debugName){
	return new VQTexture(gpu, this.debugName+"."+debugName);
    }//end newUncompressedVQTexture()
    
 public VQTexture newUncompressedVQTexture(VectorList rgba8888vl, VectorList esTuTv8888vl, final int sideLength, boolean generateMipMaps){
     final VQTexture result = newUncompressedVQTexture();
     assemble(rgba8888vl, esTuTv8888vl,sideLength, result, generateMipMaps);
     return result;
 }//end newUncompressedVQTexture

 public VQTexture newUncompressedVQTexture(String debugName, boolean uvWrapping){
     final VQTexture result = newUncompressedVQTexture();
     result.setUvWrapping(uvWrapping);
     return result;
 }//end newUncompressedVQTexture(...)
 
 public VQTexture newUncompressedVQTexture(PalettedVectorList vlRGBA, PalettedVectorList vlESTuTv, String debugName, boolean uvWrapping, boolean generateMipMaps){
	final VQTexture result = newUncompressedVQTexture();
	result.setUvWrapping(uvWrapping);
	assemble(vlRGBA,vlESTuTv,result,generateMipMaps);
	return result;
 }//end constructor

 VQTexture newUncompressedVQTexture(ByteBuffer imageRGBA8888, ByteBuffer imageESTuTv8888, String debugName, boolean uvWrapping, boolean generateMipMaps) {
	final VQTexture result = newUncompressedVQTexture();
	result.setUvWrapping(uvWrapping);
	if (imageRGBA8888.capacity() == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create texture of zero size.");
	}//end if capacity==0
	imageRGBA8888.clear();//Doesn't erase, just resets the tracking vars
	assemble(imageRGBA8888,imageESTuTv8888,result, generateMipMaps);
	return result;
 }// end constructor
 

 public VQTexture newUncompressedVQTexture(BufferedImage imgRGBA, BufferedImage imgESTuTv, String debugName, boolean uvWrapping, boolean generateMipMaps) {
        final VQTexture result = newUncompressedVQTexture(debugName,uvWrapping);
	try{assemble(new BufferedImageRGBA8888VL(imgRGBA),
		    imgESTuTv!=null?
			    new BufferedImageRGBA8888VL(imgESTuTv):
			    null,imgRGBA.getWidth(),result,generateMipMaps);
	    }catch(Exception e){e.printStackTrace();}
	return result;
 }//end constructor
 
 public VQTexture newUncompressedVQTexture(VectorListND rgba, VectorListND esTuTv, boolean generateMipMaps){
     final VQTexture result = newUncompressedVQTexture();
     assemble(rgba, esTuTv, result, generateMipMaps);
     return result;
 }
 
 private Color calulateAverageColor(RasterizedBlockVectorList rbvl) {
	float redA=0,greenA=0,blueA=0;
	    final int [] dims = rbvl.getDimensions();
	    for(int i=0; i<10; i++){
		redA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,0);
		greenA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,1);
		blueA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,2);
	    }return new Color(redA/10f,greenA/10f,blueA/10f);
 }//end calculateAverageColor(...)
 
 private void assemble(PalettedVectorList squareImageIndexedRGBA,PalettedVectorList squareImageIndexedESTuTv, VQTexture result, boolean generateMipMaps){
	checkSideLengthSanity(squareImageIndexedRGBA);
	assemble(squareImageIndexedRGBA, squareImageIndexedESTuTv,(int)Math.sqrt(squareImageIndexedRGBA.getNumVectors()),result,generateMipMaps);
 }//end assemble(...)
 
 private void assemble(ByteBuffer imageRGBA8888, ByteBuffer imageESTuTv8888, VQTexture result, boolean generateMipMaps){
	 checkSideLengthSanity(imageRGBA8888);
	 // Break down into 4x4 blocks
	 final ByteBufferVectorList 	bbvl 		= new ByteBufferVectorList(imageRGBA8888);
	 final RGBA8888VectorList 	rgba8888vl 	= new RGBA8888VectorList(bbvl);
	 
	 final VectorList	 	bbvlESTuTv 	= imageESTuTv8888!=null?new ByteBufferVectorList(imageESTuTv8888):new ConstantVectorList(0,bbvl);
	 final RGBA8888VectorList 	esTuTv8888vl 	= bbvlESTuTv!=null?new RGBA8888VectorList(bbvlESTuTv):null;
	 assemble(rgba8888vl,esTuTv8888vl,(int)Math.sqrt(imageRGBA8888.capacity() / 4), result, generateMipMaps);
}//end assemble(...)
 
 private static void checkSideLengthSanity(int totalPixels){
	final double	fuzzySideLength = Math.sqrt(totalPixels);
	final int 	sideLength	= (int)Math.floor(fuzzySideLength);
	if(!SpecialRAWDimensions.isPowerOfTwo(sideLength))
	    System.err.println("WARNING: Calculated dimensions are not power-of-two. Trouble ahead.");
	if(Math.abs(fuzzySideLength-sideLength)>.001)
	    System.err.println("WARNING: Calculated dimensions are not perfectly square. Trouble ahead.");
 }//end checkSideLengthSanity
 
 private static void checkSideLengthSanity(ByteBuffer imageRGBA8888){
	checkSideLengthSanity(imageRGBA8888.capacity() / 4);
 }//end checkSideLengthSanity(...)
 
 private static void checkSideLengthSanity(VectorList vectorList){
	checkSideLengthSanity(vectorList.getNumVectors());
 }//end checksideLengthSanity(...)
 
 //REQUIRES GPU MEM ACCESS
private final void setCodes(int diameterInCodes, int diameterInSubtextures, VQTexture tex){
	final int numCodes = diameterInCodes*diameterInCodes;
	for(int i = 0; i < numCodes; i++){
	 final int codeX			= i % diameterInCodes;
	 final int codeY			= i / diameterInCodes;
	 setCodeAt(codeX,codeY,tex);
	 }//end for(numCodes)
	}//end setCodes()
//REQUIRES GPU MEM ACCESS
private final void setCodeAt(int codeX, int codeY, VQTexture tex){
	final int subtextureCodeX = codeX % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
	final int subtextureCodeY = codeY % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
	final int codeIdx         = subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
	tex.setCodeAt(codeX, codeY, (byte)(codeIdx%256));
}//end setCodeAt()
 
private void assemble(VectorList rgba8888vl, VectorList esTuTv8888vl, final int sideLength, final VQTexture tex, boolean generateMipMaps){
    assemble(new VectorListRasterizer(rgba8888vl,new int[]{sideLength,sideLength}), esTuTv8888vl != null? new VectorListRasterizer(esTuTv8888vl, new int[]{sideLength,sideLength}) : null,tex,generateMipMaps);
}

 private final void assemble(VectorListND rgbarvl, VectorListND esTuTvrvl, final VQTexture tex, boolean generateMipMaps){
	    final int sideLength = rgbarvl.getDimensions()[0];
     	    tex.setSideLength(sideLength);
	    final int diameterInCodes 		= (int)Misc.clamp((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH, 1, Integer.MAX_VALUE);
	    final int diameterInSubtextures 	= (int)Math.ceil((double)diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER);
	    final RasterizedBlockVectorList 	rbvlRGBA 		= new RasterizedBlockVectorList(
		    rgbarvl, 4);
	    final VectorListND vlrRGBA = rbvlRGBA;
	    final RasterizedBlockVectorList 	rbvlESTuTv 		= 
		    esTuTvrvl!=null?
		    new RasterizedBlockVectorList(esTuTvrvl, diameterInSubtextures):null;
	    final VectorListND vlrESTuTv = 
		    rbvlESTuTv!=null?
		    rbvlESTuTv:null;
	    // Calculate a rough average color by averaging random samples.
	    tex.setAverageColor(calulateAverageColor(rbvlRGBA));
	    // Get a TOC
	    tex.setTocIndex(tex.getTocWindow().create());
	    if(tex.getTexturePage()>=65536)
		throw new RuntimeException("Texture TOC page out of acceptable range: "+tex.getTexturePage()+"." +
				"\n This is to report anomalies relating to issue #112." +
				"\n ( https://github.com/jtrfp/terminal-recall/issues/112 )");
	    if(tex.getTocWindow().getPhysicalAddressInBytes(tex.getTocIndex()).intValue()%PagedByteBuffer.PAGE_SIZE_BYTES!=0)
		throw new RuntimeException("Physical GPU address not perfectly aligned with page interval."); 		
	    
	    final BlockRegistry blockRegistry = new BlockRegistry(cbm);
	    
	    //threadManager.submitToThreadPool(new Callable<Void>(){
		//@Override
		//public Void call() throws Exception {
		
		//threadManager.submitToGPUMemAccess(new Callable<Void>() {
		    //@Override
		    //public final Void call() {
			tex.setSize(new Point2D.Double(sideLength,sideLength));
			final List<Integer>  subTextureIDs = tex.getSubTextureIDs();
			// Create subtextures
			final int numSubtextures = diameterInSubtextures*diameterInSubtextures;
			for(int i=0; i<numSubtextures; i++){
			    //Create subtexture ID
			    tex.newCodebook256(null,6);
			}//end for(subTextureIDs)
			
			final TextureTOCWindow tocWindow = (TextureTOCWindow)tex.getTocWindow().newContextWindow();
			final SubTextureWindow stWindow = (SubTextureWindow)tex.getSubTextureWindow().newContextWindow();
			final int tocIndex = tex.getTocIndex();
			//Set magic
			tocWindow.magic.set(tocIndex, 1337);
			for(int i=0; i<subTextureIDs.size(); i++){
			 final int id = subTextureIDs.get(i);
			 //Convert subtexture index to index of TOC
			 final int tocSubTexIndex = (i%diameterInSubtextures)+(i/diameterInSubtextures)*TextureTOCWindow.WIDTH_IN_SUBTEXTURES;
			 //Load subtexture ID into TOC
			 tocWindow.subtextureAddrsVec4.setAt(tocIndex, tocSubTexIndex,new VEC4Address(stWindow.getPhysicalAddressInBytes(id)).intValue());
			 //Render Flags
			 tocWindow.renderFlags.set(tocIndex, 
				(tex.isUvWrapping()?0x1:0x0)
				);
			 //Fill the subtexture code start offsets
			 for(int off=0; off<6; off++)
			     stWindow.codeStartOffsetTable.setAt(id, off, tex.getCodebookStartOffsets256().get(i*6+off)*256);
		    }//end for(subTextureIDs)
		// Set the TOC vars
		tocWindow.height	 .set(tocIndex, sideLength);
		tocWindow.width	         .set(tocIndex, sideLength);
		setCodes(diameterInCodes, diameterInSubtextures, tex);
		//Finished. Flush.
		tocWindow.flush();
		stWindow.flush();
		//return null;
	    //}// end call()
	 //}).get();//end gpuMemThread
		
	// Push texels to codebook
	for(int codeY=0; codeY<diameterInCodes; codeY++){
	    for(int codeX=0; codeX<diameterInCodes; codeX++){
		final int subtextureX 	  = codeX / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subtextureY 	  = codeY / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subTextureIdx	  = subtextureX + subtextureY * diameterInSubtextures;
		final int subtextureCodeX = codeX % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subtextureCodeY = codeY % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int codeIdx         = subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int globalCodeIndex = codeIdx%256
			+ tex.getCodebookStartOffsets256().get(subTextureIdx*6+codeIdx/256)*256;
		blockRegistry.setRGBACodebookTexelsAt(vlrRGBA, codeX,codeY,diameterInCodes, globalCodeIndex,tex);
		if(vlrESTuTv!=null)
		    blockRegistry.setESTuTvCodebookTexelsAt(vlrESTuTv, codeX,codeY,diameterInCodes, globalCodeIndex,tex);
		else{
		    final VectorListND blackVLR = generateBlackVectorList(sideLength);
		    blockRegistry.setESTuTvCodebookTexelsAt(blackVLR, codeX,codeY,diameterInCodes, globalCodeIndex,tex);}
		}//end for(codeX)
	}//end for(codeY)
	blockRegistry.flushRGBACodeblock256();
	blockRegistry.flushESTuTvCodeblock256();
	//return null;
	//}//end threadPool call()
	    
	//}).get();// end pool thread
	    if(generateMipMaps){
		if(tex.getMipTextures() == null)
		    tex.setMipTextures(new ArrayList<VQTexture>());
		final List<VQTexture> mipTextures = tex.getMipTextures();
		VectorListND rgba = rgbarvl, esTuTv = esTuTvrvl;
		for(int mipIndex = 0; mipIndex < 2; mipIndex++){
		    rgba   = new CachingVectorListND(new MIPScalingVectorListND(rgba,rgba,esTuTv));
		    if(esTuTv != null)
		        esTuTv = new CachingVectorListND(new MIPScalingVectorListND(esTuTv,rgba,esTuTv));
		    final VQTexture mipTexture = this.newUncompressedVQTexture(rgba, esTuTv, false);
		    mipTextures.add(mipTexture);
		}//end for(mipIndex)
	    }//end if(generateMipMaps)
}//end assemble()
 
 private VectorListND generateBlackVectorList(int sideLength) {
	final VectorList blackVL = new ConstantVectorList(0, sideLength*sideLength, 4);
	return new RasterizedBlockVectorList(
	    new VectorListRasterizer(blackVL, new int[]{sideLength,sideLength}), 4);
	}
 
 private static class BlockRegistry {
     private final VQCodebookManager cbm;
     
     public BlockRegistry(VQCodebookManager codebookManager){
	 this.cbm = codebookManager;
     }
     
     private final Map<Integer,RasterRowWriter[]>rgbaBlock256Map = new HashMap<Integer,RasterRowWriter[]>();
	    private final Map<Integer,RasterRowWriter[]>ESTuTvBlock256Map = new HashMap<Integer,RasterRowWriter[]>();
	    private final void registerRGBAToBlock256(int globalCodeIndex, RasterRowWriter rw){
		RasterRowWriter[] writers = getRGBABlock256(globalCodeIndex);
		writers[globalCodeIndex%256]=rw;
	    }//end registerRGBAToBlock256
	    private final void registerESTuTvToBlock256(int globalCodeIndex, RasterRowWriter rw){
		RasterRowWriter[] writers = getESTuTvBlock256(globalCodeIndex);
		writers[globalCodeIndex%256]=rw;
	    }//end registerRGBAToBlock256
	    private final RasterRowWriter[] getRGBABlock256(int globalCodeIndex){
		final int key = globalCodeIndex/256;
		RasterRowWriter [] writers = rgbaBlock256Map.get(key);
		if(writers==null)
		   rgbaBlock256Map.put(key,writers = new RasterRowWriter[256]);
		return writers;
	    }//end getRGBABlock256(...)
	    private final RasterRowWriter[] getESTuTvBlock256(int globalCodeIndex){
		final int key = globalCodeIndex/256;
		RasterRowWriter [] writers = ESTuTvBlock256Map.get(key);
		if(writers==null)
		   ESTuTvBlock256Map.put(key,writers = new RasterRowWriter[256]);
		return writers;
	    }//end getESTuTvBlock256(...)
	    
	    private final void flushRGBACodeblock256(){
		for(Entry<Integer,RasterRowWriter[]> entry:rgbaBlock256Map.entrySet()){
		    cbm.setRGBABlock256(entry.getKey(),entry.getValue());
		}//end for(entries)
	    }//end flushRGBACodeblock256()
	    private final void flushESTuTvCodeblock256(){
		for(Entry<Integer,RasterRowWriter[]> entry:ESTuTvBlock256Map.entrySet()){
		    cbm.setESTuTvBlock256(entry.getKey(),entry.getValue());
		}//end for(entries)
	    }//end flushRGBACodeblock256()
	    
	    private void setRGBACodebookTexelsAt(final VectorListND vlrRGBA, int codeX, int codeY,
		    int diameterInCodes, int globalCodeIndex, Texture tex) {
		final int coord[] = new int[]{codeX,codeY};
		final RasterRowWriter rw = new RowWriterImpl(vlrRGBA,coord);
		try {
		    registerRGBAToBlock256(globalCodeIndex, rw);
		} catch (ArrayIndexOutOfBoundsException e) {
		    throw new RuntimeException("this="
			    + tex.toString(), e);
		}//end catch(ArrayIndexOutOfBoundsException)
	    }// end setCodebookTexelsAt
	    
	    private void setESTuTvCodebookTexelsAt(final VectorListND vlrESTuTv, int codeX, int codeY,
		    int diameterInCodes, int globalCodeIndex, Texture tex) {
		final int coord[] = new int[]{codeX,codeY};
		final RasterRowWriter rw = new RowWriterImpl(vlrESTuTv,coord);
		try {
		    registerESTuTvToBlock256(globalCodeIndex, rw);
		} catch (ArrayIndexOutOfBoundsException e) {
		    throw new RuntimeException("this="
			    + tex.toString(), e);
		}//end catch(ArrayIndexOutOfBoundsException)
	    }// end setCodebookTexelsAt
 }//end BlockRegistry
 
 static final class RowWriterImpl implements RasterRowWriter{
	private final VectorListND _vlr;
	private final int [] coord;
	public RowWriterImpl(VectorListND vlrRGBA, int [] coord){
	    this._vlr=vlrRGBA;
	    this.coord=coord;
	}
	@Override
	    public void applyRow(int row, ByteBuffer dest) {
		int position = row * 16;
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
		dest.put((byte) (_vlr
			.componentAt(coord, position++) * 255.));
	    }// end applyRow
}//end RasterRowWriter
}//end UncompressedVQTextureFactory
