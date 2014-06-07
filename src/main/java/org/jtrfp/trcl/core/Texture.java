/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;
import javax.media.opengl.GLRunnable;

import org.jtrfp.trcl.OutOfTextureSpaceException;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.img.vq.ByteBufferVectorList;
import org.jtrfp.trcl.img.vq.RGBA8888VectorList;
import org.jtrfp.trcl.img.vq.RasterizedBlockVectorList;
import org.jtrfp.trcl.mem.PagedByteBuffer;

public class Texture implements TextureDescription {
    TextureTreeNode 			nodeForThisTexture;
    private final TR 			tr;
    private final GPU 			gpu;
    private final TextureManager 	tm ;
    private final VQCodebookManager 	cbm;
    private final TextureTOCWindow 	toc;
    private final SubTextureWindow	stw;
    private 	  Color 		averageColor;
    private final String 		debugName;
    private	  int[]			subTextureIDs;
    private	  int[][]		codebookStartOffsetsAbsolute;
    private 	  ByteBuffer 		rgba;
    private static double pixelSize = .7 / 4096.; // TODO: This is a kludge;
						  // doesn't scale with
						  // texture palette
    private static TextureTreeNode rootNode = null;
    private static GLTexture globalTexture;
    public static final List<Future<TextureDescription>> texturesToBeAccounted = Collections
	    .synchronizedList(new LinkedList<Future<TextureDescription>>());

