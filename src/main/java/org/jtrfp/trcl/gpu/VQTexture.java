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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.TextureBehavior;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.TriangleList;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.img.vq.CompositeVectorListND;
import org.jtrfp.trcl.img.vq.SubtextureVL;
import org.jtrfp.trcl.img.vq.VectorListND;
import org.jtrfp.trcl.img.vq.VectorListRasterizer;
import org.jtrfp.trcl.math.Misc;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.mem.VEC4Address;
import org.jtrfp.trcl.tools.Util;

import com.jogamp.opengl.GL3;

import lombok.AllArgsConstructor;

public class VQTexture implements Texture {
    private final TextureManager 	tm ;
    private final VQCodebookManager 	cbm;
    private       TextureTOCWindow 	tocWindow;
    private final SubTextureWindow	stw;
    private 	  Color 		averageColor;
    private final String 		debugName;
    private final Integer		tocIndex;
    private final ArrayList<Integer>	subTextureIDs = new ArrayList<Integer>();
    private final CompositeVectorListND subtextureVectorList = new CompositeVectorListND();
    private final ArrayList<Integer>	codebookStartOffsets256 = new ArrayList<Integer>();
    private boolean	         	uvWrapping;
    private int				sideLength;
    private TextureBehavior.Support	tbs = new TextureBehavior.Support();
    private Point2D.Double              size;
    private final Collection<Runnable>  finalizationHooks = new ArrayList<Runnable>();
    private List<VQTexture>             mipTextures;
    
    VQTexture(GPU gpu, String debugName){
   	this.tm		  =gpu.textureManager.get();
   	this.cbm	  =tm.vqCodebookManager;
   	this.tocWindow	  =tm.getTOCWindow();
   	this.stw	  =tm.getSubTextureWindow();
   	this.debugName	  =debugName.replace('.', '_');
   	this.tocIndex     =getTocWindow().create();
   	
   	Util.CLEANER.register(this, new CleaningAction(tocWindow, tocIndex, stw, cbm, subTextureIDs, codebookStartOffsets256, finalizationHooks));
    }//end constructor
    
    @AllArgsConstructor
    private static class CleaningAction implements Runnable {
	private final TextureTOCWindow tocWindow;
	private final Integer tocIndex;
	private final SubTextureWindow stw;
	private final VQCodebookManager cbm;
	private final ArrayList<Integer> subTextureIDs;
	private final ArrayList<Integer> codebookStartOffsets256;
	private final Collection<Runnable> finalizationHooks;
	
	@Override
	public void run() {
	    System.out.println("VQTexture cleaning actions...");
	    //Undo the magic
	    tocWindow.magic.set(tocIndex, 0000);
	    //TOC ID
	    if(tocIndex!=null)
		tocWindow.freeLater(tocIndex);
	    stw.freeLater(subTextureIDs);
	    //Codebook entries
	    cbm.freeCodebook256(codebookStartOffsets256);
	    for(Runnable h:finalizationHooks)
		h.run();
	}//end run()
    }//end CleaningAction
    /*
    @Override
    public void finalize() throws Throwable{
	//Undo the magic
	getTocWindow().magic.set(tocIndex, 0000);
	//TOC ID
	if(tocIndex!=null)
	    tocWindow.freeLater(tocIndex);
	stw.freeLater(subTextureIDs);
	//Codebook entries
	cbm.freeCodebook256(codebookStartOffsets256);
	for(Runnable h:finalizationHooks)
	    h.run();
	super.finalize();
    }//end finalize()
    */
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
	return tocWindow.getPhysicalAddressInBytes(tocIndex).intValue()/PagedByteBuffer.PAGE_SIZE_BYTES;
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

    public Integer getTocIndex() {
	//if(tocIndex == null)
	    //tocIndex = getTocWindow().create();
        return tocIndex;
    }/*

    public void setTocIndex(Integer tocIndex) {
        this.tocIndex = tocIndex;
    }*/

    public TextureTOCWindow getTocWindow() {
        return tocWindow;
    }

    public void setTocWindow(TextureTOCWindow tocWindow) {
        this.tocWindow = tocWindow;
    }

    public void setSideLength(int sideLength) {
        this.sideLength = sideLength;
    }

    public void setUvWrapping(boolean uvWrapping) {
        this.uvWrapping = uvWrapping;
    }

    public ArrayList<Integer> getCodebookStartOffsets256() {
        return codebookStartOffsets256;
    }

    public ArrayList<Integer> getSubTextureIDs() {
        return subTextureIDs;
    }

    public void setAverageColor(Color averageColor) {
        this.averageColor = averageColor;
    }

    @Override
    public Point2D.Double getSize() {
        return size;
    }

    protected void setSize(Point2D.Double size) {
	if(size.equals(this.size))
	    return;
        this.size = size;
        setSideLength((int)size.getX());//TODO: Remove sideLength
        subtextureVectorList.setDimensions(getSizeInCodes());
        final int diameterInSubtextures = getDiameterInSubtextures();
        setNumNeededSubtextureIDs(diameterInSubtextures*diameterInSubtextures);
    }//end setSize(...)
    
