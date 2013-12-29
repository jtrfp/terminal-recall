package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.file.Powerup;

public class LeavesPowerupOnDeathBehavior extends Behavior implements
	DeathListener {
    private final Powerup pup;
    public LeavesPowerupOnDeathBehavior(Powerup p){
	this.pup=p;
    }
    @Override
    public void notifyDeath() {
	getParent().getTr().getResourceManager().getPluralizedPowerupFactory().
		spawn(getParent().getPosition(), pup);
    }//end notifyDeath()
}//end LeavesPowerupOnDeathBehavior
