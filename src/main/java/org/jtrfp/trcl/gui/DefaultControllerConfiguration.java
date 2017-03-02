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

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.ctl.InputDeviceService;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;

public abstract class DefaultControllerConfiguration<SERVICE_TYPE extends InputDeviceService> extends
	ControllerConfiguration implements Feature<SERVICE_TYPE>{
 public DefaultControllerConfiguration(){
     super();
 }//end constructor
 
 @Override
 public void apply(InputDeviceService target){
     target.registerFallbackConfiguration(this);
 }//end apply()
}//end DefaultControllerConfiguration
