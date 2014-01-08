package org.jtrfp.trcl.obj;

import java.awt.Dimension;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.flow.NAVObjective;


public class Checkpoint extends BillboardSprite {
private NAVObjective objective;
private NAVSystem ns;
private boolean includeYAxisInCollision=true;
    public Checkpoint(TR tr) {
	super(tr);
	addBehavior(new CheckpointBehavior());
	setBillboardSize(new Dimension(80000,80000));
	setVisible(true);
	try{setTexture(
		    tr.getResourceManager().getRAWAsTexture("CHECK1.RAW", tr.getGlobalPalette(), 
			    GammaCorrectingColorProcessor.singleton, tr.getGPU().getGl())
		    ,true);
	}catch(Exception e){e.printStackTrace();}
    }//end constructor

    public void setObjectiveToRemove(NAVObjective objective, NAVSystem ns) {
	this.objective=objective;
	this.ns=ns;
    }//end setObjectiveToRemove(...)
    
    private class CheckpointBehavior extends Behavior{
	@Override
	public void _proposeCollision(WorldObject other){
	    if(other instanceof Player){
		final Player player = (Player)other;
		final WorldObject parent = getParent();
		Vector3D playerPos = includeYAxisInCollision?player.getPosition():new Vector3D(player.getPosition().getX(),0,player.getPosition().getZ());
		Vector3D parentPos = includeYAxisInCollision?parent.getPosition():new Vector3D(parent.getPosition().getX(),0,parent.getPosition().getZ());
		if(TR.twosComplimentDistance(playerPos,parentPos)<CollisionManager.SHIP_COLLISION_DISTANCE*4){
		    destroy();
		    ns.removeNAVObjective(objective);
		}//end if(collided)
	    }//end if(Player)
	}//end _proposeCollision()
    }//end CheckpointBehavior

    /**
     * @return the includeYAxisInCollision
     */
    public boolean isIncludeYAxisInCollision() {
        return includeYAxisInCollision;
    }

    /**
     * @param includeYAxisInCollision the includeYAxisInCollision to set
     */
    public Checkpoint setIncludeYAxisInCollision(boolean includeYAxisInCollision) {
        this.includeYAxisInCollision = includeYAxisInCollision;
        return this;
    }
}//end Checkpoint
