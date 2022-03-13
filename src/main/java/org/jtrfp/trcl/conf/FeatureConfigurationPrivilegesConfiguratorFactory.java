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

package org.jtrfp.trcl.conf;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.FeatureConfigurationPrivileges;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.springframework.stereotype.Component;

@Component
public class FeatureConfigurationPrivilegesConfiguratorFactory implements FeatureFactory<FeatureConfigurationPrivileges> {
    private static final Set<String> PERSISTENT_PROPERTIES = new HashSet<String>(1);
    static {
	PERSISTENT_PROPERTIES.add("privilegeData");
    }
    public class FeatureConfigurationPrivilegesConfigurator extends FeatureConfigurator<FeatureConfigurationPrivileges>{
	
	public FeatureConfigurationPrivilegesConfigurator() {
	    super();
	}//end constructor

	@Override
	protected Set<String> getPersistentProperties() {
	    return Collections.unmodifiableSet(PERSISTENT_PROPERTIES);
	}
	
    }//end FeatureConfigurationPrivilegesConfigurator
    
    @Override
    public Feature<FeatureConfigurationPrivileges> newInstance(FeatureConfigurationPrivileges target)
	    throws FeatureNotApplicableException {
	return new FeatureConfigurationPrivilegesConfigurator();
    }
    @Override
    public Class<FeatureConfigurationPrivileges> getTargetClass() {
	return FeatureConfigurationPrivileges.class;
    }
    @Override
    public Class<? extends Feature> getFeatureClass() {
	return FeatureConfigurationPrivilegesConfigurator.class;
    }
}//end FeatureConfigurationPrivilegesConfiguratorFactory
