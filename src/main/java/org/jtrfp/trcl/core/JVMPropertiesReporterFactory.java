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

package org.jtrfp.trcl.core;

import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.springframework.stereotype.Component;

@Component
public class JVMPropertiesReporterFactory implements FeatureFactory<Reporter> {
    public static class JVMPropertiesReporter implements Feature<Reporter> {

	public JVMPropertiesReporter() {}

	@Override
	public void apply(Reporter target) {
	    System.getProperties().forEach(
		    (path,value)->{target.report(JVMPropertiesReporter.class.getName()+"."+path.toString(), value.toString());
		    });
	}

	@Override
	public void destruct(Reporter target) {
	    // TODO Auto-generated method stub

	}

    }//end JVMPropertiesReporter

    @Override
    public Feature<Reporter> newInstance(Reporter target)
	    throws FeatureNotApplicableException {
	return new JVMPropertiesReporter();
    }

    @Override
    public Class<Reporter> getTargetClass() {
	return Reporter.class;
    }

    @Override
    public Class<JVMPropertiesReporter> getFeatureClass() {
	return JVMPropertiesReporter.class;
    }
}//end JVMPropertiesReporterFactory
