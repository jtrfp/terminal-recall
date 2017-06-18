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
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executor;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class SwingMenuSystemFactory implements FeatureFactory<RootWindow> {

    public static class SwingMenuSystem implements Feature<RootWindow>, MenuSystem {
	private SubMenu rootNode;
	private JFrame rw;
	private Map<JMenuItem, Double> positionMap = new HashMap<JMenuItem, Double>();
	private Map<ActionListener,TransientThreadActionListener> proxyMap = new HashMap<ActionListener,TransientThreadActionListener>();

	final JMenu file = new JMenu("File"), 
		gameMenu = new JMenu("Game"),
		debugMenu = new JMenu("Debug"),
		viewMenu = new JMenu("View");

	/**
	 * @return the gameMenu
	 */
	public JMenu getGameMenu() {
	    return gameMenu;
	}

	@Override
	public synchronized void addMenuItem(double position, String... path) throws IllegalArgumentException {
	    rootNode.addMenuItem(position, 0, path);
	}

	@Override
	public synchronized void removeMenuItem(String... path) throws IllegalArgumentException {
	    rootNode.removeMenuItem(0, path);
	}

	@Override
	public synchronized void addMenuItemListener(ActionListener l, String... path)
		throws IllegalArgumentException {
	    rootNode.addMenuItemListener(l, 0, path);
	}

	@Override
	public synchronized void removeMenuItemListener(ActionListener l, String... path)
		throws IllegalArgumentException {
	    rootNode.removeMenuItemListener(l, 0, path);
	}

	@Override
	public synchronized void setMenuItemEnabled(boolean enabled, String... path)
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

	    public abstract void addMenuItem   (double position, int index, String ... path) throws IllegalArgumentException;
	    public abstract void removeMenuItem(int index, String ... path) throws IllegalArgumentException;
	    public abstract void addMenuItemListener   (ActionListener l, int index, String ... path)    throws IllegalArgumentException;
	    public abstract void removeMenuItemListener(ActionListener l, int index, String ... path) throws IllegalArgumentException;
	    public abstract void setMenuItemEnabled    (boolean enabled,  int index, String ... path)      throws IllegalArgumentException;
	    public abstract boolean isEmpty();
	    public abstract void destroy();
	}//end MenuNode

	protected void addSubMenuLater(final JMenu itemToAdd, final JComponent parent){
	    SwingUtilities.invokeLater(new Runnable(){
		public void run(){
		    if(itemToAdd!=null){
			parent.add(itemToAdd);
			refreshOrdering(parent);
		    }
		    rw.invalidate();
		    rw.validate();
		}});
	}//end addSubMenuLater(...)

	private class SubMenu extends MenuNode{
	    private final Map<String,MenuNode> nameMap = new HashMap<String,MenuNode>();
	    private final JComponent item;
	    private JComponent parent;

	    protected SubMenu(double position, String name){
		super(name);
		this.item = new JMenu(name);
		positionMap.put((JMenu)item, position);
	    }

	    public SubMenu(double position, String name, final JComponent parent) {
		this(position, name);
		addSubMenuLater((JMenu)item, parent);
		this.parent = parent;
	    }

	    public SubMenu(JComponent delegate) {
		super("root");
		item = delegate;
	    }

	    @Override
	    public void addMenuItem(double position, int index, String... path)
		    throws IllegalArgumentException {
		final String thisName = path[index];
		MenuNode node = nameMap.get(thisName);
		if(node==null){//No pre-existing node
		    if(index==path.length-1){//Leaf
			node = new MenuItem(position, thisName, this.item);
		    }else{//Stem
			node = new SubMenu(.5, thisName, this.item);
			node.addMenuItem(position, index+1, path);
		    }
		    nameMap.put(thisName, node);
		}//end if(node==null)
		else{// !null
		    if(index!=path.length-1)
			node.addMenuItem(position, index+1, path);
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
		final MenuNode node = nameMap.get(path[index]);
		if(node == null)
		    throw new RuntimeException("Node of name `"+path[index]+"` not found.");
		node.removeMenuItemListener(l, index+1, path);
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
		    final JComponent it = item;
		    SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
			    if(it!=null)
				parent.remove(it);
			    rw.invalidate();
			    rw.validate();
			    //rw.revalidate();
			}});
		}//end if(!null)
	    }//end destroy()

	    public void setPosition(double position, int index, String ... path) {
		final String thisName = path[index];
		MenuNode node = nameMap.get(thisName);
		if(node != null){
		    if(index == path.length - 1){//This is the node in question
			if(node instanceof MenuItem)
			    positionMap.put(((MenuItem)node).item, position);
			else if(node instanceof SubMenu)
			    positionMap.put((JMenuItem)((SubMenu)node).item, position);
		    }
		    else//We're not there yet.
			((SubMenu)node).setPosition(position, index+1, path);
		}else throw new IllegalArgumentException("Node entry of name "+thisName+" not found.");
	    }
	}//end SubMenu

	protected double positionOf(JMenuItem item){
	    final Double pos = positionMap.get(item);
	    if( pos == null )
		return .5;
	    return pos;
	}//end positionOf(...)

	protected class JMenuItemComparator implements Comparator<JMenuItem> {
	    @Override
	    public int compare(JMenuItem l, JMenuItem r) {
		final double leftPos  = positionOf(l);
		final double rightPos = positionOf(r);
		final double result   = leftPos - rightPos;
		if(result != 0)
		    return (int)(result*10000);
		return l.getText().compareTo(r.getText());
	    }
	}//end JMenuItemComparator

	protected class JMenuComparator implements Comparator<JMenu> {
	    @Override
	    public int compare(JMenu l, JMenu r) {
		final double leftPos  = positionOf(l);
		final double rightPos = positionOf(r);
		final double result = leftPos - rightPos;
		if(result != 0)
		    return (int)(result*10000);
		return l.getText().compareTo(r.getText());
	    }
	}//end JMenuComparator

	protected void addMenuItemLater(final JMenuItem item,  final JMenu component){
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    if(item!=null){
			component.add(item);
			refreshOrdering(component);
		    }
		    rw.invalidate();
		    rw.validate();
		    //rw.revalidate();
		}});
	}//end addMenuItemLater(...)

	protected void refreshOrdering(final JComponent component){
	    final TreeSet<JMenuItem> items = new TreeSet<JMenuItem>(new JMenuItemComparator());
	    if(component instanceof JMenu){
		for(java.awt.Component comp : ((JMenu)component).getMenuComponents())
		    if(comp instanceof JMenuItem)
			items.add((JMenuItem)comp);
	    }else if(component instanceof JMenuBar)
		for(MenuElement elm : ((JMenuBar)component).getSubElements())
		    if(elm instanceof JMenuItem)
			items.add((JMenuItem)elm);
	    for(JMenuItem jMenuItem : items)
		component.remove(jMenuItem);
	    for(JMenuItem jMenuItem : items)
		component.add(jMenuItem);
	    rw.invalidate();
	    rw.validate();
	}//end refreshOrderingLater(...)

	private class MenuItem extends MenuNode{
	    private final JComponent parent;
	    private final JMenuItem item;
	    public MenuItem(double position, String name, final JComponent parent) {
		super(name);
		this.parent= parent;
		item  = new JMenuItem(name);
		item.setEnabled(false);
		positionMap.put(item, position);
		addMenuItemLater(item, (JMenu)parent);
	    }//end constructor

	    @Override
	    public void addMenuItem(double position, int index, String... path)
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
		final JComponent it = item;
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			if(it!=null)
			    parent.remove(it);
		    }});
	    }

	    @Override
	    public void addMenuItemListener(final ActionListener l, int index,
		    String... path) throws IllegalArgumentException {
		checkLeafRequest(index, path);
		final JMenuItem it = item;
		if(it!=null){
		    final TransientThreadActionListener proxy = new TransientThreadActionListener(l);
		    proxyMap.put(l, proxy);
		    SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
				it.addActionListener(proxy);
			}});
		}//end if(!null)
	    }//end addMenuItemListener

	    @Override
	    public void removeMenuItemListener(final ActionListener l, int index,
		    String... path) throws IllegalArgumentException {
		checkLeafRequest(index, path);
		final JMenuItem it = item;
		if(it != null){
		    final ActionListener proxy = proxyMap.remove(l);
		    if(proxy != null)
			SwingUtilities.invokeLater(new Runnable(){
			    @Override
			    public void run() {
				it.removeActionListener(proxy);
			    }});
		}//end if(!null)
	    }//end removeMenuItemListener(...)

	    @Override
	    public void setMenuItemEnabled(final boolean enabled, int index,
		    String... path) throws IllegalArgumentException {
		checkLeafRequest(index,path);
		final JMenuItem it = item;
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			if(it!=null)
			    it.setEnabled(enabled);
		    }});
	    }//end seMenuItemEnabled(...)

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
		final JMenuItem item = this.item;
		if(parent!=null && item !=null){
		    SwingUtilities.invokeLater(new Runnable(){
			@Override
			public void run() {
			    parent.remove(item);
			    rw.invalidate();
			    rw.validate();
			}});
		}//end invokeLater
	    }//end destroy()
	}//end MenuItem

	@Override
	public void apply(RootWindow target) {
	    rw = target;
	    final JMenuBar [] menuBar = new JMenuBar[] {rw.getJMenuBar()};
	    try {
		if(menuBar[0] == null)
		    SwingUtilities.invokeAndWait(new Runnable(){
			@Override
			public void run() {
			    rw.setJMenuBar(menuBar[0] = new JMenuBar()); 
			    rw.invalidate();
			    rw.validate();
			}});
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    } catch (InvocationTargetException e) {
		e.printStackTrace();
	    }
	    rootNode = new SubMenu(menuBar[0]);
	}//end apply(...)

	@Override
	public void destruct(RootWindow target) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void setMenuPosition(double position, String... path) {
	    rootNode.setPosition(position, 0, path);
	}
    }//end SwingMenuSystem

    @Override
    public Feature<RootWindow> newInstance(RootWindow target) {
	return new SwingMenuSystem();
    }

    @Override
    public Class<RootWindow> getTargetClass() {
	return RootWindow.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SwingMenuSystem.class;
    }
    
    private static class TransientThreadActionListener implements ActionListener {
	private final ActionListener transientActionListener;
	
	public TransientThreadActionListener(ActionListener transientActionListener){
	    this.transientActionListener = transientActionListener;
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
	    final Executor executor = TransientExecutor.getSingleton();
	    synchronized(executor){
	    executor.execute(new Runnable(){
		@Override
		public void run() {
		    transientActionListener.actionPerformed(e);
		}});
	    }//end sync
	}//end actionPerformed(...)
    }//end TransientThreadActionListener
}//end SwingMenuSystemFactory
