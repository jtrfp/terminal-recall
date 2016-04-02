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

import org.jtrfp.trcl.miss.NAVObjective;

public class F3DashboardLayout implements DashboardLayout {
    private static final double TOP_LINE_Y    = .94;
    private static final double BOTTOM_LINE_Y = .85;
    private static final double FONT_SIZE     = .04;

    private static final Point2D.Double DASH_DIMS = new Point2D.Double(2,.55);
    @Override
    public Double getDashboardDimensions() {
	return DASH_DIMS;
    }
    private static final Point2D.Double AMMO_POS = new Point2D.Double(.01, BOTTOM_LINE_Y);
    @Override
    public Double getAmmoPosition() {
	return AMMO_POS;
    }
    private static final Point2D.Double WEP_NAME_POS = new Point2D.Double(-.44, BOTTOM_LINE_Y);
    @Override
    public Double getWeaponNamePosition() {
	return WEP_NAME_POS;
    }
    
    private static final Point2D.Double OBJ_POS = new Point2D.Double(-.45, TOP_LINE_Y);
    @Override
    public Double getObjectivePosition() {
	return OBJ_POS;
    }
    private static final Point2D.Double THROT_POS = new Point2D.Double(.18875 - 1, 1 - .175);
    @Override
    public Double getThrottlePosition() {
	return THROT_POS;
    }
    private static final Point2D.Double SHIELD_POS = new Point2D.Double(.13000 - 1, 1 - .175);
    @Override
    public Double getShieldPosition() {
	return SHIELD_POS;
    }
    private static final Point2D.Double DIST_POS = new Point2D.Double(.42, TOP_LINE_Y);
    @Override
    public Double getDistancePosition() {
	return DIST_POS;
    }
    private static final Point2D.Double SECT_POS = new Point2D.Double(.38, BOTTOM_LINE_Y);
    @Override
    public Double getSectorPosition() {
	return SECT_POS;
    }
    @Override
    public Double getDashboardPosition() {
	// TODO Auto-generated method stub
	return null;
    }
    private static final Point2D.Double MM_POS = new Point2D.Double(.825,.82);
    @Override
    public Double getMiniMapPosition() {
	return MM_POS;
    }
    @Override
    public boolean isThrottleHorizontal() {
	return false;
    }
    @Override
    public boolean isShieldHorizontal() {
	return false;
    }
    @Override
    public double getThrottleBarLength() {
	return .14;
    }
    @Override
    public double getShieldBarLength() {
	return .14;
    }
    @Override
    public String getHumanReadableObjective(NAVObjective obj) {
	return obj.getDescription();
    }

}//end F3DashboardLayout
