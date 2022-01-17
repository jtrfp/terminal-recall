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

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class CheckboxUI extends AbstractObjectEditorUI<Boolean> {
    private final JPanel rootPanel = new JPanel();
    private final JCheckBox checkBox = new JCheckBox();
    private final JLabel label = new JLabel();
    
    public CheckboxUI() {
	super();
	checkBox.setPreferredSize(null);
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(checkBox);
	rootPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE,50));
	rootPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,10));
    }//end constructor

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<Boolean> getPropertyUISupplier() {
	return ()->checkBox.isSelected();
    }

    @Override
    public Consumer<Boolean> getPropertyUIConsumer() {
	return (x)->{checkBox.setSelected(x); SwingUtilities.invokeLater(()->{checkBox.repaint();});};
    }

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
	label.invalidate();
    }

}//end StringEditorUI
