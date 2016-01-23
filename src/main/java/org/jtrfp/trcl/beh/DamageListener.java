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

import java.util.ArrayList;
import java.util.Collection;


public interface DamageListener {
    public void damageEvent(Event ev);
    
    public static abstract class Event{
	private int damageAmount;
	private ArrayList<String> suggestedSFX = new ArrayList<String>();
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
	protected Collection<String> getSuggestedSFX() {
	    return suggestedSFX;
	}
	protected void setSuggestedSFX(Collection<String> suggestedSFX) {
	    this.suggestedSFX.clear();
	    this.suggestedSFX.addAll(suggestedSFX);	}
    }//end Event
    public static class ProjectileDamage extends Event{public ProjectileDamage(){super();getSuggestedSFX().add("EXP1.WAV");}}
    public static class CollisionDamage extends Event{public CollisionDamage(){super();getSuggestedSFX().add("EXP4.WAV");}}
    public static class SurfaceCollisionDamage extends CollisionDamage{public SurfaceCollisionDamage(){super();}}
    public static class GroundCollisionDamage extends CollisionDamage{public GroundCollisionDamage(){super();getSuggestedSFX().clear();getSuggestedSFX().add("GROUND.WAV");}}
    public static class ElectrocutionDamage extends Event{public ElectrocutionDamage(){super();getSuggestedSFX().add("EXP3.WAV");}}
    public static class ShearDamage extends SurfaceCollisionDamage{public ShearDamage(){super();getSuggestedSFX().add("SCRAPE.WAV");}}
    public static class AirCollisionDamage extends CollisionDamage{public AirCollisionDamage(){super();getSuggestedSFX().add("EXP5.WAV");}}
}//end DamageListener
