/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola and contributors. See CREDITS for details.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Sprite2D;

public class SatelliteDashboard extends Sprite2D {
    public SatelliteDashboard(TR tr) throws IllegalAccessException, FileLoadException, IOException{
	super(tr, .000000001, 2, 2, tr.getResourceManager().getSpecialRAWAsTextures("MAPBACK.RAW", tr.getGlobalPalette(), tr.gpu.get().getGl(), 0, false), true, "SatelliteDashboard");
    }
}//end MapDashboard
