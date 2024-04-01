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

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.tools.Util;

public class PUPObjectPlacer implements ObjectPlacer {
    ArrayList<PowerupObject> objs = new ArrayList<PowerupObject>();
    private LoadingProgressReporter []placementReporters;
    private Vector3D positionOffset = Vector3D.ZERO;
    private RenderableSpacePartitioningGrid targetGrid;
    private TR tr;
    private PUPFile pupData;
    private LoadingProgressReporter rootReporter;
    
    @Override
    public void placeObjects() {
	Util.assertPropertiesNotNull(this, "tr","rootReporter","targetGrid");
	final PUPFile pupData = getPupData();
	final LoadingProgressReporter [] locationReporters = getPlacementReporters();
	int pupIndex=0;
	for (PowerupLocation loc : pupData.getPowerupLocations()) {
	    locationReporters[pupIndex++].complete();
	    try {
		PowerupObject powerup = new PowerupObject(loc.getType());
		final double[] pupPos = powerup.getPosition();
		pupPos[0] = TRFactory.legacy2Modern(loc.getZ());
		pupPos[1] = (TRFactory.legacy2Modern(loc.getY()) / TRFactory.mapWidth) * 16.
			* tr.getWorld().sizeY;
		pupPos[2] = TRFactory.legacy2Modern(loc.getX());
		powerup.notifyPositionChange();
		objs.add(powerup);
	    } catch(FileNotFoundException e) {e.printStackTrace();}
	}// end for(locations)
	final RenderableSpacePartitioningGrid target         = getTargetGrid();
	final Vector3D                        positionOffset = getPositionOffset();
	int objIndex=0;
	for (PowerupObject obj : objs) {
	    placementReporters[objIndex++].complete();
	    obj.movePositionBy(positionOffset);
	    target.add(obj);
	}//end for(objs)
    }//end placeObjects()

    @Override
    public Vector3D getPositionOffset() {
        return positionOffset;
    }

    @Override
    public void setPositionOffset(Vector3D positionOffset) {
        this.positionOffset = positionOffset;
    }

    @Override
    public RenderableSpacePartitioningGrid getTargetGrid() {
        return targetGrid;
    }

    @Override
    public void setTargetGrid(RenderableSpacePartitioningGrid targetGrid) {
        this.targetGrid = targetGrid;
    }

    public TR getTr() {
        return tr;
    }

    public void setTr(TR tr) {
        this.tr = tr;
    }

    public PUPFile getPupData() {
        return pupData;
    }

    public void setPupData(PUPFile pupData) {
        this.pupData = pupData;
    }

    public LoadingProgressReporter getRootReporter() {
        return rootReporter;
    }

    public void setRootReporter(LoadingProgressReporter rootReporter) {
        this.rootReporter = rootReporter;
    }

    public LoadingProgressReporter[] getPlacementReporters() {
	if(placementReporters == null){
	    placementReporters = getRootReporter()
			.generateSubReporters(getPupData().getPowerupLocations().length);
	}
        return placementReporters;
    }
}// end PUPObjectPlacer
