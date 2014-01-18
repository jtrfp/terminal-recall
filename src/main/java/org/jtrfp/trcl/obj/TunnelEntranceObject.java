package org.jtrfp.trcl.obj;

import java.awt.Color;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.tun.TunnelEntryListener;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.flow.NAVObjective;

public class TunnelEntranceObject extends WorldObject {
    private final Tunnel tunnel;
    private NAVObjective navObjectiveToRemove;
    public TunnelEntranceObject(TR tr, Tunnel tunnel) {
	super(tr);
	this.tunnel=tunnel;
	addBehavior(new TunnelEntranceBehavior());
	setVisible(false);
	DirectionVector entrance = tunnel.getSourceTunnel().getEntrance();
	final double [] position = getPosition();
	position[0]=TR.legacy2Modern(entrance.getZ());
	position[1]=TR.legacy2Modern(entrance.getY())-45000;
	position[2]=TR.legacy2Modern(entrance.getX());
	notifyPositionChange();
	try{Model m = tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl());
	setModel(m);}
	catch(Exception e){e.printStackTrace();}
    }//end constructor

    private class TunnelEntranceBehavior extends Behavior{
	@Override
	public void _proposeCollision(WorldObject other){
	    WorldObject p = getParent();
	      if(other instanceof Player){
	        if(new Vector3D(p.getPosition()).distance(new Vector3D(other.getPosition()))<CollisionManager.SHIP_COLLISION_DISTANCE*3){
		 //Turn off overworld
		 final TR tr = getTr();
		 tr.getOverworldSystem().deactivate();
		 //Turn on tunnel
		 tunnel.activate();
		 //Move player to tunnel
		 tr.getWorld().setFogColor(Color.black);
		 tr.getBackdropSystem().tunnelMode();
		 //Ensure chamber mode is off
		 tr.getOverworldSystem().setChamberMode(false);
		 //Update debug data
		 tr.getReporter().report("org.jtrfp.Tunnel.isInTunnel?", "true");
		 
		 final ProjectileFactory [] pfs = tr.getResourceManager().getProjectileFactories();
		 for(ProjectileFactory pf:pfs){
		     Projectile [] projectiles = pf.getProjectiles();
		     for(Projectile proj:projectiles){
			 ((WorldObject)proj).getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		     }//end for(projectiles)
		 }//end for(projectileFactories)
		 p.getBehavior().probeForBehaviors(TELsubmitter, TunnelEntryListener.class);
		 final Player player = tr.getPlayer();
		 player.setPosition(Tunnel.TUNNEL_START_POS.toArray());
		 player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
		 player.getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		 player.getBehavior().probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
		 player.getBehavior().probeForBehavior(CollidesWithTerrain.class).setEnable(false);
		 final NAVObjective navObjective = getNavObjectiveToRemove();
	         if(navObjective!=null){
	             tr.getCurrentMission().removeNAVObjective(navObjective);
	         }//end if(have NAV to remove
	        }//end if(close to Player)
	    }//end if(Player)
	}//end _proposeCollision
    }//end TunnelEntranceBehavior
    private final AbstractSubmitter<TunnelEntryListener> TELsubmitter = new AbstractSubmitter<TunnelEntryListener>(){
	@Override
	public void submit(TunnelEntryListener tel){tel.notifyTunnelEntered();} 
    };
    /**
     * @return the navObjectiveToRemove
     */
    public NAVObjective getNavObjectiveToRemove() {
        return navObjectiveToRemove;
    }
    /**
     * @param navObjectiveToRemove the navObjectiveToRemove to set
     */
    public void setNavObjectiveToRemove(NAVObjective navObjectiveToRemove) {
        this.navObjectiveToRemove = navObjectiveToRemove;
    }
}//end TunnelEntrance
