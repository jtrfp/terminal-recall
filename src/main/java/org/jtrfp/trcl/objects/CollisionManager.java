package org.jtrfp.trcl.objects;

import java.util.ArrayList;
import java.util.Collection;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;

public class CollisionManager
	{
	private final TR tr;
	private ArrayList<WorldObject> visibilityList = new ArrayList<WorldObject>();
	public static final int SHIP_COLLISION_DISTANCE = 50000;
	public CollisionManager(TR tr)
		{this.tr=tr;
		}
	public synchronized void updateVisibilityList()
		{
		visibilityList.clear();
		tr.getWorld().itemsWithinRadiusOf(tr.getRenderer().getCamera().getCameraPosition(), new Submitter<PositionedRenderable>()
			{
			@Override
			public void submit(PositionedRenderable item)
				{if(item instanceof WorldObject)visibilityList.add((WorldObject)item);}

			@Override
			public void submit(Collection<PositionedRenderable> items)
				{for(PositionedRenderable pr:items.toArray(new PositionedRenderable[]{})){submit(pr);}}
			});
		}//end updateVisibilityList()
	public synchronized void performCollisionTests()
		{
		for(int i=0; i<visibilityList.size(); i++)
			{
			final WorldObject left=visibilityList.get(i);
			for(int j=i; j<visibilityList.size(); j++)
				{final WorldObject right=visibilityList.get(j);
				left.proposeCollision(right);
				right.proposeCollision(left);
				}//end for(j)
			}//end for(i)
		}
	public void remove(WorldObject worldObject)
		{visibilityList.remove(worldObject);}
	}//end CollisionManager
