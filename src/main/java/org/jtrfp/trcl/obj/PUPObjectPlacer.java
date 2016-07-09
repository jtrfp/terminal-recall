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

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;
import org.jtrfp.trcl.miss.LoadingProgressReporter;

public class PUPObjectPlacer implements ObjectPlacer {
    ArrayList<PowerupObject> objs = new ArrayList<PowerupObject>();
    private final LoadingProgressReporter []placementReporters;

    public PUPObjectPlacer(PUPFile pupFile, TR tr, LoadingProgressReporter pupObjectReporter) {
	final LoadingProgressReporter[] locationReporters = pupObjectReporter
		.generateSubReporters(pupFile.getPowerupLocations().length);
	placementReporters = pupObjectReporter
		.generateSubReporters(pupFile.getPowerupLocations().length);
	int pupIndex=0;
	for (PowerupLocation loc : pupFile.getPowerupLocations()) {
	    locationReporters[pupIndex++].complete();
	    PowerupObject powerup = new PowerupObject(loc.getType());
	    final double[] pupPos = powerup.getPosition();
	    pupPos[0] = TRFactory.legacy2Modern(loc.getZ());
	    pupPos[1] = (TRFactory.legacy2Modern(loc.getY()) / TRFactory.mapWidth) * 16.
		    * tr.getWorld().sizeY;
	    pupPos[2] = TRFactory.legacy2Modern(loc.getX());
	    powerup.notifyPositionChange();
	    objs.add(powerup);
	}// end for(locations)
    }// end PUPObjectPlacer

    @Override
    public void placeObjects(RenderableSpacePartitioningGrid target, Vector3D positionOffset) {
	int objIndex=0;
	for (PowerupObject obj : objs) {
	    placementReporters[objIndex++].complete();
	    obj.movePositionBy(positionOffset);
	    target.add(obj);
	}//end for(objs)
    }//end placeObjects()
}// end PUPObjectPlacer