    private static void waitUntilTextureProcessingEnds() {
	while (!texturesToBeAccounted.isEmpty()) {
	    try {
		texturesToBeAccounted.remove(0).get();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}//end while(texturesToBeAccounted)
    }// end waitUntilTextureProcessingEnds()

    public static final ArrayList<GLRunnable> executeInGLFollowingFinalization = new ArrayList<GLRunnable>();
    private static ByteBuffer emptyRow = null;
    
    private Texture(TR tr, String debugName){
	this.tr=tr;
	this.gpu	=tr.gpu.get();
	this.tm		=gpu.textureManager.get();
	this.cbm	=tm.vqCodebookManager.get();
	this.toc	=tm.getTOCWindow();
	this.stw	=tm.getSubTextureWindow();
	this.debugName	=debugName.replace('.', '_');
    }//end constructor

    private Texture(Texture parent, double uOff, double vOff, double uSize,
	    double vSize, TR tr) {
	this(tr,"subtexture: "+parent.debugName);
	nodeForThisTexture = new UVTranslatingTextureTreeNode(
		parent.getNodeForThisTexture(), uOff, vOff, uSize, vSize);
    }//end constructor
    
    public Texture subTexture(double uOff, double vOff, double uSize,
	    double vSize){
	return new Texture(this,uOff,vOff,uSize,vSize,tr);
    }

    Texture(ByteBuffer imageRGBA8888, String debugName, TR tr) {
	this(tr,debugName);
	if (imageRGBA8888.capacity() == 0) {
	    throw new IllegalArgumentException(
		    "Cannot create texture of zero size.");
	}//end if capacity==0
	if (tr.getTrConfig().isUsingNewTexturing()) {// Temporary; conform size
						     // to 64x64
	    imageRGBA8888.clear();//Doesn't erase, just resets the tracking vars
	    vqCompress(imageRGBA8888);
	return;
	}// end if(newTexturing)
	
	final int sideLength = (int) Math.sqrt((imageRGBA8888.capacity() / 4));
	TextureTreeNode newNode = new TextureTreeNode(sideLength, null,
		debugName);
	nodeForThisTexture = newNode;
	newNode.setImage(imageRGBA8888);
	registerNode(newNode);
    }// end constructor
    
    private void vqCompress(ByteBuffer imageRGBA8888){
	    final int sideLength 				= (int)Math.sqrt((imageRGBA8888.capacity() / 4));
	    // Break down into 4x4 blocks
	    final ByteBufferVectorList 		bbvl 		= new ByteBufferVectorList(imageRGBA8888);
	    final RGBA8888VectorList 		rgba8888vl 	= new RGBA8888VectorList(bbvl);
	    final RasterizedBlockVectorList 	rbvl 		= new RasterizedBlockVectorList(
		    rgba8888vl, sideLength, 4);
	    // Get a TOC
	    final int tocIndex = toc.create();
	    final ByteBuffer vectorBuffer = ByteBuffer
		    .allocateDirect(4 * 4 * 4);
	    // Create subtextures
	    final int diameterInCodes 		= (int)Math.ceil((double)sideLength/(double)VQCodebookManager.CODE_SIDE_LENGTH);
	    final int diameterInSubtextures 	= (int)Math.ceil((double)diameterInCodes/(double)SubTextureWindow.SIDE_LENGTH_CODES);
	    subTextureIDs 			= new int[diameterInSubtextures*diameterInSubtextures];
	    codebookStartOffsetsAbsolute	= new int[diameterInSubtextures*diameterInSubtextures][6];
	    for(int i=0; i<subTextureIDs.length; i++){
		//Create subtexture ID
		subTextureIDs[i]=stw.create();
		for(int off=0; off<6; off++){
		codebookStartOffsetsAbsolute[i][off] =  tm.vqCodebookManager.get()
			    .newCodebook256() * 256;}
	    }//end for(subTextureIDs)
	tr.getThreadManager().submitToGL(new Callable<Object>() {
	    @Override
	    public Object call() {
		Thread.currentThread().setName("Texture.java:189");
		for(int i=0; i<subTextureIDs.length; i++){
			final int id = subTextureIDs[i];
			//Convert subtexture index to index of TOC
			final int tocSubTexIndex = (i%diameterInSubtextures)+(i/diameterInSubtextures)*TextureTOCWindow.WIDTH_IN_SUBTEXTURES;
			//Load subtexture ID into TOC
			toc.subtextureAddrsVec4.setAt(tocIndex, tocSubTexIndex,stw.getPhysicalAddressInBytes(id)/GPU.BYTES_PER_VEC4);
			//Fill the subtexture code start offsets
			for(int off=0; off<6; off++)
			    stw.codeStartOffsetTable.setAt(id, off, codebookStartOffsetsAbsolute[i][off]);
		    }//end for(subTextureIDs)
		// Set the TOC vars
		toc.height	 .set(tocIndex, sideLength);
		toc.width	 .set(tocIndex, sideLength);
		// Push codes to subtextures
		    for(int codeY=0; codeY<diameterInCodes; codeY++){
			for(int codeX=0; codeX<diameterInCodes; codeX++){
			    final int subtextureX 		= codeX / SubTextureWindow.SIDE_LENGTH_CODES;
			    final int subtextureY 		= codeY / SubTextureWindow.SIDE_LENGTH_CODES;
			    final int subTextureIdx		= subtextureX + subtextureY * diameterInSubtextures;
			    final int subtextureID		= subTextureIDs[subTextureIdx];
			    final int subtextureCodeX 		= codeX % SubTextureWindow.SIDE_LENGTH_CODES;
			    final int subtextureCodeY 		= codeY % SubTextureWindow.SIDE_LENGTH_CODES;
			    final int codeIdx			= subtextureCodeX + subtextureCodeY * SubTextureWindow.SIDE_LENGTH_CODES;
			    vectorBuffer.clear();
			    for (int vi = 0; vi < 4 * 4 * 4; vi++)
				vectorBuffer.put((byte) (rbvl
					.componentAt(codeX+codeY*diameterInCodes, vi) * 255.));
			    final int globalCodeIndex = codeIdx%256
					    + codebookStartOffsetsAbsolute[subTextureIdx][codeIdx/256];
				    vectorBuffer.clear();
			    cbm.setRGBA(globalCodeIndex, vectorBuffer);
			    stw.codeIDs.setAt(subtextureID, codeIdx, (byte)(codeIdx%256));
			}//end for(codeX)
		    }//end for(codeY)
		return null;
	    }// end run()
	}).get();
	
	    TextureTreeNode newNode = new TextureTreeNode(64, null,
			debugName);
	    	newNode.setOffsetU(0);
	    	newNode.setOffsetV(0);
	    	newNode.setSizeU(1);
	    	newNode.setSizeV(1);
	    	newNode.setTexturePage((toc.getPhysicalAddressInBytes(tocIndex)/PagedByteBuffer.PAGE_SIZE_BYTES));
	    	if(toc.getPhysicalAddressInBytes(tocIndex)%PagedByteBuffer.PAGE_SIZE_BYTES!=0)throw new RuntimeException("Nonzero modulus."); 		
	    	nodeForThisTexture = newNode;
		//Do not register the node.
		//Report to debug
		//This is commented out due to extreme increase in loading time.
	    	/*
		tr.getReporter().report("org.jtrfp.trcl.core.Texture."+debugName+".textureTOC.page", newNode.getTextureID());
		tr.getReporter().report("org.jtrfp.trcl.core.Texture."+debugName+".textureTOC.index", tocIndex);
		for(int id:subTextureIDs){
		    tr.getReporter().report("org.jtrfp.trcl.core.Texture."+debugName+".SubTexture.id", id);
		    tr.getReporter().report("org.jtrfp.trcl.core.Texture."+debugName+".SubTexture.page", stw.getPhysicalAddressInBytes(id)/GPU.BYTES_PER_VEC4);
		}//end for(ids)
		*/
    }//end vqCompress(...)

    Texture(BufferedImage img, String debugName, TR tr) {
	this(tr,debugName);
	    final int sideLength = img.getWidth();
	    
	    TextureTreeNode newNode = new TextureTreeNode(sideLength, null,
		    debugName);
	    nodeForThisTexture = newNode;
	    // TODO Add true alpha support and optimize like the one below
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
	    newNode.setImage(rgba);
	if(tr.getTrConfig().isUsingNewTexturing()){
	    vqCompress(rgba);
	}else{registerNode(newNode);}
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

    static double getPixelSize() {
	return pixelSize;
    }

    public static GLTexture getGlobalTexture() {
	return globalTexture;
    }

    private static synchronized void registerNode(TextureTreeNode newNode) {
	if (rootNode == null) {
	    rootNode = new TextureTreeNode(newNode.getSideLength() * 2, null,
		    "Root or former root as branch");// Assuming square
	    rootNode.setSizeU(1);
	    rootNode.setSizeV(1);
	    rootNode.addNode(newNode);
	} else {
	    if (newNode.getSideLength() >= rootNode.getSideLength())// Too big
								    // to fit
	    {// New, bigger root
		TextureTreeNode oldRoot = rootNode;
		rootNode = new TextureTreeNode(newNode.getSideLength() * 2,
			null, "Root or former root as branch");
		rootNode.addNode(newNode);
		// Try again recursively until we fit the old root into the new
		// root
		registerNode(oldRoot);
	    } else {// Small enough to fit but might be out of space
		try {
		    rootNode.addNode(newNode);
		} catch (OutOfTextureSpaceException e) {// New, bigger root
		    TextureTreeNode oldRoot = rootNode;
		    rootNode = new TextureTreeNode(oldRoot.getSideLength() * 2,
			    null, "Root or former root as branch");
		    rootNode.addNode(oldRoot);
		    // Try again recursively until we fit the new node into the
		    // tree
		    registerNode(newNode);
		}
	    }// end else(small enough to fit)
	}// end else{rootNode!=null}
    }// end registerNode(...)

    public Color getAverageColor() {
	if (averageColor == null) {// Compute a new one
	    double sRed = 0, sGreen = 0, sBlue = 0;
	    final int sLen = nodeForThisTexture.getSideLength();
	    final ByteBuffer img = nodeForThisTexture.getImage();
	    img.clear();// Doesn't erase it. Clears the marks, limits,
			// positions.
	    for (int i = 0; i < sLen; i++) {
		sRed += (int) img.get() & 0xFF;
		sGreen += (int) img.get() & 0xFF;
		sBlue += (int) img.get() & 0xFF;
	    }
	    sRed /= sLen;
	    sGreen /= sLen;
	    sBlue /= sLen;
	    averageColor = new Color((int) sRed, (int) sGreen, (int) sBlue);
	}
	return averageColor;
    }// end getAverageColor()

    public static void finalize(GPU gpu) {
	if(rootNode==null){
	    System.out.println("WARNING: RootNode is null. If using new texturing system this is normal.\n" +
	    		"Else, expect empty textures. Skipping texture finalization phase...");
	    return;
	}
	final int gSideLen = rootNode.getSideLength();
	// Setup the empty rows
	emptyRow = ByteBuffer.allocate(gSideLen * 4);
	for (int i = 0; i < gSideLen; i++) {
	    emptyRow.put((byte) (Math.random() * 256));
	}
	emptyRow.rewind();
	System.out.println("Finalizing global U/V coordinates...");
	waitUntilTextureProcessingEnds();
	rootNode.finalizeUV(0, 0, 1, 1);
	System.out.println("\t...Done.");
	System.out.println("Allocating " + gSideLen + "x" + gSideLen
		+ " shared texture in client RAM...");
	ByteBuffer buf = ByteBuffer.allocateDirect(gSideLen * gSideLen * 4);
	System.out.println("\t...Done.");
	System.out.println("Assembling the texture palette...");
	// Fill the buffer, raster row by raster row
	buf.rewind();
	for (int row = 0; row < gSideLen * gSideLen * 4; row++) {
	    buf.put((byte) (Math.random() * 255.));
	}
	buf.rewind();

	for (int row = gSideLen - 1; row >= 0; row--) {
	    rootNode.dumpRowToBuffer(buf, row);
	}

	System.out.println("\t...Done.");
	buf.rewind();
	System.out
		.println("Creating a new OpenGL texture for texture palette...");

	GLTexture tex = gpu.newTexture();
	tex.setTextureImageRGBA(buf);
	globalTexture = tex;
	final TR tr = gpu.getTr();
	System.out.println("Legacy texturing mode: Committing models to atlas U/Vs...");
	tr.getRootWindow().getCanvas().invoke(false, Texture.executeInGLFollowingFinalization);
	/*for(final GLRunnable r:Texture.executeInGLFollowingFinalization){
	    tr.getThreadManager().submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    System.out.println("Texture.finalize.executeInGLFollowingFinalization "+r);
		    r.run();
		    return null;
		}//end call()
	    });
	}//end for(executeInGLFollowingFinalization)
	*/
    }// end finalize()

    public static final int createTextureID(GL3 gl) {
	IntBuffer ib = IntBuffer.allocate(1);
	gl.glGenTextures(1, ib);
	ib.clear();
	return ib.get();
    }

    public static final Color[] GREYSCALE;
    static {
	GREYSCALE = new Color[256];
	for (int i = 0; i < 256; i++) {
	    GREYSCALE[i] = new Color(i, i, i);
	}
    }// end static{}

    static class UVTranslatingTextureTreeNode extends TextureTreeNode {
	private final TextureTreeNode pNode;
	private final double uOffset, vOffset, uSize, vSize;

	public UVTranslatingTextureTreeNode(TextureTreeNode parent,
		double uOffset, double vOffset, double uSize, double vSize) {
	    super(parent.sideLength, parent.parent, parent.debugName);
	    pNode = parent;
	    this.uOffset = uOffset;
	    this.vOffset = vOffset;
	    this.uSize = uSize;
	    this.vSize = vSize;
	}

	@Override
	public double getGlobalUFromLocal(double u) {
	    return pNode.getGlobalUFromLocal(u * uSize + uOffset);
	}

	@Override
	public double getGlobalVFromLocal(double v) {
	    return pNode.getGlobalVFromLocal(v * vSize + vOffset);
	}
    }//end class UVTranslatingTextureTreeNode

    public static class TextureTreeNode {
	private TextureTreeNode parent;
	private double offsetU, offsetV;// In OpenGL orientation: (0,0) is
					// bottom left.
	private double sizeU, sizeV;
	private TextureTreeNode topLeft, topRight, bottomLeft, bottomRight;
	private ByteBuffer image;
	private int sideLength;
	private String debugName = "[unset]";
	private volatile int texturePage=10;

	public TextureTreeNode(int sideLength, TextureTreeNode parent,
		String debugName) {
	    this.sideLength = sideLength;
	    this.parent = parent;
	    this.debugName = debugName;
	}

	public boolean isFull() {
	    if (image != null)
		return true;
	    if (topLeft != null && topRight != null && bottomLeft != null
		    && bottomRight != null) {
		return topLeft.isFull() && topRight.isFull()
			&& bottomLeft.isFull() && bottomRight.isFull();
	    }
	    return false;
	}// end isFull()

	public void finalizeUV(double offU, double offV, double sU, double sV) {
	    this.setOffsetU(offU + Texture.getPixelSize());
	    this.setOffsetV(offV + Texture.getPixelSize());
	    this.setSizeU(sU - Texture.getPixelSize() * 2.);
	    this.setSizeV(sV - Texture.getPixelSize() * 2.);

	    if (topLeft != null) {
		topLeft.finalizeUV(offU, offV + sV / 2., sU / 2., sV / 2.);
	    }
	    if (topRight != null) {
		topRight.finalizeUV(offU + sU / 2., offV + sV / 2., sU / 2.,
			sV / 2.);
	    }
	    if (bottomLeft != null) {
		bottomLeft.finalizeUV(offU, offV, sU / 2., sV / 2.);
	    }
	    if (bottomRight != null) {
		bottomRight.finalizeUV(offU + sU / 2., offV, sU / 2., sV / 2.);
	    }
	}

	public void dumpRowToBuffer(ByteBuffer buf, int row) {// Rows start at
							      // top, not
							      // OpenGL-bottom.
	    if (this.getSizeU() <= 0) {
		System.out.println("Usize is " + this.getSizeU() + " name is "
			+ debugName);
		System.exit(1);
	    }
	    if (this.getSizeV() <= 0) {
		System.out.println("Vsize is " + this.getSizeV());
		System.exit(1);
	    }
	    if (image == null) {
		if (row >= getSideLength() / 2) {
		    // Bottom two
		    if (bottomLeft != null)
			bottomLeft.dumpRowToBuffer(buf, row - getSideLength()
				/ 2);
		    else {
			emptyRow.clear();
			emptyRow.limit(4 * getSideLength() / 2);
			buf.put(emptyRow);
		    }
		    if (bottomRight != null)
			bottomRight.dumpRowToBuffer(buf, row - getSideLength()
				/ 2);
		    else {
			emptyRow.clear();
			emptyRow.limit(4 * getSideLength() / 2);
			buf.put(emptyRow);
		    }
		} else {
		    // Top two
		    if (topLeft != null)
			topLeft.dumpRowToBuffer(buf, row);
		    else {
			emptyRow.clear();
			emptyRow.limit(4 * getSideLength() / 2);
			buf.put(emptyRow);
		    }
		    if (topRight != null)
			topRight.dumpRowToBuffer(buf, row);
		    else {
			emptyRow.clear();
			emptyRow.limit(4 * getSideLength() / 2);
			buf.put(emptyRow);
		    }
		}
	    }// end image==null
	    else {
		image.clear();
		image.limit((row * getSideLength() * 4) + (getSideLength() * 4));
		image.position(row * getSideLength() * 4);
		buf.put(image);
	    }// end image!=null
	}// end dumpRowToBuffer()

	public boolean isLeaf() {
	    return image != null;
	}

	public void addNode(TextureTreeNode newNode) {
	    if (isFull())
		throw new OutOfTextureSpaceException();

	    final int sideLength = newNode.getSideLength();
	    if (sideLength == this.sideLength / 2)// Perfectly matches
						  // branches/leaves
	    {
		if (topLeft == null) {
		    newNode.setParent(this);
		    topLeft = newNode;
		    return;
		} else if (topRight == null) {
		    newNode.setParent(this);
		    topRight = newNode;
		    return;
		} else if (bottomLeft == null) {
		    newNode.setParent(this);
		    bottomLeft = newNode;
		    return;
		} else if (bottomRight == null) {
		    newNode.setParent(this);
		    bottomRight = newNode;
		    return;
		}
		throw new OutOfTextureSpaceException();
	    }// end if(sideLength==this.sideLength/2)
	    else if (sideLength <= this.sideLength / 2)// Smaller than
						       // branches/leaves
	    {// Find a non-leaf, if none, try to create a leaf. If none can be
	     // created, throw exception
	     // Find non-leaf and try to push it there.
		if (topLeft != null && !topLeft.isLeaf()) {
		    try {
			topLeft.addNode(newNode);
			return;
		    } catch (OutOfTextureSpaceException e) {
		    }
		}// end if(topLeft)
		if (topRight != null && !topRight.isLeaf()) {
		    try {
			topRight.addNode(newNode);
			return;
		    } catch (OutOfTextureSpaceException e) {
		    }
		}// end if(topRight)
		if (bottomLeft != null && !bottomLeft.isLeaf()) {
		    try {
			bottomLeft.addNode(newNode);
			return;
		    } catch (OutOfTextureSpaceException e) {
		    }
		}// end if(bottomLeft)
		if (bottomRight != null && !bottomRight.isLeaf()) {
		    try {
			bottomRight.addNode(newNode);
			return;
		    } catch (OutOfTextureSpaceException e) {
		    }
		}// end if(bottomRight)
		// No leaf found. Try to create one
		// System.out.println("Attempt to find an existing branch which can accept this as a leaf has failed.");
		// System.out.println("Attempting to create a child branch which is larger than this node which can contain the newNode.");
		try {
		    if (topLeft == null) {
			topLeft = new TextureTreeNode(this.sideLength / 2,
				this, "Branch");
			topLeft.addNode(newNode);
			return;
		    }
		    if (topRight == null) {
			topRight = new TextureTreeNode(this.sideLength / 2,
				this, "Branch");
			topRight.addNode(newNode);
			return;
		    }
		    if (bottomLeft == null) {
			bottomLeft = new TextureTreeNode(this.sideLength / 2,
				this, "Branch");
			bottomLeft.addNode(newNode);
			return;
		    }
		    if (bottomRight == null) {
			bottomRight = new TextureTreeNode(this.sideLength / 2,
				this, "Branch");
			bottomRight.addNode(newNode);
			return;
		    }
		}// end try{}
		catch (OutOfTextureSpaceException e) {
		    e.printStackTrace();
		    System.out.println("ASSERT: Impossible situation!");
		}
		// Could not create leaf. Throw exception
		throw new OutOfTextureSpaceException();
	    }// end else if(sideLength<=this.sideLength/2)
	    else if (sideLength > this.sideLength / 2)// Bigger than these
						      // leaves
	    {
		throw new RuntimeException(
			"Added Node's sideLength is bigger than half this Node's sideLength. Can't integrate. Proposed sideLength="
				+ sideLength
				+ " this.sideLength="
				+ this.sideLength);
	    }
	}// end addTexture(...)

	public double getGlobalUFromLocal(double localU) {
	    double sizeOfPixel = .5 * this.getSizeU()
		    / (double) this.getSideLength();
	    double borderingScalar = (double) this.getSideLength()
		    / ((double) this.getSideLength() + 1.);
	    return this.getOffsetU() + sizeOfPixel + localU * this.getSizeU()
		    * borderingScalar;
	}

	public double getGlobalVFromLocal(double localV) {
	    double sizeOfPixel = .5 * this.getSizeU()
		    / (double) this.getSideLength();
	    double borderingScalar = (double) this.getSideLength()
		    / ((double) this.getSideLength() + 1.);
	    return this.getOffsetV() + sizeOfPixel + localV * this.getSizeV()
		    * borderingScalar;
	}
	/**
	 * @return the offsetU
	 */
	public double getOffsetU() {
	    return offsetU;
	}

	/**
	 * @param offsetU
	 *            the offsetU to set
	 */
	public void setOffsetU(double offsetU) {
	    this.offsetU = offsetU;
	}

	/**
	 * @return the offsetV
	 */
	public double getOffsetV() {
	    return offsetV;
	}

	/**
	 * @param offsetV
	 *            the offsetV to set
	 */
	public void setOffsetV(double offsetV) {
	    this.offsetV = offsetV;
	}

	/**
	 * @return the topLeft
	 */
	public TextureTreeNode getTopLeft() {
	    return topLeft;
	}

	/**
	 * @param topLeft
	 *            the topLeft to set
	 */
	public void setTopLeft(TextureTreeNode topLeft) {
	    this.topLeft = topLeft;
	}

	/**
	 * @return the topRight
	 */
	public TextureTreeNode getTopRight() {
	    return topRight;
	}

	/**
	 * @param topRight
	 *            the topRight to set
	 */
	public void setTopRight(TextureTreeNode topRight) {
	    this.topRight = topRight;
	}

	/**
	 * @return the bottomLeft
	 */
	public TextureTreeNode getBottomLeft() {
	    return bottomLeft;
	}

	/**
	 * @param bottomLeft
	 *            the bottomLeft to set
	 */
	public void setBottomLeft(TextureTreeNode bottomLeft) {
	    this.bottomLeft = bottomLeft;
	}

	/**
	 * @return the bottomRight
	 */
	public TextureTreeNode getBottomRight() {
	    return bottomRight;
	}

	/**
	 * @param bottomRight
	 *            the bottomRight to set
	 */
	public void setBottomRight(TextureTreeNode bottomRight) {
	    this.bottomRight = bottomRight;
	}

	/**
	 * @return the sideLength
	 */
	public int getSideLength() {
	    return sideLength;
	}

	/**
	 * @param sideLength
	 *            the sideLength to set
	 */
	public void setSideLength(int sideLength) {
	    this.sideLength = sideLength;
	}

	/**
	 * @return the parent
	 */
	public TextureTreeNode getParent() {
	    return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(TextureTreeNode parent) {
	    this.parent = parent;
	}

	/**
	 * @return the sizeU
	 */
	public double getSizeU() {
	    return sizeU;
	}

	/**
	 * @param sizeU
	 *            the sizeU to set
	 */
	public void setSizeU(double sizeU) {
	    this.sizeU = sizeU;
	}

	/**
	 * @return the sizeV
	 */
	public double getSizeV() {
	    return sizeV;
	}

	/**
	 * @param sizeV
	 *            the sizeV to set
	 */
	public void setSizeV(double sizeV) {
	    this.sizeV = sizeV;
	}

	/**
	 * @return the image
	 */
	public ByteBuffer getImage() {
	    return image;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(ByteBuffer image) {
	    this.image = image;
	}

	/**
	 * @return the textureID
	 */
	public int getTexturePage() {
	    return texturePage;
	}

	/**
	 * @param texturePage the texturePage, where a page is 1536 bytes
	 */
	public void setTexturePage(int textureID) {
	    this.texturePage = textureID;
	}
    }// end TextureTreeNode

    /**
     * @return the nodeForThisTexture
     */
    public TextureTreeNode getNodeForThisTexture() {
	return nodeForThisTexture;
    }

    /**
     * @param nodeForThisTexture
     *            the nodeForThisTexture to set
     */
    public void setNodeForThisTexture(TextureTreeNode nodeForThisTexture) {
	this.nodeForThisTexture = nodeForThisTexture;
    }
    /**
     * @return the rgba
     */
    public ByteBuffer getRgba() {
	return rgba;
    }

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
}// end Texture