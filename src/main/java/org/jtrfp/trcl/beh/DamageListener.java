/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

public interface DamageListener {
    public void airCollisionDamage(int dmg);
    public void projectileDamage(int dmg);
    public void groundCollisionDamage(int dmg);
    public void tunnelCollisionDamage(int dmg);
    public void electrocutionDamage(int dmg);
    public void shearDamage(int dmg);
}//end DamageListener
