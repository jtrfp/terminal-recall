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

import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.VectorList;

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
     final VQTexture result = vqtf.newUncompressedVQTexture(new PalettedVectorList(colorZeroRasterVL(), colorVL(c)),null,"SolidColor r="+c.getRed()+" g="+c.getGreen()+" b="+c.getBlue(),false); 
     result.setAverageColor(c);
     return result;
 }//end newSolidColorTexture
 
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
