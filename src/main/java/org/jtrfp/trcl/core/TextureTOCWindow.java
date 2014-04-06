package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureTOCWindow extends MemoryWindow {
    public final IntArrayVariable subtexturePageIndices = new IntArrayVariable(361);//1444 bytes
    public final ShortVariable width = new ShortVariable();//2
    public final ShortVariable height = new ShortVariable();//2
    public final ByteVariable encodingMode = new ByteVariable();//1
    public final IntArrayVariable codeBookPageIndices = new IntArrayVariable(4);//16
    //Tally: 1465
    public final ByteArrayVariable unused = new ByteArrayVariable(71);//71
    
    public TextureTOCWindow(TR tr){
	this.init(tr, "TextureTOCWindow");
    }//end constructor
}//end TextureTOCWindow
