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

package org.jtrfp.trcl.conf;

import java.util.HashMap;
import java.util.Map;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRMaintainerConfigRootFactory;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Component
public class FeatureConfigurationPrivilegesFactory implements FeatureFactory<Features> {
    
    @Getter @Setter @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode
    public static final class PropertyKey {
	private Class<? extends Feature> featureClass;
	private String propertyName;
    }

    public static class FeatureConfigurationPrivilegeData {
	@Getter @Setter
	int privilegeLevel = 0;
    }//end FeatureConfigurationPrivilegeData

    public static class FeatureConfigurationPrivileges implements Feature<Features> {
	@Getter @Setter
	public Map<PropertyKey,FeatureConfigurationPrivilegeData> privilegeData = new HashMap<>();
	
	public FeatureConfigurationPrivileges() {
	    super();
	    final FeatureConfigurationPrivilegeData fcpd = new FeatureConfigurationPrivilegeData();
	    fcpd.setPrivilegeLevel(TRMaintainerConfigRootFactory.PRIVILEGE_LEVEL_MAINTAINER);
	    privilegeData.put(new PropertyKey(this.getClass(),"privilegeData"), fcpd);
	}//end constructor
	
	@Override
	public void destruct(Features target) {}
	
	@Override
	public void apply(Features target){}
	
    }//end FeatureConfigurationPrivileges

    @Override
    public Feature<Features> newInstance(Features target) {
	return new FeatureConfigurationPrivileges();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return FeatureConfigurationPrivileges.class;
    }
}//end FeatureConfigurationPrivileges