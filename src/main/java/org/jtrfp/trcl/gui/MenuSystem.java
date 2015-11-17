/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jtrfp.trcl.core.RootWindow;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.EngineTests;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.mem.GPUMemDump;

import com.jogamp.newt.event.KeyEvent;

public class MenuSystem {
    private final FramebufferStateWindow fbsw;
    private final LevelSkipWindow	levelSkipWindow;
    private final IndirectProperty<Game>game      = new IndirectProperty<Game>();
    private final IndirectProperty<Boolean>paused = new IndirectProperty<Boolean>();
    private final JCheckBoxMenuItem	view_crosshairs = new JCheckBoxMenuItem("Crosshairs");
    final JMenu file = new JMenu("File"), 
	    gameMenu = new JMenu("Game"),
	    debugMenu = new JMenu("Debug"),
	    viewMenu = new JMenu("View");
    
    public MenuSystem(final TR tr){
	final RootWindow rw = tr.getRootWindow();
	
	// And items to menus
	final JMenuItem file_quit = new JMenuItem("Quit");
	final JMenuItem file_config = new JMenuItem("Configure");
	final JMenuItem game_new = new JMenuItem("New Game");
	final JMenuItem game_start = new JMenuItem("Start Game");
	final JMenuItem game_skip = new JMenuItem("Skip To Level...");
	final JMenuItem game_abort= new JMenuItem("Abort Game");
	final JMenuItem debugStatesMenuItem = new JMenuItem("Debug States");
	final JMenuItem frameBufferStatesMenuItem = new JMenuItem("Framebuffer States");
	final JMenuItem gpuMemDump = new JMenuItem("Dump GPU Memory");
	final JMenuItem codePageDump = new JMenuItem("Dump Code Pages");
	final JMenuItem debugSinglet = new JMenuItem("Singlet (fill)");
	final JMenuItem debugDQ = new JMenuItem("Depth Queue Test");
	final JMenuItem view_sat = new JCheckBoxMenuItem("Satellite");
	// Accellerator keys
	file_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	
	view_sat.setAccelerator(KeyStroke.getKeyStroke("TAB"));
	view_crosshairs.setAccelerator(KeyStroke.getKeyStroke("X"));
	
	fbsw = new FramebufferStateWindow(tr);
	levelSkipWindow = new LevelSkipWindow(tr);
	
	// Menu item behaviors
	game_new.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			tr.getGameShell().newGame();
			return null;
		    }});
	    }});
	game_start.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			tr.getGameShell().startGame();
			return null;
		    }});
	    }});
	
	
	Action satelliteAction = new AbstractAction("SATELLITE_VIEW"){
	    private static final long serialVersionUID = -6843605846847411702L;
	    @Override
	    public void actionPerformed(ActionEvent l) {
		final Mission mission = tr.getGame().getCurrentMission();
		mission.setSatelliteView(view_sat.isSelected());
	    }};
	    Action satelliteKeyAction = new AbstractAction("SATELLITE_VIEW_KEY"){
		private static final long serialVersionUID = -6843605846847411702L;
		@Override
		public void actionPerformed(ActionEvent l) {
		    if(view_sat.isEnabled())
			view_sat.doClick();
		}};
	String satKey = "SATELLITE_KEY";
	view_sat.addActionListener(satelliteAction);
	view_sat.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB,0), satKey);
	view_sat.getActionMap().put(satKey, satelliteKeyAction);
	
	view_crosshairs.setSelected(tr.config.isCrosshairsEnabled());
	view_crosshairs.addChangeListener(new ChangeListener(){
	    @Override
	    public void stateChanged(ChangeEvent evt) {
		tr.config.setCrosshairsEnabled(view_crosshairs.isSelected());
	    }});
	game_skip.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		levelSkipWindow.setVisible(true);
	    }});
	game_abort.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		game_abort.setText("Aborting Game...");
		game_abort.setEnabled(false);
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			tr.abortCurrentGame();
			SwingUtilities.invokeLater(new Runnable(){
			    @Override
			    public void run() {
				game_abort.setText("Abort Game");
				game_abort.setEnabled(false);
			    }});//end EDT task
			return null;
		    }});//end threadPool task
	    }});//end actionListener(game_abort)
	file_config.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		tr.getConfigWindow().setVisible(true);
	    }});
	file_quit.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		System.exit(1);
	    }
	});
	debugStatesMenuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ev) {
		tr.getReporter().setVisible(true);
	    };
	});
	frameBufferStatesMenuItem.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent e) {
		fbsw.setVisible(true);
	    }});
	gpuMemDump.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ev) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			new GPUMemDump(tr).dumpRootMemory();
			return null;
		    }});
	    };
	});
	codePageDump.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ev) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			new GPUMemDump(tr).dumpCodePages();
			return null;
		    }});
	    };
	});
	debugSinglet.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		Object result = JOptionPane.showInputDialog(rw, 
			"Enter number of instances", "How many?", 
			JOptionPane.QUESTION_MESSAGE, null, null, null);
		try{
		 final int numInstances = Integer.parseInt((String)result);
		 tr.threadManager.submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			EngineTests.singlet(tr, numInstances);
			return null;
		    }});
		}catch(NumberFormatException e)
		 {JOptionPane.showMessageDialog(rw, "Please supply an integer value.");}
	    }});
	debugDQ.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		 tr.threadManager.submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			EngineTests.depthQueueTest(tr);
			return null;
		    }});
	    }});
	final String showDebugStatesOnStartup = System
		.getProperty("org.jtrfp.trcl.showDebugStates");
	if (showDebugStatesOnStartup != null) {
	    if (showDebugStatesOnStartup.toUpperCase().contains("TRUE")) {
		tr.getReporter().setVisible(true);
	    }
	}
	try{//Get this done in the local thread to minimize use of the EDT
	    final JMenuBar mb = new JMenuBar();
	    file.add(file_config);
	    file.add(file_quit);
	    debugMenu.add(debugStatesMenuItem);
	    debugMenu.add(frameBufferStatesMenuItem);
            gameMenu.add(game_new);
            game_start.setEnabled(false);
            game_skip.setEnabled(false);
            game_abort.setEnabled(false);
            gameMenu.add(game_start);
            gameMenu.add(game_skip);
            gameMenu.add(game_abort);
            debugMenu.add(debugSinglet);
            debugMenu.add(debugDQ);
            debugMenu.add(gpuMemDump);
            debugMenu.add(codePageDump);
            viewMenu.add(view_sat);
            view_sat.setEnabled(false);
            viewMenu.add(view_crosshairs);
            view_crosshairs.setEnabled(false);
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
	            rw.setVisible(false);//Frame must be invisible to modify.
		    rw.setJMenuBar(mb);
		    mb.add(file);
		    mb.add(gameMenu);
		    mb.add(debugMenu);
		    mb.add(viewMenu);
		    rw.setVisible(true);
		}});
	}catch(Exception e){tr.showStopper(e);}
	
	
	
	tr.addPropertyChangeListener("game", new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		game_new.setEnabled(evt.getNewValue()==null);
		game_skip.setEnabled(evt.getNewValue()!=null);
		game_abort.setEnabled(evt.getNewValue()!=null);
	    }});
	
	IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
	IndirectProperty<Mission>currentMissionIP = new IndirectProperty<Mission>();
	tr.addPropertyChangeListener(TR.GAME, gameIP);
	gameIP.addTargetPropertyChangeListener(Game.CURRENT_MISSION, currentMissionIP);
	
	gameIP.addTargetPropertyChangeListener(Game.CURRENT_MISSION, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent pc) {
		game_start.setEnabled(pc.getNewValue()!=null && !tr.getGame().isInGameplay());
	    }});
	gameIP.addTargetPropertyChangeListener("inGameplay", new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent pc) {
		game_start.setEnabled(pc.getNewValue()!=null && pc.getNewValue()==Boolean.FALSE);
		Boolean newValue = (Boolean)pc.getNewValue();
		if(newValue==null) newValue=false;
		view_crosshairs.setEnabled(newValue);
	    }});
	currentMissionIP.addTargetPropertyChangeListener(Mission.MISSION_MODE, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getNewValue()==null){
		    view_sat.setEnabled(false);
		    return;
		}//end if(null)
		view_sat.setEnabled(evt.getNewValue() instanceof Mission.AboveGroundMode);
	    }});
	
	gameIP.addTargetPropertyChangeListener(Game.PAUSED, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Game game = tr.getGame();
		if(game==null)
		    return;
		if(evt.getNewValue()==Boolean.TRUE)
		    view_sat.setEnabled(false);
		else if(tr.getGame().getCurrentMission()!=null)
		    view_sat.setEnabled(game.getCurrentMission().getMissionMode() instanceof Mission.AboveGroundMode);
	    }});
    }//end constructor

    /**
     * @return the view_crosshairs
     */
    public JCheckBoxMenuItem getView_crosshairs() {
        return view_crosshairs;
    }

    /**
     * @return the gameMenu
     */
    public JMenu getGameMenu() {
        return gameMenu;
    }
}//end MenuSystem
