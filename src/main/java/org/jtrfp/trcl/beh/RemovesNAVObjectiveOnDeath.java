package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.flow.NAVObjective;

public class RemovesNAVObjectiveOnDeath extends Behavior implements
	DeathListener {
    private final NAVObjective objective;
    private final NAVSystem ns;
    public RemovesNAVObjectiveOnDeath(NAVObjective objective, NAVSystem ns) {
	this.objective=objective;
	this.ns=ns;
    }

    @Override
    public void notifyDeath() {
	ns.removeNAVObjective(objective);
    }

}//ebd RemovesNAVObjectiveOnDeath
