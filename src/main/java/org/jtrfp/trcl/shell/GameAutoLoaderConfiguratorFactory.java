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

import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.shell.GameAutoLoaderFactory.GameAutoLoader;
import org.springframework.stereotype.Component;

@Component
public class GameAutoLoaderConfiguratorFactory implements FeatureFactory<GameAutoLoader> {

    public class GameAutoLoaderConfigurator
    extends FeatureConfigurator<GameAutoLoader> {
	private final Set<String> PERSISTENT_PROPERTIES = Set.of("enabled","packedSelectedCampaign");

	@Override
	protected Set<String> getPersistentProperties() {
	    return PERSISTENT_PROPERTIES;
	}
    }//end GameAutoLoaderconfigurator

    @Override
    public Feature<GameAutoLoader> newInstance(GameAutoLoader target)
	    throws FeatureNotApplicableException {
	return new GameAutoLoaderConfigurator();
    }

    @Override
    public Class<GameAutoLoader> getTargetClass() {
	return GameAutoLoader.class;
    }

    @Override
    public Class<? extends Feature<GameAutoLoader>> getFeatureClass() {
	return GameAutoLoaderConfigurator.class;
    }
}//end GameAutoLoaderconfiguratorFactory
