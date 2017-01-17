/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2017 Chuck Ritola
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
import org.jtrfp.trcl.core.DefaultPODRegistryFactory.DefaultPODRegistry;
import org.springframework.stereotype.Component;

@Component
public class DefaultPODRegistryConfiguratorFactory implements FeatureFactory<DefaultPODRegistry> {
    private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<String>(1);
    static {
	PERSISTENT_PROPERTIES.add("podsAsArray");
    }
    public class DefaultPODRegistryConfigurator extends FeatureConfigurator<DefaultPODRegistry>{

	@Override
	protected Set<String> getPersistentProperties() {
	    return PERSISTENT_PROPERTIES;
	}
	
    }//end PODRegistryConfigurator
    @Override
    public Feature<DefaultPODRegistry> newInstance(DefaultPODRegistry target)
	    throws FeatureNotApplicableException {
	return new DefaultPODRegistryConfigurator();
    }
    @Override
    public Class<DefaultPODRegistry> getTargetClass() {
	return DefaultPODRegistry.class;
    }
    @Override
    public Class<? extends Feature> getFeatureClass() {
	return DefaultPODRegistryConfigurator.class;
    }
}//end PODRegistryConfiguratorFactory
