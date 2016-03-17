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
import java.awt.Dimension;
import java.nio.ByteBuffer;

import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.mem.VEC4Address;

public class SolidColorTextureFactory {
    private final GPU gpu;
    private final ThreadManager threadManager;
    private final UncompressedVQTextureFactory vqtf;
    
    public SolidColorTextureFactory(GPU gpu, ThreadManager threadManager){
	this.gpu = gpu;
	this.threadManager = threadManager;
	vqtf = new UncompressedVQTextureFactory(gpu, threadManager, "SolidColorTextureFactory");
    }//end constructor
    
 public VQTexture newSolidColorTexture(Color c){
     final VQTexture result = new VQTexture(gpu, "Solid Color: "+c.toString());
     result.setSize(new Dimension(4,4));
     result.setAverageColor(c);
     //Allocate a codebook256
     final int codebook256 = result.newCodebook256();
     //Assign to subtexture 0 (we can assume there is only one)
     final int stid = result.getSubTextureIDs().get(0);
     final SubTextureWindow stw = result.getSubTextureWindow();
     result.getSubTextureWindow().codeStartOffsetTable.setAt(stid, 0, codebook256*256);
     //Write the color to the codebook
     RasterRowWriter [] writers = new RasterRowWriter[256];
     //TODO: Use a tile pool for solid colors
     final SolidColorRowWriter rw = new SolidColorRowWriter(c);
     for(int i=0; i<256; i++)
	 writers[i] = rw;
     new VEC4Address(stw.getPhysicalAddressInBytes(stid)).intValue();
     gpu.textureManager.get().vqCodebookManager.get().setRGBABlock256(codebook256, writers);
     return result;
 }//end newSolidColorTexture
 
 private final class SolidColorRowWriter implements RasterRowWriter {
     private final Color color;
     
     public SolidColorRowWriter(Color color){
	 this.color = color;
     }

    @Override
    public void applyRow(int row, ByteBuffer dest) {
	for(int i=0; i<4; i++){
	    dest.put((byte)color.getRed());
	    dest.put((byte)color.getGreen());
	    dest.put((byte)color.getBlue());
	    dest.put((byte)color.getAlpha());
	}//end for(4)
    }//end applyRow
 }//end SolidColorRowWriter
 
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
}//end SolidColorTextureFactory
