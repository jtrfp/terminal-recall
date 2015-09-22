/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class F3BriefingLayout implements BriefingLayout {

    private static final Point2D.Double TEXT_POS = new Point2D.Double(-.7, -.45); 
    @Override
    public Double getTextPosition() {
	return TEXT_POS;
    }

    @Override
    public int getNumCharsPerLine() {
	return 36;
    }

    @Override
    public int getNumLines() {
	return 10;
    }

    private static final Rotation CAM_HDG_ADJ = new Rotation(Vector3D.PLUS_I,(5./180.)*Math.PI);
    @Override
    public Rotation cameraHeadingAdjust() {
	return CAM_HDG_ADJ;
    }

    @Override
    public double getFontSizeGL() {
	return .047;
    }

    @Override
    public Collection<String> getNameTokens() {
	return Arrays.asList("$C");
    }

}//end F3BriefingLayout