    protected void setNumNeededSubtextureIDs(int num){
	if(num <0)
	    throw new IllegalArgumentException("Quantity intolerably negative: "+num);
	final List<Integer>  subTextureIDs = getSubTextureIDs();
	int sizeDelta = num-subTextureIDs.size();
	if(sizeDelta == 0)
	    return;
	if(sizeDelta > 0)
	    increaseSubtextureIDs(sizeDelta);
	else
	    decreaseSubtextureIDs(-sizeDelta);
	reEvaluateSubTextureTOCAssignments();
    }//end setNumNeededSubtextureIDs(...)
    
    private void reEvaluateSubTextureTOCAssignments(){
	final int tocIndex = getTocIndex();
	final List<Integer> stids = getSubTextureIDs();
	final int size = stids.size();
	final int [] buf = new int[size];
	for(int i=0; i<size; i++)
	    buf[i]=new VEC4Address(stw.getPhysicalAddressInBytes(stids.get(i))).intValue();
	getTocWindow().subtextureAddrsVec4.set(tocIndex, buf);
    }//end reEvaluateSubTextureTOCAssignments()
    
    protected void increaseSubtextureIDs(int num){
	for(int i=0; i<num; i++){
	    final int id = stw.create();
	    subTextureIDs.add(id);
	    subtextureVectorList.getSubLists().add(new VectorListRasterizer(new SubtextureVL(stw,id), 
		    new int [] {SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER,SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER}));
	    }
    }//end increaseSubtextureIDs(...)
    
    protected void decreaseSubtextureIDs(int num){
	final int size = subTextureIDs.size();
	final List<Integer> subList = subTextureIDs.subList(size-num, size);
	final List<Integer> toRemove = new ArrayList<Integer>(subList);
	for(int i=0; i<num; i++)
	    stw.free(toRemove);
	subList.clear();
	subtextureVectorList.getSubLists().subList(size-num, size).clear();
    }//end increaseSubtextureIDs(...)
    
    public int getDiameterInSubtextures(){
	final double diameterInCodes = (double)getDiameterInCodes();
	return (int)Math.ceil(diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER);
    }
    
    public int getDiameterInCodes(){
	final double sideLength = (double)getSize().getX();//TODO: Only works for square textures
	return (int)Misc.clamp((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH, 1, Integer.MAX_VALUE);
    }
    
    public int [] getSizeInCodes(){
	return new int[]{
        	(int)Math.ceil(size.getX()/VQCodebookManager.CODE_SIDE_LENGTH),
        	(int)Math.ceil(size.getY()/VQCodebookManager.CODE_SIDE_LENGTH)
        	};
    }

    protected SubTextureWindow getSubTextureWindow() {
        return stw;
    }

    int newCodebook256() {
	final int codebook256 = cbm.newCodebook256();
	getCodebookStartOffsets256().add(codebook256);
	return codebook256;
    }
    
    public final void setCodeAt(int globalCodeX, int globalCodeY, byte value){
	subtextureVectorList.setComponentAt(new int[]{globalCodeX, globalCodeY}, 0, value);
    }//end setCodeAt(...)
    
    /**
     * Returns the index of the subtexture as stored in the List returned by getSubTextureIDs()
     * @param globalCodeX
     * @param globalCodeY
     * @return
     * @since Mar 17, 2016
     */
    public int getSubtextureIdxFor(int globalCodeX, int globalCodeY){
	final int subtextureX     = globalCodeX / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
	final int subtextureY     = globalCodeY / SubTextureWindow.SIDE_LENGTH_CODES_WITH_BORDER;
	return subtextureX + subtextureY * getDiameterInSubtextures();
    }
    
    public VEC4Address getPhysicalVEC4OfSubtextureFor(int globalCodeX, int globalCodeY){
	return getPhysicalVEC4OfSubtextureIdx(getSubtextureIdxFor(globalCodeX, globalCodeY));
    }
    
    /**
     * Takes the index as used when reading the List returned by getSubTextureIDs()
     * @param subtextureIdx
     * @return
     * @since Mar 17, 2016
     */
    public VEC4Address getPhysicalVEC4OfSubtextureIdx(int subtextureIdx){
	final int objectIndex = getSubTextureIDs().get(subtextureIdx);
	return getSubTextureWindow().getPhysicalAddressInBytes(objectIndex).asVEC4Address();
    }
    
    void newCodebook256(Collection<Integer> dest, int numberOfCodeblocksToCreate){
	final List<Integer> result = new ArrayList<Integer>(numberOfCodeblocksToCreate);
	cbm.newCodebook256(result, numberOfCodeblocksToCreate);
	getCodebookStartOffsets256().addAll(result);
	if(dest!=null)
	    dest.addAll(result);
    }//end newCodebook256
    
    void freeCodebook256(int codebook256){
	cbm.freeCodebook256(codebook256);
	if(!getCodebookStartOffsets256().remove(Integer.valueOf(codebook256)))
	    throw new IllegalStateException("Specified codebook256 index was not found: "+codebook256);
    }

    protected List<VectorListND> getSubTextureVLs() {
        return  subtextureVectorList.getSubLists();
    }
    
    public void setMagic(int magic){
	getTocWindow().magic.set(getTocIndex(), magic);
    }
    
    public void addFinalizationHook(Runnable r){
	finalizationHooks.add(r);
    }

    public List<VQTexture> getMipTextures() {
        return mipTextures;
    }

    public void setMipTextures(List<VQTexture> mipTextures) {
        this.mipTextures = mipTextures;
    }
}// end Texture
