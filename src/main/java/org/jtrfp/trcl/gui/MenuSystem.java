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
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.RootWindow;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.EngineTests;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.mem.GPUMemDump;

import com.jogamp.newt.event.KeyEvent;

public class MenuSystem {
    private final FramebufferStateWindow fbsw;
    private final ConfigWindow		configWindow;
    private final LevelSkipWindow	levelSkipWindow;
    private final PropertyChangeListener pausePCL;
    private final IndirectProperty<Game>game      = new IndirectProperty<Game>();
    private final IndirectProperty<Boolean>paused = new IndirectProperty<Boolean>();
    
    public MenuSystem(final TR tr){
	final RootWindow rw = tr.getRootWindow();
	final JMenu file = new JMenu("File"), 
		    gameMenu = new JMenu("Game"),
		    debugMenu = new JMenu("Debug");
	// And menus to menubar
	final JMenuItem file_quit = new JMenuItem("Quit");
	final JMenuItem file_config = new JMenuItem("Configure");
	final JMenuItem game_new = new JMenuItem("New Game");
	final JMenuItem game_start = new JMenuItem("Start Game");
	final JMenuItem game_pause = new JMenuItem("Pause");
	final JMenuItem game_skip = new JMenuItem("Skip To Level...");
	final JMenuItem game_abort= new JMenuItem("Abort Game");
	final JMenuItem debugStatesMenuItem = new JMenuItem("Debug States");
	final JMenuItem frameBufferStatesMenuItem = new JMenuItem("Framebuffer States");
	final JMenuItem gpuMemDump = new JMenuItem("Dump GPU Memory");
	final JMenuItem codePageDump = new JMenuItem("Dump Code Pages");
	final JMenuItem debugSinglet = new JMenuItem("Singlet (fill)");
	// Accellerator keys
	file_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	game_pause.setAccelerator(KeyStroke.getKeyStroke("F3"));
	
	fbsw = new FramebufferStateWindow(tr);
	configWindow = new ConfigWindow(tr.getTrConfig());
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
	final Action pauseAction = new AbstractAction("Pause Button"){
	    private static final long serialVersionUID = -5172325691052703896L;
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			final Game game = tr.getGame();
			game.setPaused(!game.isPaused());
			return null;
		    }});
	    }};
	game_pause.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent evt) {
		pauseAction.actionPerformed(evt);
	    }});
	
	String pauseKey = "PAUSE_KEY";
	game_pause.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P,0), pauseKey);
	game_pause.getActionMap().put(pauseKey, pauseAction);
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
		configWindow.setVisible(true);
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
            game_pause.setEnabled(false);
            game_start.setEnabled(false);
            game_skip.setEnabled(false);
            game_abort.setEnabled(false);
            gameMenu.add(game_start);
            gameMenu.add(game_pause);
            gameMenu.add(game_skip);
            gameMenu.add(game_abort);
            debugMenu.add(debugSinglet);
            debugMenu.add(gpuMemDump);
            debugMenu.add(codePageDump);
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
	            rw.setVisible(false);//Frame must be invisible to modify.
		    rw.setJMenuBar(mb);
		    mb.add(file);
		    mb.add(gameMenu);
		    mb.add(debugMenu);
		    rw.setVisible(true);
		}});
	}catch(Exception e){tr.showStopper(e);}
	
	pausePCL = new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		if(evt.getPropertyName().contentEquals("paused"))
		    game_pause.setText((Boolean)evt.getNewValue()==true?"Unpause":"Pause");
	    }//end if(paused)
	};//end gamePCL
	
	tr.addPropertyChangeListener("game", new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		game_pause.setEnabled(evt.getNewValue()!=null);
		game_new.setEnabled(evt.getNewValue()==null);
		game_skip.setEnabled(evt.getNewValue()!=null);
		game_abort.setEnabled(evt.getNewValue()!=null);
	    }});
	
	IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
	tr.addPropertyChangeListener("game", gameIP);
	gameIP.addTargetPropertyChangeListener("paused", pausePCL);
	gameIP.addTargetPropertyChangeListener("currentMission", new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent pc) {
		game_start.setEnabled(pc.getNewValue()!=null && !tr.getGame().isInGameplay());
	    }});
	gameIP.addTargetPropertyChangeListener("inGameplay", new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent pc) {
		game_start.setEnabled(pc.getNewValue()!=null && pc.getNewValue()==Boolean.FALSE);
	    }});
    }//end constructor
}//end MenuSystem
