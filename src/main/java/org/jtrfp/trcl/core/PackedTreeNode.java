/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017-2022 Chuck Ritola
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

import javax.swing.tree.DefaultMutableTreeNode;

import lombok.Getter;
import lombok.Setter;

/**
 * Makes DefaultMutableTreeNodes more XMLEncoder-friendly by packing the whole tree with it.
 * @author Chuck Ritola
 *
 */

@Getter @Setter
public class PackedTreeNode {
 private DefaultMutableTreeNode node;
 private DefaultMutableTreeNode root;
 
 public PackedTreeNode() {}
 
 public PackedTreeNode(DefaultMutableTreeNode node) {
     this.node = node;
     this.root = (DefaultMutableTreeNode)(node.getRoot());
 }
}//end PackedTreeNode
