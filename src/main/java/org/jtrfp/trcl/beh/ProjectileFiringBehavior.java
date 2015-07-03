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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.WorldObject;

public class ProjectileFiringBehavior extends Behavior implements HasQuantifiableSupply{
    private static final Vector3D [] DEFAULT_POS=new Vector3D[]{Vector3D.ZERO};
    long 			timeWhenNextFiringPermittedMillis=0;
    long 			timeBetweenFiringsMillis	 =130;
    private Vector3D [] 	firingPositions			 =DEFAULT_POS;
    private Vector3D 		firingHeading;
    private int 		firingPositionIndex		 =0;
    private ProjectileFactory 	projectileFactory;
    private boolean 		pendingFiring			 =false;
    private int 		multiplexLevel			 =1;
    private int 		ammoLimit			 =Integer.MAX_VALUE;
    private int 		ammo				 =0;
    @Override
    public void _tick(long tickTimeMillis){
	if(tickTimeMillis>timeWhenNextFiringPermittedMillis && pendingFiring){
	    if(takeAmmo()){
	    	final WorldObject p = getParent();
	    	Vector3D heading=this.firingHeading;
	    	if(this.firingHeading==null)heading = p.getHeading();
	    	for(int mi=0; mi<multiplexLevel;mi++){
	    	    final Vector3D firingPosition = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,
	    		heading,p.getTop()).applyTo(getNextFiringPosition());
	    	    resetFiringTimer();
	    	    projectileFactory.
	    	      fire(Vect3D.add(
	    		      p.getPositionWithOffset(),
	    		      firingPosition.toArray(),
	    		      new double[3]), 
	    		      heading, 
	    		      getParent());
	    	}//for(multiplex)
	    	heading = p.getHeading();
	    }//end if(ammo)
	    	pendingFiring=false;
	}//end timeWhenNextfiringPermitted
    }//end _tick
    
    public boolean requestFire(){
	if(System.currentTimeMillis()>timeWhenNextFiringPermittedMillis)
	 pendingFiring=true;
	return pendingFiring;
    }
    
    public boolean canFire(){
	return getAmmo()>0;
    }
    
    public ProjectileFiringBehavior requestFire(Vector3D heading){
	//System.out.println("Firing requested.");
	pendingFiring=true;
	firingHeading=heading;
	return this;
    }
    
    
    protected boolean takeAmmo(){
	if(ammo<=0)return false; ammo--; return true;
    }
    
    private void resetFiringTimer(){
	timeWhenNextFiringPermittedMillis = System.currentTimeMillis()+timeBetweenFiringsMillis;}

    private Vector3D getNextFiringPosition(){
	firingPositionIndex++;
	firingPositionIndex%=firingPositions.length;
	return firingPositions[firingPositionIndex];
    }
    /**
     * @return the firingPositions
     */
    public Vector3D[] getFiringPositions() {
        return firingPositions;
    }
    /**
     * @param firingPositions the firingPositions to set
     */
    public ProjectileFiringBehavior setFiringPositions(Vector3D[] firingPositions) {
        this.firingPositions = firingPositions;
        return this;
    }//end ProjectileFiringBehavior

    /**
     * @return the projectileFactory
     */
    public ProjectileFactory getProjectileFactory() {
        return projectileFactory;
    }

    /**
     * @param projectileFactory the projectileFactory to set
     */
    public ProjectileFiringBehavior setProjectileFactory(ProjectileFactory projectileFactory) {
        this.projectileFactory = projectileFactory;
        return this;
    }

    /**
     * @return the ammo
     */
    public int getAmmo() {
        return (int)getSupply();
    }

    @Override
    public void addSupply(double amount) throws SupplyNotNeededException {
	if(ammo==ammoLimit)throw new SupplyNotNeededException();
	ammo=(int)Math.min(ammo+amount, ammoLimit);
	
    }

    @Override
    public double getSupply() {
	return ammo;
    }

    /**
     * @return the multiplexLevel
     */
    public int getMultiplexLevel() {
        return multiplexLevel;
    }

    /**
     * @param multiplexLevel the multiplexLevel to set
     */
    public ProjectileFiringBehavior setMultiplexLevel(int multiplexLevel) {
        this.multiplexLevel = multiplexLevel;
        return this;
    }

    /**
     * @return the ammoLimit
     */
    public int getAmmoLimit() {
        return ammoLimit;
    }

    /**
     * @param ammoLimit the ammoLimit to set
     */
    public void setAmmoLimit(int ammoLimit) {
        this.ammoLimit = ammoLimit;
        ammo=(int)(Math.min(getSupply(), ammoLimit));
    }
}//end ProjectileFiringBehavior
