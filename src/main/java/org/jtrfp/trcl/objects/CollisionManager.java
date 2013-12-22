package org.jtrfp.trcl.objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;

public class CollisionManager
	{
	private final TR tr;
	private final ArrayList<WorldObject> [] visibilityList = new ArrayList[2];
	public static final int SHIP_COLLISION_DISTANCE = 15000;
	private boolean flip=false;
	public CollisionManager(TR tr)
		{this.tr=tr;
		visibilityList[0]=new ArrayList<WorldObject>();
		visibilityList[1]=new ArrayList<WorldObject>();}
	public synchronized void updateVisibilityList()
		{final List<WorldObject> list =getWriteVisibilityList();list.clear();
		tr.getWorld().itemsWithinRadiusOf(tr.getRenderer().getCamera().getCameraPosition(), new Submitter<PositionedRenderable>()
			{@Override
			public void submit(PositionedRenderable item)
				{if(item instanceof WorldObject)list.add((WorldObject)item);}
			@Override
			public void submit(Collection<PositionedRenderable> items)
				{for(PositionedRenderable pr:items.toArray(new PositionedRenderable[]{})){submit(pr);}}
			});
		flip=!flip;
		}//end updateVisibilityList()
	public synchronized void performCollisionTests()
		{List<WorldObject>list=getVisibilityList();
		for(int i=0; i<list.size(); i++)
			{final WorldObject left=list.get(i);
			for(int j=i; j<list.size(); j++)
				{final WorldObject right=list.get(j);
				left.proposeCollision(right);
				right.proposeCollision(left);
				}//end for(j)
			}//end for(i)
		}
	public void remove(WorldObject worldObject)
		{getVisibilityList().remove(worldObject);}
	public List<WorldObject> getVisibilityList()
		{return visibilityList[flip?1:0];}
	private List<WorldObject> getWriteVisibilityList()
	{return visibilityList[flip?0:1];}
	}//end CollisionManager
