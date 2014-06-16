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
package org.jtrfp.trcl.dbg;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class Reporter extends JFrame {

    private JPanel contentPane;
    private final JTree tree;
    private final DefaultMutableTreeNode top = new DefaultMutableTreeNode("debug");
    private JSplitPane splitPane;
    private JScrollPane leftScrollPane;
    private JScrollPane rightScrollPane;
    private JPanel infoBackdropPanel;
    private JLabel lblStringValue;
    private JLabel lblvalueHere;
    private Executor reportExecutor = Executors.newSingleThreadExecutor();
    private Executor updateExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean needsUpdating = new AtomicBoolean();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    Reporter frame = new Reporter();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the frame.
     */
    public Reporter() {
    	setTitle("Debug States");
	setBounds(300, 300, 850, 700);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	contentPane.setLayout(new BorderLayout(0, 0));
	setContentPane(contentPane);
	
	splitPane = new JSplitPane();
	contentPane.add(splitPane, BorderLayout.CENTER);
	
	leftScrollPane = new JScrollPane();
	splitPane.setLeftComponent(leftScrollPane);
	
	tree = new JTree(top);
	leftScrollPane.setViewportView(tree);
	
	rightScrollPane = new JScrollPane();
	splitPane.setRightComponent(rightScrollPane);
	
	splitPane.setResizeWeight(.65);
	
	infoBackdropPanel = new JPanel();
	rightScrollPane.setViewportView(infoBackdropPanel);
	GridBagLayout gbl_infoBackdropPanel = new GridBagLayout();
	gbl_infoBackdropPanel.columnWidths = new int[]{0, 260, 0};
	gbl_infoBackdropPanel.rowHeights = new int[]{0, 0};
	gbl_infoBackdropPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
	gbl_infoBackdropPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
	infoBackdropPanel.setLayout(gbl_infoBackdropPanel);
	
	lblStringValue = new JLabel("String Value:");
	GridBagConstraints gbc_lblStringValue = new GridBagConstraints();
	gbc_lblStringValue.insets = new Insets(0, 0, 0, 5);
	gbc_lblStringValue.gridx = 0;
	gbc_lblStringValue.gridy = 0;
	infoBackdropPanel.add(lblStringValue, gbc_lblStringValue);
	
	lblvalueHere = new JLabel("[UNSET]");
	GridBagConstraints gbc_lblvalueHere = new GridBagConstraints();
	gbc_lblvalueHere.anchor = GridBagConstraints.WEST;
	gbc_lblvalueHere.gridx = 1;
	gbc_lblvalueHere.gridy = 0;
	infoBackdropPanel.add(lblvalueHere, gbc_lblvalueHere);
	
	tree.addTreeSelectionListener(new TreeSelectionListener(){
	    @Override
	    public void valueChanged(TreeSelectionEvent evt) {
		final Object o = ((DefaultMutableTreeNode)evt.getPath().getLastPathComponent()).getUserObject();
		if(o != null){
		    if(o instanceof TreeEntry){
			final Object stored = ((TreeEntry)o).getStored();
		    	if(stored!=null)lblvalueHere.setText(stored.toString());
		    	else lblvalueHere.setText("[null]");
		    }//end if(TreeEntry)
		}//end if(!null)
	    }
	    
	});
    }//end constructor
    
    private void refreshNodeDetails(){
	tree.updateUI();
    }//end refreshModeDetails()
    
    public Reporter report(final String path, final Object item){
	reportExecutor.execute(new Runnable(){
	    @Override
	    public void run() {
		Scanner dotScanner = new Scanner(path);
		DefaultMutableTreeNode workNode = top;
		dotScanner.useDelimiter("\\.");
		while(dotScanner.hasNext()){
		    final String treeItem = dotScanner.next();
		    Enumeration<DefaultMutableTreeNode> en = workNode.children();
		    DefaultMutableTreeNode matchingNode=null;
		    while(en.hasMoreElements()){
			DefaultMutableTreeNode testNode = en.nextElement();
			if(((TreeEntry)testNode.getUserObject()).getLabel().contentEquals(treeItem)){
			    matchingNode=testNode;
			}//end if(label matches)
		    }//end while(children)
		    if(matchingNode!=null){
			workNode=matchingNode;
		    }else{
			final DefaultMutableTreeNode n =new DefaultMutableTreeNode(new TreeEntry(treeItem,null));
			workNode.add(n);
			final DefaultMutableTreeNode _workNode=workNode;
			SwingUtilities.invokeLater(new Runnable(){
			    @Override
			    public void run() {
				tree.expandPath(new TreePath(_workNode.getPath()));
				Reporter.this.notifyUpdate();
			    }
			});
			workNode=n;
		    }//end matchingNode==null
		}//end while(hasNext())
		//Should be at the leaf of the tree. Set the child.
		((TreeEntry)workNode.getUserObject()).setStored(item);
		if(tree.getSelectionPath()!=null){
		    if(workNode == tree.getSelectionPath().getLastPathComponent()){
		        lblvalueHere.setText(item.toString());
		        refreshNodeDetails();
		    }//end if(selected)
		}//end if(path1=null)
	    }});
	return this;
    }//end report(...)
    
    protected void notifyUpdate() {
	if(needsUpdating.compareAndSet(false, true)){
	    //Enqueue a delayed update
	    updateExecutor.execute(new Runnable(){
		@Override
		public void run() {
		    try{Thread.currentThread().sleep(2000);}catch(Exception e){e.printStackTrace();}
		    SwingUtilities.invokeLater(new Runnable(){
			    @Override
			    public void run() {
				needsUpdating.set(false);//Makes sure anything written during updateUI isn't missed.
				tree.updateUI();
			    }//end swing.run()
			});
		}});//end updateExecutor.execute(){}
	}//end if(!needsupdating)
    }//end notifyUpdate

    private class TreeEntry{
	private final String label;
	private Object stored;
	public TreeEntry(String label, Object stored){
	    this.label=label;
	    this.stored=stored;
	}
	public String getLabel(){return label;}
	public Object getStored(){return stored;}
	public void setStored(Object o){stored=o;}
	@Override
	public String toString(){return label;}
    }//end TreeEntry

}
