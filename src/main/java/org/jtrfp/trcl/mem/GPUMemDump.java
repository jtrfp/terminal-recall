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
package org.jtrfp.trcl.mem;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.VQCodebookManager;

public class GPUMemDump {
private final GPU gpu;
private final TR tr;
    public GPUMemDump(TR tr) {
	//Dump raw memory
	this.tr=tr;
	gpu = Features.get(tr, GPUFeature.class);
	try{
	System.out.println("Dumping root memory...");
	dumpRootMemory();
	//System.out.println("Dumping code pages...");
	//dumpCodePages();
	}
	catch(Exception e){e.printStackTrace();}
    }//end constructor
    
    public void dumpCodePages() throws Exception {
	final VQCodebookManager vq = gpu.textureManager.get().vqCodebookManager
		.get();
	
	final ByteBuffer[] pagesRGBA8888 = tr.getThreadManager().submitToGL(new Callable<ByteBuffer[]>(){
	    @Override
	    public ByteBuffer[] call() throws Exception {
		return vq.dumpPagesToBuffer();
	    }}).get();
	for (int pageIndex = 0; pageIndex < VQCodebookManager.NUM_CODE_PAGES; pageIndex++) {
	    final File outFile = new File("debugCodePage" + pageIndex + ".png");
	    if (outFile.exists())
		outFile.delete();
	    outFile.createNewFile();
	    final BufferedImage bi = new BufferedImage(VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS,VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS, BufferedImage.TYPE_INT_RGB);
	    Graphics g = bi.getGraphics();
	    System.out.println("Saving page "+pageIndex+"...");
	    for(int y=0;y<VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS; y++)
		for(int x=0; x<VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS; x++){
		    byte r = pagesRGBA8888[pageIndex].get((x+y*VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS)*4);
		    byte gr = pagesRGBA8888[pageIndex].get((x+y*VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS)*4+1);
		    byte b = pagesRGBA8888[pageIndex].get((x+y*VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS)*4+2);
		    byte a = pagesRGBA8888[pageIndex].get((x+y*VQCodebookManager.CODE_PAGE_SIDE_LENGTH_TEXELS)*4+3);
		    g.setColor(new Color(r&0xFF,gr&0xFF,b&0xFF,a&0xFF));
		    g.fillRect(x, y, 1, 1);
		}//end for(xy)
	    ImageIO.write(bi, "PNG", outFile);
	}//end for(pageIndex)
    }// end dumpCodePages()
    
    public void dumpRootMemory() throws Exception{
	File outFile = new File("gpuMemDump.bin");
	final MemoryManager memMgr = gpu.memoryManager.get();
	final int gpuMemSize = memMgr.getMaxCapacityInBytes();
	if(outFile.exists())outFile.delete();
	outFile.createNewFile();
	RandomAccessFile raf = new RandomAccessFile(outFile,"rw");
	raf.setLength(gpuMemSize);
	FileChannel channel = raf.getChannel();
	final MappedByteBuffer bb = channel.map(MapMode.READ_WRITE, 0, gpuMemSize);
	tr.getThreadManager().submitToGL(new Callable<Void>(){

	    @Override
	    public Void call() throws Exception {
		memMgr.dumpAllGPUMemTo(bb);
		return null;
	    }});
	raf.close();
    }//end dumpRootMemory

}//end GPUMemDump
