package org.jtrfp.trcl.obj;

import java.util.ArrayList;

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;

public class PUPObjectPlacer implements ObjectPlacer {
    ArrayList<PowerupObject> objs = new ArrayList<PowerupObject>();

    public PUPObjectPlacer(PUPFile pupFile, World world) {
	for (PowerupLocation loc : pupFile.getPowerupLocations()) {
	    PowerupObject powerup = new PowerupObject(loc.getType(), world);
	    final double[] pupPos = powerup.getPosition();
	    pupPos[0] = TR.legacy2Modern(loc.getZ());
	    pupPos[1] = (TR.legacy2Modern(loc.getY()) / TR.mapWidth) * 16.
		    * world.sizeY;
	    pupPos[2] = TR.legacy2Modern(loc.getX());
	    powerup.notifyPositionChange();
	    objs.add(powerup);
	}// end for(locations)
    }// end PUPObjectPlacer

    @Override
    public void placeObjects(RenderableSpacePartitioningGrid target) {
	for (PowerupObject obj : objs) {
	    target.add(obj);
	}//end for(objs)
    }//end placeObjects()

}// end PUPObjectPlacer
