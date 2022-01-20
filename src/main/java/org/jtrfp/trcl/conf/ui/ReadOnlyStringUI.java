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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ReadOnlyStringUI implements ObjectEditorUI<String> {
    private final JPanel rootPanel = new JPanel();
    private final JLabel label = new JLabel();
    private final JTextField textField = new JTextField("",64);
    private Supplier<String> propertyGetter;
    
    public ReadOnlyStringUI() {
	textField.setPreferredSize(null);
	textField.setEditable(false);
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(textField);
    }//end constructor

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public boolean isNeedingRestart() {
	return false;
    }

    @Override
    public void proposeApplySettings() {}

    @Override
    public void proposeRevertSettings(long revertTimeMillis) {
	SwingUtilities.invokeLater(()->{
	    textField.setText(propertyGetter.get());
	});
    }

    @Override
    public void configure(Consumer<String> propertySetter,
	    Supplier<String> propertyGetter, Set<Annotation> annotations,
	    String humanReadablePropertyName) {
	this.propertyGetter = propertyGetter;
	label.setText(humanReadablePropertyName);
    }//end configure()
}//end StringEditorUI
