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


/**
 * 3-dimensional chunk of unchanging triangles.
 * @author Chuck Ritola
 *
 */
public class TerrainChunk extends WorldObject{
    private boolean isCeiling=false;

    public TerrainChunk(TR tr){
	super(tr);
    }

    public TerrainChunk(TR tr, Model m){
	super(tr,m);
	m.setDebugName("TerrainChunk: "+m.getDebugName());
    }

    public boolean isCeiling(){return isCeiling;}

    public void setCeiling(boolean b) {
	isCeiling=b;
    }
}//end TerrainChunk
