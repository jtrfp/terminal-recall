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
package org.jtrfp.trcl.beh.ui;

import org.jtrfp.trcl.ManuallySetController;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Propelled;

public class UpdatesThrottleMeterBehavior extends Behavior implements GUIUpdateBehavior{
    private ManuallySetController controller;
    @Override
    public void tick(long tickTimeMillis){
	    Propelled prop = getParent().probeForBehavior(Propelled.class);
	    final double minPropulsion = prop.getMinPropulsion();
	    final double range = prop.getMaxPropulsion() - minPropulsion;
	    controller.setFrame(1.-((prop.getPropulsion()-minPropulsion)/range));
    }
    /**
     * @return the controller
     */
    public ManuallySetController getController() {
        return controller;
    }
    /**
     * @param controller the controller to set
     */
    public UpdatesThrottleMeterBehavior setController(ManuallySetController controller) {
        this.controller = controller;
        return this;
    }
}
