package org.jtrfp.trcl.mem;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jtrfp.trcl.core.IndexPool;
import org.jtrfp.trcl.core.IndexPool.GrowthBehavior;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.MemoryUsageHint;
import org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer;

public final class MemoryManager {
    private final IndexPool 			pageIndexPool 	= new IndexPool();
    private final ByteBuffer [] 		physicalMemory 	= new ByteBuffer[1];
    private final ReallocatableGLTextureBuffer 	glPhysicalMemory;
    private final GPU				gpu;
    
    public MemoryManager(GPU gpu){
	this.gpu=gpu;
	glPhysicalMemory=new ReallocatableGLTextureBuffer(gpu);
	glPhysicalMemory.reallocate(PagedByteBuffer.PAGE_SIZE_BYTES);
	physicalMemory[0] = glPhysicalMemory.map();
	glPhysicalMemory.setUsageHint(MemoryUsageHint.DymamicDraw);
	pageIndexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		glPhysicalMemory.reallocate(previousMaxCapacity*PagedByteBuffer.PAGE_SIZE_BYTES*2);
		physicalMemory[0] = glPhysicalMemory.map();
		return previousMaxCapacity*2;
	    }//end grow(...)
	});
    }//end constructor
    
    public int getMaxCapacityInBytes(){
	return PagedByteBuffer.PAGE_SIZE_BYTES*pageIndexPool.getMaxCapacity();
    }
    public void map(){
	if((physicalMemory[0] = glPhysicalMemory.map())==null)throw new NullPointerException("Failed to map GPU memory. (returned null)");
    }
    public void unmap(){
	physicalMemory[0]=null;//Make sure we don't start reading/writing somewhere bad.
	glPhysicalMemory.unmap();
	}
    public PagedByteBuffer createPagedByteBuffer(int initialSizeInBytes, String debugName){
	return new PagedByteBuffer(physicalMemory, pageIndexPool, initialSizeInBytes, debugName);
    }
    public void bindToUniform(int i, GLProgram shaderProgram, GLUniform uniform) {
	glPhysicalMemory.bindToUniform(i, shaderProgram, uniform);
    }

    public void dumpAllGPUMemTo(ByteBuffer dest) throws IOException{
	map();
	physicalMemory[0].clear();
	dest.clear();
	physicalMemory[0].limit(dest.limit());//Avoid overflow
	dest.put(physicalMemory[0]);
	physicalMemory[0].clear();
	dest.clear();
    }
}//end MemmoryManager
