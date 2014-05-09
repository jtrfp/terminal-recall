package org.jtrfp.trcl.mem;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.MemoryUsageHint;
import org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;

public final class MemoryManager {
    private final IndexPool 			pageIndexPool 	= new IndexPool();
    private final ByteBuffer [] 		physicalMemory 	= new ByteBuffer[1];
    private final ReallocatableGLTextureBuffer 	glPhysicalMemory;
    private final GPU				gpu;
    
    public MemoryManager(GPU gpu){
	this.gpu=gpu;
	try{
	glPhysicalMemory = gpu.getTr().getThreadManager().submitToGL(new Callable<ReallocatableGLTextureBuffer>(){
	    @Override
	    public ReallocatableGLTextureBuffer call() throws Exception {
		ReallocatableGLTextureBuffer tb;
		tb=new ReallocatableGLTextureBuffer(MemoryManager.this.gpu);
		tb.reallocate(PagedByteBuffer.PAGE_SIZE_BYTES);
		physicalMemory[0] = tb.map();
		tb.setUsageHint(MemoryUsageHint.DymamicDraw);
		return tb;
	    }}).get();
	}catch(Exception e){throw new RuntimeException(e);}
	
	pageIndexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(final int previousMaxCapacity) {
		final TRFuture<Integer> ft = MemoryManager.this.gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
		    @Override
		    public Integer call(){
			glPhysicalMemory.reallocate(previousMaxCapacity*PagedByteBuffer.PAGE_SIZE_BYTES*2);
			physicalMemory[0] = glPhysicalMemory.map();
			return previousMaxCapacity*2;
		    }//end call()
		});
		try{return ft.get();}catch(Exception e){e.printStackTrace();}
		return previousMaxCapacity;//Fail by maintaining original size
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
    public void bindToUniform(int textureUnit, GLProgram shaderProgram, GLUniform uniform) {
	glPhysicalMemory.bindToUniform(textureUnit, shaderProgram, uniform);
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
