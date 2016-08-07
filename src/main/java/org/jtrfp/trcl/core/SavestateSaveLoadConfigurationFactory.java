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

package org.jtrfp.trcl.core;

import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.GameConfigRootFactory;
import org.springframework.stereotype.Component;

/**
 * Configuration holder for Game load/save settings.
 * @author Chuck Ritola
 *
 */

@Component
public class SavestateSaveLoadConfigurationFactory implements
	FeatureFactory<TR> {
    //PROPERTIES
    public static final String DEFAULT_SAVESTATE_URI = "defaultSavestateURI";

    @Override
    public Feature<TR> newInstance(TR target) {
	return new SavestateSaveLoadConfiguration();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SavestateSaveLoadConfiguration.class;
    }
    
    public static class SavestateSaveLoadConfiguration implements Feature<TR> {
	private String defaultSavestateURI = GameConfigRootFactory.DEFAULT_SAVE_URI;

	@Override
	public void apply(TR target) {
	}

	@Override
	public void destruct(TR target) {
	}

	public String getDefaultSavestateURI() {
	    new Throwable("getDefaultSavestateURI "+defaultSavestateURI+" hash="+hashCode()).printStackTrace();
	    return defaultSavestateURI;
	}

	public void setDefaultSavestateURI(String defaultSavestateURI) {
	    System.out.println("setDefaultSavestateURI "+defaultSavestateURI+" hash="+hashCode());
	    this.defaultSavestateURI = defaultSavestateURI;
	}
    }//end SavestateSaveLoadConfiguration

}//end SavestateSaveLoadConfigurationFactory
