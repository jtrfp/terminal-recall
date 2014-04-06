package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureMipmapTOCWindow extends MemoryWindow {
    final IntArrayVariable textureTOCIndexInBytes = new IntArrayVariable(64);//8x8 mipmap levels
    
    public void setMipmapTOC(int mipmapTOCWindowIndex, int widthLOD, int heightLOD, int tocIndexInBytes){
	textureTOCIndexInBytes.setAt(mipmapTOCWindowIndex, widthLOD+heightLOD*8, tocIndexInBytes);
    }//end setMipmapTOC(...)
    
    public TextureMipmapTOCWindow(TR tr){
	init(tr,"TextureMipmapTOCWindow");
    }
}//end TextureMipmapTOCWindow
