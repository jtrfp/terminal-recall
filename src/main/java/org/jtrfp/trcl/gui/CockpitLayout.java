/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public interface CockpitLayout {
 public Vector3D   getMiniMapPosition();
 public double     getMiniMapRadius();
 public Vector3D   getMiniMapNormal();
 public Vector3D   getMiniMapTopOrigin();
 
 public static class Default implements CockpitLayout {

    @Override
    public Vector3D getMiniMapPosition() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public double getMiniMapRadius() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public Vector3D getMiniMapNormal() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Vector3D getMiniMapTopOrigin() {
	// TODO Auto-generated method stub
	return null;
    }
 }//end Default
}//end CockpitLayout
