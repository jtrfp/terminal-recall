package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureTOCWindow extends MemoryWindow {
    //19^2 * 4 = 1444 bytes
    public final IntArrayVariable subtexturePageIndices = new IntArrayVariable(361);
    //Offset 1456B, 91VEC4
    public final IntVariable width = new IntVariable();//4B
    public final IntVariable height = new IntVariable();//4B
    public final IntVariable startTile = new IntVariable();//4B
    //Tally: 1468B
    public final ByteArrayVariable unused = new ByteArrayVariable(68);//68B
    
    public TextureTOCWindow(TR tr){
	this.init(tr, "TextureTOCWindow");
    }//end constructor
}//end TextureTOCWindow
