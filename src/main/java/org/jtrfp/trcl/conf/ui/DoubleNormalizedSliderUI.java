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
import javax.swing.JSlider;
import javax.swing.SwingUtilities;

public class DoubleNormalizedSliderUI extends AbstractObjectEditorUI<Double> {
    private final JPanel rootPanel = new JPanel();
    private final JSlider checkBox = new JSlider();
    private final JLabel label = new JLabel();
    private static final double RESOLUTION = 1000.;
    
    public DoubleNormalizedSliderUI() {
	checkBox.setPreferredSize(null);
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
	rootPanel.add(checkBox);
	checkBox.setMinimum(0);
	checkBox.setMaximum((int)RESOLUTION);
    }//end constructor

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<Double> getPropertyUISupplier() {
	return ()->checkBox.getValue()/RESOLUTION;
    }

    @Override
    public Consumer<Double> getPropertyUIConsumer() {
	return (x)->{checkBox.setValue((int)(x*RESOLUTION)); SwingUtilities.invokeLater(()->{checkBox.repaint();});};
    }

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
    }

}//end StringEditorUI
