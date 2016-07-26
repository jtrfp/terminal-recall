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

package org.jtrfp.trcl.game;

import org.jtrfp.trcl.conf.ConfigRootFeature;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.springframework.stereotype.Component;

@Component
public class GameConfigRootFactory implements FeatureFactory<TVF3Game> {
    public static String SAVE_URI_SUFFIX = ".sav.trcl.xml";
    public static String DEFAULT_SAVE_URI = "game"+SAVE_URI_SUFFIX;
    
    public static class GameConfigRootFeature extends ConfigRootFeature<TVF3Game>{

	@Override
	public void destruct(TVF3Game target) {
	    // TODO Auto-generated method stub
	    
	}

	@Override
	protected String getDefaultSaveURI() {
	    return DEFAULT_SAVE_URI;
	}
	
    }//end GameConfigRootFeature

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target) {
	final GameConfigRootFeature result = new GameConfigRootFeature(); 
	return result;
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GameConfigRootFeature.class;
    }

}//end GameConfigRoot
