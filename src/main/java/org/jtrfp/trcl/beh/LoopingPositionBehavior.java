package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class LoopingPositionBehavior extends Behavior {
    @Override
    public void _tick(long timeInMillis){
	// Loop correction
	Vector3D newPos = getParent().getPosition();
	final Vector3D oldPos=newPos;
	if (WorldObject.LOOP){
		if (newPos.getX() > TR.mapWidth)
			newPos = newPos.subtract(new Vector3D(TR.mapWidth, 0, 0));
		if (newPos.getY() > TR.mapWidth)
			newPos = newPos.subtract(new Vector3D(0, TR.mapWidth, 0));
		if (newPos.getZ() > TR.mapWidth)
			newPos = newPos.subtract(new Vector3D(0, 0, TR.mapWidth));
		
		if (newPos.getX() < 0)
			newPos = newPos.add(new Vector3D(TR.mapWidth, 0, 0));
		if (newPos.getZ() < 0)
			newPos = newPos.add(new Vector3D(0, 0, TR.mapWidth));
	if(newPos!=oldPos)getParent().setPosition(newPos);
	}//end if(LOOP)
    }//end _tick(...)
}//end LoopingPositionBehavior
