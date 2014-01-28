package org.jtrfp.trcl.flow;

import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DeathListener;

public class CustomDeathBehavior extends Behavior implements DeathListener {
    private final Runnable r;
    public CustomDeathBehavior(final Runnable r){
	this.r=r;
    }
    @Override
    public void notifyDeath() {
	r.run();
    }

}
