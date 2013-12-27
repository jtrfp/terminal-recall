package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ExplodesOnDeath extends Behavior implements DeathListener {
private boolean dead=false;
private final ExplosionType type;
private int explosionTally=0;
    public ExplodesOnDeath(ExplosionType type){
	this.type=type;
    }
    @Override
    public synchronized void notifyDeath() {
	if(!dead){
	    dead=true;
	    final WorldObject p = getParent();
	    p.getTr().getResourceManager().getExplosionFactory().triggerExplosion(p.getPosition(),type);
	    
	    }
    }
    @Override
    public void _tick(long tickTimeMillis){
	
    }//end _tick()
}//end ExplodesOnDeath
