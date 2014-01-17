package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Powerup;

public class PluralizedPowerupFactory {
    private final PowerupFactory [] factories = new PowerupFactory[Powerup.values().length];
    public PluralizedPowerupFactory(TR tr){
	for(Powerup p:Powerup.values()){
	    factories[p.ordinal()]=new PowerupFactory(tr, p);
	}//end for(Powerups)
    }//end constructor
    
    public PowerupObject spawn(double[] ds, Powerup type) {
	return factories[type.ordinal()].spawn(ds);
    }//end spawn(...)
}//end PluralizedPowerupFactory
