/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
    private TRConfiguration config;

    public TRConfiguration getConfig(){
	if(config==null)
	    config=TRConfiguration.getConfig();
	return config;
    }//end getConfig()
}//end ConfigManager
