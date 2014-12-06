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
package org.jtrfp.trcl.flow;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;

public class EngineTests {

    public static void singlet(TR tr, int numInstances) {
	final TextureDescription test = tr.getResourceManager().getTestTexture();
	tr.abortCurrentGame();
	tr.getWorld().removeAll();
	for (int i = 0; i < numInstances; i++) {
	    WorldObject wo = new Sprite2D(tr, .5, 1, 1, test, false);
	    wo.setPosition(new double[] { 0, 0, .5 });
	    wo.setActive(true);
	    wo.setVisible(true);
	    tr.getWorld().add(wo);
	}//end for(numInstances)
    }//end singlet(tr)

}//end EngineTests
