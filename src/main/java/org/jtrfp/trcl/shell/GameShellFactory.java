/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2022 Chuck Ritola.
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

package org.jtrfp.trcl.shell;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.EarlyLoadingScreen;
import org.jtrfp.trcl.GLFont;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.SkyCubeCloudModeUpdateBehavior;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.PODRegistry;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.FZone;
import org.jtrfp.trcl.flow.Fury3;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.flow.TV;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.Game.CanceledException;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;
import org.springframework.stereotype.Component;

@Component
public class GameShellFactory implements FeatureFactory<TR>{
    //// PROPERTIES ////
    public static final String	GAME				="game";

    public static final String [] ABORT_GAME_MENU_PATH   = new String [] {"Game","Abort Game"};
    public static final String [] START_GAME_MENU_PATH = new String [] {"Game","Start Game"};
    public static final double    START_GAME_MENU_POS = MenuSystem.Utils.between(MenuSystem.BEGINNING,MenuSystem.MIDDLE);
    public static final double    ABORT_GAME_MENU_POS = START_GAME_MENU_POS+.01;
    public static final SkyCubeGen DEFAULT_GRADIENT = new HorizGradientCubeGen
	    (Color.darkGray,Color.black);
    private static final Color TEXT_COLOR=new Color(80,200,180);

    public interface GameShellRunState     extends TRFactory.TRConstructed{}
    public interface GameShellConstructing extends GameShellRunState{}
    public interface GameShellConstructed  extends GameShellRunState{}
    public interface GameShellReady        extends GameShellConstructed{}
    public interface GameShellDestructing  extends GameShellRunState{}
    public interface GameShellDestructed   extends GameShellRunState{}

    public static class GameShell implements Feature<TR> {

	private final TR   tr;
	private       Game game;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private EarlyLoadingScreen earlyLoadingScreen;
	private GLFont             greenFont;
	private boolean []	       initialized = new boolean[]{false};
	private final EndGameMenuItemListener
	endGameMenuItemListener = new EndGameMenuItemListener();
	private final StartGameMenuItemListener
	startGameMenuItemListener = new StartGameMenuItemListener();
	private final RunStateListener
	runStateListener = new RunStateListener();
	private TRConfiguration trConfiguration;
	private MenuSystem menuSystem;
	private PODRegistry podRegistry;

	public GameShell(TR tr){
	    this.tr=tr;
	}//end constructor(TR)

