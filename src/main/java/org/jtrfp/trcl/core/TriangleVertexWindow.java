package org.jtrfp.trcl.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.IndirectObject;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class TriangleVertexWindow {
    public static final int BYTES_PER_VERTEX = GLTextureBuffer.BYTES_PER_VEC4;
    private IndirectObject<Integer> arrayOffset;
    private final AtomicInteger numTriangles = new AtomicInteger();
    private final IndexPool indexPool = new IndexPool();
    
    private static final int X=0;
    private static final int Y=2;
    private static final int Z=4;
    // 6 is unused
    /**
    * Scale texture coordinate up by 4096 prior to setting.
    */
    private static final int U=8;
    private static final int V=10;
    // 12, 14 are unused
    
    public final int createTriangle(){
	numTriangles.incrementAndGet();
	return indexPool.pop();
    }//end createTriangle
    
    public final int createTriangleVertices(int numNewVertices){
	numTriangles.addAndGet(numNewVertices);
	return indexPool.popConsecutive(numNewVertices);
    }
    
    public final void setX(int id, short val){
	GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+X,(short)val);
    }
    public final void setY(int id, short val){
	GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+Y,(short)val);
    }
    public final void setZ(int id, short val){
	GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+Z,(short)val);
    }
    public final void setU(int id, short val){
	GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+U,(short)val);
    }
    public final void setV(int id, short val){
	GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+V,(short)val);
    }

    /**
     * @return the arrayOffset
     */
    public IndirectObject<Integer> getArrayOffset() {
        return arrayOffset;
    }

    /**
     * @param arrayOffset the arrayOffset to set
     */
    public void setArrayOffset(IndirectObject<Integer> arrayOffset) {
        this.arrayOffset = arrayOffset;
    }

    public int getStartAddressInBytes(int id) {
	return arrayOffset.get()+id*BYTES_PER_VERTEX;
    }
    
}//end TriangleWindow
