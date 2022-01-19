/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf.ui;

import java.awt.BorderLayout;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import lombok.Getter;

public class TreeSelectionUI implements ObjectEditorUI<DefaultMutableTreeNode> {
    private PropertyEditBinding<DefaultMutableTreeNode> binding;
    @Getter(lazy=true)
    private final JTree tree = new JTree(new DefaultMutableTreeNode("[unnamed]")) {
	protected void setExpandedState(TreePath path, boolean state) {
	    if( state )
		super.setExpandedState(path, state);
	}//end setExpandedState(...)
    };
    private final JScrollPane scrollPane = new JScrollPane();
    private boolean treePopulated = false;
    private JPanel rootPanel = new JPanel(new BorderLayout());

    public TreeSelectionUI() {
	super();
	final JTree tree = getTree();
	SwingUtilities.invokeLater(()->{
	    tree.setExpandsSelectedPaths(true);
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    tree.setRootVisible(false);
	    tree.addTreeSelectionListener(new TreeSelectionListener() {
		public void valueChanged(TreeSelectionEvent evt) {
		    for (TreePath path:evt.getPaths()) {
			DefaultMutableTreeNode node =
				(DefaultMutableTreeNode)path.getLastPathComponent();
			if (!node.isLeaf())
			    ((JTree)evt.getSource()).removeSelectionPath(path);
		    }
		}
	    });

	    scrollPane.setViewportView(tree);
	    rootPanel.add(scrollPane);
	    
	});
    }//end constructor

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public void proposeApplySettings() {
	binding.proposeApply();
    }

    @Override
    public void proposeRevertSettings(long revertTimeMillis) {
	binding.proposeRevert(revertTimeMillis);
    }
    
    private DefaultMutableTreeNode getSelection() {
	return (DefaultMutableTreeNode)(getTree().getLastSelectedPathComponent());
    }
    
    private void setSelection(DefaultMutableTreeNode selection) {
	SwingUtilities.invokeLater(()->{
	    final JTree tree = getTree();
	    final TreePath newPath = new TreePath(selection.getPath());
	    final TreeSelectionModel sm = tree.getSelectionModel();
	    sm.setSelectionPath(null);
	    sm.setSelectionPath(newPath);
	    assert (sm.isPathSelected(newPath));
	    tree.repaint();
	    });
    }
    
    @Override
    public void configure(Consumer<DefaultMutableTreeNode> setter,
	    Supplier<DefaultMutableTreeNode> getter, Set<Annotation> annotations, String humanReadablePropertyName) {
	binding = new PropertyEditBinding<DefaultMutableTreeNode>(getter, setter, 
		()->getSelection(),//Get state
		(newStateTreeNode)->{//Set state
		    if(Objects.equals(getSelection(), newStateTreeNode))
			return;
		    if(newStateTreeNode!=null && !treePopulated)
			populateUITree(newStateTreeNode);
		    setSelection(newStateTreeNode);
		    rootPanel.setBorder(new TitledBorder(humanReadablePropertyName));
	});
    }//end configure(...)
    
    private void populateUITree(DefaultMutableTreeNode sourceNode) {
	final JTree tree = getTree();
	final DefaultTreeModel model = (DefaultTreeModel)(tree.getModel());
	SwingUtilities.invokeLater(()->{
	    model.setRoot(sourceNode.getRoot());
	    model.reload();
	    for( int i = 0 ; i < tree.getRowCount(); i++ )
		tree.expandRow(i);
	});
	treePopulated = true;
    }//end populateUITree
    
    @Override
    public boolean isNeedingRestart() {
	return false;
    }//end constructor

}//end MutableListUI
