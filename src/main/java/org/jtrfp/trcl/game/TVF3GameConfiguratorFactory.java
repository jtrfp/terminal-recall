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

package org.jtrfp.trcl.game;

import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.springframework.stereotype.Component;

@Component
public final class TVF3GameConfiguratorFactory implements FeatureFactory<TVF3Game> {
    private static Set<String> PERSISTENT_PROPERTIES;
    
    protected static Set<String> getPersistentProperties(){
	if(PERSISTENT_PROPERTIES == null){
	    PERSISTENT_PROPERTIES = new HashSet<String>();
	    PERSISTENT_PROPERTIES.add(TVF3Game.GAME_VERSION);
	    PERSISTENT_PROPERTIES.add(TVF3Game.PLAYER_NAME);
	    PERSISTENT_PROPERTIES.add(TVF3Game.CURRENT_MISSION);
	    PERSISTENT_PROPERTIES.add(TVF3Game.DIFFICULTY);
	}
	return PERSISTENT_PROPERTIES;
    }//end getPersistentProperties()
    
    public static class TVF3GameConfigurator extends FeatureConfigurator<TVF3Game> {

	@Override
	protected Set<String> getPersistentProperties() {
	    return TVF3GameConfiguratorFactory.getPersistentProperties();
	}
	
    }//end TVF3GameConfigurator

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	return new TVF3GameConfigurator();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TVF3GameConfigurator.class;
    }

}
