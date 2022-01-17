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

import java.awt.GridLayout;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jtrfp.trcl.tools.Util;

public class ComboBoxUI extends AbstractObjectEditorUI<DefaultMutableTreeNode> {
    private JPanel rootPanel = new JPanel();
    private JLabel label = new JLabel();
    private final JComboBox<DefaultMutableTreeNode> comboBox = new JComboBox<>();
    private DefaultComboBoxModel<DefaultMutableTreeNode> model;
    private boolean choicesSet = false;
    
    public ComboBoxUI() {
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(comboBox);
	model = (DefaultComboBoxModel<DefaultMutableTreeNode>)comboBox.getModel();
	model.removeAllElements();
    }//end constructor
    
    private final Consumer<DefaultMutableTreeNode> valueConsumer = (x)->{
	if(!choicesSet)
	    setupChoices((DefaultMutableTreeNode)(x.getRoot()));
	model.setSelectedItem(x);
    };
    
    private final Supplier<DefaultMutableTreeNode> valueSupplier = ()->(DefaultMutableTreeNode)model.getSelectedItem();

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<DefaultMutableTreeNode> getPropertyUISupplier() {
	return valueSupplier;
    }

    @Override
    public Consumer<DefaultMutableTreeNode> getPropertyUIConsumer() {
	return valueConsumer;
    }
    
    @Override
    public void configure(Consumer<DefaultMutableTreeNode> propertySetter, Supplier<DefaultMutableTreeNode> propertyGetter, Set<Annotation> annotations, String humanReadablePropertyName) {
	super.configure(propertySetter, propertyGetter, annotations, humanReadablePropertyName);
    }//end configure(...)
    
    private void setupChoices(DefaultMutableTreeNode root) {
	Util.getLeaves(root).stream().forEach(x->model.addElement(x));
	choicesSet = true;
    }//end setupChoices(...)

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
	label.invalidate();
    }

}//end ComboBoxUI
