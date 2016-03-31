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
import java.nio.ByteBuffer;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.VQCodebookManager.RasterRowWriter;
import org.jtrfp.trcl.pool.ObjectPool;
import org.jtrfp.trcl.pool.ObjectPool.GenerativeMethod;
import org.jtrfp.trcl.pool.ObjectPool.PreparationMethod;

public class SolidColorTextureFactory {
    private final GPU gpu;
    private final TileGeneratingMethod tileGeneratingMethod = new TileGeneratingMethod();
    private final TilePreparationMethod tilePreparationMethod = new TilePreparationMethod();
    private final ObjectPool<Integer> tilePool = new ObjectPool<Integer>(new ObjectPool.LazyAllocate<Integer>(), tilePreparationMethod, tileGeneratingMethod);
    public SolidColorTextureFactory(GPU gpu, ThreadManager threadManager){
	this.gpu = gpu;
    }//end constructor
    
 public VQTexture newSolidColorTexture(Color c){
     final VQTexture result = new VQTexture(gpu, "Solid Color: "+c.toString());
     result.setSize(new Point2D.Double(4,4));
     result.setAverageColor(c);
     result.setMagic(1337);
     //Allocate a codebook256
     final int codebookID = obtainTile();
     //Assign to subtexture 0 (we can assume there is only one)
     final int stid = result.getSubTextureIDs().get(0);
     final SubTextureWindow stw = result.getSubTextureWindow();
     //Set the code-start offset
     stw.codeStartOffsetTable.setAt(stid, 0, codebookID);
     //Write the color to the codebook
     final SolidColorRowWriter rw = new SolidColorRowWriter(c);
     gpu.textureManager.get().vqCodebookManager.get().setRGBA(codebookID, new RasterRowWriter[]{rw});
     final byte codeID = 0x0;
     result.setCodeAt(0,0,codeID);
     result.addFinalizationHook(new Runnable(){
	@Override
	public void run() {
	    releaseTile(codebookID);
	}});
     return result;
 }//end newSolidColorTexture
 
 private int obtainTile(){
     return tilePool.pop();
 }
 
 private void releaseTile(int codebookID){
     tilePool.expire(codebookID);
 }
 
 private class TilePreparationMethod implements PreparationMethod<Integer>{

    @Override
    public Integer deactivate(Integer obj) {
	return obj;
    }

    @Override
    public Integer reactivate(Integer obj) {
	return obj;
    }
     
 }//end TilePreparationMethod
 
 private class TileGeneratingMethod implements GenerativeMethod<Integer> {
    @Override
    public int getAtomicBlockSize() {
	return 256;
    }

    @Override
    public Submitter<Integer> generateConsecutive(int numBlocks,
	    Submitter<Integer> populationTarget) {
	for(int i=0; i<numBlocks; i++){
	    int startID = gpu.textureManager.get().vqCodebookManager.get().newCodebook256()*256;
	    for(int j=0; j<256; j++)
		populationTarget.submit(startID+j);
	}//end for(blockID)
	return populationTarget;
    }//end generateConsecutive(...)
 }//end TileGeneratingMethod
 
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
}//end SolidColorTextureFactory
