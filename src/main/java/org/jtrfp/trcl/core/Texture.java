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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.core.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.img.vq.ByteBufferVectorList;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.RGBA8888VectorList;
import org.jtrfp.trcl.img.vq.RasterizedBlockVectorList;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.mem.PagedByteBuffer;

public class Texture implements TextureDescription {
    private final TR 			tr;
    private final GPU 			gpu;
    private final TextureManager 	tm ;
    private final VQCodebookManager 	cbm;
    private final TextureTOCWindow 	toc;
    private final SubTextureWindow	stw;
    private 	  Color 		averageColor;
    private final String 		debugName;
    private	  Integer		tocIndex;
    private	  int[]			subTextureIDs;
    private	  int[][]		codebookStartOffsetsAbsolute;
    private 	  ByteBuffer 		rgba;
    private final boolean		uvWrapping;
    private volatile int		texturePage;
    private int				width;
    @Override
    public void finalize() throws Throwable{
	System.out.println("Texture.finalize() "+debugName);
	//TOC ID
	if(tocIndex!=null)
	    toc.free(tocIndex);
	//Subtexture IDs
	if(subTextureIDs!=null)
	 for(int stID:subTextureIDs)
	    stw.free(stID);
	//Codebook entries
	if(codebookStartOffsetsAbsolute!=null)
	 for(int [] array:codebookStartOffsetsAbsolute){
	    for(int entry:array){
		tm.vqCodebookManager.get().freeCodebook256(entry/256);
	    }//end for(entries)
	 }//end for(arrays)
	super.finalize();
    }//end finalize()
    
    Texture(Color c, TR tr){
	this(new PalettedVectorList(colorZeroRasterVL(), colorVL(c)),"SolidColor r="+c.getRed()+" g="+c.getGreen()+" b="+c.getBlue(),tr,false);
    }//end constructor
    
    private static VectorList colorZeroRasterVL(){
	return new VectorList(){
	    @Override
	    public int getNumVectors() {
		return 16;
	    }

	    @Override
	    public int getNumComponentsPerVector() {
		return 1;
	    }

	    @Override
	    public double componentAt(int vectorIndex, int componentIndex) {
		return 0;
	    }

	    @Override
	    public void setComponentAt(int vectorIndex, int componentIndex,
		    double value) {
		throw new RuntimeException("Cannot write to Texture.colorZeroRasterVL VectorList");
	    }};
    }//end colorZeroRasterVL
    
    private static VectorList colorVL(Color c){
	final double [] color = new double[]{
		c.getRed()/255.,c.getGreen()/255.,c.getBlue()/255.,1.};
	
	return  new VectorList(){
	    @Override
	    public int getNumVectors() {
		return 1;
	    }

	    @Override
	    public int getNumComponentsPerVector() {
		return 4;
	    }

	    @Override
	    public double componentAt(int vectorIndex, int componentIndex) {
		return color[componentIndex];
	    }

	    @Override
	    public void setComponentAt(int vectorIndex, int componentIndex,
		    double value) {
		throw new RuntimeException("Static palette created by Texture(Color c, TR tr) cannot be written to.");
	    }};
    }//end colorVL(...)
    
    private Texture(TR tr, String debugName, boolean uvWrapping){
	this.tr=tr;
	this.gpu	=tr.gpu.get();
	this.tm		=gpu.textureManager.get();
	this.cbm	=tm.vqCodebookManager.get();
	this.toc	=tm.getTOCWindow();
	this.stw	=tm.getSubTextureWindow();
	this.debugName	=debugName.replace('.', '_');
	this.uvWrapping =uvWrapping;
    }//end constructor

    private Texture(Texture parent, double uOff, double vOff, double uSize,
	    double vSize, TR tr, boolean uvWrapping) {
	this(tr,"subtexture: "+parent.debugName,uvWrapping);
    }//end constructor
    
    public Texture subTexture(double uOff, double vOff, double uSize,
	    double vSize){
	return new Texture(this,uOff,vOff,uSize,vSize,tr,false);
    }
    
    Texture(PalettedVectorList vl, String debugName, TR tr, boolean uvWrapping){
	this(tr,debugName,uvWrapping);
	vqCompress(vl);
    }//end constructor

