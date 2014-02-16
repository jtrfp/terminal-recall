package org.jtrfp.trcl.obj;

import java.awt.Color;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.tun.TunnelEntryListener;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;
import org.jtrfp.trcl.math.Vect3D;

public class TunnelEntranceObject extends WorldObject {
    private final Tunnel tunnel;
    private NAVObjective navObjectiveToRemove;
    private boolean onlyRemoveIfCurrent=false;
    private final double GROUND_HEIGHT_PAD=10000;
    public TunnelEntranceObject(TR tr, Tunnel tunnel) {
	super(tr);
	this.tunnel=tunnel;
	addBehavior(new TunnelEntranceBehavior());
	setVisible(false);
	DirectionVector entrance = tunnel.getSourceTunnel().getEntrance();
	final double [] position = getPosition();
	position[0]=TR.legacy2Modern(entrance.getZ());
	position[1]=TR.legacy2Modern(entrance.getY());
	position[2]=TR.legacy2Modern(entrance.getX());
	notifyPositionChange();
	try{Model m = tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl());
	setModel(m);}
	catch(Exception e){e.printStackTrace();}
    }//end constructor

    public class TunnelEntranceBehavior extends Behavior implements CollisionBehavior{
	@Override
	public void proposeCollision(WorldObject other){
	      if(other instanceof Player){
		 WorldObject entranceObject = getParent();
		final TR tr = entranceObject.getTr();
		final World world = tr.getWorld();
		final InterpolatingAltitudeMap map = tr.getAltitudeMap();
		double [] playerPos = other.getPosition();
		double [] thisPos = entranceObject.getPosition();
		final double groundHeightNorm =map.heightAt((thisPos[0]/TR.mapSquareSize), 
			    (thisPos[2]/TR.mapSquareSize));
		final double groundHeight = groundHeightNorm*(world.sizeY/2);
		//Ignore ground height with chambers because entrances don't behave themselves with this.
		if(!tr.getOverworldSystem().isChamberMode()&&playerPos[1]>groundHeight+GROUND_HEIGHT_PAD)return;
	        if(Vect3D.distanceXZ(entranceObject.getPosition(),other.getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE){
		 //Turn off overworld
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
		 entranceObject.getBehavior().probeForBehaviors(TELsubmitter, TunnelEntryListener.class);
		 final Player player = tr.getPlayer();
		 player.setPosition(Tunnel.TUNNEL_START_POS.toArray());
		 player.setDirection(Tunnel.TUNNEL_START_DIRECTION);
		 player.getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(false);
		 player.getBehavior().probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(true);
		 player.getBehavior().probeForBehavior(CollidesWithTerrain.class).setEnable(false);
		 final NAVObjective navObjective = getNavObjectiveToRemove();
	         if(navObjective!=null){
	             final Mission m = tr.getCurrentMission();
	             if(!(onlyRemoveIfCurrent&&navObjective!=m.currentNAVObjective()))m.removeNAVObjective(navObjective);
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
    public void setNavObjectiveToRemove(NAVObjective navObjectiveToRemove, boolean onlyRemoveIfLast) {
	this.onlyRemoveIfCurrent=true;
	setNavObjectiveToRemove(navObjectiveToRemove);
    }
    /**
     * @return the onlyRemoveIfCurrent
     */
    public boolean isOnlyRemoveIfCurrent() {
        return onlyRemoveIfCurrent;
    }
    /**
     * @param onlyRemoveIfCurrent the onlyRemoveIfCurrent to set
     */
    public void setOnlyRemoveIfCurrent(boolean onlyRemoveIfCurrent) {
        this.onlyRemoveIfCurrent = onlyRemoveIfCurrent;
    }
}//end TunnelEntrance
