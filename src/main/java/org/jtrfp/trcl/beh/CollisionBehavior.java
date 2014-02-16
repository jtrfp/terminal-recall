package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;

public interface CollisionBehavior {
    public void proposeCollision(WorldObject other);
}
