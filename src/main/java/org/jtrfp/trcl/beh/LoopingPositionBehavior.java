package org.jtrfp.trcl.beh;

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.WorldObject;

public class LoopingPositionBehavior extends Behavior {
    @Override
    public void _tick(long timeInMillis){
	// Loop correction
	double [] oldPos = getParent().getPosition();
	boolean _transient=false;
	if (WorldObject.LOOP){
		if (oldPos[0] > TR.mapWidth)
			//oldPos = oldPos.subtract(new Vector3D(TR.mapWidth, 0, 0));
			{oldPos[0]-=TR.mapWidth;_transient=true;}
		if (oldPos[1] > TR.mapWidth)
			//oldPos = oldPos.subtract(new Vector3D(0, TR.mapWidth, 0));
		    	{oldPos[1]-=TR.mapWidth;_transient=true;}
		if (oldPos[2] > TR.mapWidth)
			//oldPos = oldPos.subtract(new Vector3D(0, 0, TR.mapWidth));
		    	{oldPos[2]-=TR.mapWidth;_transient=true;}
		if (oldPos[0] < 0)
			//oldPos = oldPos.add(new Vector3D(TR.mapWidth, 0, 0));
			{oldPos[0]+=TR.mapWidth;_transient=true;}
		if (oldPos[2] < 0)
			//oldPos = oldPos.add(new Vector3D(0, 0, TR.mapWidth));
		    	{oldPos[2]+=TR.mapWidth;_transient=true;}
	if(_transient)getParent().notifyPositionChange();
	}//end if(LOOP)
    }//end _tick(...)
}//end LoopingPositionBehavior
