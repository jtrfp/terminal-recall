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

import org.jtrfp.trcl.miss.NAVObjective;

public interface DashboardLayout {
 public Point2D.Double getAmmoPosition();
 public Point2D.Double getWeaponNamePosition();
 public Point2D.Double getObjectivePosition();
 public double         getThrottleBarLength();
 public boolean        isThrottleHorizontal();
 public Point2D.Double getThrottlePosition();
 public double         getShieldBarLength();
 public boolean        isShieldHorizontal();
 public Point2D.Double getShieldPosition();
 public Point2D.Double getDistancePosition();
 public Point2D.Double getSectorPosition();
 public Point2D.Double getDashboardPosition();
 public Point2D.Double getDashboardDimensions();
 public Point2D.Double getMiniMapPosition();
 public String         getHumanReadableObjective(NAVObjective obj);
}//end DashboardLayout
