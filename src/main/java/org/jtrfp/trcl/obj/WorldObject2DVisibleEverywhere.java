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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;

public abstract class WorldObject2DVisibleEverywhere extends WorldObject2D implements
	RelevantEverywhere {

    public WorldObject2DVisibleEverywhere(TR tr) {
	super(tr);
    }

    public WorldObject2DVisibleEverywhere(TR tr, Model model) {
	super(tr,model);
    }
    
}//end WorldOBject2DVisibleEverywhere