    Texture(ByteBuffer imageRGBA8888, String debugName, TR tr, boolean uvWrapping) {
	this(tr,debugName,uvWrapping);
	if (imageRGBA8888.capacity() == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create texture of zero size.");
	}//end if capacity==0
	imageRGBA8888.clear();//Doesn't erase, just resets the tracking vars
	vqCompress(imageRGBA8888);
    }// end constructor
    
    private void vqCompress(PalettedVectorList squareImageIndexed){
	final double	fuzzySideLength = Math.sqrt(squareImageIndexed.getNumVectors());
	final int 	sideLength	= (int)Math.floor(fuzzySideLength);
	if(!SpecialRAWDimensions.isPowerOfTwo(sideLength))
	    System.err.println("WARNING: Calculated dimensions are not power-of-two. Trouble ahead.");
	if(Math.abs(fuzzySideLength-sideLength)>.001)
	    System.err.println("WARNING: Calculated dimensions are not perfectly square. Trouble ahead.");
	vqCompress(squareImageIndexed,sideLength);
    }
    
    private void vqCompress(ByteBuffer imageRGBA8888){
	final double	fuzzySideLength = Math.sqrt((imageRGBA8888.capacity() / 4));
	final int 	sideLength	= (int)Math.floor(fuzzySideLength);
	if(!SpecialRAWDimensions.isPowerOfTwo(sideLength))
	    System.err.println("WARNING: Calculated dimensions are not power-of-two. Trouble ahead.");
	if(Math.abs(fuzzySideLength-sideLength)>.001)
	    System.err.println("WARNING: Calculated dimensions are not perfectly square. Trouble ahead.");
	 // Break down into 4x4 blocks
	 final ByteBufferVectorList 	bbvl 		= new ByteBufferVectorList(imageRGBA8888);
	 final RGBA8888VectorList 	rgba8888vl 	= new RGBA8888VectorList(bbvl);
	 vqCompress(rgba8888vl,sideLength);
    }
    
