package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Powerup;

public class PluralizedPowerupFactory {
    private final PowerupFactory [] factories = new PowerupFactory[Powerup.values().length];
    public PluralizedPowerupFactory(TR tr){
	for(Powerup p:Powerup.values()){
	    factories[p.ordinal()]=new PowerupFactory(tr, p);
	}//end for(Powerups)
    }//end constructor
    
    public PowerupObject spawn(Vector3D newPosition, Powerup type) {
	return factories[type.ordinal()].spawn(newPosition);
    }//end spawn(...)
}//end PluralizedPowerupFactory
