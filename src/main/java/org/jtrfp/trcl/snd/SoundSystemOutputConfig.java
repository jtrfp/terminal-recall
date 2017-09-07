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
 */

package org.jtrfp.trcl.snd;

public class SoundSystemOutputConfig {
    private String driverByName, deviceByName, portByName, formatByName;

    public String getDriverByName() {
	return driverByName;
    }

    public void setDriverByName(String driverByName) {
	this.driverByName = driverByName;
    }

    public String getDeviceByName() {
	return deviceByName;
    }

    public void setDeviceByName(String deviceByName) {
	this.deviceByName = deviceByName;
    }

    public String getPortByName() {
	return portByName;
    }

    public void setPortByName(String portByName) {
	this.portByName = portByName;
    }

    public String getFormatByName() {
	return formatByName;
    }

    public void setFormatByName(String formatByName) {
	this.formatByName = formatByName;
    }
}//end SoundSystemOutputConfig
