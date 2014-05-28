package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureTOCWindow extends MemoryWindow {
    public static final int WIDTH_IN_SUBTEXTURES=19;
    //19^2 * 4 = 1444 bytes
    public final IntArrayVariable subtextureAddrsVec4 = new IntArrayVariable(361);
    public final ByteArrayVariable filler0 = new ByteArrayVariable(12);//12 bytes to quantize to next VEC4
    //Offset 1456B, 91VEC4
    public final IntVariable width = new IntVariable();//4B
    public final IntVariable height = new IntVariable();//4B
    //Tally: 1464B
    public final ByteArrayVariable unused = new ByteArrayVariable(72);//72B
    
    public TextureTOCWindow(TR tr){
	this.init(tr, "TextureTOCWindow");
    }//end constructor
}//end TextureTOCWindow