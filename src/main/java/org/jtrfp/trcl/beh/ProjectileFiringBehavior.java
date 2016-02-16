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

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.gpu.BasicModelSource;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundTexture;

public class ProjectileFiringBehavior extends Behavior implements HasQuantifiableSupply{
    private static final Vector3D [] DEFAULT_POS=new Vector3D[]{Vector3D.ZERO};
    
    public static final String PENDING_FIRING = "pendingFiring";
    
    long 			timeWhenNextFiringPermittedMillis=0;
    long 			timeBetweenFiringsMillis	 =130;
    private Vector3D [] 	firingPositions			 =DEFAULT_POS;
    //private Vector3D 		firingHeading;
    private Rotation		firingRotation;
    private int 		firingPositionIndex		 =0;
    private ProjectileFactory 	projectileFactory;
    private boolean 		pendingFiring			 =false;
    private int 		multiplexLevel			 =1;
    private int 		ammoLimit			 =Integer.MAX_VALUE;
    private int 		ammo				 =0;
    private Integer []          firingVertices;
    private Vector3D []         firingDirections;
    private BasicModelSource    modelSource;
    private boolean             sumProjectorVelocity             = false;
    private SoundTexture        firingSFX;
    private final VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    
    @Override
    public void tick(long tickTimeMillis){
	if(tickTimeMillis>timeWhenNextFiringPermittedMillis && isPendingFiring()){
	    if(takeAmmo()){
	    	final WorldObject p = getParent();
	    	final Rotation firingRot = getFiringRotation();
	    	
	    	for(int mi=0; mi<multiplexLevel;mi++){
	    	    final Vector3D firingPosition     = getNextModelViewFiringPosition();
	    	    Vector3D rawFiringDirection = getFiringDirectionForPosition(getFiringPositionIndex());
	    	    rawFiringDirection          = new Vector3D(-rawFiringDirection.getX(),rawFiringDirection.getY(), rawFiringDirection.getZ());
	    	    final Vector3D firingDirection    = firingRot.applyTo(rawFiringDirection);
	    	    resetFiringTimer();
	    	    if(firingSFX==null)
	    	     projectileFactory.
	    	      fire(Vect3D.add(
	    		      p.getPositionWithOffset(),
	    		      firingPosition.toArray(),
	    		      new double[3]), 
	    		      firingDirection, 
	    		      getParent(),
	    		      isSumProjectorVelocity());
	    	    else
	    	     projectileFactory.
		    	fire(Vect3D.add(
		    	      p.getPositionWithOffset(),
		    	      firingPosition.toArray(),
		    	      new double[3]), 
		    	      firingDirection, 
		    	      getParent(),
		    	      isSumProjectorVelocity(),
		    	      firingSFX);
	    	}//for(multiplex)
	    }//end if(ammo)
	    	try{setPendingFiring(false);}catch(PropertyVetoException e){}
	}//end timeWhenNextfiringPermitted
    }//end _tick
    
    private boolean isAbsoluteFiringPositions(){
	return firingVertices==null || modelSource==null;
    }
    
    public boolean requestFire(){
	if(System.currentTimeMillis()>timeWhenNextFiringPermittedMillis)
	 try{setPendingFiring(true);setFiringRotation(null);}
	  catch(PropertyVetoException e){return false;}
	return true;
    }//end requestFire()
    
    public boolean canFire(){
	return getAmmo()>0;
    }
    
