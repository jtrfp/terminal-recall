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
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.file.VOXFile.MissionLevel;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.game.Game.CanceledException;
import org.jtrfp.trcl.game.TVF3Game;

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
		
		if(tr.getRootWindow()!=null)
		    setupListeners();
	}//end constructor

	private void setupListeners(){
	    /*
	    final IndirectProperty<Game> gameIP = new IndirectProperty<Game>();
	    tr.addPropertyChangeListener("game", gameIP);
	    gameIP.addTargetPropertyChangeListener("vox", new PropertyChangeListener(){
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		    if(evt.getNewValue()!=null){
			levelLM.clear();
			final VOXFile vox = (VOXFile)evt.getNewValue();
			for(MissionLevel ml:vox.getLevels()){
			    levelLM.addElement(ml.getLvlFile());
			}//end for(levels)
		    }//end if(!null)
		}});
	    gameIP.addTargetPropertyChangeListener("currentMission", new PropertyChangeListener(){
		    @Override
		    public void propertyChange(PropertyChangeEvent evt) {
			    levelList.setSelectedValue(evt.getNewValue(), true);
		    }});
	    */
	    btnGo.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt) {
		    LevelSkipWindow.this.setVisible(false);
		    tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
			    try{
			    ((TVF3Game)tr.getGame()).abortCurrentMission();
			    ((TVF3Game)tr.getGame()).setLevelIndex(levelList.getSelectedIndex());
			    ((TVF3Game)tr.getGame()).doGameplay();
			    }catch(CanceledException e){}//Do nothing.
			    return null;
			}});
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
}//end LevelSkipWindow
