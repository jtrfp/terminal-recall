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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.SpecialRAWDimensions;
import org.jtrfp.trcl.TextureBehavior;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.TriangleList;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.ext.tr.GPUResourceFinalizer;
import org.jtrfp.trcl.gpu.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.img.vq.BufferedImageRGBA8888VL;
import org.jtrfp.trcl.img.vq.ByteBufferVectorList;
import org.jtrfp.trcl.img.vq.ConstantVectorList;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.RGBA8888VectorList;
import org.jtrfp.trcl.img.vq.RasterizedBlockVectorList;
import org.jtrfp.trcl.img.vq.SubtextureVL;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.img.vq.VectorListND;
import org.jtrfp.trcl.img.vq.VectorListRasterizer;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;

public class VQTexture implements TextureDescription {
    private final ThreadManager         threadManager;
    private final TextureManager 	tm ;
    private final VQCodebookManager 	cbm;
    private final TextureTOCWindow 	toc;
    private final SubTextureWindow	stw;
    private 	  Color 		averageColor;
    private final String 		debugName;
    private	  Integer		tocIndex;
    private	  ArrayList<Integer>	subTextureIDs = new ArrayList<Integer>();
    private	  ArrayList<Integer>	codebookStartOffsets256 = new ArrayList<Integer>();
    private final boolean		uvWrapping;
    private volatile int		texturePage;
    private int				sideLength;
    private TextureBehavior.Support	tbs = new TextureBehavior.Support();
    private final GPU                   gpu;
    private final GPUResourceFinalizer  gpuResourceFinalizer;
    
    public VQTexture(GPU gpu, ThreadManager threadManager, Color c){
	this(gpu,threadManager,new PalettedVectorList(colorZeroRasterVL(), colorVL(c)),null,"SolidColor r="+c.getRed()+" g="+c.getGreen()+" b="+c.getBlue(),false);
    }//end constructor
    
