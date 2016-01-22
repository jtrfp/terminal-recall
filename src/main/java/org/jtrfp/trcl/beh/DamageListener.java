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
    public void damageEvent(Event ev);
    
    public static abstract class Event{
	private int damageAmount;
	public Event(){
	}
	/**
	 * @return the damageAmount
	 */
	public int getDamageAmount() {
	    return damageAmount;
	}
	
	public void setDamageAmount(int damageAmount){
	    this.damageAmount=damageAmount;
	}
    }//end Event
    public static class ProjectileDamage extends Event{public ProjectileDamage(){super();}}
    public static class CollisionDamage extends Event{public CollisionDamage(){super();}}
    public static class SurfaceCollisionDamage extends Event{public SurfaceCollisionDamage(){super();}}
    public static class GroundCollisionDamage extends Event{public GroundCollisionDamage(){super();}}
    public static class ElectrocutionDamage extends Event{public ElectrocutionDamage(){super();}}
    public static class ShearDamage extends SurfaceCollisionDamage{public ShearDamage(){super();}}
    public static class AirCollisionDamage extends CollisionDamage{public AirCollisionDamage(){super();}}
}//end DamageListener
