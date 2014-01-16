package org.jtrfp.trcl.beh.ui;

import org.jtrfp.trcl.ManuallySetController;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageableBehavior;

public class UpdatesHealthMeterBehavior extends Behavior implements GUIUpdateBehavior {
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
