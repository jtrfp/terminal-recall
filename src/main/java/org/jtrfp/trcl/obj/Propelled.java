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
package org.jtrfp.trcl.obj;

public interface Propelled {
    public Propelled setPropulsion(double magnitude);

    public double getPropulsion();

    public Propelled setMaxPropulsion(double max);

    public double getMaxPropulsion();

    public Propelled setMinPropulsion(double min);

    public double getMinPropulsion();

    public Propelled deltaPropulsion(double delta);
}
