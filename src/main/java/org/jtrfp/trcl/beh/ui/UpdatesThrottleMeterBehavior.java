package org.jtrfp.trcl.beh.ui;

import org.jtrfp.trcl.ManuallySetController;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Propelled;

public class UpdatesThrottleMeterBehavior extends Behavior {
    private ManuallySetController controller;
    @Override
    public void _tick(long tickTimeMillis){
	    Propelled prop = getParent().getBehavior().probeForBehavior(Propelled.class);
	    controller.setFrame(1.-((prop.getPropulsion()-prop.getMinPropulsion())/prop.getMaxPropulsion()));
    }
    /**
     * @return the controller
     */
    public ManuallySetController getController() {
        return controller;
    }
    /**
     * @param controller the controller to set
     */
    public UpdatesThrottleMeterBehavior setController(ManuallySetController controller) {
        this.controller = controller;
        return this;
    }
}
