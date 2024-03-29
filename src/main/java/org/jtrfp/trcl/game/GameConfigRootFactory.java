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
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.FeaturesImpl.FeatureNotFoundException;
import org.jtrfp.trcl.core.LoadOrderAware;
import org.jtrfp.trcl.core.SavestateSaveLoadConfigurationFactory.SavestateSaveLoadConfiguration;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.springframework.stereotype.Component;

@Component
public class GameConfigRootFactory implements FeatureFactory<TVF3Game>, LoadOrderAware{
    public static String SAVE_URI_SUFFIX = ".sav.trcl.xml";
    public static String DEFAULT_SAVE_URI = "game"+SAVE_URI_SUFFIX;
    
    public static class GameConfigRootFeature extends ConfigRootFeature<TVF3Game>{
	private SavestateSaveLoadConfiguration savestateSaveLoadConf;
	
	@Override
	public void apply(TVF3Game target){
	    super.apply(target);
	    getSavestateSaveLoadConf();//XXX to ensure Feature is extracted in non-display() thread.
	}

	@Override
	public void destruct(TVF3Game target) {
	}

	@Override
	protected String getDefaultSaveURI() {
	    return null;
	}
	
	@Override
	public String getConfigSaveURI(){
	    SavestateSaveLoadConfiguration conf = getSavestateSaveLoadConf();
	    if(conf != null){
		final String uri = conf.getDefaultSavestateURI();
		System.out.println("getConfigSaveURI returning "+uri);
		return uri;
	    }
	    return super.getConfigSaveURI();
	}//end getConfigSaveURI()
	
	@Override
	public void setConfigSaveURI(String uri){
	    SavestateSaveLoadConfiguration conf = getSavestateSaveLoadConf();
	    if(conf != null)
		conf.setDefaultSavestateURI(uri);
	    else
		super.setConfigSaveURI(uri);
	}

	protected SavestateSaveLoadConfiguration getSavestateSaveLoadConf() {
	    if(savestateSaveLoadConf == null){
		final TR tr = getTarget().getTr();
		try{
		    final SavestateSaveLoadConfiguration conf = Features.get(tr, SavestateSaveLoadConfiguration.class);
		    setSavestateSaveLoadConf(conf);
		}catch(FeatureNotFoundException e){
		    System.out.println("Warning: GameConfigRootFeature failed to find SavestateSaveLoadConfiguration feature in TR.");
		}
	    }//end if(null)
	    return savestateSaveLoadConf;
	}

	protected void setSavestateSaveLoadConf(
		SavestateSaveLoadConfiguration savestateSaveLoadConf) {
	    this.savestateSaveLoadConf = savestateSaveLoadConf;
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
    public Class<GameConfigRootFeature> getFeatureClass() {
	return GameConfigRootFeature.class;
    }

    @Override
    public int getFeatureLoadPriority() {
	return LoadOrderAware.LAST;
    }

}//end GameConfigRoot
