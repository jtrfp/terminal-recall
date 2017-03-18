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

package org.jtrfp.trcl.ext.tr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.FeatureConfigurator;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class SoundSystemConfiguratorFactory
	implements FeatureFactory<SoundSystem> {
    
    private static final String [] PERSISTENT_PROPERTIES = 
	    new String [] {
	    SoundSystem.LINEAR_FILTERING,
	    SoundSystem.BUFFER_LAG,
	    SoundSystem.DEVICE_BY_NAME,
	    SoundSystem.DRIVER_BY_NAME,
	    SoundSystem.FORMAT_BY_NAME,
	    SoundSystem.OUTPUT_BY_NAME,
	    SoundSystem.MOD_STEREO_WIDTH,
	    SoundSystem.BUFFER_SIZE_FRAMES_STRING
	    //"componentConfigs"
    };
    private static final Set<String> PERSISTENT_PROPERTIES_SET = 
	    Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(PERSISTENT_PROPERTIES)));

    @Override
    public Feature<SoundSystem> newInstance(SoundSystem target)
	    throws FeatureNotApplicableException {
	return new SoundSystemConfigurator();
    }

    @Override
    public Class<SoundSystem> getTargetClass() {
	return SoundSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SoundSystemConfigurator.class;
    }
    
    public static class SoundSystemConfigurator extends FeatureConfigurator<SoundSystem> {

	@Override
	protected Set<String> getPersistentProperties() {
	    return PERSISTENT_PROPERTIES_SET;
	}
	
    }//end SoundSystemConfigurator

}//end SoundSystemConfigurationFactory
