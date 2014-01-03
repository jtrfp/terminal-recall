package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.TerrainChunk;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.obj.WorldObject;

public class LeavesPowerupOnDeathBehavior extends Behavior implements
	DeathListener {
    private InterpolatingAltitudeMap map;
    private static final int OVER_TERRAIN_PAD=20000;
    private final Powerup pup;
    public LeavesPowerupOnDeathBehavior(Powerup p){
	this.pup=p;
    }
    @Override
    public void notifyDeath() {//Y-fudge to ensure powerup is not too close to ground.
	final WorldObject p=getParent();
	final Vector3D thisPos=p.getPosition();
	double height;
	if(map!=null)height= map.heightAt((thisPos.getX()/TR.mapSquareSize), 
		    (thisPos.getZ()/TR.mapSquareSize))*(p.getTr().getWorld().sizeY/2);
	else{height=Double.NEGATIVE_INFINITY;}
	final Vector3D yFudge=thisPos.getY()<height+13000?new Vector3D(0,13000,0):Vector3D.ZERO;
	getParent().getTr().getResourceManager().getPluralizedPowerupFactory().
		spawn(getParent().getPosition().add(yFudge), pup);
    }//end notifyDeath()
    
    //I'm not proud of this; it's a hack to get the heightmap.
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof TerrainChunk){
	    if(map==null)map = (InterpolatingAltitudeMap)((TerrainChunk)other).getAltitudeMap();}
    }//end _tick
}//end LeavesPowerupOnDeathBehavior
