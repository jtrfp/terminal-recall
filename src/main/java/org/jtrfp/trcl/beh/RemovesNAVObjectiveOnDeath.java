package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.NAVSystem;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.flow.NAVObjective;

public class RemovesNAVObjectiveOnDeath extends Behavior implements
	DeathListener {
    private final NAVObjective objective;
    private final Mission m;
    public RemovesNAVObjectiveOnDeath(NAVObjective objective, Mission m) {
	this.objective=objective;
	this.m=m;
    }

    @Override
    public void notifyDeath() {
	m.removeNAVObjective(objective);
    }

}//ebd RemovesNAVObjectiveOnDeath
