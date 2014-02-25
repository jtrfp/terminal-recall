package org.jtrfp.trcl.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.IndirectObject;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;
import org.jtrfp.trcl.mem.MemoryWindow;

public final class TriangleVertexWindow extends MemoryWindow {
    public static final int BYTES_PER_VERTEX = GLTextureBuffer.BYTES_PER_VEC4;
    //private IndirectObject<Integer> arrayOffset;
    private final AtomicInteger numTriangles = new AtomicInteger();
    //private final IndexPool indexPool = new IndexPool();
    
    public final ShortVariable x = new ShortVariable();
    public final ShortVariable y = new ShortVariable();
    public final ShortVariable z = new ShortVariable();
    public final ShortVariable unusedShort6 = new ShortVariable();
    public final ShortVariable u = new ShortVariable();
    public final ShortVariable v = new ShortVariable();
    public final ShortVariable unusedShort12 = new ShortVariable();
    public final ShortVariable unusedShort14 = new ShortVariable();
    
    public TriangleVertexWindow(){
	init();
    }
    
    /*
    private static final int X=0;
    private static final int Y=2;
    private static final int Z=4;
    private static final int UNUSED_SHORT_6=6;
    /**
    * Scale texture coordinate up by 4096 prior to setting.
    */
    /*
    private static final int U=8;
    private static final int V=10;
    private static final int UNUSED_SHORT_12=12;
    private static final int UNUSED_SHORT_14=14;
    */
    
    public synchronized final int createTriangle(){
	numTriangles.addAndGet(3);
	return create();
    }//end createTriangle
    
    ///TODO: Pages may be out of order. Must submit every ID
    public synchronized final int createTriangleVertices(final int numNewVertices){
	numTriangles.addAndGet(numNewVertices);
	int first=create();
	for(int i=0; i<numNewVertices-1; i++){
	    create();
	}
	return first;
    }
    
    public final void setX(int id, short val){
	//GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+X,(short)val);
	x.set(id, val);
    }
    public final void setY(int id, short val){
	//GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+Y,(short)val);
	y.set(id, val);
    }
    public final void setZ(int id, short val){
	//GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+Z,(short)val);
	z.set(id, val);
    }
    public final void setU(int id, short val){
	//GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+U,(short)val);
	u.set(id, val);
    }
    public final void setV(int id, short val){
	//GlobalDynamicTextureBuffer.putShort(arrayOffset.get()+id*BYTES_PER_VERTEX+V,(short)val);
	v.set(id, val);
    }
    
}//end TriangleWindow
