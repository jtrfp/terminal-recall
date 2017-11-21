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

package org.jtrfp.trcl;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.springframework.stereotype.Component;

@Component
public class MavenBuildInformationFactory
	implements FeatureFactory<Features> {
    
    @Override
    public Feature<Features> newInstance(Features target)
	    throws FeatureNotApplicableException {
	return new BuildInformationFeature();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return BuildInformationFeature.class;
    }
    
    public static class BuildInformationFeature extends MavenBuildInformation implements Feature<Features> {
	
	@Override
	public void apply(Features target) {
	    //Do nothing
	}

	@Override
	public void destruct(Features target) {
	    //Do nothing
	}
    }//end MavenBuildInformationFeature

}//end MavenBuildInformationFactory
