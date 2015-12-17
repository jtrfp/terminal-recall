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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jtrfp.trcl.core.RootWindow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jogamp.newt.event.KeyEvent;

@Component
public class SwingMenuSystem implements MenuSystem {
    private final SubMenu rootNode;
    private final RootWindow rw;
    
    //private final FramebufferStateWindow fbsw;
    //private final LevelSkipWindow	levelSkipWindow;
    //private final IndirectProperty<Game>game      = new IndirectProperty<Game>();
    //private final IndirectProperty<Boolean>paused = new IndirectProperty<Boolean>();
    //private final JCheckBoxMenuItem	view_crosshairs = new JCheckBoxMenuItem("Crosshairs");
    //private final JCheckBoxMenuItem view_sat = new JCheckBoxMenuItem("Satellite");
    final JMenu file = new JMenu("File"), 
	    gameMenu = new JMenu("Game"),
	    debugMenu = new JMenu("Debug"),
	    viewMenu = new JMenu("View");
    
    @Autowired
    public SwingMenuSystem(RootWindow rw){
	rootNode = new SubMenu(rw.getJMenuBar());
	this.rw = rw;
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
	//final JMenuItem view_sat = new JCheckBoxMenuItem("Satellite");
	// Accellerator keys
	file_quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_MASK));
	
	//view_sat.setAccelerator(KeyStroke.getKeyStroke("TAB"));
	/*
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
			tr.getGameShell().newGame(null);
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
		final Mission mission = ((TVF3Game)tr.getGame()).getCurrentMission();
		mission.setSatelliteView(view_sat.isSelected());
	    }};
	    
	view_sat.addActionListener(satelliteAction);
	
	
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
		game_start.setEnabled(pc.getNewValue()!=null && !((TVF3Game)tr.getGame()).isInGameplay());
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
		final Game game = ((TVF3Game)tr.getGame());
		if(game==null)
		    return;
		if(evt.getNewValue()==Boolean.TRUE)
		    view_sat.setEnabled(false);
		else if(((TVF3Game)tr.getGame()).getCurrentMission()!=null)
		    view_sat.setEnabled(game.getCurrentMission().getMissionMode() instanceof Mission.AboveGroundMode);
	    }});
	    */
    }//end constructor

    /**
     * @return the view_crosshairs
     */
    /*
    public JCheckBoxMenuItem getView_crosshairs() {
        return view_crosshairs;
    }*/

    /**
     * @return the gameMenu
     */
    public JMenu getGameMenu() {
        return gameMenu;
    }
