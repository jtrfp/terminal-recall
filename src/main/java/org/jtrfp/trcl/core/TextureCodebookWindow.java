package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureCodebookWindow extends MemoryWindow {
    //1024 bytes
    public final VEC4ArrayVariable codebook = new VEC4ArrayVariable(64);
    public final ByteArrayVariable unused = new ByteArrayVariable(512);
    
    public TextureCodebookWindow(TR tr){
	init(tr,"TextureCodebookWindow");
    }//end constructor
}//end TextureCodebookWindow
