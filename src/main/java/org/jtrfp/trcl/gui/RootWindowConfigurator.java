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

package org.jtrfp.trcl.gui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jtrfp.trcl.conf.Configurator;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class RootWindowConfigurator extends Configurator<RootWindow> {
    private static final HashSet<String> PERSISTENT_PROPERTIES = 
	    new HashSet<String>(Arrays.asList(
		    "bounds"));

    @Override
    protected Set<String> getPersistentProperties() {
	return PERSISTENT_PROPERTIES;
    }

    @Override
    public Class<RootWindow> getConfiguredClass() {
	return RootWindow.class;
    }

}//end RootWindowConfigurator
