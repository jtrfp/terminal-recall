/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class RootWindowConfiguratorFactory implements FeatureFactory<RootWindow>{
    private static final HashSet<String> PERSISTENT_PROPERTIES = 
		new HashSet<String>(Arrays.asList(
			"bounds",
			"fullScreen"));
    public static class RootWindowConfigurator extends FeatureConfigurator<RootWindow> {
	@Override
	protected Set<String> getPersistentProperties() {
	    return PERSISTENT_PROPERTIES;
	}
    }//end RootWindowConfigurator

    @Override
    public Feature<RootWindow> newInstance(RootWindow target) {
	return new RootWindowConfigurator();
    }

    @Override
    public Class<RootWindow> getTargetClass() {
	return RootWindow.class;
    }

    @Override
    public Class<RootWindowConfigurator> getFeatureClass() {
	return RootWindowConfigurator.class;
    }
}//end RootWindowConfiguratorFactory
