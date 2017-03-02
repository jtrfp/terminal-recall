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

package org.jtrfp.trcl.ctl;

import java.util.Collection;

import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.jtrfp.trcl.gui.DefaultControllerConfiguration;

public interface InputDeviceService {
    public String getAuthor();
    public String getDescription();
    public Collection<InputDevice> getInputDevices();
    public void registerFallbackConfiguration(ControllerConfiguration conf);
    public void registerDefaultConfiguration (DefaultControllerConfiguration<?> conf);
}//end InputDeviceService()