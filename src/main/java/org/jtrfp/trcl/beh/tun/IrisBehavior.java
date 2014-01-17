package org.jtrfp.trcl.beh.tun;

import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class IrisBehavior extends Behavior {
public static final int DAMAGE_ON_IMPACT=6554;
private final Controller controller;
private final double maxRadius;
private static final double X_FLUFF=80000;
    public IrisBehavior(Controller controller, double maxRadius) {
	if(controller==null)throw new NullPointerException("Controller is intolerably null.");
	this.controller=controller;
	this.maxRadius=maxRadius;
    }//end constructor
    @Override
    public void _proposeCollision(WorldObject wo){
	if(wo instanceof Player){
	    final WorldObject p = getParent();
	    final double [] thisPos = p.getPosition();
	    final double [] playerPos = wo.getPosition();
	    if(playerPos[0]>thisPos[0]&&playerPos[0]<thisPos[0]+X_FLUFF){
		final double dY=thisPos[1]-playerPos[1];
		final double dZ=thisPos[2]-playerPos[2];
		final double dist=Math.sqrt(dY*dY+dZ*dZ);
		final double currentRadius=maxRadius*(1.-Math.abs(1-controller.getCurrentFrame()));
		if(dist>currentRadius){
		    wo.getBehavior().probeForBehavior(DamageableBehavior.class).shearDamage(DAMAGE_ON_IMPACT);
		}//end if(crushed)
	    }//end if(in range)
	}//end if(Player)
    }//end proposeCollision(...)
}//end IrisBehavior
