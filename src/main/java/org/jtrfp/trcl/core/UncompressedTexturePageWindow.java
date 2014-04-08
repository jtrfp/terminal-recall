package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class UncompressedTexturePageWindow extends MemoryWindow {
    public static final int BYTES_PER_PIXEL 	= 2;
    public static final int SIDE_WIDTH 		= 27;
    public static final int DERASTERIZED_SIZE 	= SIDE_WIDTH * SIDE_WIDTH;
    public final ShortArrayVariable rawShorts 	= new ShortArrayVariable(DERASTERIZED_SIZE);// 27x27
    public final ByteArrayVariable unused 	= new ByteArrayVariable(78);

    public void setAt(int objectIndex, int u, int v, int r, int g, int b, int a) {
	rawShorts.setAt(objectIndex, u + v * SIDE_WIDTH,
		(short) (((r << 12) & 0x0F) | ((g << 8) & 0x0F)
			| ((b << 4) & 0x0F) | (a & 0x0F)));
    }
}//end UncompressedTexturePageWindow
