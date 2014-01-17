package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Powerup;

public class PowerupFactory {
    private final int POOL_SIZE = 20;
    private final PowerupObject [] objects = new PowerupObject[POOL_SIZE];
    private int powerupIndex=0;
    private final TR tr;
    public PowerupFactory(TR tr, Powerup type){
	this.tr=tr;
	for(int i=0; i<objects.length;i++){
	    (objects[i]=new PowerupObject(type,tr.getWorld())).setVisible(false);
	}//end for(objects)
    }//end constructor
    public PowerupObject spawn(double[] ds) {
	final PowerupObject result = objects[powerupIndex];
	result.destroy();
	result.reset(ds);
	tr.getWorld().add(result);
	powerupIndex++;
	powerupIndex%=objects.length;
	return result;
    }//end fire(...)
}//end PowerupFactory
