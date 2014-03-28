package org.jtrfp.trcl.obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.core.TR;

public class CollisionManager {
    public static final double MAX_CONSIDERATION_DISTANCE = TR.mapSquareSize * 15;
    private final TR tr;
    private final ArrayList<WorldObject>[] collisionList = new ArrayList[2];//Collideable objects are O(.5n^2-.5n)  !!!
    public static final int SHIP_COLLISION_DISTANCE = 15000;
    private boolean flip = false;

    public CollisionManager(TR tr) {
	this.tr = tr;
	collisionList[0] = new ArrayList<WorldObject>();
	collisionList[1] = new ArrayList<WorldObject>();
    }

    public synchronized void updateCollisionList() {
	final List<WorldObject> collideable = getWriteCollisionList();
	collideable.clear();
	tr.getWorld().itemsWithinRadiusOf(
		tr.getRenderer().getCamera().getCameraPosition(),
		new Submitter<PositionedRenderable>() {
		    @Override
		    public void submit(PositionedRenderable item) {
			if (item instanceof WorldObject) {
			    final WorldObject wo = (WorldObject)item;
			    if(wo.isCollideable())collideable.add(wo);
			}
		    }

		    @Override
		    public void submit(Collection<PositionedRenderable> items) {
			for (PositionedRenderable pr : items
				.toArray(new PositionedRenderable[] {})) {
			    submit(pr);
			}
		    }
		});
	flip = !flip;
    }// end updateVisibilityList()

    public synchronized void performCollisionTests() {
	List<WorldObject> collideable = getCurrentlyActiveCollisionList();
	for (int i = 0; i < collideable.size(); i++) {
	    final WorldObject left = collideable.get(i);
	    for (int j = i + 1; j < collideable.size(); j++) {
		final WorldObject right = collideable.get(j);
		if (left.isActive()&& right.isActive()){
			if(TR.sloppyTwosComplimentTaxicabDistanceXZ(left.getPosition(),
				right.getPosition()) < MAX_CONSIDERATION_DISTANCE) {
		    left.proposeCollision(right);
		    right.proposeCollision(left);
		    }//end if(distance<MAX_CONSIDERATION)
		}
	    }// end for(j)
	}// end for(i)
    }

    public void remove(WorldObject worldObject) {
	getCurrentlyActiveCollisionList().remove(worldObject);
    }

    public List<WorldObject> getCurrentlyActiveCollisionList() {
	return collisionList[flip ? 1 : 0];
    }

    private List<WorldObject> getWriteCollisionList() {
	return collisionList[flip ? 0 : 1];
    }
}// end CollisionManager
