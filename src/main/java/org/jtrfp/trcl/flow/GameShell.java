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

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

public class GameShell {
    private final TR tr;
    public static final SkyCubeGen DEFAULT_GRADIENT = new HorizGradientCubeGen
		(Color.darkGray,Color.black);
    
    public GameShell(TR tr){
	this.tr=tr;
    }
    public GameShell startShell(){
	tr.gatherSysInfo();
	registerPODs();
	applyGFXState();
	return this;
    }//end startShell()
    
    public void applyGFXState(){
	final Renderer renderer = tr.renderer.get();
	final Camera camera = renderer.getCamera();
	camera.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	renderer.getSkyCube().setSkyCubeGen(DEFAULT_GRADIENT);
	camera.setHeading(Vector3D.PLUS_I);
	camera.setTop(Vector3D.PLUS_J);
    }
    
    public GameShell newGame(){
	VOXFile vox;
	vox = determineVOXFile();
	if(vox==null)
	    return this;//Abort
	final Game game = tr.newGame(vox);
	try{game.boot();}
	catch(Exception e){
	    gameFailure(e);}
	return this;
    }//end newGame()
    
    public GameShell startGame(){
	try{tr.getGame().doGameplay();}
	catch(Exception e){
	    gameFailure(e);}
	return this;
    }
    
    private void gameFailure(Exception e){
	handleLoadFailureException(e);
	tr.abortCurrentGame();
    }
    
    private void handleLoadFailureException(Throwable e){
	while(e instanceof RuntimeException || e instanceof ExecutionException)
	    e=e.getCause();
	StringBuilder sb = new StringBuilder();
	    //sb.append("Failure to read the file "+e.get+".\n");
	    if(e instanceof FileNotFoundException){
		sb.append("Could not load file from any of the registered PODs.\n");
		sb.append("Ensure that the necessary PODs are registered in the File->Configure menu.\n");
	    }else if(e instanceof FileLoadException){
		sb.append("File was found but could not be loaded, possibly by parsing/formatting error.\n");
	    }else if(e instanceof UnrecognizedFormatException){
		sb.append("File was found but could not be loaded to the LVL format being unrecognized. (parse error)\n");
	    }else if(e instanceof IllegalAccessException){
		sb.append("Check disk permissions for registered PODs.\n");
	    }else if(e instanceof IOException){
		sb.append("An undocumented IO failure has occurred..\n");
	    }if(e!=null)sb.append(e.getLocalizedMessage());
	    JOptionPane.showMessageDialog(tr.getRootWindow(), sb, "File Load Failure", JOptionPane.ERROR_MESSAGE);
    }//end handleLoadFailureException(...)
    
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
	System.out.println("Auto-determine active... pods:"+tr.getResourceManager().getRegisteredPODs().size());
	for(IPodData pod:tr.getResourceManager().getRegisteredPODs()){
	    final String podComment = pod.getComment();
	    System.out.println("POD comment="+podComment);
	    f3Hint     |= podComment.toUpperCase().startsWith("FURY3");
	    tvHint     |= podComment.toUpperCase().startsWith("TV");
	    furyseHint |= podComment.toUpperCase().startsWith("FURYSE");
	}//end for(pods)
	
	int numValidHints=0 + (f3Hint?1:0) + (tvHint?1:0) + (furyseHint?1:0);
	if(numValidHints==1){
	 voxFileName=f3Hint?     "Fury3":voxFileName;
	 voxFileName=tvHint?        "TV":voxFileName;
	 voxFileName=furyseHint?"FurySE":voxFileName;
	 tr.getTrConfig()[0].setGameVersion(f3Hint?GameVersion.F3:tvHint?GameVersion.TV:GameVersion.FURYSE);
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
	DefaultListModel<String> podList = tr.getTrConfig()[0].getPodList();
	for(int i=0; i<podList.size(); i++){
	    final String podPath = podList.get(i);
	    if(podPath!=null){
		final File file = new File(podPath);
		PodFile pod = new PodFile(file);
		try {
		    tr.getResourceManager().registerPOD(file.getAbsolutePath(),
			    pod);
		} catch (FileLoadException e) {
		    JOptionPane.showMessageDialog(tr.getRootWindow(),
			    "Failed to parse (understand) POD file " + podPath,
			    "Parsing failure", JOptionPane.ERROR_MESSAGE);
		}//end catch(...)
	    }//end if(!null)
	}//end for(pods)
    }//end registerPODs
}//end GameShell