    private void vqCompress(VectorList rgba8888vl, final int sideLength){
	    width=sideLength;
	    final RasterizedBlockVectorList 	rbvl 		= new RasterizedBlockVectorList(
		    rgba8888vl, sideLength, 4);
	    // Calculate a rough average color by averaging random samples.
	    float redA=0,greenA=0,blueA=0;
	    final double size = rbvl.getNumVectors();
	    for(int i=0; i<10; i++){
		redA+=rbvl.componentAt((int)(Math.random()*size), 0);
		greenA+=rbvl.componentAt((int)(Math.random()*size), 1);
		blueA+=rbvl.componentAt((int)(Math.random()*size), 2);
	    }averageColor = new Color(redA/10f,greenA/10f,blueA/10f);
	    // Get a TOC
	    tocIndex = toc.create();
	    setTexturePage((toc.getPhysicalAddressInBytes(tocIndex)/PagedByteBuffer.PAGE_SIZE_BYTES));
	    if(toc.getPhysicalAddressInBytes(tocIndex)%PagedByteBuffer.PAGE_SIZE_BYTES!=0)
		throw new RuntimeException("Nonzero modulus."); 		
	    
	    tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		// Create subtextures
		final int diameterInCodes 		= (int)Misc.clamp((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH, 1, Integer.MAX_VALUE);
		final int diameterInSubtextures 	= (int)Math.ceil((double)diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES);
		subTextureIDs 				= new int[diameterInSubtextures*diameterInSubtextures];
		codebookStartOffsetsAbsolute		= new int[diameterInSubtextures*diameterInSubtextures][6];
		for(int i=0; i<subTextureIDs.length; i++){
		    //Create subtexture ID
		    subTextureIDs[i]=stw.create();
		    for(int off=0; off<6; off++){
			codebookStartOffsetsAbsolute[i][off] =  tm.vqCodebookManager.get()
			    .newCodebook256() * 256;}
		}//end for(subTextureIDs)
		tr.getThreadManager().submitToGPUMemAccess(new Callable<Void>() {
		    @Override
		    public Void call() {
			//Set magic
			toc.magic.set(tocIndex, 1337);
			for(int i=0; i<subTextureIDs.length; i++){
			 final int id = subTextureIDs[i];
			 //Convert subtexture index to index of TOC
			 final int tocSubTexIndex = (i%diameterInSubtextures)+(i/diameterInSubtextures)*TextureTOCWindow.WIDTH_IN_SUBTEXTURES;
			 //Load subtexture ID into TOC
			 toc.subtextureAddrsVec4.setAt(tocIndex, tocSubTexIndex,stw.getPhysicalAddressInBytes(id)/GPU.BYTES_PER_VEC4);
			 //Render Flags
			 toc.renderFlags.set(tocIndex, 
				(uvWrapping?0x1:0x0)
				);
			 //Fill the subtexture code start offsets
			 for(int off=0; off<6; off++)
			    stw.codeStartOffsetTable.setAt(id, off, codebookStartOffsetsAbsolute[i][off]);
		    }//end for(subTextureIDs)
		// Set the TOC vars
		toc.height	 .set(tocIndex, sideLength);
		toc.width	 .set(tocIndex, sideLength);
		final int numCodes = diameterInCodes*diameterInCodes;
		for(int i = 0; i < numCodes; i++){
		    final int codeX			= i % diameterInCodes;
		    final int codeY			= i / diameterInCodes;
		    final int subtextureX 		= codeX / SubTextureWindow.SIDE_LENGTH_CODES;
		    final int subtextureY 		= codeY / SubTextureWindow.SIDE_LENGTH_CODES;
		    final int subtextureCodeX 		= codeX % SubTextureWindow.SIDE_LENGTH_CODES;
		    final int subtextureCodeY 		= codeY % SubTextureWindow.SIDE_LENGTH_CODES;
		    final int codeIdx			= subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES;
		    final int subTextureIdx		= subtextureX + subtextureY * diameterInSubtextures;
		    final int subtextureID		= subTextureIDs[subTextureIdx];
		    stw.codeIDs.setAt(subtextureID, codeIdx, (byte)(codeIdx%256));
		}//end for(numCodes)
		return null;
	    }// end run()
	 }).get();//end gpuMemThread
	// Push codes to subtextures
	for(int codeY=0; codeY<diameterInCodes; codeY++){
	    for(int codeX=0; codeX<diameterInCodes; codeX++){
		final int subtextureX 		= codeX / SubTextureWindow.SIDE_LENGTH_CODES;
		final int subtextureY 		= codeY / SubTextureWindow.SIDE_LENGTH_CODES;
		final int subTextureIdx		= subtextureX + subtextureY * diameterInSubtextures;
		final int subtextureCodeX 	= codeX % SubTextureWindow.SIDE_LENGTH_CODES;
		final int subtextureCodeY 	= codeY % SubTextureWindow.SIDE_LENGTH_CODES;
		final int codeIdx		= subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES;
		final int globalCodeIndex 	= codeIdx%256
			+ codebookStartOffsetsAbsolute[subTextureIdx][codeIdx/256];
		final int blockPosition 	= codeX+codeY*diameterInCodes;
		final RasterRowWriter rw = new RasterRowWriter(){
		    @Override
		    public void applyRow(int row, ByteBuffer dest) {
			int position = row*16;
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
			dest.put((byte) (rbvl
				.componentAt(blockPosition, position++) * 255.));
		    }//end applyRow
		};
		try{cbm.setRGBA(globalCodeIndex, rw);}
		catch(ArrayIndexOutOfBoundsException e){
		    throw new RuntimeException("this="+Texture.this.toString(),e);
		}
		}//end for(codeX)
	}//end for(codeY)
	return null;
	}}).get();//end pool thread
    }//end vqCompress(...)

    Texture(BufferedImage img, String debugName, TR tr, boolean uvWrapping) {
	this(tr,debugName,uvWrapping);
	    long redA = 0, greenA = 0, blueA = 0;
	    rgba = ByteBuffer.allocateDirect(img.getWidth() * img.getHeight()
		    * 4);
	    for (int y = 0; y < img.getHeight(); y++) {
		for (int x = 0; x < img.getWidth(); x++) {
		    Color c = new Color(img.getRGB(x, y), true);
		    rgba.put((byte) c.getRed());
		    rgba.put((byte) c.getGreen());
		    rgba.put((byte) c.getBlue());
		    rgba.put((byte) c.getAlpha());
		    redA += c.getRed();
		    greenA += c.getGreen();
		    blueA += c.getBlue();
		}// end for(x)
	    }// end for(y)
	    
	    final int div = rgba.capacity() / 4;
	    averageColor = new Color((redA / div) / 255f,
		    (greenA / div) / 255f, (blueA / div) / 255f);
	if(tr.getTrConfig().isUsingNewTexturing()){
	    vqCompress(rgba);
	}//else{registerNode(newNode);}
    }//end constructor
    
    public static ByteBuffer RGBA8FromPNG(File f) {
	try {
	    return RGBA8FromPNG(new FileInputStream(f));
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public static ByteBuffer RGBA8FromPNG(InputStream is) {
	try {
	    BufferedImage bi = ImageIO.read(is);
	    return RGBA8FromPNG(bi, 0, 0, bi.getWidth(), bi.getHeight());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }//end RGBA8FromPNG(...)

    public static ByteBuffer RGBA8FromPNG(BufferedImage image, int startX,
	    int startY, int sizeX, int sizeY) {
	int color;
	ByteBuffer buf = ByteBuffer.allocateDirect(image.getWidth()
		* image.getHeight() * 4);
	for (int y = startY; y < startY + sizeY; y++) {
	    for (int x = startX; x < startX + sizeX; x++) {
		color = image.getRGB(x, y);
		buf.put((byte) ((color & 0x00FF0000) >> 16));
		buf.put((byte) ((color & 0x0000FF00) >> 8));
		buf.put((byte) (color & 0x000000FF));
		buf.put((byte) ((color & 0xFF000000) >> 24));
	    }// end for(x)
	}// end for(y)
	buf.clear();// Rewind
	return buf;
    }// end RGB8FromPNG(...)
    
    public static final Color[] GREYSCALE;
    static {
	GREYSCALE = new Color[256];
	for (int i = 0; i < 256; i++) {
	    GREYSCALE[i] = new Color(i, i, i);
	}
    }// end static{}

    public static ByteBuffer fragmentRGBA(ByteBuffer input, int quadDepth,
	    int x, int y) {
	final int originalSideLen = (int) Math.sqrt(input.capacity() / 4);
	final int splitAmount = (int) Math.pow(2, quadDepth);
	final int newSideLen = originalSideLen / splitAmount;
	ByteBuffer result = ByteBuffer.allocateDirect((int) (Math.pow(
		newSideLen, 2) * 4));
	for (int row = y * newSideLen; row < (y + 1) * newSideLen; row++) {
	    input.clear();
	    input.limit((x + 1) * newSideLen * 4 + row * originalSideLen * 4);
	    input.position(x * newSideLen * 4 + row * originalSideLen * 4);
	    result.put(input);
	}
	return result;
    }// end fragmentRGBA(...)

    public static ByteBuffer indexed2RGBA8888(ByteBuffer indexedPixels,
	    Color[] palette) {
	Color color;
	ByteBuffer buf = ByteBuffer.allocate(indexedPixels.capacity() * 4);
	final int cap = indexedPixels.capacity();
	for (int i = 0; i < cap; i++) {
	    color = palette[(indexedPixels.get() & 0xFF)];
	    buf.put((byte) color.getRed());
	    buf.put((byte) color.getGreen());
	    buf.put((byte) color.getBlue());
	    buf.put((byte) color.getAlpha());
	}// end for(i)
	buf.clear();// Rewind
	return buf;
    }// end indexed2RGBA8888(...)

    public static ByteBuffer[] indexed2RGBA8888(ByteBuffer[] indexedPixels,
	    Color[] palette) {
	final int len = indexedPixels.length;
	ByteBuffer[] result = new ByteBuffer[len];
	for (int i = 0; i < len; i++) {
	    result[i] = indexed2RGBA8888(indexedPixels[i], palette);
	}
	return result;
    }// end indexed2RGBA8888(...)

    /**
     * @return the uvWrapping
     */
    public boolean isUvWrapping() {
        return uvWrapping;
    }

    /**
     * @return the texturePage
     */
    public int getTexturePage() {
        return texturePage;
    }

    /**
     * @param texturePage the texturePage to set
     */
    public void setTexturePage(int texturePage) {
        this.texturePage = texturePage;
    }

    @Override
    public Color getAverageColor() {
	return averageColor;
    }
    
    public static final int createTextureID(GL3 gl) {
	IntBuffer ib = IntBuffer.allocate(1);
	gl.glGenTextures(1, ib);
	ib.clear();
	return ib.get();
    }//end createTextureID
    
    @Override
    public String toString(){
	return "Texture debugName="+debugName+" width="+width;
    }
}// end Texture
