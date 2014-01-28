package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Submitter;

public class ChangesBehaviorWhenTargeted extends Behavior implements
	NAVTargetableBehavior {
    private final boolean enabled;
    private final Class<? extends Behavior>[]behaviorsToChange;
    
    public ChangesBehaviorWhenTargeted(final boolean enabled, final Class<? extends Behavior> ... behaviorsToChange){
	super();
	this.enabled=enabled;
	this.behaviorsToChange=behaviorsToChange;
    }
    
    @Override
    public void notifyBecomingCurrentTarget() {
	for(Class<? extends Behavior> c:behaviorsToChange){
	    getParent().getBehavior().probeForBehaviors(bcSubmitter, (Class<Behavior>)c);
	}//end for(behaviors)
    }//end notifyBecomingCurrentTarget()
    
    private final Submitter<Behavior> bcSubmitter = new AbstractSubmitter<Behavior>(){
	@Override
	public void submit(Behavior item) {
	    item.setEnable(enabled);
	}
    };//end bcSubmitter
}//end ChangesBehaviorWhenTargeted
