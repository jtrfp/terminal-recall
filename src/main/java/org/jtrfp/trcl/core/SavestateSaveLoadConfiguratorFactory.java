/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.SavestateSaveLoadConfigurationFactory.SavestateSaveLoadConfiguration;
import org.springframework.stereotype.Component;

/**
 * Hook feature for persistent properties in game state save/load
 * @author Chuck Ritola
 *
 */

@Component
public class SavestateSaveLoadConfiguratorFactory implements
	FeatureFactory<SavestateSaveLoadConfiguration> {
    
    private static Set<String> PERSISTENT_PROPERTIES;
    
    protected static Set<String> getPersistentProperties(){
	if(PERSISTENT_PROPERTIES == null){
	    final Set<String> newValue = new HashSet<String>();
	    newValue.add(SavestateSaveLoadConfigurationFactory.DEFAULT_SAVESTATE_URI);
	    PERSISTENT_PROPERTIES = newValue;
	    }
	return PERSISTENT_PROPERTIES;
    }//end getPersistentProperties
    
    public static class SavestateSaveLoadConfigurator extends FeatureConfigurator<SavestateSaveLoadConfiguration>{

	@Override
	protected Set<String> getPersistentProperties() {
	    return SavestateSaveLoadConfiguratorFactory.getPersistentProperties();
	}
	
    }//end SavestateSaveLoadConfigurator

    @Override
    public Feature<SavestateSaveLoadConfiguration> newInstance(
	    SavestateSaveLoadConfiguration target) {
	return new SavestateSaveLoadConfigurator();
    }

    @Override
    public Class<SavestateSaveLoadConfiguration> getTargetClass() {
	return SavestateSaveLoadConfiguration.class;
    }

    @Override
    public Class<SavestateSaveLoadConfigurator> getFeatureClass() {
	return SavestateSaveLoadConfigurator.class;
    }

}//end SavestateSaveLoadConfigurationFactory
