package org.jtrfp.trcl.beh;

import java.awt.event.KeyEvent;

import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.WorldObject;

public class AfterburnerBehavior extends Behavior implements HasQuantifiableSupply {
    boolean firstDetected=true;
    private double fuelRemaining=0;
    private double formerMax,formerProp,newMax;
    @Override
    public void _tick(long tickTimeMillis){
	WorldObject p = getParent();
	if(p.getTr().getKeyStatus().isPressed(KeyEvent.VK_F)){
	    if(firstDetected){
		afterburnerOnTransient(p);
		firstDetected=false;
		fuelRemaining-=((double)p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/
			(double)Powerup.AFTERBURNER_TIME_PER_UNIT_MILLIS);
	    }//end if(firstDetected)
	    p.getBehavior().probeForBehavior(Propelled.class).setPropulsion(newMax).setMaxPropulsion(newMax);
	}//end if(F)
	else{
	    if(firstDetected==false)
	    	{afterburnerOffTransient(p);}
	    firstDetected=true;
	}//end else{}
    }//end _tick
    
    private void afterburnerOnTransient(WorldObject p){
	//Save former max, former propulsion
	//TODO: Ignition SFX, start sustain SFX
	Propelled prop = p.getBehavior().probeForBehavior(Propelled.class);
	formerMax=prop.getMaxPropulsion();
	formerProp=prop.getPropulsion();
	newMax=formerMax*3;
    }
    private void afterburnerOffTransient(WorldObject p){
	//TODO: De-Ignition SFX, end sustain SFX
	Propelled prop = p.getBehavior().probeForBehavior(Propelled.class);
	prop.setMaxPropulsion(formerMax);
	prop.setPropulsion(formerProp);
    }

    @Override
    public void addSupply(double amount) {
	fuelRemaining+=amount;
	
    }

    @Override
    public double getSupply() {
	return fuelRemaining;
    }
}//end AfterburnerBehavior
