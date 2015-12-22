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
package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.Player;

public class DamageableBehavior extends Behavior {
    	private int maxHealth=65535;
	private int health=maxHealth;
	private boolean acceptsProjectileDamage=true;
	private long invincibilityExpirationTime=System.currentTimeMillis()+100;//Safety time in case init causes damage
	
	protected void generalDamage(final DamageListener.Event evt){
	    if(evt==null)
		throw new NullPointerException("Passed damage event intolerably null.");
	    if(!isEnabled())return;
	    if(isInvincible())return;
	    if(health<=0)return;
	    if(evt instanceof DamageListener.ProjectileDamage && !isAcceptsProjectileDamage())
		return;
		
	    probeForBehaviors(new AbstractSubmitter<DamageListener>(){
		@Override
		public void submit(DamageListener item) {
		    item.damageEvent(evt);
		}}, DamageListener.class);
	    health-=evt.getDamageAmount();
		if(health<=0)
		    die();
		else if(getParent() instanceof Player)addInvincibility(2500);//Safety/Escape
	}//end generalDamage(...)
	
	private void die(){
	    getParent().probeForBehavior(DeathBehavior.class).die();
	    //getParent().destroy();
	}

	public boolean isInvincible(){
	    return invincibilityExpirationTime>System.currentTimeMillis();
	}
	
	public int getHealth(){
		return health;
		}

	public void unDamage(int amt) throws SupplyNotNeededException{
	    	if(!isEnabled())throw new SupplyNotNeededException();
	    	if(amt==maxHealth){unDamage();return;}
	    	if(health+amt>maxHealth){
	    	    throw new SupplyNotNeededException();}
		health+=amt;
		}

	public void unDamage() throws SupplyNotNeededException{
	    	if(!isEnabled())throw new SupplyNotNeededException();
	    	//10% of hysteresis to avoid frivolous use of full shield restores.
	    	if(health+maxHealth*.1>=maxHealth)throw new SupplyNotNeededException();
		health=maxHealth;
		}
	public DamageableBehavior setHealth(int val){
	    health=val;return this;
	}
	
	private final Submitter<DeathListener> deathSub = new Submitter<DeathListener>(){

	    @Override
	    public void submit(DeathListener item) {
		item.notifyDeath();
	    }

	    @Override
	    public void submit(Collection<DeathListener> items) {
		for(DeathListener l:items){submit(l);}
	    }
	};

	public void addInvincibility(int invincibilityTimeDeltaMillis) {
	    ensureIsInvincible();
	    invincibilityExpirationTime+=invincibilityTimeDeltaMillis;
	}

	protected void ensureIsInvincible() {
	    if(!isInvincible())invincibilityExpirationTime=System.currentTimeMillis()+10;//10 for padding
	}

	/**
	 * @return the maxHealth
	 */
	public int getMaxHealth() {
	    return maxHealth;
	}

	/**
	 * @param maxHealth the maxHealth to set
	 */
	public DamageableBehavior setMaxHealth(int maxHealth) {
	    this.maxHealth = maxHealth;
	    return this;
	}
	
	
	public static class SupplyNotNeededException extends Exception{
	    
	}


	public DamageableBehavior addHealth(int delta) {
	    setHealth(getHealth()+delta);
	    return this;
	}
	/**
	 * @return the acceptsProjectileDamage
	 */
	public boolean isAcceptsProjectileDamage() {
	    return acceptsProjectileDamage;
	}
	/**
	 * @param acceptsProjectileDamage the acceptsProjectileDamage to set
	 */
	public DamageableBehavior setAcceptsProjectileDamage(boolean acceptsProjectileDamage) {
	    this.acceptsProjectileDamage = acceptsProjectileDamage;
	    return this;
	}
	
	public void proposeDamage(DamageListener.Event evt){
	    generalDamage(evt);
	}
    }//end DamageableBehavior
