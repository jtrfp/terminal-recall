/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola and contributors.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.ScreenSaverDisablerFactory.ScreenSaverDisabler;
import org.springframework.stereotype.Component;

@Component
public class ScreenSaverDisablerConfiguratorFactory
	implements FeatureFactory<ScreenSaverDisabler> {
    
    public static class ScreenSaverDisablerConfigurator extends FeatureConfigurator<ScreenSaverDisabler> {
	
	private static Set<String> PERSISTENT_PROPERTIES;
	
	private static Set<String> getPersistentPropertiesStatic() {
	    if( PERSISTENT_PROPERTIES != null )
		return PERSISTENT_PROPERTIES;
	    final HashSet<String> pp = new HashSet<>();
	    pp.add("keyIntervalInSeconds");
	    pp.add("enabled");
	    pp.add("keyEventToUsePacked");
	    return PERSISTENT_PROPERTIES = Collections.unmodifiableSet(pp);
	}

	@Override
	protected Set<String> getPersistentProperties() {
	    return getPersistentPropertiesStatic();
	}
    }//end ScreenSaverDisablerConfigurator

    @Override
    public Feature<ScreenSaverDisabler> newInstance(ScreenSaverDisabler target)
	    throws FeatureNotApplicableException {
	return new ScreenSaverDisablerConfigurator();
    }

    @Override
    public Class<ScreenSaverDisabler> getTargetClass() {
	return ScreenSaverDisabler.class;
    }

    @Override
    public Class<? extends Feature<ScreenSaverDisabler>> getFeatureClass() {
	return ScreenSaverDisablerConfigurator.class;
    }

}//end ScreenSaverDisablerConfiguratorFactory
