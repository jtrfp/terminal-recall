package org.jtrfp.trcl.core;

import java.util.concurrent.atomic.AtomicInteger;

import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.MemoryWindow;

public final class TriangleVertexWindow extends MemoryWindow {
    public static final int BYTES_PER_VERTEX = GPU.BYTES_PER_VEC4;
    private final AtomicInteger numTriangles = new AtomicInteger();

    public final ShortVariable x = new ShortVariable();
    public final ShortVariable y = new ShortVariable();
    public final ShortVariable z = new ShortVariable();
    public final ByteVariable normX = new ByteVariable();
    public final ByteVariable normY = new ByteVariable();
    public final ShortVariable u = new ShortVariable();
    public final ShortVariable v = new ShortVariable();
    public final ByteVariable normZ = new ByteVariable();
    
    public final ByteVariable textureIDLo = new ByteVariable();
    public final ByteVariable textureIDMid = new ByteVariable();
    public final ByteVariable textureIDHi = new ByteVariable();
    //public final ByteVariable unusedByte13 = new ByteVariable();
    //public final ShortVariable unusedShort14 = new ShortVariable();

    public TriangleVertexWindow(TR tr, String debugName) {
	init(tr, "TriangleVertexWindow." + debugName);
    }

    public synchronized final int createTriangle() {
	numTriangles.addAndGet(3);
	return create();
    }// end createTriangle
}// end TriangleWindow
