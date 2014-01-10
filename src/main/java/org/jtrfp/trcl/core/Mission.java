package org.jtrfp.trcl.core;

import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.GameSetup;
import org.jtrfp.trcl.file.LVLFile;

public class Mission {
    private final TR tr;
    private final LVLFile lvl;
    public Mission(TR tr, LVLFile lvl){
	this.tr=tr;
	this.lvl=lvl;
    }
    
    public Result go(){
	try{new GameSetup(lvl,tr);}
	catch(IOException e){e.printStackTrace();}
	catch(FileLoadException e){e.printStackTrace();}
	catch(IllegalAccessException e){e.printStackTrace();}
	return new Result(null);//TODO: Replace null with actual value unless end of game.
    }//end go()
    public class Result{
	private final String nextLVL;
	public Result(String nextLVL){
	    this.nextLVL=nextLVL;
	}
	public String getNextLVL() {
	    return nextLVL;
	}
    }//end Result
}//end Mission
