package org.jtrfp.trcl.core;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureTileWindow extends MemoryWindow {
    public final int SIDE_LENGTH_TILES = 39;
    //39 tiles squared = 1521
    final ByteArrayVariable tileIDs = new ByteArrayVariable(1521);
    public final ByteArrayVariable unused = new ByteArrayVariable(15);// 15
    public void setTile(int objectID, int x, int y, byte tileID){
	tileIDs.setAt(objectID,x+y*SIDE_LENGTH_TILES,tileID);
    }//end MemoryWindow
    
    public TextureTileWindow(TR tr){
	this.init(tr,"TextureTileWindow");
    }
}//end TextureTileWindow
