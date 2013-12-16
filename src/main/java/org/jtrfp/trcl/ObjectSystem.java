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

import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.PUPFile;

public class ObjectSystem extends RenderableSpacePartitioningGrid
	{
	
	public ObjectSystem(RenderableSpacePartitioningGrid parentGrid, World w,TerrainSystem terrain, LVLFile lvl) throws IllegalAccessException, IOException, FileLoadException
		{
		super(parentGrid);
		TR tr = w.getTr();
		DEFFile defFile = tr.getResourceManager().getDEFData(lvl.getEnemyDefinitionAndPlacementFile());
		PUPFile pupFile = tr.getResourceManager().getPUPData(lvl.getPowerupPlacementFile());
		//TDFFile tdf = tr.getResourceManager().getTDFData(lvl.getTunnelDefinitionFile());
		//this.palette=palette;
		DEFObjectPlacer defPlacer = new DEFObjectPlacer(defFile,w,terrain);
		defPlacer.placeObjects(this);
		
		PUPObjectPlacer pupPlacer = new PUPObjectPlacer(pupFile,w);
		pupPlacer.placeObjects(this);
		}//end ObjectSystem(...)
	}//end ObjectSystem
