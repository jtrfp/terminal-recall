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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.EarlyLoadingScreen;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.Game.CanceledException;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

public class GameShell {
    private final TR tr;
    public static final String [] ABORT_GAME_MENU_PATH   = new String [] {"Game","Abort Game"};
    public static final String [] START_GAME_MENU_PATH = new String [] {"Game","Start Game"};
    public static final SkyCubeGen DEFAULT_GRADIENT = new HorizGradientCubeGen
		(Color.darkGray,Color.black);
    private EarlyLoadingScreen earlyLoadingScreen;
    private GLFont             greenFont;
    private boolean []	       initialized = new boolean[]{false};
    private final EndGameMenuItemListener
                               endGameMenuItemListener = new EndGameMenuItemListener();
    private final StartGameMenuItemListener
                               startGameMenuItemListener = new StartGameMenuItemListener();
    private final RunStateListener
                               runStateListener = new RunStateListener();
    
    public interface GameShellRunState     extends TR.TRConstructed{}
    public interface GameShellConstructing extends GameShellRunState{}
    public interface GameShellConstructed  extends GameShellRunState{}
    public interface GameShellDestructing  extends GameShellRunState{}
    public interface GameShellDestructed   extends GameShellRunState{}
    
    public GameShell(TR tr){
	this.tr=tr;
	Features.init(this);
	tr.setRunState(new GameShellConstructing(){});
	tr.addPropertyChangeListener(TR.GAME, new PropertyChangeListener(){//TODO: Redesign then remove
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue()==null){
		    earlyLoadingScreen.setStatusText("No game loaded.");
		    showGameshellScreen();
		}else{hideGameshellScreen();}
	    }});
	final MenuSystem menuSystem = tr.getMenuSystem();
	
	menuSystem.addMenuItem(START_GAME_MENU_PATH);
	menuSystem.addMenuItemListener(startGameMenuItemListener, START_GAME_MENU_PATH);
	
	menuSystem.addMenuItem(ABORT_GAME_MENU_PATH);
	menuSystem.addMenuItemListener(endGameMenuItemListener, ABORT_GAME_MENU_PATH);
	
	tr.addPropertyChangeListener(TR.RUN_STATE, runStateListener);
	tr.setRunState(new GameShellConstructed(){});
    }//end constructor(TR)
    
    private class EndGameMenuItemListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent e) {
	    tr.abortCurrentGame();
	}
    }//end EndGameMenuItmListener
    
    private class StartGameMenuItemListener implements ActionListener{
	@Override
	public void actionPerformed(ActionEvent e) {
	    tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    startGame();
		    return null;
		}});
	}//end actionPerformed(...)
    }//end StartGameMenuItmListener
    
    private class RunStateListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object newValue = evt.getNewValue();
	    tr.getMenuSystem().setMenuItemEnabled(
		    newValue instanceof Game.GameRunningMode,
		    ABORT_GAME_MENU_PATH);
	    tr.getMenuSystem().setMenuItemEnabled(
		    (newValue instanceof Game.GameLoadedMode), 
		        START_GAME_MENU_PATH);
	}//end RunStateListener
    }//end RunStateListener
    
    public GameShell startShell(){
	tr.gatherSysInfo();
	registerPODs();
	applyGFXState();
	initLoadingScreen();
	openInitializationFence();
	return this;
    }//end startShell()
    
    private void openInitializationFence(){
	synchronized(initialized){
	    initialized[0]=true;
	    initialized.notifyAll();
	}
    }//end openInitializationFence()
    
    private void initializationFence(){
	if(initialized[0]) return;
	synchronized(initialized){
	    while(!initialized[0])
		try{initialized.wait();}catch(InterruptedException e){}
	}//end sync(initialized)
    }//end ensureInitialized()
    
    public void showGameshellScreen(){
	initializationFence();
	tr.getDefaultGrid().nonBlockingAddBranch(earlyLoadingScreen);
    }
    
    public void hideGameshellScreen(){
	initializationFence();
	tr.getDefaultGrid().nonBlockingRemoveBranch(earlyLoadingScreen);
    }
    
    private void initLoadingScreen(){
	System.out.println("Initializing general resources...");
	try{greenFont          = new GLFont(tr.getResourceManager().getFont("OCRA.zip", "OCRA.ttf"),tr);
	    earlyLoadingScreen = new EarlyLoadingScreen(tr, greenFont);
	    earlyLoadingScreen.setStatusText("No game loaded.");
	    tr.getDefaultGrid().nonBlockingAddBranch(earlyLoadingScreen);
	}catch(Exception e){gameFailure(e);}
    }//end initLoadingScreen()
    
    public void applyGFXState(){
	final Renderer renderer = tr.mainRenderer.get();
	final Camera camera     = renderer.getCamera();
	camera.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	renderer.getSkyCube().setSkyCubeGen(DEFAULT_GRADIENT);
	camera.setHeading(Vector3D.PLUS_I);
	camera.setTop(Vector3D.PLUS_J);
    }
    
    public GameShell newGame(VOXFile vox){
	initializationFence();
	GameVersion newGameVersion = determineGameVersion();
	tr.config.setGameVersion(newGameVersion!=null?newGameVersion:GameVersion.TV);
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
	initializationFence();
	try{tr.getGame().doGameplay();}
	catch(CanceledException e){
	    return this;}
	catch(Exception e){
	    gameFailure(e);}
	return this;
    }
    
    private void gameFailure(Exception e){
	handleLoadFailureException(e);
	tr.abortCurrentGame();
    }
    
    private void handleLoadFailureException(Throwable th){
	assert th!=null;
	while(th instanceof RuntimeException || th instanceof ExecutionException)
	    if(th.getCause()!=null)th=th.getCause();
	    else break;
	StringBuilder sb = new StringBuilder();
	    if(th instanceof FileNotFoundException){
		sb.append("Could not load file from any of the registered PODs.\n");
		sb.append("Ensure that the necessary PODs are registered in the File->Configure menu.\n");
	    }else if(th instanceof FileLoadException){
		sb.append("File was found but could not be loaded, possibly by parsing/formatting error.\n");
	    }else if(th instanceof UnrecognizedFormatException){
		sb.append("File was found but could not be loaded to the LVL format being unrecognized. (parse error)\n");
	    }else if(th instanceof IllegalAccessException){
		sb.append("Check disk permissions for registered PODs.\n");
	    }else if(th instanceof IOException){
		sb.append("An undocumented IO failure has occurred..\n");
	    }if(th!=null)throwable2StringBuilder(th,sb);
	    JOptionPane.showMessageDialog(tr.getRootWindow(), new JTextArea(sb.toString()), "File Load Failure", JOptionPane.ERROR_MESSAGE);
	    throw new RuntimeException(th);
    }//end handleLoadFailureException(...)
    
    private void throwable2StringBuilder(Throwable e, StringBuilder sb){
	assert e!=null;
	assert sb!=null;
	sb.append(e.getClass().getName()+" "+e.getLocalizedMessage()+"\n");
	final StackTraceElement [] stackTraceElements = e.getStackTrace();
	for(StackTraceElement ste:stackTraceElements)
	    sb.append("\tat "+ste.getClassName()+"."+ste.getMethodName()+"("+ste.getFileName()+":"+ste.getLineNumber()+")\n");
    }
    
    private VOXFile determineVOXFile() {
	String voxName = tr.config.getVoxFile();
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
    
    private GameVersion determineGameVersion(){
	String voxName = tr.config.getVoxFile();
	if(voxName==null)
	    return tr.config.getGameVersion();
	else if(voxName.contentEquals(TRConfiguration.AUTO_DETECT))
	    {return guessGameVersionFromPods();}
	else if(voxName.contentEquals("Fury3"))
	    return GameVersion.F3;
	else if(voxName.contentEquals("TV"))
	    return GameVersion.TV;
	else if(voxName.contentEquals("FurySE"))
	    return GameVersion.FURYSE;
	else return tr.config.getGameVersion();
    }
    
    private GameVersion guessGameVersionFromPods(){
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
	 return (f3Hint?GameVersion.F3:tvHint?GameVersion.TV:GameVersion.FURYSE);
	}//end if(hints==1)
	return null;
    }
    
    private VOXFile autoDetermineVOXFile(){
	String voxFileName=null;
	System.out.println("Auto-determine active... pods:"+tr.getResourceManager().getRegisteredPODs().size());
	GameVersion gameVersion = guessGameVersionFromPods();
	if (gameVersion != null) {
	    switch (gameVersion) {
	    case TV:    voxFileName = "TV";
		break;
	    case F3:    voxFileName = "Fury3";
		break;
	    case FURYSE:voxFileName = "FurySE";
		break;
	    }//end switch(...)
	}// end if(!null)
	
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
	DefaultListModel<String> podList = tr.config.getPodList();
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
    /**
     * @return the greenFont
     */
    public GLFont getGreenFont() {
        return greenFont;
    }
    /**
     * @return the earlyLoadingScreen
     */
    public EarlyLoadingScreen getEarlyLoadingScreen() {
	initializationFence();
        return earlyLoadingScreen;
    }
}//end GameShell
