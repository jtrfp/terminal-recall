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
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class TextFieldUI extends AbstractObjectEditorUI<String> {
    private final JPanel rootPanel = new JPanel();
    private final JLabel label = new JLabel();
    private final JTextField textField = new JTextField("",50);
    
    public TextFieldUI() {
	textField.setPreferredSize(null);
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(textField);
    }//end constructor

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<String> getPropertyUISupplier() {
	return ()->textField.getText();
    }

    @Override
    public Consumer<String> getPropertyUIConsumer() {
	return (x)->{textField.setText(x); SwingUtilities.invokeLater(()->{textField.repaint();});};
    }

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
	label.invalidate();
    }

}//end StringEditorUI
