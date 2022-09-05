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
public class BuildInformationPrinterFactory
implements FeatureFactory<Features> {
    
    public static class BuildInformationPrinter implements Feature<Features> {
	private BuildInformation buildInformation;
	
	public void printBuildInformation() {
	    System.out.println("Build ID: "+buildInformation.getUniqueBuildId());
	    System.out.println("Branch: "+buildInformation.getBranch());
	}

	@Override
	public void apply(Features target) {
	    buildInformation = Features.get(target, BuildInformation.class);
	    printBuildInformation();
	}//end apply(...)

	@Override
	public void destruct(Features target) {}

    }//end BuildInformationPrinter

    @Override
    public Feature<Features> newInstance(Features target)
	    throws FeatureNotApplicableException {
	return new BuildInformationPrinter();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<BuildInformationPrinter> getFeatureClass() {
	return BuildInformationPrinter.class;
    }
}//end BuildInformationPrinterFactory