	private class EndGameMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
		abortCurrentGame();
	    }
	}//end EndGameMenuItmListener

	private class StartGameMenuItemListener implements ActionListener{
	    @Override
	    public void actionPerformed(ActionEvent e) {
			startGame();
	    }//end actionPerformed(...)
	}//end StartGameMenuItmListener

	private class RunStateListener implements PropertyChangeListener{
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Object newValue = evt.getNewValue();
		final MenuSystem menuSystem = getMenuSystem();
		menuSystem.setMenuItemEnabled(
			newValue instanceof Game.GameLoadedMode,
			ABORT_GAME_MENU_PATH);
		menuSystem.setMenuItemEnabled(
			(newValue instanceof Game.GameLoadedMode) &&
			!(newValue instanceof Game.GameRunningMode), 
			START_GAME_MENU_PATH);
		menuSystem.setMenuPosition(START_GAME_MENU_POS, START_GAME_MENU_PATH);
		menuSystem.setMenuPosition(ABORT_GAME_MENU_POS, ABORT_GAME_MENU_PATH);
	    }//end RunStateListener
	}//end RunStateListener

	public GameShell startShell(){
	    tr.gatherSysInfo();
	    //registerPODs();
	    applyGFXState();
	    initLoadingScreen();
	    openInitializationFence();
	    tr.setRunState(new GameShellReady() {});
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
	    try{greenFont          = new GLFont(tr.getResourceManager().getFont("OCRA.zip", "OCRA.ttf"), TEXT_COLOR, tr);
	    earlyLoadingScreen = new EarlyLoadingScreen(tr, greenFont);
	    earlyLoadingScreen.setStatusText("No game loaded.");
	    tr.getDefaultGrid().nonBlockingAddBranch(earlyLoadingScreen);
	    }catch(Exception e){gameFailure(e);}
	}//end initLoadingScreen()

	public void applyGFXState(){
	    final Renderer renderer = tr.mainRenderer;
	    final Camera camera     = renderer.getCamera();
	    camera.probeForBehavior(SkyCubeCloudModeUpdateBehavior.class).setEnable(false);
	    renderer.getSkyCube().setSkyCubeGen(DEFAULT_GRADIENT);
	    camera.setHeading(Vector3D.PLUS_I);
	    camera.setTop(Vector3D.PLUS_J);
	}
	
	public GameShell newGame(VOXFile vox){
	    initializationFence();
	    newGame(vox, determineGameVersion());
	    return this;
	}//end newGame()

	public GameShell newGame(VOXFile vox, GameVersion newGameVersion){
	    initializationFence();
	    getTrConfiguration()._setGameVersion(newGameVersion!=null?newGameVersion:GameVersion.TV);
	    if( vox == null )
		vox = autoDetermineVOXFile2(newGameVersion);
	    if(vox==null)
		return this;//Abort
	    System.out.println("newGame() "+newGameVersion+" "+vox.getMissionName());
	    final TVF3Game newGame = new TVF3Game();
	    newGame.setGameVersion(newGameVersion);
	    newGame.setVox(vox);
	    Features.init(newGame);
	    setGame(newGame);
	    try{game.boot();}
	    catch(Exception e){
		gameFailure(e);}
	    return this;
	}//end newGame()
	
	private Future<Game> setGame(final Game newGame) {
	    if(newGame==game)
		return new DummyFuture<Game>(game);
	    final Game oldGame=game;
	    game=newGame;
	    Future<Game> result = null;
	    final RenderableSpacePartitioningGrid defaultGrid = tr.getDefaultGrid();
	    if(oldGame instanceof TVF3Game || newGame instanceof TVF3Game){
		result = World.relevanceExecutor.submit(new Callable<Game>(){
		    @Override
		    public Game call() throws Exception {
			if(oldGame instanceof TVF3Game)
			    defaultGrid.removeBranch(((TVF3Game)oldGame).getPartitioningGrid());
			if(newGame instanceof TVF3Game)
			    defaultGrid.addBranch(((TVF3Game)newGame).getPartitioningGrid());
			return oldGame;
		    }});
	    }//end if(TVF3Game)
	    if(newGame==null){
		tr.getThreadManager().setPaused(true);
		earlyLoadingScreen.setStatusText("No game loaded.");
		showGameshellScreen();
	    }else{hideGameshellScreen();}
	    pcs.firePropertyChange(GAME, oldGame, newGame);
	    if(result == null)
		result = new DummyFuture<Game>(oldGame);
	    return result;
	}//end setGame()

	public GameShell startGame(){
	    initializationFence();
	    try{getGame().doGameplay();}
	    catch(CanceledException e){
		return this;}
	    catch(Exception e){
		gameFailure(e);}
	    return this;
	}

	private void gameFailure(Exception e){
	    handleLoadFailureException(e);
	    abortCurrentGame();
	}

	public void abortCurrentGame() {
	    final Game game = getGame();
	    if (game != null)
		game.abort();
	    setGame(null);
	    tr.setRunState(new GameShellFactory.GameShellReady(){});
	}// end abortCurrentGame()

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
	    String voxName = getTrConfiguration().getVoxFile();
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
	    String voxName = getTrConfiguration().getVoxFile();
	    if(voxName==null)
		return getTrConfiguration()._getGameVersion();
	    else if(voxName.contentEquals(TRConfiguration.AUTO_DETECT))
	    {return guessGameVersionFromPods();}
	    else if(voxName.contentEquals("Fury3"))
		return GameVersion.F3;
	    else if(voxName.contentEquals("TV"))
		return GameVersion.TV;
	    else if(voxName.contentEquals("FurySE"))
		return GameVersion.FURYSE;
	    else return getTrConfiguration()._getGameVersion();
	}

	private GameVersion guessGameVersionFromPods(){
	    boolean f3Hint=false,tvHint=false,furyseHint=false;
	    final PODRegistry podRegistry     = getPodRegistry();
	    final Collection<String> podPaths = podRegistry.getPodCollection();
	    System.out.println("Auto-determine active... pods:"+podPaths.size());
	    for(String path : podPaths){
		final IPodData pod      = podRegistry.getPodData(path);
		final String podComment = pod.getComment();
		System.out.println("POD comment="+podComment);
		final String podCommentUC = podComment.toUpperCase();
		f3Hint     |= podCommentUC.startsWith("FURY3");
		tvHint     |= podCommentUC.startsWith("TV");
		furyseHint |= podCommentUC.startsWith("FURYSE");
	    }//end for(pods)

	    int numValidHints=0 + (f3Hint?1:0) + (tvHint?1:0) + (furyseHint?1:0);
	    if(numValidHints==1){
		return (f3Hint?GameVersion.F3:tvHint?GameVersion.TV:GameVersion.FURYSE);
	    }//end if(hints==1)
	    return null;
	}
	
	private VOXFile autoDetermineVOXFile2(GameVersion gameVersion) {
	    String voxFileName=null;
	    final PODRegistry podRegistry     = getPodRegistry();
	    System.out.println("Auto-determine2 active... pods:"+podRegistry.getPodCollection().size());
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
	}//end autoDetermineVOXFile2

	private VOXFile autoDetermineVOXFile(){
	    String voxFileName=null;
	    final PODRegistry podRegistry     = getPodRegistry();
	    System.out.println("Auto-determine active... pods:"+podRegistry.getPodCollection().size());
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
/*
	private void registerPODs(){
	    DefaultListModel podList = getTrConfiguration().getPodList();
	    for(int i=0; i<podList.size(); i++){
		final String podPath = (String)podList.get(i);
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
	*/
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

	public Game getGame() {
	    return game;
	}

	public TR getTr() {
	    return tr;
	}

	public TRConfiguration getTrConfiguration() {
	    return trConfiguration;
	}

	public void setTrConfiguration(TRConfiguration trConfiguration) {
	    this.trConfiguration = trConfiguration;
	}

	public MenuSystem getMenuSystem() {
	    return menuSystem;
	}

	public void setMenuSystem(MenuSystem menuSystem) {
	    this.menuSystem = menuSystem;
	}

	@Override
	public void apply(TR target) {
	    setTrConfiguration(Features.get(target, TRConfiguration.class));
	    final RootWindow rootWindow = Features.get(target,RootWindow.class);
	    setMenuSystem(Features.get(rootWindow, MenuSystem.class));
	    setPodRegistry(Features.get(target, PODRegistry.class));
	    target.setRunState(new GameShellConstructing(){});
	    final MenuSystem menuSystem = getMenuSystem();
	    menuSystem.addMenuItem(MenuSystem.MIDDLE, START_GAME_MENU_PATH);
	    menuSystem.addMenuItemListener(startGameMenuItemListener, START_GAME_MENU_PATH);

	    menuSystem.addMenuItem(MenuSystem.MIDDLE, ABORT_GAME_MENU_PATH);
	    menuSystem.addMenuItemListener(endGameMenuItemListener, ABORT_GAME_MENU_PATH);

	    target.addPropertyChangeListener(TRFactory.RUN_STATE, runStateListener);
	    target.setRunState(new GameShellConstructed(){});
	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub

	}

	public void addPropertyChangeListener(PropertyChangeListener arg0) {
	    pcs.addPropertyChangeListener(arg0);
	}

	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener arg0) {
	    pcs.removePropertyChangeListener(arg0);
	}

	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}

	public PODRegistry getPodRegistry() {
	    return podRegistry;
	}

	public void setPodRegistry(PODRegistry podRegistry) {
	    this.podRegistry = podRegistry;
	}
    }//end GameShell

    @Override
    public Feature<TR> newInstance(TR target) {
	final GameShell result = new GameShell(target);
	return result;
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GameShell.class;
    }
}//end GameShellFactory
