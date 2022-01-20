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

package org.jtrfp.trcl.shell;

import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.shell.GameCampaignRegistryFactory.GameCampaignRegistry;
import org.springframework.stereotype.Component;

@Component
public class GameCampaignRegistryConfiguratorFactory implements FeatureFactory<GameCampaignRegistry> {
    private static Set<String> PERSISTENT_PROPERTIES;

    protected static Set<String> getPersistentProperties() {
	if(PERSISTENT_PROPERTIES == null){
	    PERSISTENT_PROPERTIES = new HashSet<>();
	    PERSISTENT_PROPERTIES.add("campaignEntriesCollection");
	}//end if(null)
	return PERSISTENT_PROPERTIES;
    }//end getPersistentProperties()

    public class GameResourceRegistryConfigurator extends FeatureConfigurator<GameCampaignRegistry> {

	@Override
	protected Set<String> getPersistentProperties() {
	    return GameCampaignRegistryConfiguratorFactory.getPersistentProperties();
	}

    }//end GameResourceRegistryConfigurator

    @Override
    public Feature<GameCampaignRegistry> newInstance(
	    GameCampaignRegistry target) throws FeatureNotApplicableException {
	return new GameResourceRegistryConfigurator();
    }

    @Override
    public Class<GameCampaignRegistry> getTargetClass() {
	return GameCampaignRegistry.class;
    }

    @Override
    public Class<? extends Feature<GameCampaignRegistry>> getFeatureClass() {
	return GameResourceRegistryConfigurator.class;
    }

}//end GameResourceRegistryConfigurator
