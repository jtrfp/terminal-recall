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

import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;

public class Cloakable extends Behavior implements HasQuantifiableSupply {
    private long cloakExpirationTimeMillis=0;
    private byte updateCounter=0;
    
    @Override
    public void tick(long tickTimeMillis){
	if(updateCounter++==0){
	    if(isCloaked())
		System.out.println("Cloak time left: "+(cloakExpirationTimeMillis-System.currentTimeMillis())/1000+"s");
	}//end if(update?)
	
	updateCounter %= 1000;
    }//end _tick(...)
    
    public void setSupply(double amount) {
	if(Double.isFinite(amount)) {
	    cloakExpirationTimeMillis = System.currentTimeMillis()+(long)amount;
	}
	else if(Double.isInfinite(amount)) {
	    cloakExpirationTimeMillis = Long.MAX_VALUE;
	}
    }//end setSupply()

    @Override
    public void addSupply(double amount) throws SupplyNotNeededException {
	if(isCloaked())cloakExpirationTimeMillis+=amount;
	else{cloakExpirationTimeMillis=(long)amount+System.currentTimeMillis();}
    }

    @Override
    public double getSupply() {
	return cloakExpirationTimeMillis-System.currentTimeMillis();
    }
    
    public boolean isCloaked(){
	return (System.currentTimeMillis()<cloakExpirationTimeMillis);
    }
    
}//end Cloakable
