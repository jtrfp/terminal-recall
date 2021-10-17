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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;
import org.jtrfp.trcl.flow.IndirectProperty;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.Game.CanceledException;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.shell.GameShellFactory;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class LevelSkipWindow extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -7174180501601275652L;
    private final TR tr;
    private final JButton btnGo;
    private final JButton btnCancel;
    private final JList levelList;
    private final DefaultListModel levelLM = new DefaultListModel();
    private WeakReference<Game> game = null;
    private GameShell gameShell;
    private boolean setup = false;
    private int levelSelectionIdx = -1;//XXX: Bug 259. getSelectedValue() sometimes returns null even if selected.
    	
    	public LevelSkipWindow(){
    	    this(null);
    	}//end constructor
    
	public LevelSkipWindow(TR tr) {
	    	this.tr=tr;
		setTitle("Skip To Level");
		setSize(180,300);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel goCancelPanel = new JPanel();
		getContentPane().add(goCancelPanel, BorderLayout.SOUTH);
		goCancelPanel.setLayout(new BorderLayout(0, 0));
		
		btnGo = new JButton("Go");
		btnGo.setToolTipText("Apply this level-skip and close the window");
		goCancelPanel.add(btnGo, BorderLayout.WEST);
		
		btnCancel = new JButton("Cancel");
		btnCancel.setToolTipText("Close the window without applying any changes");
		goCancelPanel.add(btnCancel, BorderLayout.EAST);
		
		JScrollPane levelListSP = new JScrollPane();
		getContentPane().add(levelListSP, BorderLayout.CENTER);
		
		levelList = new JList();
		levelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		levelList.setToolTipText("Select a level");
		levelList.setModel(levelLM);
		levelListSP.setViewportView(levelList);
		
		levelList.addListSelectionListener(new ListSelectionListener() {

		    @Override
		    public void valueChanged(ListSelectionEvent e) {
			levelSelectionIdx = e.getFirstIndex();
		    }});
	}//end constructor
	
	protected void proposeSetup(){
	    if(!isSetup() && getGameShell() != null && tr.getRootWindow() != null){
		setupListeners();
		initialSetup();
		setSetup(true);
		}
	}//end proposeSetup()
	
	protected void initialSetup(){
	    final GameShell gameShell = getGameShell();
	    final TVF3Game game = (TVF3Game)gameShell.getGame();
	    if(game == null)
		return;
	    final VOXFile vox = game.getVox();
	    if(vox == null)
		return;
	    refreshLevelLM(vox);
	}//end initialSetup()

	protected void refreshLevelLM(VOXFile vox ){
	    levelLM.clear();
	    for(MissionLevel ml:vox.getLevels()){
		levelLM.addElement(ml.getLvlFile());
	    }//end for(levels)
	}//end refreshLevelLM(...)

	private void setupListeners(){
	    final GameShell gameShell = getGameShell();
	    final IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
	    gameShell.addPropertyChangeListener(GameShellFactory.GAME, gameIP);
	    gameIP.addTargetPropertyChangeListener(TVF3Game.VOX, new PropertyChangeListener(){
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		    if(evt.getNewValue()!=null){
			final VOXFile vox = (VOXFile)evt.getNewValue();
			refreshLevelLM(vox);
		    }//end if(!null)
		}});
	    gameIP.addTargetPropertyChangeListener("currentMission", new PropertyChangeListener(){
		    @Override
		    public void propertyChange(PropertyChangeEvent evt) {
			    levelList.setSelectedValue(evt.getNewValue(), true);
		    }});
	    
	    btnGo.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt) {
		    LevelSkipWindow.this.setVisible(false);
		    final Executor executor = TransientExecutor.getSingleton();
		    synchronized(executor) {
			TransientExecutor.getSingleton().execute(new Runnable(){
			    @Override
			    public void run() {
				try{
				    final TR tr = Features.get(Features.getSingleton(), TR.class);
				    final GameShell gameShell = Features.get(tr,GameShell.class);
				    final TVF3Game game = (TVF3Game)gameShell.getGame();
				    game.abortCurrentMission();
				    
				    //game.setLevel(levelList.getSelectedValue().toString());
				    game.setLevel(levelLM.get(levelSelectionIdx).toString());
				    
				    game.doGameplay();
				} catch(CanceledException e) {}//No need to complain, just return.
				  catch(Exception e){e.printStackTrace();}//Do nothing.
			    }});
		    }//end sync(executor)
		}});
	    
	    btnCancel.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    LevelSkipWindow.this.setVisible(false);
		}});
	}//end setupListeners()

	public Game getGame() {
	    return game.get();
	}

	public void setGame(Game game) {
	    this.game = new WeakReference<Game>(game);
	    levelLM.clear();
	    if(!(game instanceof TVF3Game))
		throw new IllegalStateException("Expected TVF3Game. Got "+game.getClass().getName());
	    final TVF3Game g = (TVF3Game)game;
	    final VOXFile vox = g.getVox();
	    for(MissionLevel ml:vox.getLevels())
		levelLM.addElement(ml.getLvlFile());
	}//end setGame(...)

	public GameShell getGameShell() {
	    return gameShell;
	}

	public void setGameShell(GameShell gameShell) {
	    this.gameShell = gameShell;
	    proposeSetup();
	}

	public boolean isSetup() {
	    return setup;
	}

	protected void setSetup(boolean setup) {
	    this.setup = setup;
	}
}//end LevelSkipWindow
