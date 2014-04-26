package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class SubTextureWindow extends MemoryWindow {
    public final int SIDE_LENGTH_CODES = 39;
    //39 tiles squared = 1521
    final ByteArrayVariable codeIDs = new ByteArrayVariable(1521);
    public final ByteArrayVariable unused = new ByteArrayVariable(15);// 15
    public void setTile(int objectID, int x, int y, byte tileID){
	codeIDs.setAt(objectID,x+y*SIDE_LENGTH_CODES,tileID);
    }//end MemoryWindow
    
    public SubTextureWindow(TR tr){
	this.init(tr,"SubTextureWindow");
    }
}//end SubTextureWindow
