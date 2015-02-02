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
package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageListener;

public class RedFlashOnDamage extends Behavior implements DamageListener {
    
    private void flash(){
	getParent().getTr().getGame().getRedFlash().flash();
    }

    @Override
    public void airCollisionDamage(int dmg) {
	flash();
    }

    @Override
    public void projectileDamage(int dmg) {
	flash();
    }

    @Override
    public void groundCollisionDamage(int dmg) {
	flash();
    }

    @Override
    public void tunnelCollisionDamage(int dmg) {
	flash();
    }

    @Override
    public void electrocutionDamage(int dmg) {
	flash();
    }

    @Override
    public void shearDamage(int dmg) {
	flash();
    }
    
}//end RedFlashOnCollisions
