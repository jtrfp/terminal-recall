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

package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.WorldObject;

public class MatchDirection extends Behavior {
    private WorldObject target;
    @Override
    public void tick(long tickTimeMillis){
	if(target!=null){
	    final double [] hdg = target.getHeadingArray();
	    getParent().setHeading(new Vector3D(hdg[0], hdg[1], hdg[2]));
	    final double [] top = target.getTopArray();
	    getParent().setTop(new Vector3D(top[0], top[1], top[2]));
	}//end if(!null)
    }//end _tick(...)
    /**
     * @return the target
     */
    public WorldObject getTarget() {
        return target;
    }
    /**
     * @param target the target to set
     */
    public void setTarget(WorldObject target) {
        this.target = target;
    }
}//end MatchDirection