    public ProjectileFiringBehavior requestFire(Vector3D heading, Vector3D top){
	if(requestFire())
	 firingRotation = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,heading,top);
	return this;
    }
    
    protected boolean takeAmmo(){
	if(ammo<=0)return false; ammo--; return true;
    }
    
    private void resetFiringTimer(){
	timeWhenNextFiringPermittedMillis = System.currentTimeMillis()+timeBetweenFiringsMillis;}

    public Vector3D getNextModelViewFiringPosition(){
	if(isAbsoluteFiringPositions())
	 return new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,
	    		getParent().getHeading(),getParent().getTop()).applyTo(getNextAbsoluteModelViewFiringPosition());
	else{
	    final double [] vtx = modelSource.getVertex(getNextFiringVertex());
	    return new Vector3D(vtx[0],vtx[1],vtx[2]);
	}
    }//end getNextFiringPosition()
    
    public Vector3D peekNextModelViewFiringPosition(){
	if(isAbsoluteFiringPositions())
	 return new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,
	    		getParent().getHeading(),getParent().getTop()).applyTo(peekNextAbsoluteModelViewFiringPosition());
	else{
	    final double [] vtx = modelSource.getVertex(peekNextFiringVertex());
	    return new Vector3D(vtx[0],vtx[1],vtx[2]);
	}
    }//end peekNextFiringPosition()
    
    private Vector3D getNextAbsoluteModelViewFiringPosition(){
	return firingPositions[getNextRawFiringPositionIndex()];
    }
    
    private Vector3D peekNextAbsoluteModelViewFiringPosition(){
   	return firingPositions[peekNextRawFiringPositionIndex()];
       }
    
    private int getNextFiringVertex(){
	final int result = peekNextFiringVertex();
	firingPositionIndex++;
	firingPositionIndex%=firingVertices.length;
	return result;
    }
    
    private int peekNextFiringVertex(){
	return firingVertices[(firingPositionIndex+1)%firingVertices.length];
    }
    
    private int getNextRawFiringPositionIndex(){
	final int result = peekNextRawFiringPositionIndex(); 
	firingPositionIndex++;
	firingPositionIndex%=firingPositions.length;
	return result;
    }
    
    private int peekNextRawFiringPositionIndex(){
	return (firingPositionIndex+1)%firingPositions.length;
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

    /**
     * @return the timeBetweenFiringsMillis
     */
    public long getTimeBetweenFiringsMillis() {
        return timeBetweenFiringsMillis;
    }

    /**
     * @param timeBetweenFiringsMillis the timeBetweenFiringsMillis to set
     */
    public ProjectileFiringBehavior setTimeBetweenFiringsMillis(long timeBetweenFiringsMillis) {
        this.timeBetweenFiringsMillis = timeBetweenFiringsMillis;
        return this;
    }

    public ProjectileFiringBehavior setFiringPositions(
	    BasicModelSource modelSource, Integer[] firingVertices) {
	if(firingVertices.length==0)
	    return this;//Nothing to do; empty.
	this.modelSource=modelSource;
	this.firingVertices=firingVertices;
	return this;
    }

    public boolean isSumProjectorVelocity() {
        return sumProjectorVelocity;
    }

    public ProjectileFiringBehavior setSumProjectorVelocity(boolean sumProjectorVelocity) {
        this.sumProjectorVelocity = sumProjectorVelocity;
        return this;
    }

    public SoundTexture getFiringSFX() {
        return firingSFX;
    }

    public void setFiringSFX(SoundTexture firingSFX) {
        this.firingSFX = firingSFX;
    }

    public boolean isPendingFiring() {
        return pendingFiring;
    }

    public void setPendingFiring(boolean pendingFiring) throws PropertyVetoException {
	final boolean oldPending = this.pendingFiring;
	vcs.fireVetoableChange(PENDING_FIRING, oldPending, pendingFiring);
        this.pendingFiring = pendingFiring;
    }

    public void addVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener) {
	vcs.addVetoableChangeListener(propertyName, listener);
    }

    public void addVetoableChangeListener(VetoableChangeListener arg0) {
	vcs.addVetoableChangeListener(arg0);
    }

    public VetoableChangeListener[] getVetoableChangeListeners() {
	return vcs.getVetoableChangeListeners();
    }

    public VetoableChangeListener[] getVetoableChangeListeners(
	    String propertyName) {
	return vcs.getVetoableChangeListeners(propertyName);
    }

    public void removeVetoableChangeListener(String propertyName,
	    VetoableChangeListener listener) {
	vcs.removeVetoableChangeListener(propertyName, listener);
    }

    public void removeVetoableChangeListener(VetoableChangeListener arg0) {
	vcs.removeVetoableChangeListener(arg0);
    }
    
    public Vector3D getFiringDirectionForPosition(int index){
	final Vector3D [] firingVectors = getFiringVectors();
	if(firingVectors == null)
	    return Vector3D.PLUS_K;
	else
	    return firingDirections[index];
    }//end getFiringVectorForPosition()

    public Vector3D[] getFiringVectors() {
        return firingDirections;
    }

    /**
     */
    public void setFiringDirections(Vector3D[] firingRotations) {
        this.firingDirections = firingRotations;
    }

    protected int getFiringPositionIndex() {
        return firingPositionIndex;
    }

    protected void setFiringPositionIndex(int firingPositionIndex) {
        this.firingPositionIndex = firingPositionIndex;
    }

    protected Rotation getFiringRotation() {
	if(firingRotation == null){
	    final WorldObject parent = getParent();
	    firingRotation = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,parent.getHeading(),parent.getTop());
	    }
        return firingRotation;
    }

    protected void setFiringRotation(Rotation firingRotation) {
        this.firingRotation = firingRotation;
    }
}//end ProjectileFiringBehavior
