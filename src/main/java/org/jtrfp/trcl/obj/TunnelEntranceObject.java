package org.jtrfp.trcl.obj;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;

public class TunnelEntranceObject extends WorldObject {
    private final Tunnel tunnel;
    public TunnelEntranceObject(TR tr, Tunnel tunnel) {
	super(tr);
	this.tunnel=tunnel;
	addBehavior(new TunnelEntranceBehavior());
	setVisible(false);
	DirectionVector entrance = tunnel.getSourceTunnel().getEntrance();
	setPosition(new Vector3D(TR.legacy2Modern(entrance.getZ()),TR.legacy2Modern(entrance.getY())-45000,TR.legacy2Modern(entrance.getX())));
	try{Model m = tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl());
	setModel(m);}
	catch(Exception e){e.printStackTrace();}
    }//end constructor

    private class TunnelEntranceBehavior extends Behavior{
	@Override
	public void _proposeCollision(WorldObject other){
	    WorldObject p = getParent();
	      if(other instanceof Player){
	        if(p.getPosition().distance(other.getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE*3){
		 //Turn off overworld
		 final TR tr = getTr();
		 tr.getOverworldSystem().deactivate();
		 //Turn on tunnel
		 tunnel.activate();
		 //Move player to tunnel
		 tr.getWorld().setFogColor(Color.black);
		 tr.getBackdropSystem().tunnelMode();
		 
		 final ProjectileFactory [] pfs = tr.getResourceManager().getProjectileFactories();
		 for(ProjectileFactory pf:pfs){
		     Projectile [] projectiles = pf.getProjectiles();
		     for(Projectile proj:projectiles){
			 ((WorldObject)proj).getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		     }//end for(projectiles)
		 }//end for(projectileFactories)
		 
		 tr.getPlayer().setPosition(Tunnel.TUNNEL_START_POS);
		 tr.getPlayer().setDirection(Tunnel.TUNNEL_START_DIRECTION);
		 tr.getPlayer().getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		 tr.getPlayer().getBehavior().probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
		 tr.getPlayer().getBehavior().probeForBehavior(CollidesWithTerrain.class).setEnable(false);
	        }//end if(close)
	    }//end if(Player)
	}//end _proposeCollision
    }//end TunnelEntranceBehavior
}//end TunnelEntrance
