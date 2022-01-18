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
import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jtrfp.trcl.core.Feature;

public class BasicSettingsPanel<FEATURE_CLASS> extends JPanel implements FeatureConfigurationUI<FEATURE_CLASS> {
    private static final long serialVersionUID = -4510144755645875748L;
    private final JPanel classNamePanel = new JPanel();
    public BasicSettingsPanel(Feature<?> configuredFeature) {
	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	this.setBorder(new TitledBorder("Basic Settings"));
	this.setMinimumSize(new Dimension(250,100));

	classNamePanel.setLayout(new BoxLayout(classNamePanel, BoxLayout.Y_AXIS));
	classNamePanel.add(new JLabel("Feature Class:"));
	classNamePanel.add(new JLabel(configuredFeature.getClass().getName()));
	add(classNamePanel);
	setBorder(new TitledBorder("Basic Settings"));
	invalidate();
    }//end constructor
    @Override
    public void apply(Object target) {
	// TODO Auto-generated method stub

    }
    @Override
    public void destruct(Object target) {
	// TODO Auto-generated method stub
    }
    @Override
    public JComponent getUIComponent() {
	return this;
    }
    @Override
    public boolean isNeedingRestart() {
	return false;
    }
    @Override
    public void proposeApplySettings() {
	// TODO Auto-generated method stub

    }
    @Override
    public void proposeRevertSettings(long revertTimeMillis) {
	// TODO Auto-generated method stub

    }
    @Override
    public void configure(Consumer<FEATURE_CLASS> propertySetter,
	    Supplier<FEATURE_CLASS> propertyGetter, Set<Annotation> annotations,
	    String humanReadablePropertyName) {
    }
    
}//end BasicSettingsPanel
