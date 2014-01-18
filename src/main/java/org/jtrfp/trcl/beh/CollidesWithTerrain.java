package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.InterpolatingAltitudeMap;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.TerrainChunk;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class CollidesWithTerrain extends Behavior {
    private static final double nudge=1;
    private boolean bounce=false;
    private boolean groundLock=false;
    private InterpolatingAltitudeMap map;
    private Vector3D surfaceNormalVar;
    public CollidesWithTerrain(){}
    @Override
    public void _tick(long tickTimeMillis){
	if(map==null)return;//null map means no terrain present, which means no need to check
	final WorldObject p = getParent();
	final TR tr = p.getTr();
	final World world = tr.getWorld();
	final double [] thisPos=p.getPosition();
	final double groundHeight = map.heightAt((thisPos[0]/TR.mapSquareSize), 
	    (thisPos[2]/TR.mapSquareSize))*(world.sizeY/2);
	final double ceilingHeight = (2.-map.heightAt((thisPos[0]/TR.mapSquareSize), 
		    (thisPos[2]/TR.mapSquareSize)))*(world.sizeY/2);
	final Vector3D groundNormal = (map.normalAt((thisPos[0]/TR.mapSquareSize), 
	    (thisPos[2]/TR.mapSquareSize)));
	final boolean terrainMirror=tr.getOverworldSystem().isChamberMode();
	final double thisY=thisPos[1];
    	final boolean groundImpact=thisY<groundHeight;
    	final boolean ceilingImpact=(thisY>ceilingHeight&&terrainMirror);
	final Vector3D ceilingNormal = new Vector3D(groundNormal.getX(),-groundNormal.getY(),groundNormal.getZ());
	final Vector3D surfaceNormal = groundImpact?groundNormal:ceilingNormal;
	
    	if(groundLock){
    	    thisPos[1]=groundHeight;p.notifyPositionChange();return;
    	    }
    	
	if( groundImpact || ceilingImpact){//detect collision
	    thisPos[1]=(groundImpact?groundHeight:ceilingHeight)+(groundImpact?nudge:-nudge);
	    p.notifyPositionChange();
	    //Call impact listeners
	    surfaceNormalVar=surfaceNormal;
	    final Behavior behavior = p.getBehavior();
	    behavior.probeForBehaviors(sub,SurfaceImpactListener.class);
	    
	    //Reflect heading,top
	    if(bounce){
	    	final Vector3D oldHeading = p.getHeading();
	    	final Vector3D oldTop = p.getTop();
	    	final Vector3D newHeading = (surfaceNormal.scalarMultiply(surfaceNormal.dotProduct(oldHeading)*-2).add(oldHeading));
	    	final Velocible v=behavior.probeForBehavior(Velocible.class);
	    	final RotationalMomentumBehavior rmb = behavior.probeForBehavior(RotationalMomentumBehavior.class);
	    	if(rmb!=null){//If this is a spinning object, reverse its spin momentum
	    	    rmb.setLateralMomentum(rmb.getLateralMomentum()*-1);
	    	    rmb.setEquatorialMomentum(rmb.getEquatorialMomentum()*-1);
	    	    rmb.setPolarMomentum(rmb.getPolarMomentum()*-1);
	    	    }
	    	final Vector3D oldVelocity = v.getVelocity();
	    	v.setVelocity(surfaceNormal.scalarMultiply(surfaceNormal.dotProduct(oldVelocity)*-2).add(oldVelocity));
	    	p.setHeading(newHeading);
	    	final Rotation resultingRotation = new Rotation(oldHeading,newHeading);
	    	Vector3D newTop = resultingRotation.applyTo(oldTop);
		p.setTop(newTop);
	    	}//end if(bounce)
	    }//end if(collision)
    map=null;
    }//end _tick
    private final Submitter<SurfaceImpactListener>sub=new Submitter<SurfaceImpactListener>(){
	@Override
	public void submit(SurfaceImpactListener item) {
	    	item.collidedWithSurface(null,surfaceNormalVar.toArray());//TODO: Isolate which chunk and pass it
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
