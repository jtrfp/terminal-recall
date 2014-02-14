package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class CustomPlayerWithinRangeBehavior extends Behavior {
    private double range = TR.mapSquareSize*10;
    public abstract void withinRange();
    
    @Override
    public void _tick(long timeInMillis){
	    final WorldObject thisObject=getParent();
	    final WorldObject other=thisObject.getTr().getPlayer();
	    if(Vect3D.distance(thisObject.getPosition(), other.getPosition())<=range){
		withinRange();
	    }//end if(close)
    }//end _proposeCollision

    /**
     * @return the range
     */
    public double getRange() {
        return range;
    }

    /**
     * @param range the range to set
     */
    public void setRange(double range) {
        this.range = range;
    }
}//end CustomPlayerWithinRangeBehavior
