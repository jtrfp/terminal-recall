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

package org.jtrfp.trcl.conf;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.springframework.stereotype.Component;
import static org.jtrfp.trcl.conf.TRConfigurationFactory.*;

/**
 * Temporary class to allow the deprecated TRConfiguration to be used in the new scheme of
 * Configurators.
 * @author Chuck Ritola
 *
 */

@Component
public class TRConfiguratorFactory implements FeatureFactory<TR> {
    private static final String [] PERSISTENT_PROPERTIES = 
	    new String [] {
	    ACTIVE_AUDIO_DRIVER,
	    ACTIVE_AUDIO_DEVICE,
	    ACTIVE_AUDIO_OUTPUT,
	    ACTIVE_AUDIO_FORMAT,
	    AUDIO_BUFFER_LAG,
	    AUDIO_BUFFER_SIZE,
	    CROSSHAIRS_ENABLED,
	    "usingTextureBufferUnmap",
	    "debugMode",
	    "targetFPS",
	    "voxFile",
	    "waitForProfiler",
	    "audioLinearFiltering",
	    "missionList",
	    "modStereoWidth",
	    "podList",
	    "fileDialogStartDir",
	    "componentConfigs"
    };
    private static final Set<String> PERSISTENT_PROPERTIES_SET = 
	    new HashSet<String>(Arrays.asList(PERSISTENT_PROPERTIES));
    
    public class TRConfigurator extends FeatureConfigurator<TR>{
	@Override
	    protected Set<String> getPersistentProperties() {
		return PERSISTENT_PROPERTIES_SET;
	    }
    }//end TRConfiguration

    @Override
    public Feature<TR> newInstance(TR target) {
	return new TRConfigurator();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TRConfigurator.class;
    }
}//end TRConfigurationConfigurator
