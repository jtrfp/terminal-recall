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

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class FeatureConfigWindowFactory implements FeatureFactory<TR> {
    
    public class FeatureConfigWindow implements Feature<TR> {
	@Getter
	private final FeatureConfigFrame featureConfigWindow = new FeatureConfigFrame();

	@Override
	public void apply(TR target) {
	    featureConfigWindow.setFeatureRoot(Features.getSingleton());
	    final RootWindow rw = Features.get(target, RootWindow.class);
	    featureConfigWindow.setRootWindow(rw);
	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub
	    
	}
    }//end Feature

    @Override
    public Feature<TR> newInstance(TRFactory.TR target) {
	return new FeatureConfigWindow();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return FeatureConfigWindow.class;
    }
}//end ConfigMenuItemFactory
