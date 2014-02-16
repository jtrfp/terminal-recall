package org.jtrfp.trcl.beh.tun;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.ProjectileBehavior;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Projectile;
import org.jtrfp.trcl.obj.WorldObject;

public class DestructibleWallBehavior extends Behavior implements CollisionBehavior {
    private static final double THICKNESS_X=4000;
    public static final int DAMAGE_ON_IMPACT=6554;
    @Override
    public void proposeCollision(WorldObject other){
	final double [] otherPos=other.getPosition();
	final WorldObject p = getParent();
	final double [] thisPos=p.getPosition();
	if(otherPos[0]>thisPos[0]&& otherPos[0]<thisPos[0]+THICKNESS_X){
    	    if(other instanceof Player){
    	        final Player player=(Player)other;
    	        player.getBehavior().probeForBehavior(DamageableBehavior.class).impactDamage(DAMAGE_ON_IMPACT);
    	        }//end if(Player)
    	    else if(other instanceof Projectile){
    		other.getBehavior().probeForBehavior(ProjectileBehavior.class).forceCollision(p);
    	    	}//end if(Projectile)
    	}//end if(in range)
    }//end _proposeCollision(...)
}//end DestructibleAllBehavior
