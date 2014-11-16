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

package org.jtrfp.trcl.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.dbg.FramebufferStateWindow;
import org.jtrfp.trcl.flow.Game;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.gui.ConfigWindow;
import org.jtrfp.trcl.mem.GPUMemDump;

import com.jogamp.newt.event.KeyEvent;

public class MenuSystem {
    private final FramebufferStateWindow fbsw;
    private final ConfigWindow		configWindow;
    private final PropertyChangeListener pausePCL;
    private final IndirectProperty<Game>game      = new IndirectProperty<Game>();
    private final IndirectProperty<Boolean>paused = new IndirectProperty<Boolean>();
    
    public MenuSystem(final TR tr){
	final RootWindow rw = tr.getRootWindow();
	final JMenu file = new JMenu("File"), window = new JMenu("Window"), gameMenu = new JMenu("Game");
	// And menus to menubar
	final JMenuItem file_quit = new JMenuItem("Quit");
	final JMenuItem file_config = new JMenuItem("Configure");
	final JMenuItem game_new = new JMenuItem("New Game");
	final JMenuItem game_pause = new JMenuItem("Pause");
	final JMenuItem debugStatesMenuItem = new JMenuItem("Debug States");
	final JMenuItem frameBufferStatesMenuItem = new JMenuItem("Framebuffer States");
	final JMenuItem gpuMemDump = new JMenuItem("Dump GPU Memory");
	// Accellerator keys
	file_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	game_pause.setAccelerator(KeyStroke.getKeyStroke("F3"));
	
	fbsw = new FramebufferStateWindow(tr);
	configWindow = new ConfigWindow(tr.getTrConfig());
	
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
	game_pause.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			final Game game = tr.getGame();
			game.setPaused(!game.isPaused());
			return null;
		    }});
	    }});
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
			new GPUMemDump(tr);
			return null;
		    }});
	    };
	});
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
	    file.add(gpuMemDump);
	    file.add(file_quit);
	    window.add(debugStatesMenuItem);
	    window.add(frameBufferStatesMenuItem);
            gameMenu.add(game_new);
            game_pause.setEnabled(false);
            gameMenu.add(game_pause);
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
	            rw.setVisible(false);//Frame must be invisible to modify.
		    rw.setJMenuBar(mb);
		    mb.add(file);
		    mb.add(gameMenu);
		    mb.add(window);
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
	    }});
	
	tr.addPropertyChangeListener("game", new IndirectProperty<Game>().
		addTargetPropertyChangeListener("paused", pausePCL));
	//game.addTargetPropertyChangeListener("paused", pausePCL);
    }//end constructor
}//end MenuSystem
