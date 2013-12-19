package org.jtrfp.trcl;

import java.util.ArrayList;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;
import org.jtrfp.trcl.objects.ObjectPlacer;

public class PUPObjectPlacer implements ObjectPlacer
	{
	ArrayList<PowerupObject>objs = new ArrayList<PowerupObject>();
	public PUPObjectPlacer(PUPFile pupFile, World world)
		{
		for(PowerupLocation loc:pupFile.getPowerupLocations())
			{PowerupObject powerup = new PowerupObject(loc,world);
			powerup.setPosition(new Vector3D(
					TR.legacy2Modern(loc.getZ()),
					(TR.legacy2Modern(loc.getY())/TR.mapWidth)*16.*world.sizeY,
					TR.legacy2Modern(loc.getX())
					));
			objs.add(powerup);
			}//end for(locations)
		}//end PUPObjectPlacer

	@Override
	public void placeObjects(RenderableSpacePartitioningGrid target)
		{for(PowerupObject obj:objs){target.add(obj);}}

	}//end PUPObjectPlacer
