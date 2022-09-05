/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.ctl.ControllerConfigSupportImplFactory.ControllerConfigSupportImpl;
import org.springframework.stereotype.Component;

@Component
public class ControllerConfigSupportConfiguratorFactory
	implements FeatureFactory<ControllerConfigSupportImpl> {
    private static Set<String> PERSISTENT_PROPERTIES;
    
    protected static Set<String> getPersistentProperties(){
	if(PERSISTENT_PROPERTIES == null){
	    PERSISTENT_PROPERTIES = new HashSet<>();
	    PERSISTENT_PROPERTIES.add(ControllerConfigSupportImpl.CONFIG_BEANS);
	}//end if(null)
	return PERSISTENT_PROPERTIES;
    }//end getPersistentProperties()
    
    public static class ControllerConfigSupportConfigurator extends FeatureConfigurator<ControllerConfigSupportImpl> {

	@Override
	protected Set<String> getPersistentProperties() {
	    return ControllerConfigSupportConfiguratorFactory.getPersistentProperties();
	}
	
    }//end ControllerConfigSupportConfigurator

    @Override
    public Feature<ControllerConfigSupportImpl> newInstance(
	    ControllerConfigSupportImpl target)
	    throws FeatureNotApplicableException {
	return new ControllerConfigSupportConfigurator();
    }

    @Override
    public Class<ControllerConfigSupportImpl> getTargetClass() {
	return ControllerConfigSupportImpl.class;
    }

    @Override
    public Class<ControllerConfigSupportConfigurator> getFeatureClass() {
	return ControllerConfigSupportConfigurator.class;
    }

}//end ControllerConfigSupportConfiguratorFactory
