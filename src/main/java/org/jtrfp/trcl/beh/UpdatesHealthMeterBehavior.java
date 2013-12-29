package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.ManuallySetController;

public class UpdatesHealthMeterBehavior extends Behavior {
    private ManuallySetController controller;
    @Override
    public void _tick(long tickTimeMillis){
	    DamageableBehavior dmg = getParent().getBehavior().probeForBehavior(DamageableBehavior.class);
	    controller.setFrame(1.-((dmg.getHealth())/65535.));
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
    public UpdatesHealthMeterBehavior setController(ManuallySetController controller) {
        this.controller = controller;
        return this;
    }
}
