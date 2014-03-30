package org.jtrfp.trcl.beh;


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
