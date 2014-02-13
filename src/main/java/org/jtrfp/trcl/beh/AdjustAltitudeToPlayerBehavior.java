package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class AdjustAltitudeToPlayerBehavior extends Behavior {
    private final Player player;
    private Vector3D DOWN=new Vector3D(0,-80000,0),UP=new Vector3D(0,80000,0);
    private boolean reverse=false;
    public AdjustAltitudeToPlayerBehavior(Player player){
	this.player=player;
    }//end AdjustAltitudeToPlayerBehavior
    
    @Override
    public void _tick(long tickTimeInMillis){
	final WorldObject thisObject = getParent();
	final boolean up=(player.getPosition()[1]>thisObject.getPosition()[1])!=reverse;
	    thisObject.getBehavior().probeForBehavior(MovesByVelocity.class).accellerate(up?UP:DOWN);
    }//end _tick(...)

    public AdjustAltitudeToPlayerBehavior setAccelleration(int accelleration) {
	UP=new Vector3D(0,accelleration,0);
	DOWN=new Vector3D(0,-accelleration,0);
	return this;
    }//end setAccelleration

    public void setReverse(boolean reverse) {
	this.reverse=reverse;
    }
}//end AdjustAltitudeToPlayerBehavior
