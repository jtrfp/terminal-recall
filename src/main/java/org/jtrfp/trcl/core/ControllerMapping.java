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

public class ControllerMapping implements StateListener{
    private ControllerInput controllerInput;
    private ControllerSource controllerSource;
    private double scale, offset;

    public ControllerMapping(){super();}

    public ControllerMapping(ControllerSource controllerSource, ControllerInput controllerInput, double scale, double offset){
	this.controllerInput = controllerInput;
	this.controllerSource = controllerSource;
	this.scale           = scale;
	this.offset          = offset;
    }

	@Override
	public void stateChanged(ControllerSource source, double value) {
		controllerInput.setState(value*scale+offset);
	}
    
    public ControllerInput getControllerInput() {
	return controllerInput;
    }
    public double getScale() {
	return scale;
    }
    public double getOffset() {
	return offset;
    }
    public void setControllerInput(ControllerInput controllerInput) {
	this.controllerInput = controllerInput;
    }
    public void setScale(double scale) {
	this.scale = scale;
    }
    public void setOffset(double offset) {
	this.offset = offset;
    }

    public ControllerSource getControllerSource() {
        return controllerSource;
    }

    public void setControllerSource(ControllerSource controllerSource) {
        this.controllerSource = controllerSource;
    }
}//end ControllerMapping
