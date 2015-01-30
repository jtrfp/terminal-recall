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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.core.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.img.vq.BufferedImageRGBA8888VL;
import org.jtrfp.trcl.img.vq.ByteBufferVectorList;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.RGBA8888VectorList;
import org.jtrfp.trcl.img.vq.RasterizedBlockVectorList;
import org.jtrfp.trcl.img.vq.SubtextureVL;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.img.vq.VectorListRasterizer;
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
    private final boolean		uvWrapping;
    private volatile int		texturePage;
    private int				sideLength;
    @Override
    public void finalize() throws Throwable{
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
    
    private final void vqCompress(VectorList rgba8888vl, final int sideLength){
	    this.sideLength=sideLength;
	    final int diameterInCodes 		= (int)Misc.clamp((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH, 1, Integer.MAX_VALUE);
	    final int diameterInSubtextures 	= (int)Math.ceil((double)diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER);
	    final RasterizedBlockVectorList 	rbvl 		= new RasterizedBlockVectorList(
		    rgba8888vl, sideLength, 4);
	    final VectorListRasterizer vlr = new VectorListRasterizer(rbvl, new int [] {diameterInCodes,diameterInCodes});
	    // Calculate a rough average color by averaging random samples.
	    calulateAverageColor(rbvl);
	    // Get a TOC
	    tocIndex = toc.create();
	    setTexturePage((toc.getPhysicalAddressInBytes(tocIndex)/PagedByteBuffer.PAGE_SIZE_BYTES));
	    if(toc.getPhysicalAddressInBytes(tocIndex)%PagedByteBuffer.PAGE_SIZE_BYTES!=0)
		throw new RuntimeException("Physical GPU address not perfectly aligned with page interval."); 		
	    
	    tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		// Create subtextures
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
		    public final Void call() {
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
		setCodes(diameterInCodes, diameterInSubtextures);
		return null;
	    }// end run()
	
	    //REQUIRES GPU MEM ACCESS
            private final void setCodes(int diameterInCodes, int diameterInSubtextures){
		final int numCodes = diameterInCodes*diameterInCodes;
		for(int i = 0; i < numCodes; i++){
		 final int codeX			= i % diameterInCodes;
		 final int codeY			= i / diameterInCodes;
		 setCodeAt(codeX,codeY);
		 }//end for(numCodes)
		}//end setCodes()
            //REQUIRES GPU MEM ACCESS
            private final void setCodeAt(int codeX, int codeY){
        	final int subtextureX     = codeX / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subtextureY     = codeY / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subtextureCodeX = codeX % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subtextureCodeY = codeY % SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int codeIdx         = subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		final int subTextureIdx   = subtextureX + subtextureY * diameterInSubtextures;
		final int subtextureID    = subTextureIDs[subTextureIdx];
		new SubtextureVL(stw, subtextureID).setComponentAt(codeIdx, 0, (byte)(codeIdx%256));//TODO: Could make a lot of garbage.
            }//end setCodeAt()
	 }).get();//end gpuMemThread
	/*
	for(int subtextureY=0; subtextureY<diameterInSubtextures; subtextureY++){
	    for(int subtextureX=0; subtextureX<diameterInSubtextures; subtextureX++){
		final int subTextureIdx	  = subtextureX + subtextureY * diameterInSubtextures;
		for(int block256=0; block256<6; block256++){
		    final int codeStartY=subtextureY*SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		    final int codeStartX=subtextureX*SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
		    
		}
	    }//end for(subtextureX)
	}//end for(subtextureY)
	*/
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
			+ codebookStartOffsetsAbsolute[subTextureIdx][codeIdx/256];
		setCodebookTexelsAt(codeX,codeY,diameterInCodes, globalCodeIndex);
		}//end for(codeX)
	}//end for(codeY)
	flushCodeblock256();
	return null;
	}//end threadPool call()
	    private void setCodebookTexelsAt(int codeX, int codeY,
		    int diameterInCodes, int globalCodeIndex) {
		final int coord[] = new int[]{codeX,codeY};
		final RasterRowWriter rw = new RasterRowWriter() {
		    @Override
		    public void applyRow(int row, ByteBuffer dest) {
			int position = row * 16;
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
			dest.put((byte) (vlr
				.componentAt(coord, position++) * 255.));
		    }// end applyRow
		};
		try {
		    registerRGBAToBlock256(globalCodeIndex, rw);
		} catch (ArrayIndexOutOfBoundsException e) {
		    throw new RuntimeException("this="
			    + Texture.this.toString(), e);
		}//end catch(ArrayIndexOutOfBoundsException)
	    }// end setCodebookTexelsAt
	    private final Map<Integer,RasterRowWriter[]>block256Map = new HashMap<Integer,RasterRowWriter[]>();
	    private final void registerRGBAToBlock256(int globalCodeIndex, RasterRowWriter rw){
		RasterRowWriter[] writers = getBlock256(globalCodeIndex);
		writers[globalCodeIndex%256]=rw;
	    }//end registerRGBAToBlock256
	    private final RasterRowWriter[] getBlock256(int globalCodeIndex){
		final int key = globalCodeIndex/256;
		RasterRowWriter [] writers = block256Map.get(key);
		if(writers==null)
		   block256Map.put(key,writers = new RasterRowWriter[256]);
		return writers;
	    }//end getBlock256(...)
	    private final void flushCodeblock256(){
		for(Entry<Integer,RasterRowWriter[]> entry:block256Map.entrySet()){
		    cbm.setRGBABlock256(entry.getKey(),entry.getValue());
		}//end for(entries)
	    }//end flushCodeblock256()
	});// end pool thread
    }//end vqCompress(...)

    private void calulateAverageColor(RasterizedBlockVectorList rbvl) {
	float redA=0,greenA=0,blueA=0;
	    final double size = rbvl.getNumVectors();
	    for(int i=0; i<10; i++){
		redA+=rbvl.componentAt((int)(Math.random()*size), 0);
		greenA+=rbvl.componentAt((int)(Math.random()*size), 1);
		blueA+=rbvl.componentAt((int)(Math.random()*size), 2);
	    }averageColor = new Color(redA/10f,greenA/10f,blueA/10f);
    }//end calculateAverageColor(...)

    Texture(BufferedImage image, String debugName, TR tr, boolean uvWrapping) {
	this(tr,debugName,uvWrapping);
	    /*
	    rgba = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight()
		    * 4);
	    final int [] row = new int[image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++) {
		    image.getRGB(0, y, image.getWidth(), 1, row, 0, image.getWidth());
		    for (int color:row) {
			rgba.put((byte) ((color & 0x00FF0000) >> 16));
			rgba.put((byte) ((color & 0x0000FF00) >> 8));
			rgba.put((byte) (color & 0x000000FF));
			rgba.put((byte) ((color & 0xFF000000) >> 24));
		    }// end for(x)
		}// end for(y)
	    final int div = rgba.capacity() / 4;
	    
	    //TODO: This doesn't do anything but output black. Is it even used?
	    averageColor = new Color((redA / div) / 255f,
		    (greenA / div) / 255f, (blueA / div) / 255f);
	    */
	    //vqCompress(rgba);
	    try{vqCompress(new BufferedImageRGBA8888VL(image),image.getWidth());}catch(Exception e){e.printStackTrace();}
    }//end constructor

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
	//int color;
	ByteBuffer buf = ByteBuffer.allocateDirect(image.getWidth()
		* image.getHeight() * 4);
	final int [] row = new int[image.getWidth()];
	for (int y = startY; y < startY + sizeY; y++) {
	    image.getRGB(0, y, image.getWidth(), 1, row, 0, image.getWidth());
	    for (int color:row) {
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
	ByteBuffer buf = ByteBuffer.allocateDirect(indexedPixels.capacity() * 4);
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
	return "Texture debugName="+debugName+" width="+sideLength;
    }
    
    public int getSideLength(){
	return sideLength;
    }
}// end Texture
