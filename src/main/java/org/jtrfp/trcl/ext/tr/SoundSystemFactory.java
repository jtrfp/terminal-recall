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

package org.jtrfp.trcl.ext.tr;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class SoundSystemFactory implements FeatureFactory<TR> {
    
    public static class SoundSystemFeature extends SoundSystem implements Feature<TR> {

	public SoundSystemFeature(TR tr) {
	    super(tr);
	}

	@Override
	public void apply(TR target) {
	    // TODO Auto-generated method stub
	    
	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub
	    
	}
    }//end SoundSystemFeature

    @Override
    public Feature<TR> newInstance(TR target) {
	final SoundSystemFeature result = new SoundSystemFeature(target);
	Runtime.getRuntime().addShutdownHook(new Thread(){
	    @Override
	    public void run(){
		result.setPaused(true);
	    }
	});
	return result;//TODO: Refactor as empty constructor and init in apply()
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SoundSystemFeature.class;
    }

}//end SoundSystemFactory
