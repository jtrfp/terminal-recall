/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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

import org.jtrfp.trcl.conf.ConfigRootFeature;
import org.springframework.stereotype.Component;

@Component
public class TRMaintainerConfigRootFactory implements FeatureFactory<Features>, LoadOrderAware{
    //Load immediately before the user config root
    public static final int FEATURE_LOAD_PRIORITY = TRConfigRootFactory.FEATURE_LOAD_PRIORITY-1;
    public static final int PRIVILEGE_LEVEL_MAINTAINER = 10;
    
    public class TRMaintainerConfigRoot extends ConfigRootFeature<Features> implements GraphStabilizationListener{
	public static final String PROP_MAINT_SETTINGS_PATH = "org.jtrfp.trcl.maintSettingsPath";
	
	public TRMaintainerConfigRoot() {
	    super();
	    this.setPrivilegeLevel(PRIVILEGE_LEVEL_MAINTAINER);
	}//end constructor
	
	@Override
	public void destruct(Features target) {}
	
	@Override
	public void apply(Features target){
	    super.apply(target);
	}

	@Override
	protected String getDefaultSaveURI() {
	    return  System.getProperty(PROP_MAINT_SETTINGS_PATH, 
		     "maint.config.trcl.xml");
	}//end getDefaultSaveURI
	
	@Override
	public void graphStabilized(Object target){
	    super.loadConfigurations();
	}
    }//end TRMaintainerConfigRoot

    @Override
    public Feature<Features> newInstance(Features target) {
	return new TRMaintainerConfigRoot();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature<Features>> getFeatureClass() {
	return TRMaintainerConfigRoot.class;
    }

    @Override
    public int getFeatureLoadPriority() {
	return FEATURE_LOAD_PRIORITY;
    }
}//end TRMaintainerConfigRootFactory