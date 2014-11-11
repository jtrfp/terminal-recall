/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.flow;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.file.VOXFile;

public class GameShell {
    private final TR tr;
    public GameShell(TR tr){
	this.tr=tr;
    }
    public GameShell startShell(){
	tr.gatherSysInfo();
	registerPODs();
	return this;
    }//end startShell()
    
    public GameShell newGame(){
	VOXFile vox;
	vox = determineVOXFile();
	if(vox==null)
	    return this;//Abort
	    final Game game = tr.newGame(vox);
	    /*//Not finished cooking yet.
	     * 
	    final String level = tr.getTrConfig()[0].skipToLevel();
	    if(level!=null){
		System.out.println("Skipping to level: "+level);
	    game.setLevel(tr.getTrConfig()[0].skipToLevel());
	    }
	    */
	game.go();
	return this;
    }//end newGame()
    
    private VOXFile determineVOXFile() {
	String voxName = tr.getTrConfig()[0].getVoxFile();
	if(voxName==null)
	    return autoDetermineVOXFile();
	else if(voxName.contentEquals(TRConfiguration.AUTO_DETECT))
	    return autoDetermineVOXFile();
	else if(voxName.contentEquals("Fury3"))
	    return Fury3.getDefaultMission();
	else if(voxName.contentEquals("TV"))
	    return TV.getDefaultMission();
	else if(voxName.contentEquals("FurySE"))
	    return FZone.getDefaultMission();
	else return attemptGetVOX(voxName);
    }//end determineVOXFile()
    
    private VOXFile autoDetermineVOXFile(){
	String voxFileName=null;
	boolean f3Hint=false,tvHint=false,furyseHint=false;
	for(IPodData pod:tr.getResourceManager().getRegisteredPODs()){
	    final String podComment = pod.getComment();
	    f3Hint     |= podComment.toUpperCase().startsWith("FURY3");
	    tvHint     |= podComment.toUpperCase().startsWith("TV");
	    furyseHint |= podComment.toUpperCase().startsWith("FURYSE");
	}//end for(pods)
	
	int numValidHints=0 + (f3Hint?1:0) + (tvHint?1:0) + (furyseHint?1:0);
	if(numValidHints==1){
	 voxFileName=f3Hint?     "Fury3":voxFileName;
	 voxFileName=tvHint?        "TV":voxFileName;
	 voxFileName=furyseHint?"FurySE":voxFileName;
	}//end if(hints==1)
	if(voxFileName==null){
	    JOptionPane.showMessageDialog(tr.getRootWindow(), "Could not auto-detect the default mission.\nEnsure all necessary PODs are registered in the File->Configure window or specify a VOX file if it is a custom game.","Auto-Detect Failure", JOptionPane.ERROR_MESSAGE);
	    return null;
	}
	return attemptGetVOX(voxFileName);
    }//end autoDetermineVOXFile
    
    private VOXFile attemptGetVOX(String voxFileName){
	try{final VOXFile result = tr.getResourceManager().getVOXFile(voxFileName);
	    return result;
	}catch(FileLoadException e){JOptionPane.showMessageDialog(tr.getRootWindow(), "Failed to parse (understand) VOX file "+voxFileName,"Parsing failure", JOptionPane.ERROR_MESSAGE);}
	 catch(IOException e){JOptionPane.showMessageDialog(tr.getRootWindow(), "Failed to read VOX file "+voxFileName+" from source.","Read failure", JOptionPane.ERROR_MESSAGE);}
	 catch(IllegalAccessException e){JOptionPane.showMessageDialog(tr.getRootWindow(), "Could not access specified vox "+voxFileName,"Permission problem?", JOptionPane.ERROR_MESSAGE);}
    return null;
    }//end attemptGetVOX()
    
    private void registerPODs(){
	for(String podPath:tr.getTrConfig()[0].getPodList()){
	    final File file = new File(podPath);
	    PodFile pod     = new PodFile(file);
	    try{tr.getResourceManager().registerPOD(pod);
	    }catch(FileLoadException e){JOptionPane.showMessageDialog(tr.getRootWindow(), "Failed to parse (understand) POD file "+podPath,"Parsing failure", JOptionPane.ERROR_MESSAGE);}
	}//end for(pods)
    }//end registerPODs
}//end GameShell
