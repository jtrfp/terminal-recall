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

import java.io.IOException;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.DEFObjectPlacer;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.miss.LoadingProgressReporter;

public class ObjectSystem extends RenderableSpacePartitioningGrid {
    private final DEFObjectPlacer defPlacer;
    public ObjectSystem(TR tr,
	    LVLFile lvl, List<DEFObject> defList,
	    Vector3D headingOverride, Vector3D positionOffset, LoadingProgressReporter objectReporter)
	    throws IllegalAccessException, IOException, FileLoadException {
	super();
	final LoadingProgressReporter [] reporters 	= objectReporter.generateSubReporters(2);
	final LoadingProgressReporter defObjectReporter = reporters[0];
	final LoadingProgressReporter pupObjectReporter = reporters[1];
	DEFFile defFile = tr.getResourceManager().getDEFData(
		lvl.getEnemyDefinitionAndPlacementFile());
	PUPFile pupFile = tr.getResourceManager().getPUPData(
		lvl.getPowerupPlacementFile());
	defPlacer = new DEFObjectPlacer(defFile, tr, defList,defObjectReporter);
	defPlacer.setHeadingOverride(headingOverride);
	defPlacer.placeObjects(this, positionOffset);
	PUPObjectPlacer pupPlacer = new PUPObjectPlacer(pupFile, tr,pupObjectReporter);
	pupPlacer.placeObjects(this, positionOffset);
	//tr.getDefaultGrid().blockingAddBranch(this);
    }// end ObjectSystem(...)
    /**
     * @return the defPlacer
     */
    public DEFObjectPlacer getDefPlacer() {
        return defPlacer;
    }
}// end ObjectSystem
