package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;


public class UpgradeableProjectileFiringBehavior extends
	ProjectileFiringBehavior {
    private int capabilityLevel=0;
    private int maxCapabilityLevel=2;
    private boolean limitlessBottomLevel=true;
    private Vector3D [][] firingMultiplexMap;
    @Override
    public void addSupply(double amt) throws SupplyNotNeededException{
	raiseCapabilityLevel();
	super.addSupply(amt);
    }
    @Override
    public double getSupply(){
	if(limitlessBottomLevel && capabilityLevel==0)return -1;
	else return super.getSupply();
    }
    
    @Override
    protected boolean takeAmmo(){
	if(capabilityLevel==0&&limitlessBottomLevel())return true;
	boolean result = super.takeAmmo();
	if(!result)resetCapabilityLevel();
	return result;
    }
    
    protected void raiseCapabilityLevel(){
	capabilityLevel++;
	capabilityLevel=Math.min(capabilityLevel,maxCapabilityLevel);
	super.setFiringPositions(firingMultiplexMap[capabilityLevel]);
	super.setMultiplexLevel((int)Math.pow(2,capabilityLevel));//Double the multiplex for each capability
    }
    
    protected void resetCapabilityLevel(){
	capabilityLevel=0;
	super.setFiringPositions(firingMultiplexMap[capabilityLevel]);
	super.setMultiplexLevel((int)Math.pow(2,capabilityLevel));//Double the multiplex for each capability
    }
    
    protected boolean limitlessBottomLevel(){return limitlessBottomLevel;}
    
    public UpgradeableProjectileFiringBehavior setFiringMultiplexMap(Vector3D firingMultiplexMap [][]){
	this.firingMultiplexMap=firingMultiplexMap;
	this.setFiringPositions(firingMultiplexMap[0]);
	return this;
    }

    /**
     * @return the capabilityLevel
     */
    public int getCapabilityLevel() {
        return capabilityLevel;
    }

    /**
     * @param capabilityLevel the capabilityLevel to set
     */
    public UpgradeableProjectileFiringBehavior setCapabilityLevel(int capabilityLevel) {
        this.capabilityLevel = capabilityLevel;
        return this;
    }

    /**
     * @return the maxCapabilityLevel
     */
    public int getMaxCapabilityLevel() {
        return maxCapabilityLevel;
    }

    /**
     * @param maxCapabilityLevel the maxCapabilityLevel to set
     */
    public UpgradeableProjectileFiringBehavior setMaxCapabilityLevel(int maxCapabilityLevel) {
        this.maxCapabilityLevel = maxCapabilityLevel;
        return this;
    }

    /**
     * @return the limitlessBottomLevel
     */
    public boolean isLimitlessBottomLevel() {
        return limitlessBottomLevel;
    }

    /**
     * @param limitlessBottomLevel the limitlessBottomLevel to set
     */
    public UpgradeableProjectileFiringBehavior setLimitlessBottomLevel(boolean limitlessBottomLevel) {
        this.limitlessBottomLevel = limitlessBottomLevel;
        return this;
    }

    /**
     * @return the firingMultiplexMap
     */
    public Vector3D[][] getFiringMultiplexMap() {
        return firingMultiplexMap;
    }
}//end UpgradeableProjectilFiringBehavior
