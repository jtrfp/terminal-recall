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

public class TVDashboardLayout implements DashboardLayout {
    private static final double TOP_LINE_Y    = .89;
    private static final double BOTTOM_LINE_Y = .79;
    private static final double FONT_SIZE     = .04;
    private static final double LEFT_MARGIN   = -.73;
    private static final double AMMO_MARGIN   = -.25;
    private static final double WEPN_MARGIN   = -.14;
    private static final double OBJ_LINE      = .67;

    private static final Point2D.Double DASH_DIMS = new Point2D.Double(2,.64);
    @Override
    public Double getDashboardDimensions() {
	return DASH_DIMS;
    }
    private static final Point2D.Double AMMO_POS = new Point2D.Double(AMMO_MARGIN, BOTTOM_LINE_Y);
    @Override
    public Double getAmmoPosition() {
	return AMMO_POS;
    }
    private static final Point2D.Double WEP_NAME_POS = new Point2D.Double(WEPN_MARGIN, TOP_LINE_Y);
    @Override
    public Double getWeaponNamePosition() {
	return WEP_NAME_POS;
    }
    
    private static final Point2D.Double OBJ_POS = new Point2D.Double(LEFT_MARGIN, OBJ_LINE);
    @Override
    public Double getObjectivePosition() {
	return OBJ_POS;
    }
    private static final Point2D.Double THROT_POS = new Point2D.Double(.32, .857);
    @Override
    public Double getThrottlePosition() {
	return THROT_POS;
    }
    private static final Point2D.Double SHIELD_POS = new Point2D.Double(.32, .94);
    @Override
    public Double getShieldPosition() {
	return SHIELD_POS;
    }
    private static final Point2D.Double DIST_POS = new Point2D.Double(LEFT_MARGIN, BOTTOM_LINE_Y);
    @Override
    public Double getDistancePosition() {
	return DIST_POS;
    }
    private static final Point2D.Double SECT_POS = new Point2D.Double(LEFT_MARGIN, TOP_LINE_Y);
    @Override
    public Double getSectorPosition() {
	return SECT_POS;
    }
    @Override
    public Double getDashboardPosition() {
	// TODO Auto-generated method stub
	return null;
    }
    private static final Point2D.Double MM_POS = new Point2D.Double(.77,.7);
    @Override
    public Double getMiniMapPosition() {
	return MM_POS;
    }
    @Override
    public boolean isThrottleHorizontal() {
	return true;
    }
    @Override
    public boolean isShieldHorizontal() {
	return true;
    }
    @Override
    public double getThrottleBarLength() {
	return .22;
    }
    @Override
    public double getShieldBarLength() {
	return .22;
    }
    @Override
    public String getHumanReadableObjective(NAVObjective obj) {
	//Abbreviate. A little hacky, but it should work well into version 2 when something better is figured out.
	final String desc = obj.getDescription();
	if(desc.contentEquals("Enter Tunnel"))
	    return "TUN";
	else if(desc.contentEquals("Exit Tunnel"))
	    return "EXT";
	else if(desc.contentEquals("Fly To Jump Zone"))
	    return "DUN";
	else if(desc.contains("Destroy Shield"))
	    return "SHD";
	else if(desc.contentEquals("Destroy Boss"))
	    return "BOS";
	else if(desc.contentEquals("Checkpoint"))
	    return "CHK";
	else if(desc.contentEquals("Destroy Target"))
	    return "TGT";
	else return "???";
    }
    @Override
    public double getMiniMapRadius() {
	return .2;
    }

}//end TVDashboardLayout
