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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class IntegerSpinnerUI extends AbstractObjectEditorUI<Integer> {
    private final JPanel rootPanel = new JPanel(), centerPanel = new JPanel();
    private JSpinner spinner;
    private final JLabel label = new JLabel();
    
    public IntegerSpinnerUI() {
	rootPanel.setLayout(new GridLayout(1,2));
	rootPanel.add(label);
    }//end constructor
    
    @Override
    public void configure(Consumer<Integer> propertySetter, Supplier<Integer> propertyGetter, Set<Annotation> annotations, String humanReadablePropertyName) {
	super.configure(propertySetter, propertyGetter, annotations, humanReadablePropertyName);
	int min = Integer.MIN_VALUE, max = Integer.MAX_VALUE, step = 1;
	for(Annotation ann : annotations) {
	    if( ann instanceof IntegerFormat ) {
		IntegerFormat fmt = (IntegerFormat)ann;
		min = fmt.min();
		max = fmt.max();
		step = fmt.stepSize();
	    }//end if(ImageFormat)
	}//end for(annotations)
	
	spinner = new JSpinner(new SpinnerNumberModel((int)(propertyGetter.get()),min,max,step));
	spinner.setPreferredSize(null);
	spinner.setMaximumSize(new Dimension(1024, 50));
	//rootPanel.add(spinner);
	centerPanel.setLayout(new GridBagLayout());
	centerPanel.add(spinner, new GridBagConstraints());
	rootPanel.add(centerPanel);
    }//end configure(...)

    @Override
    public JComponent getUIComponent() {
	return rootPanel;
    }

    @Override
    public Supplier<Integer> getPropertyUISupplier() {
	return ()->((Integer)(spinner.getValue()));
    }

    @Override
    public Consumer<Integer> getPropertyUIConsumer() {
	return (x)->{
	    if( x == null )
		return;
	    spinner.setValue((int)x); SwingUtilities.invokeLater(()->{spinner.repaint();});
	    };
    }

    @Override
    protected void setName(String humanReadableName) {
	label.setText(humanReadableName);
    }

}//end IntegerSpinnerUI
