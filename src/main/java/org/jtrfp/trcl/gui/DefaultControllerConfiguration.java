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
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;

public abstract class DefaultControllerConfiguration extends
	ControllerConfiguration implements Feature<ControllerMapper>{
 public DefaultControllerConfiguration(){
     super();
     registerWithMapper();
 }//end constructor
 
 private void registerWithMapper(){
     
 }//end registerWithMapper()
 
 @Override
 public void apply(ControllerMapper target){
     target.registerDefaultConfiguration(this);
 }//end apply()
}//end DefaultControllerConfiguration
