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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.Player;

public class DamageableBehavior extends Behavior {
    //////////// PROPERTIES //////////////////////
    public static final String HEALTH       = "health";
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    	private int maxHealth=65535;
	private int health=maxHealth;
	private boolean acceptsProjectileDamage=true;
	private boolean dieOnZeroHealth=true;//TODO: Redesign such that there's a DieOnZeroHealth behavior?
	private long invincibilityExpirationTime=System.currentTimeMillis()+100;//Safety time in case init causes damage
	
	protected void generalDamage(final DamageListener.Event evt){
	    if(evt==null)
		throw new NullPointerException("Passed damage event intolerably null.");
	    if(!isEnabled())return;
	    if(isInvincible())return;
	    if(getHealth()<=0)return;
	    if(evt instanceof DamageListener.ProjectileDamage && !isAcceptsProjectileDamage())
		return;
	    
	    setHealth(getHealth()-evt.getDamageAmount());
	    probeForBehaviors(new AbstractSubmitter<DamageListener>(){
		@Override
		public void submit(DamageListener item) {
		    item.damageEvent(evt);
		}}, DamageListener.class);
		if(getHealth()<=0 && isDieOnZeroHealth())
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
	    	if(amt==getMaxHealth()){unDamage();return;}
	    	if(getHealth()+amt>getMaxHealth()){
	    	    throw new SupplyNotNeededException();}
		setHealth(getHealth()+amt);
		}

	public void unDamage() throws SupplyNotNeededException{
	    	if(!isEnabled())throw new SupplyNotNeededException();
	    	//10% of hysteresis to avoid frivolous use of full shield restores.
	    	final int maxHealth = getMaxHealth();
	    	if(getHealth()+maxHealth*.1>=maxHealth)throw new SupplyNotNeededException();
		setHealth(getMaxHealth());
		}
	
	public DamageableBehavior setHealth(int val){
	    final int oldHealth = getHealth();
	    health=val;
	    pcs.firePropertyChange(HEALTH, oldHealth, val);
	    return this;
	}//end setHealth(...)
	
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
	    setHealth(Math.min(getHealth(), maxHealth));
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

	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}

	public boolean isDieOnZeroHealth() {
	    return dieOnZeroHealth;
	}

	public DamageableBehavior setDieOnZeroHealth(boolean dieOnZeroHealth) {
	    this.dieOnZeroHealth = dieOnZeroHealth;
	    return this;
	}
    }//end DamageableBehavior