    @Override
    public void finalize() throws Throwable{
	//Undo the magic
	    toc.magic.set(tocIndex, 0000);
	    //TOC ID
	    if(tocIndex!=null)
		toc.freeLater(tocIndex);
	    stw.freeLater(subTextureIDs);
	    //Codebook entries
	    tm.vqCodebookManager.get().freeCodebook256(codebookStartOffsets256);
	super.finalize();
    }//end finalize()
    
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
		c.getRed()/255.,c.getGreen()/255.,c.getBlue()/255.,c.getAlpha()/255.};
	
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
    
    private VQTexture(GPU gpu, ThreadManager threadManager, String debugName, boolean uvWrapping){
	this.threadManager=threadManager;
	this.tm		  =gpu.textureManager.get();
	this.cbm	  =tm.vqCodebookManager.get();
	this.toc	  =tm.getTOCWindow();
	this.stw	  =tm.getSubTextureWindow();
	this.debugName	  =debugName.replace('.', '_');
	this.uvWrapping   =uvWrapping;
	this.gpu          =gpu;
	this.gpuResourceFinalizer = gpu.getGPUResourceFinalizer();
    }//end constructor
    
    public VQTexture(GPU gpu, ThreadManager threadManager, PalettedVectorList vlRGBA, PalettedVectorList vlESTuTv, String debugName, boolean uvWrapping){
	this(gpu,threadManager,debugName,uvWrapping);
	assemble(vlRGBA,vlESTuTv);
    }//end constructor

    VQTexture(GPU gpu, ThreadManager threadManager, ByteBuffer imageRGBA8888, ByteBuffer imageESTuTv8888, String debugName, boolean uvWrapping) {
	this(gpu,threadManager,debugName,uvWrapping);
	if (imageRGBA8888.capacity() == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create texture of zero size.");
	}//end if capacity==0
	imageRGBA8888.clear();//Doesn't erase, just resets the tracking vars
	assemble(imageRGBA8888,imageESTuTv8888);
    }// end constructor
    
    private void assemble(PalettedVectorList squareImageIndexedRGBA,PalettedVectorList squareImageIndexedESTuTv){
	final double	fuzzySideLength = Math.sqrt(squareImageIndexedRGBA.getNumVectors());
	final int 	sideLength	= (int)Math.floor(fuzzySideLength);
	if(!SpecialRAWDimensions.isPowerOfTwo(sideLength))
	    System.err.println("WARNING: Calculated dimensions are not power-of-two. Trouble ahead.");
	if(Math.abs(fuzzySideLength-sideLength)>.001)
	    System.err.println("WARNING: Calculated dimensions are not perfectly square. Trouble ahead.");
	assemble(squareImageIndexedRGBA, squareImageIndexedESTuTv,sideLength);
    }
    
    private void assemble(ByteBuffer imageRGBA8888, ByteBuffer imageESTuTv8888){
	final double	fuzzySideLength = Math.sqrt((imageRGBA8888.capacity() / 4));
	final int 	sideLength	= (int)Math.floor(fuzzySideLength);
	if(!SpecialRAWDimensions.isPowerOfTwo(sideLength))
	    System.err.println("WARNING: Calculated dimensions are not power-of-two. Trouble ahead.");
	if(Math.abs(fuzzySideLength-sideLength)>.001)
	    System.err.println("WARNING: Calculated dimensions are not perfectly square. Trouble ahead.");
	 // Break down into 4x4 blocks
	 final ByteBufferVectorList 	bbvl 		= new ByteBufferVectorList(imageRGBA8888);
	 final RGBA8888VectorList 	rgba8888vl 	= new RGBA8888VectorList(bbvl);
	 
	 final VectorList	 	bbvlESTuTv 	= imageESTuTv8888!=null?new ByteBufferVectorList(imageESTuTv8888):new ConstantVectorList(0,bbvl);
	 final RGBA8888VectorList 	esTuTv8888vl 	= bbvlESTuTv!=null?new RGBA8888VectorList(bbvlESTuTv):null;
	 assemble(rgba8888vl,esTuTv8888vl,sideLength);
    }
    
    private final void assemble(VectorList rgba8888vl, VectorList esTuTv8888vl, final int sideLength){
	    this.sideLength=sideLength;
	    final int diameterInCodes 		= (int)Misc.clamp((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH, 1, Integer.MAX_VALUE);
	    final int diameterInSubtextures 	= (int)Math.ceil((double)diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER);
	    final RasterizedBlockVectorList 	rbvlRGBA 		= new RasterizedBlockVectorList(
		    new VectorListRasterizer(rgba8888vl,new int[]{sideLength,sideLength}), 4);
	    final VectorListND vlrRGBA = rbvlRGBA;
	    final RasterizedBlockVectorList 	rbvlESTuTv 		= 
		    esTuTv8888vl!=null?
		    new RasterizedBlockVectorList(
		    new VectorListRasterizer(esTuTv8888vl, new int[]{sideLength,sideLength}), 4):null;
	    final VectorListND vlrESTuTv = 
		    rbvlESTuTv!=null?
		    rbvlESTuTv:null;
	    // Calculate a rough average color by averaging random samples.
	    calulateAverageColor(rbvlRGBA);
	    // Get a TOC
	    tocIndex = toc.create();
	    setTexturePage((toc.getPhysicalAddressInBytes(tocIndex).intValue()/PagedByteBuffer.PAGE_SIZE_BYTES));
	    if(getTexturePage()>=65536)
		throw new RuntimeException("Texture TOC page out of acceptable range: "+getTexturePage()+"." +
				"\n This is to report anomalies relating to issue #112." +
				"\n ( https://github.com/jtrfp/terminal-recall/issues/112 )");
	    if(toc.getPhysicalAddressInBytes(tocIndex).intValue()%PagedByteBuffer.PAGE_SIZE_BYTES!=0)
		throw new RuntimeException("Physical GPU address not perfectly aligned with page interval."); 		
	    
	    threadManager.submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		
		threadManager.submitToGPUMemAccess(new Callable<Void>() {
		    @Override
		    public final Void call() {
			// Create subtextures
			for(int i=0; i<diameterInSubtextures*diameterInSubtextures; i++){
			    //Create subtexture ID
			    subTextureIDs.add(stw.create());
			    tm.vqCodebookManager.get().newCodebook256(codebookStartOffsets256, 6);
			}//end for(subTextureIDs)
			//Set magic
			toc.magic.set(tocIndex, 1337);
			for(int i=0; i<subTextureIDs.size(); i++){
			 final int id = subTextureIDs.get(i);
			 //Convert subtexture index to index of TOC
			 final int tocSubTexIndex = (i%diameterInSubtextures)+(i/diameterInSubtextures)*TextureTOCWindow.WIDTH_IN_SUBTEXTURES;
			 //Load subtexture ID into TOC
			 toc.subtextureAddrsVec4.setAt(tocIndex, tocSubTexIndex,new VEC4Address(stw.getPhysicalAddressInBytes(id)).intValue());
			 //Render Flags
			 toc.renderFlags.set(tocIndex, 
				(uvWrapping?0x1:0x0)
				);
			 //Fill the subtexture code start offsets
			 for(int off=0; off<6; off++)
			    stw.codeStartOffsetTable.setAt(id, off, codebookStartOffsets256.get(i*6+off)*256);
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
		final int subtextureID    = subTextureIDs.get(subTextureIdx);
		new SubtextureVL(stw, subtextureID).setComponentAt(codeIdx, 0, (byte)(codeIdx%256));//TODO: Could make a lot of garbage.
            }//end setCodeAt()
	 }).get();//end gpuMemThread
		
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
			+ codebookStartOffsets256.get(subTextureIdx*6+codeIdx/256)*256;
		setRGBACodebookTexelsAt(vlrRGBA, codeX,codeY,diameterInCodes, globalCodeIndex);
		if(vlrESTuTv!=null)
		 setESTuTvCodebookTexelsAt(vlrESTuTv, codeX,codeY,diameterInCodes, globalCodeIndex);
		else{
		    final VectorListND blackVLR = generateBlackVectorList(sideLength);
		 setESTuTvCodebookTexelsAt(blackVLR, codeX,codeY,diameterInCodes, globalCodeIndex);}
		}//end for(codeX)
	}//end for(codeY)
	flushRGBACodeblock256();
	flushESTuTvCodeblock256();
	return null;
	}//end threadPool call()
	    private VectorListND generateBlackVectorList(int sideLength) {
		final VectorList blackVL = new ConstantVectorList(0, sideLength*sideLength, 4);
		return new RasterizedBlockVectorList(
		    new VectorListRasterizer(blackVL, new int[]{sideLength,sideLength}), 4);
		}
	    private void setRGBACodebookTexelsAt(final VectorListND vlrRGBA, int codeX, int codeY,
		    int diameterInCodes, int globalCodeIndex) {
		final int coord[] = new int[]{codeX,codeY};
		final RasterRowWriter rw = new RowWriterImpl(vlrRGBA,coord);
		try {
		    registerRGBAToBlock256(globalCodeIndex, rw);
		} catch (ArrayIndexOutOfBoundsException e) {
		    throw new RuntimeException("this="
			    + VQTexture.this.toString(), e);
		}//end catch(ArrayIndexOutOfBoundsException)
	    }// end setCodebookTexelsAt
	    
	    private void setESTuTvCodebookTexelsAt(final VectorListND vlrESTuTv, int codeX, int codeY,
		    int diameterInCodes, int globalCodeIndex) {
		final int coord[] = new int[]{codeX,codeY};
		final RasterRowWriter rw = new RowWriterImpl(vlrESTuTv,coord);
		try {
		    registerESTuTvToBlock256(globalCodeIndex, rw);
		} catch (ArrayIndexOutOfBoundsException e) {
		    throw new RuntimeException("this="
			    + VQTexture.this.toString(), e);
		}//end catch(ArrayIndexOutOfBoundsException)
	    }// end setCodebookTexelsAt
	    
	    final class RowWriterImpl implements RasterRowWriter{
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
	}).get();// end pool thread
    }//end vqCompress(...)

    private void calulateAverageColor(RasterizedBlockVectorList rbvl) {
	float redA=0,greenA=0,blueA=0;
	    final int [] dims = rbvl.getDimensions();
	    for(int i=0; i<10; i++){
		redA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,0);
		greenA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,1);
		blueA+=rbvl.componentAt(new int[]{(int)(Math.random()*dims[0]),(int)(Math.random()*dims[1])} ,2);
	    }averageColor = new Color(redA/10f,greenA/10f,blueA/10f);
    }//end calculateAverageColor(...)

    public VQTexture(GPU gpu, ThreadManager threadManager, BufferedImage imgRGBA, BufferedImage imgESTuTv, String debugName, boolean uvWrapping) {
	this(gpu,threadManager,debugName,uvWrapping);
	try{
	    assemble(new BufferedImageRGBA8888VL(imgRGBA),
		    imgESTuTv!=null?
			    new BufferedImageRGBA8888VL(imgESTuTv):
			    null,imgRGBA.getWidth());
	    }catch(Exception e){e.printStackTrace();}
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

    /**
     * @param beh
     * @see org.jtrfp.trcl.TextureBehavior.Support#addBehavior(org.jtrfp.trcl.TextureBehavior)
     */
    public void addBehavior(TextureBehavior beh) {
	tbs.addBehavior(beh);
    }

    /**
     * @param beh
     * @see org.jtrfp.trcl.TextureBehavior.Support#removeBehavior(org.jtrfp.trcl.TextureBehavior)
     */
    public void removeBehavior(TextureBehavior beh) {
	tbs.removeBehavior(beh);
    }

    /**
     * @param triangleList
     * @param gpuTVIndex
     * @param numFrames
     * @param thisTriangle
     * @param pos
     * @param vw
     * @see org.jtrfp.trcl.TextureBehavior.Support#apply(org.jtrfp.trcl.TriangleList, int, int, org.jtrfp.trcl.Triangle, org.apache.commons.math3.geometry.euclidean.threed.Vector3D, org.jtrfp.trcl.core.TriangleVertexWindow)
     */
    public void apply(TriangleList triangleList, int gpuTVIndex, int numFrames,
	    Triangle thisTriangle, Vector3D pos, TriangleVertexWindow vw) {
	tbs.apply(triangleList, gpuTVIndex, numFrames, thisTriangle, pos, vw);
    }
}// end Texture
