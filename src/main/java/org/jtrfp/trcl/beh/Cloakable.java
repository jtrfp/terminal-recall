package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;

public class Cloakable extends Behavior implements HasQuantifiableSupply {
    private long cloakExpirationTimeMillis=0;
    private boolean cloaked=false;
    private byte updateCounter=0;
    
    @Override
    public void _tick(long tickTimeMillis){
	if(updateCounter++==0){
	    if(isCloaked())
		{if(System.currentTimeMillis()>cloakExpirationTimeMillis)cloaked=false;}
	}//end if(update?)
    }//end _tick(...)

    @Override
    public void addSupply(double amount) throws SupplyNotNeededException {
	if(isCloaked())cloakExpirationTimeMillis+=amount;
	else{cloakExpirationTimeMillis=(long)amount+System.currentTimeMillis();cloaked=true;}
    }

    @Override
    public double getSupply() {
	return cloakExpirationTimeMillis-System.currentTimeMillis();
    }
    
    public boolean isCloaked(){
	return cloaked;
    }
    
}//end Cloakable
