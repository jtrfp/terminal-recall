/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject;


/**
 * 3-dimensional chunk of unchanging triangles.
 * @author Chuck Ritola
 *
 */
public class TerrainChunk extends WorldObject{
    	private boolean isCeiling=false;
    	private final AltitudeMap map;
	public TerrainChunk(TR tr, Model m, AltitudeMap map){
		super(tr,m);
		this.map=map;
		m.setDebugName("TerrainChunk");
		}
	
	public AltitudeMap getAltitudeMap(){return map;}
	
	public boolean isCeiling(){return isCeiling;}
	
	public void setCeiling(boolean b) {
	    isCeiling=b;
	}
}//end TerrainChunk
