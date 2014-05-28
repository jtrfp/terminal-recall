package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class SubTextureWindow extends MemoryWindow {
    public static final int SIDE_LENGTH_CODES = 38;
    //38 tiles squared = 1444
    public final ByteArrayVariable codeIDs = new ByteArrayVariable(1444);
    public final ByteArrayVariable unused = new ByteArrayVariable(92);// 19
    public void setTile(int objectID, int x, int y, byte tileID){
	codeIDs.setAt(objectID,x+y*SIDE_LENGTH_CODES,tileID);
    }//end MemoryWindow
    
    public SubTextureWindow(TR tr){
	this.init(tr,"SubTextureWindow");
    }
}//end SubTextureWindow