/*
    public JCheckBoxMenuItem getView_sat() {
        return view_sat;
    }*/

    @Override
    public void addMenuItem(String... path) throws IllegalArgumentException {
	rootNode.addMenuItem(0, path);
    }

    @Override
    public void removeMenuItem(String... path) throws IllegalArgumentException {
	rootNode.removeMenuItem(0, path);
    }

    @Override
    public void addMenuItemListener(ActionListener l, String... path)
	    throws IllegalArgumentException {
	rootNode.addMenuItemListener(l, 0, path);
    }

    @Override
    public void removeMenuItemListener(ActionListener l, String... path)
	    throws IllegalArgumentException {
	rootNode.removeMenuItemListener(l, 0, path);
    }

    @Override
    public void setMenuItemEnabled(boolean enabled, String... path)
	    throws IllegalArgumentException {
	rootNode.setMenuItemEnabled(enabled, 0, path);
    }
    
    private MenuNode getRootNode(){
	return rootNode;
    }
    
    private abstract class MenuNode{
	private final String name;
	public MenuNode(String name){
	    this.name=name;
	}//end constructor
	public String getName() {
	    return name;
	}
	
	public abstract void addMenuItem   (int index, String ... path) throws IllegalArgumentException;
	public abstract void removeMenuItem(int index, String ... path) throws IllegalArgumentException;
	public abstract void addMenuItemListener   (ActionListener l, int index, String ... path)    throws IllegalArgumentException;
	public abstract void removeMenuItemListener(ActionListener l, int index, String ... path) throws IllegalArgumentException;
	public abstract void setMenuItemEnabled    (boolean enabled,  int index, String ... path)      throws IllegalArgumentException;
	public abstract boolean isEmpty();
	public abstract void destroy();
    }//end MenuNode
    
    private class SubMenu extends MenuNode{
	private final Map<String,MenuNode> nameMap = new HashMap<String,MenuNode>();
	private final JComponent item;
	private JComponent parent;
	
	protected SubMenu(String name){
	    super(name);
	    this.item = new JMenu(name);
	}
	
	public SubMenu(String name, JComponent parent) {
	    this(name);
	    parent.add(item);
	    rw.invalidate();
	    rw.validate();
	    //rw.revalidate();
	    this.parent = parent;
	}

	public SubMenu(JComponent delegate) {
	    super("root");
	    item = delegate;
	}

	@Override
	public void addMenuItem(int index, String... path)
		throws IllegalArgumentException {
	    final String thisName = path[index];
	    MenuNode node = nameMap.get(thisName);
	    if(node==null){//No pre-existing node
		if(index==path.length-1){//Leaf
		    node = new MenuItem(thisName, this.item);
		}else{//Stem
		    node = new SubMenu(thisName, this.item);
		    node.addMenuItem(index+1, path);
		}
		nameMap.put(thisName, node);
	    }//end if(node==null)
	    else{// !null
		if(index!=path.length-1)
		    node.addMenuItem(index+1, path);
		else
		    throw new IllegalArgumentException("Cannot add item as there is a submenu already in its place. Path[index]="+path[index]+" index="+index+" this="+getName());
	    }//end !null
	}//end addMenuItem(...)

	@Override
	public void removeMenuItem(int index, String... path)
		throws IllegalArgumentException {
	    final String thisName = path[index];
	    MenuNode node = nameMap.get(thisName);
	    if(node!=null){
		if(index==path.length-1){// thisName is Leaf
		    node.destroy();
		    nameMap.remove(thisName);
		}else{//Stem
		    assert !(node instanceof MenuItem);
		    node.removeMenuItem(index+1, path);
		    if(node.isEmpty())
			nameMap.remove(thisName);
		    }
		}//end if(stem)
	    else
		throw new IllegalArgumentException("Could not find leaf menu item `"+thisName+"` in "+getName());
	    if(isEmpty()){
		destroy();
	    }//end if(node!=null)
	}

	@Override
	public void addMenuItemListener(ActionListener l, int index,
		String... path) throws IllegalArgumentException {
	    final MenuNode node = nameMap.get(path[index]);
	    if(node == null)
		throw new IllegalArgumentException("Failed to find node: `"+path[index]+"` at index "+index);
	    node.addMenuItemListener(l, index+1, path);
	}

	@Override
	public void removeMenuItemListener(ActionListener l, int index,
		String... path) throws IllegalArgumentException {
	    nameMap.get(path[index]).removeMenuItemListener(l, index+1, path);
	}

	@Override
	public void setMenuItemEnabled(boolean enabled, int index,
		String... path) throws IllegalArgumentException {
	    final MenuNode node = nameMap.get(path[index]);
	    if(node!=null)
	     node.setMenuItemEnabled(enabled, index+1, path);
	    else
		throw new IllegalArgumentException("Cannot find subnode "+path[index]+" inside of submenu "+getName());
	}
	
	private void checkNonLeafRequest(int index, String ... path){
	    if(!path[index].contentEquals(getName()))
		throw new IllegalArgumentException("Supplied path non-leaf name `"+path[index]+"` doesn't match name of this non-leaf `"+getName()+"`");
	    if(index>=path.length-1)
		throw new IllegalArgumentException("Requested a leaf but this is not a leaf. Index="+index+" path="+path[index]);
	}

	@Override
	public boolean isEmpty() {
	    return nameMap.isEmpty();
	}

	@Override
	public void destroy() {
	    if(parent!=null){
	     parent.remove(item);
	     rw.invalidate();
	     rw.validate();
	     //rw.revalidate();
	     }
	}//end destroy()
    }//end SubMenu
    
    private class MenuItem extends MenuNode{
	private final JComponent parent;
	private final JMenuItem item;
	//private final Collection<ActionListener> menuItemListeners = new HashSet<ActionListener>();
	
	public MenuItem(String name, JComponent parent) {
	    super(name);
	    this.parent= parent;
	    item       = new JMenuItem(name);
	    item.setEnabled(false);
	    parent.add(item);
	    rw.invalidate();
	    rw.validate();
	    //rw.revalidate();
	}//end constructor

	@Override
	public void addMenuItem(int index, String... path)
		throws IllegalArgumentException {
	    throw new UnsupportedOperationException("Cannot add item to Menu item. Path="+path+" index="+index);
	}

	@Override
	public void removeMenuItem(int index, String... path)
		throws IllegalArgumentException {
	    if(index != path.length-1)
	      throw new UnsupportedOperationException("Cannot remove item from Menu item. Path="+path+" index="+index);
	    if(!path[index].contentEquals(getName()))
		throw new IllegalArgumentException("Name mismatch. Got "+path[index]+" expected "+getName());
	    parent.remove(item);
	}

	@Override
	public void addMenuItemListener(ActionListener l, int index,
		String... path) throws IllegalArgumentException {
	    checkLeafRequest(index, path);
	    item.addActionListener(l);
	}

	@Override
	public void removeMenuItemListener(ActionListener l, int index,
		String... path) throws IllegalArgumentException {
	    checkLeafRequest(index, path);
	    item.removeActionListener(l);
	}

	@Override
	public void setMenuItemEnabled(boolean enabled, int index,
		String... path) throws IllegalArgumentException {
	    checkLeafRequest(index,path);
	    item.setEnabled(enabled);
	}
	
	private void checkLeafRequest(int index, String ... path){
	    if(!path[path.length-1].contentEquals(getName()))
		throw new IllegalArgumentException("Supplied path leaf name `"+path[path.length-1]+"` doesn't match name of this leaf `"+getName()+"`");
	    if(!path[path.length-1].contentEquals(getName()))
		throw new IllegalArgumentException("Supplied path leaf name `"+path[path.length-1]+"` doesn't match name of this leaf `"+getName()+"`");
	}

	@Override
	public boolean isEmpty() {
	    return true;
	}

	@Override
	public void destroy() {
	    if(parent!=null && item !=null){
		parent.remove(item);
		rw.invalidate();
		rw.validate();
		//rw.revalidate();
		}
	}//end destroy()
    }//end MenuItem
}//end MenuSystem
