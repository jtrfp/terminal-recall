/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2021 Chuck Ritola
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

import java.io.File;

import org.jtrfp.trcl.conf.ConfigRootFeature;
import org.springframework.stereotype.Component;

@Component
public class TRConfigRootFactory implements FeatureFactory<Features>, LoadOrderAware{
    public static final int FEATURE_LOAD_PRIORITY = LoadOrderAware.LAST;
    
    public class TRConfigRoot extends ConfigRootFeature<Features> implements GraphStabilizationListener{
	public static final String PROP_USER_SETTINGS_PATH = "org.jtrfp.trcl.userSettingsPath";

	@Override
	public void destruct(Features target) {}
	
	@Override
	public void apply(Features target){
	    super.apply(target);
	}

	@Override
	protected String getDefaultSaveURI() {
	    return  System.getProperty(PROP_USER_SETTINGS_PATH, 
		     System.getProperty("user.home", "")+File.separator+"settings.config.trcl.xml");
	}//end getDefaultSaveURI
	
	@Override
	public void graphStabilized(Object target){
	    super.loadConfigurations();
	}
    }//end TRConfigRoot

    @Override
    public Feature<Features> newInstance(Features target) {
	return new TRConfigRoot();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<TRConfigRoot> getFeatureClass() {
	return TRConfigRoot.class;
    }

    @Override
    public int getFeatureLoadPriority() {
	return FEATURE_LOAD_PRIORITY;
    }
}//end TRConfigRootFactory