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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.Model;


/**
 * A WorldObject which exists flat on the screen, immune to camera position or perspective effects.
 * Typically used for GUIs, messages, and HUDs.
 * @author Chuck Ritola
 *
 */
public class WorldObject2D extends WorldObject{

    public WorldObject2D(){
	super();
	setTop(Vector3D.PLUS_J);
	setHeading(Vector3D.PLUS_K);
	setRenderFlag(RenderFlags.IgnoreCamera);
    }//end constructor
    
    @Override
    public boolean supportsLoop(){
	return false;
    }
}//end WorldObject2D
