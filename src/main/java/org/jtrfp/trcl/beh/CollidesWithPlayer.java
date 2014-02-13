package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithPlayer extends Behavior {
    private final double boundingRadius;
    private Player player;
    public CollidesWithPlayer(double boundingRadius){
	this.boundingRadius=boundingRadius;
    }
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof Player){
	    final double distance=TR.twosComplimentDistance(other.getPosition(), getParent().getPosition());
	    player=(Player)other;
	    if(distance<(boundingRadius+2048)){
		getParent().getBehavior().probeForBehaviors(sub, PlayerCollisionListener.class);
	    }//end if(close enough)
	}//end if(player)
    }//end _proposeCollision()
    
    private final Submitter<PlayerCollisionListener> sub = new AbstractSubmitter<PlayerCollisionListener>(){
	@Override
	public void submit(PlayerCollisionListener l){
	    l.collidedWithPlayer(player);
	}//end submit(...)
    };//end Submitter
}
