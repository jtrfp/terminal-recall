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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class PulledDownByGravityBehavior extends Behavior {
    private static final Vector3D G = new Vector3D(0,-1400,0);
    @Override
    public void tick(long tickTimeMillis){
	final WorldObject p = getParent();
	p.probeForBehavior(Velocible.class).accellerate(G);
    }
}//end PulledDownByGravityBehavior
