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
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class EnumComboBoxUI extends AbstractObjectEditorUI<Enum<?>> {
    private JPanel rootPanel = new JPanel();
    private JLabel label = new JLabel();
    private final JComboBox<Enum<?>> comboBox = new JComboBox<>();
    private DefaultComboBoxModel<Enum<?>> model;
    private Class<? extends Enum<?>> valueClass;
    
    public EnumComboBoxUI() {
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(comboBox);
	model = (DefaultComboBoxModel<Enum<?>>)comboBox.getModel();
	model.removeAllElements();
    }//end constructor
    
    private final Consumer<Enum<?>> valueConsumer = (x)->{
	if(x == null) {
	    model.setSelectedItem(null);
	    return;
	    }
	@SuppressWarnings("unchecked")
	final Class<? extends Enum<?>> newValueClass = (Class<? extends Enum<?>>)x.getClass();
	if(!newValueClass.equals(valueClass)) {
	    valueClass = newValueClass;
	    model.removeAllElements();
	    model.addAll(Arrays.asList(newValueClass.getEnumConstants()));
	}
	model.setSelectedItem(x);
    };
    
    private final Supplier<Enum<?>> valueSupplier = ()->(Enum<?>)model.getSelectedItem();

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<Enum<?>> getPropertyUISupplier() {
	return valueSupplier;
    }

    @Override
    public Consumer<Enum<?>> getPropertyUIConsumer() {
	return valueConsumer;
    }

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
	label.invalidate();
    }

}//end EnumComboBoxUI
