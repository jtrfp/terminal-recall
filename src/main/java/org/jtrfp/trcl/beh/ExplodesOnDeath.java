package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class ExplodesOnDeath extends Behavior implements DeathListener {
private ExplosionType type;
    public ExplodesOnDeath(ExplosionType type){
	this.type=type;
    }
    @Override
    public synchronized void notifyDeath() {
	    final WorldObject p = getParent();
	    p.getTr().getResourceManager().getExplosionFactory().triggerExplosion(p.getPosition(),type);
    }
    @Override
    public void _tick(long tickTimeMillis){
	
    }//end _tick()
    public ExplodesOnDeath setExplosionType(ExplosionType type) {
	this.type=type;
	return this;
    }//end setExplosionType
}//end ExplodesOnDeath
