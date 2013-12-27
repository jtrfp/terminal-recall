package org.jtrfp.trcl.beh;

import java.util.Collection;

import org.jtrfp.trcl.Submitter;

public class DamageableBehavior extends Behavior{
	private int health=65535;

	public void damage(int dmg){
		health-=dmg;
		if(health<=0){
		    getParent().getBehavior().probeForBehaviors(deathSub, DeathListener.class);
		}//end if(dead)
	    }

	public int getHealth(){
		return health;
		}

	public void unDamage(int amt){
		health+=amt;
		}

	public void unDamage(){
		health=65535;
		}
	
	private final Submitter<DeathListener> deathSub = new Submitter<DeathListener>(){

	    @Override
	    public void submit(DeathListener item) {
		item.notifyDeath();
	    }

	    @Override
	    public void submit(Collection<DeathListener> items) {
		for(DeathListener l:items){submit(l);}
	    }
	};
	
    }//end DamageableBehavior
