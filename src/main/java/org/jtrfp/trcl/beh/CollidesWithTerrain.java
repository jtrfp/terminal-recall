package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.TerrainChunk;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTerrain extends Behavior {
    private static final double nudge=1;
    private boolean bounce=false;
    private boolean groundLock=false;
    private InterpolatingAltitudeMap map;
    private Vector3D surfaceNormalVar;
    public CollidesWithTerrain(){
    }
    @Override
    public void _tick(long tickTimeMillis){
	if(map==null)return;//null map means no terrain present, which means no need to check
	final WorldObject p = getParent();
	final Vector3D thisPos=p.getPosition();
	final double height = map.heightAt((thisPos.getX()/TR.mapSquareSize), 
	    (thisPos.getZ()/TR.mapSquareSize))*(p.getTr().getWorld().sizeY/2);
	final Vector3D groundNormal = (map.normalAt((thisPos.getX()/TR.mapSquareSize), 
	    (thisPos.getZ()/TR.mapSquareSize)));
	
    	if(groundLock){p.setPosition(new Vector3D(thisPos.getX(),height,thisPos.getZ()));return;}
    	
	if(thisPos.getY()<height)
	    {p.setPosition(new Vector3D(thisPos.getX(),height+nudge,thisPos.getZ()));
	    //Call impact listeners
	    surfaceNormalVar=groundNormal;
	    getParent().getBehavior().probeForBehaviors(sub,SurfaceImpactListener.class);
	    
	    //Reflect heading,top
	    if(bounce){
	    	final Vector3D oldHeading = p.getHeading();
	    	final Vector3D oldTop = p.getTop();
	    	final Vector3D newHeading = (groundNormal.scalarMultiply(groundNormal.dotProduct(oldHeading)*-2).add(oldHeading));
	    	final Velocible v=p.getBehavior().probeForBehavior(Velocible.class);
	    	final RotationalMomentumBehavior rmb = p.getBehavior().probeForBehavior(RotationalMomentumBehavior.class);
	    	if(rmb!=null){//If this is a spinning object, reverse its spin momentum
	    	    rmb.setLateralMomentum(rmb.getLateralMomentum()*-1);
	    	    rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*-1);
	    	    rmb.setPolarMomentum(rmb.getPolarMomentum()*-1);
	    	    }
	    	final Vector3D oldVelocity = v.getVelocity();
	    	v.setVelocity(groundNormal.scalarMultiply(groundNormal.dotProduct(oldVelocity)*-2).add(oldVelocity));
	    	p.setHeading(newHeading);
	    	final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
	    	Vector3D newTop = resultingRotation.applyTo(oldTop);
		p.setTop(newTop);
	    	}//end if(bounce)
	    	
	    }//end if()
    map=null;
    }//end _tick
    private final Submitter<SurfaceImpactListener>sub=new Submitter<SurfaceImpactListener>(){
	@Override
	public void submit(SurfaceImpactListener item) {
	    	item.collidedWithSurface(null,surfaceNormalVar);//TODO: Isolate which chunk and pass it
		}
	@Override
	public void submit(Collection<SurfaceImpactListener> items) {
	    	for(SurfaceImpactListener l:items){submit(l);}
		}
    };
    @Override
    public void _proposeCollision(WorldObject other){
	if(other instanceof TerrainChunk){
	    if(map==null)map = (InterpolatingAltitudeMap)((TerrainChunk)other).getAltitudeMap();}
    }//end _tick
}//end BouncesOffTerrain
