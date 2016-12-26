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

package org.jtrfp.trcl;

import java.io.FileNotFoundException;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.springframework.stereotype.Component;

@Component
public class OutputDumpFactory implements FeatureFactory<Features> {

    @Override
    public Feature<Features> newInstance(Features target)
	    throws FeatureNotApplicableException {
	try{return new OutputDumpFeature();}
	catch(FileNotFoundException e){throw new RuntimeException(e);}
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return OutputDumpFeature.class;
    }
    
    private static class OutputDumpFeature extends OutputDump implements Feature<Features> {

	public OutputDumpFeature() throws FileNotFoundException {
	    super();
	}//end constructor

	@Override
	public void apply(Features target) {
	    // TODO Auto-generated method stub
	}

	@Override
	public void destruct(Features target) {
	    // TODO Auto-generated method stub
	}
	
    }//end OutputDumpFeature

}//end OutputDumpFactory
