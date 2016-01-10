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

import static org.jtrfp.trcl.core.TRConfiguration.ACTIVE_AUDIO_DEVICE;
import static org.jtrfp.trcl.core.TRConfiguration.ACTIVE_AUDIO_DRIVER;
import static org.jtrfp.trcl.core.TRConfiguration.ACTIVE_AUDIO_FORMAT;
import static org.jtrfp.trcl.core.TRConfiguration.ACTIVE_AUDIO_OUTPUT;
import static org.jtrfp.trcl.core.TRConfiguration.AUDIO_BUFFER_LAG;
import static org.jtrfp.trcl.core.TRConfiguration.AUDIO_BUFFER_SIZE;
import static org.jtrfp.trcl.core.TRConfiguration.CROSSHAIRS_ENABLED;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.core.TRConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * Temporary class to allow the deprecated TRConfiguration to be used in the new scheme of
 * Configurators.
 * @author Chuck Ritola
 *
 */

@Configuration
public class TRConfigurationConfigurator extends Configurator<TRConfiguration> {
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

    @Override
    protected Set<String> getPersistentProperties() {
	return PERSISTENT_PROPERTIES_SET;
    }

    @Override
    public Class<TRConfiguration> getConfiguredClass() {
	return TRConfiguration.class;
    }

}//end TRConfigurationConfigurator
