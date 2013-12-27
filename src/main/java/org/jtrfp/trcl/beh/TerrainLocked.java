package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.TerrainChunk;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class TerrainLocked extends Behavior {
    private double pad=0;
    private InterpolatingAltitudeMap map;
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject p = getParent();
	final Vector3D thisPos=p.getPosition();
	if(map==null)return;
	final double height = map.heightAt((thisPos.getX()/TR.mapSquareSize), 
		    (thisPos.getZ()/TR.mapSquareSize))*(p.getTr().getWorld().sizeY/2);
	p.setPosition(new Vector3D(thisPos.getX(),height+pad,thisPos.getZ()));
    }
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof TerrainChunk){
	    if(map==null)map = (InterpolatingAltitudeMap)((TerrainChunk)other).getAltitudeMap();}
    }//end _tick
}//end TerrainLocked
