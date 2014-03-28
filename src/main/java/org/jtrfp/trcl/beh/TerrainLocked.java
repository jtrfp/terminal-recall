package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class TerrainLocked extends Behavior {
    private double pad=0;
    @Override
    public void _tick(long tickTimeMillis){
	final WorldObject p = getParent();
	final double[] thisPos=p.getPosition();
	final double height = p.getTr().getAltitudeMap().heightAt((thisPos[0]/TR.mapSquareSize), 
		    (thisPos[2]/TR.mapSquareSize))*(p.getTr().getWorld().sizeY/2);
	final double [] pPos = p.getPosition();
	pPos[0]=thisPos[0];
	pPos[1]=height+pad;
	pPos[2]=thisPos[2];
	p.setPosition(pPos);
	p.notifyPositionChange();
    }
}//end TerrainLocked
