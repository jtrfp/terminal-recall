package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class MissionCompleteOnDeathOrCollision extends Behavior implements DeathListener, CollisionBehavior {

    @Override
    public void notifyDeath() {
	getParent().getTr().getGame().missionComplete();
    }
    @Override
    public void proposeCollision(WorldObject other){
	if(other instanceof Player){getParent().getTr().getGame().missionComplete();}
    }
}
