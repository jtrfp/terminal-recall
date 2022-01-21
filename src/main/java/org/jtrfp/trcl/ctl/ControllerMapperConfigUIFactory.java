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

package org.jtrfp.trcl.ctl;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.jtrfp.trcl.conf.ui.FeatureConfigurationUI;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.gui.ControllerConfigPanel;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class ControllerMapperConfigUIFactory implements FeatureFactory<ControllerMapper> {

    public static class ControllerMapperConfigUI implements Feature<ControllerMapper>, FeatureConfigurationUI<ControllerMapper> {
	@Getter(lazy=true)
	private final ControllerConfigPanel controllerConfigPanel = generateControllerConfigPanel();
	@Getter(lazy=true)
	private final JPanel rootPanel = generateRootPanel();
	private final ControllerMapper target;

	public ControllerMapperConfigUI(ControllerMapper target) {
	    this.target = target;
	}

	@Override
	public boolean isNeedingRestart() {
	    return false;
	}

	@Override
	public void proposeApplySettings() {
	    //TODO
	}

	@Override
	public void proposeRevertSettings(long revertTimeMillis) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void configure(Consumer<ControllerMapper> propertySetter,
		Supplier<ControllerMapper> propertyGetter,
		Set<Annotation> annotations, String humanReadablePropertyName) {
	    
	}

	@Override
	public JComponent getUIComponent() {
	    return getRootPanel();
	}

	@Override
	public void apply(ControllerMapper target) {
	   
	}
	
	private ControllerConfigPanel generateControllerConfigPanel() {
	    final ControllerSinks cInputs = Features.get(target, ControllerSinks.class);
	    ControllerConfigPanel result = new ControllerConfigPanel(target,cInputs);
	    return result;
	}
	
	private JPanel generateRootPanel() {
	    final JPanel result = new JPanel();
	    result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
	    result.add(new JLabel("Settings on this panel apply immediately.",JLabel.LEFT));
	    result.add(getControllerConfigPanel());
	    result.setBorder(new TitledBorder("Controller Mappings"));
	    return result;
	}

	@Override
	public void destruct(ControllerMapper target) {
	    // TODO Auto-generated method stub

	}

    }//end ControllerMapperConfigUI

    @Override
    public Feature<ControllerMapper> newInstance(ControllerMapper target)
	    throws FeatureNotApplicableException {
	return new ControllerMapperConfigUI(target);
    }

    @Override
    public Class<ControllerMapper> getTargetClass() {
	return ControllerMapper.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ControllerMapperConfigUI.class;
    }
}//end ControllerMapperConfigUIFactory
