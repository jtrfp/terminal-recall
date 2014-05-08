package org.jtrfp.trcl.obj;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.core.TR;

public abstract class WorldObject2DVisibleEverywhere extends WorldObject2D implements
	VisibleEverywhere {

    public WorldObject2DVisibleEverywhere(TR tr) {
	super(tr);
    }

    public WorldObject2DVisibleEverywhere(TR tr, Model model) {
	super(tr,model);
    }
    
}//end WorldOBject2DVisibleEverywhere
