package org.jtrfp.trcl.mem;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GPU;

public class GPUMemDump {

    public GPUMemDump(TR tr) {
	//Dump raw memory
	final GPU gpu = tr.gpu.get();
	final MemoryManager memMgr = gpu.memoryManager.get();
	final int gpuMemSize = memMgr.getMaxCapacityInBytes();
	try{
	File outFile = new File("gpuMemDump.bin");
	if(outFile.exists())outFile.delete();
	outFile.createNewFile();
	RandomAccessFile raf = new RandomAccessFile(outFile,"rw");
	raf.setLength(gpuMemSize);
	FileChannel channel = raf.getChannel();
	MappedByteBuffer bb = channel.map(MapMode.READ_WRITE, 0, gpuMemSize);
	memMgr.dumpAllGPUMemTo(bb);
	raf.close();}
	catch(Exception e){e.printStackTrace();}
    }//end constructor

}//end GPUMemDump
