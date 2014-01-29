package org.jtrfp.trcl.beh;

public class CustomNAVTargetableBehavior extends Behavior implements NAVTargetableBehavior {
    private final Runnable r;
    public CustomNAVTargetableBehavior(Runnable r){
	this.r=r;
    }
    @Override
    public void notifyBecomingCurrentTarget() {
	r.run();
    }
}//end CustomNAVTargetableBehavior
