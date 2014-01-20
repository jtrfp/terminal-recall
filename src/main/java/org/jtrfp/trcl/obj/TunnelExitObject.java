package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.Tunnel;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.flow.NAVObjective;

public class TunnelExitObject extends WorldObject {
    private  Vector3D exitLocation;
    private  Vector3D exitHeading,exitTop;
    private final Tunnel tun;
    private final TR tr;
    private NAVObjective navObjectiveToRemove;
    private boolean mirrorTerrain=false;
    public TunnelExitObject(TR tr, Tunnel tun) {
	super(tr);
	addBehavior(new TunnelExitBehavior());
	final DirectionVector v = tun.getSourceTunnel().getExit();
	final double EXIT_Y_NUDGE=-8000;
	this.exitLocation=new Vector3D(TR.legacy2Modern(v.getZ()),TR.legacy2Modern(v.getY()+EXIT_Y_NUDGE),TR.legacy2Modern(v.getX()));
	this.tun=tun;
	exitHeading = tr.getAltitudeMap().normalAt(exitLocation.getZ()/TR.mapWidth, exitLocation.getX()/TR.mapWidth);
	Vector3D horiz = exitHeading.crossProduct(Vector3D.PLUS_J);
	if(horiz.getNorm()==0)horiz=Vector3D.PLUS_I;else horiz=horiz.normalize();
	exitTop = exitHeading.crossProduct(horiz.negate()).normalize().negate();
	this.tr=tr;
	setVisible(false);
	try{Model m = tr.getResourceManager().getBINModel("SHIP.BIN", tr.getGlobalPalette(), tr.getGPU().getGl());
	setModel(m);}
	catch(Exception e){e.printStackTrace();}
    }
    
    private class TunnelExitBehavior extends Behavior{
	@Override
	public void _proposeCollision(WorldObject other){
	    if(other instanceof Player){
		if(other.getPosition()[0]>TunnelExitObject.this.getPosition()[0]){
		    tr.getOverworldSystem().setChamberMode(mirrorTerrain);
		    tr.getWorld().setFogColor((tr.getOverworldSystem().getFogColor()));
		    tr.getBackdropSystem().overworldMode();
		    //Teleport
		    other.setPosition(exitLocation.toArray());
		    //Heading
		    other.setHeading(exitHeading);
		    other.setTop(exitTop);
		    //Tunnel off
		    tun.deactivate();
		    //World on
		    tr.getOverworldSystem().activate();
		    //Reset player behavior
		    tr.getPlayer().getBehavior().probeForBehavior(DamageableBehavior.class).addInvincibility(250);//Safety kludge when near walls.
		    tr.getPlayer().getBehavior().probeForBehavior(CollidesWithTerrain.class).setEnable(true);
		    tr.getPlayer().getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(true);
		    tr.getPlayer().getBehavior().probeForBehavior(HeadingXAlwaysPositiveBehavior.class).setEnable(false);
		    //Update debug data
		    tr.getReporter().report("org.jtrfp.Tunnel.isInTunnel?", "false");
		    //Reset projectile behavior
		    final ProjectileFactory [] pfs = tr.getResourceManager().getProjectileFactories();
			 for(ProjectileFactory pf:pfs){
			     Projectile [] projectiles = pf.getProjectiles();
			     for(Projectile proj:projectiles){
				 ((WorldObject)proj).getBehavior().probeForBehavior(LoopingPositionBehavior.class).setEnable(true);
			     }//end for(projectiles)
			 }//end for(projectileFactories)
		final NAVObjective navObjective = getNavObjectiveToRemove();
		    if(navObjective!=null){
		        tr.getCurrentMission().removeNAVObjective(navObjective);
		    }//end if(have NAV to remove
		}//end if(x past threshold)
	    }//end if(Player)
	}//end proposeCollision()
    }//end TunnelExitBehavior

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

    public void setMirrorTerrain(boolean b) {
	mirrorTerrain=b;
    }

    /**
     * @return the exitLocation
     */
    public Vector3D getExitLocation() {
        return exitLocation;
    }

    /**
     * @param exitLocation the exitLocation to set
     */
    public void setExitLocation(Vector3D exitLocation) {
        this.exitLocation = exitLocation;
    }

    /**
     * @return the mirrorTerrain
     */
    public boolean isMirrorTerrain() {
        return mirrorTerrain;
    }

}//end TunnelExitObject
