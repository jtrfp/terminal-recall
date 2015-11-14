/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh.phy;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Propelled;

public class HasPropulsion extends Behavior implements Propelled {
    private double propulsion=0;
    private double max=Double.POSITIVE_INFINITY;
    private double min=Double.NEGATIVE_INFINITY;
    @Override
    public Propelled setPropulsion(double magnitude) {
	propulsion=Math.max(min,Math.min(magnitude,max));
	return this;
    }

    @Override
    public double getPropulsion() {
	return propulsion;
    }

    @Override
    public HasPropulsion setMaxPropulsion(double max) {
	this.max=max;
	return this;
    }

    @Override
    public double getMaxPropulsion() {
	return max;
    }

    @Override
    public HasPropulsion deltaPropulsion(double delta) {
	propulsion+=delta;
	if(propulsion>max){propulsion=max;}
	else if(propulsion<min){propulsion=min;}
	return this;
    }

    @Override
    public HasPropulsion setMinPropulsion(double min) {
	this.min=min;
	return this;
    }

    @Override
    public double getMinPropulsion() {
	return min;
    }

}//end HasPropulsion
