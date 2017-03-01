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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ControllerMapping implements PropertyChangeListener{
    public static final double 
            PRIORITY_USER     = 10,
            PRIORITY_DEFAULT  = 20,
            PRIORITY_FALLBACK = 30;
    private ControllerSink controllerSink;
    private ControllerSource controllerSource;
    private double scale, offset, priority;

    public ControllerMapping(){super();}

    public ControllerMapping(ControllerSource controllerSource, ControllerSink controllerSink, double priority, double scale, double offset){
	this.controllerSink   = controllerSink;
	this.controllerSource = controllerSource;
	this.scale            = scale;
	this.offset           = offset;
	this.priority         = priority;
    }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		controllerSink.setState((Double)evt.getNewValue()*scale+offset);
	}
    
    public ControllerSink getControllerSink() {
	return controllerSink;
    }
    public double getScale() {
	return scale;
    }
    public double getOffset() {
	return offset;
    }
    public void setControllerInput(ControllerSink controllerInput) {
	this.controllerSink = controllerInput;
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

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
}//end ControllerMapping
