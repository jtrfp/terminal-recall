package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.Explosion.ExplosionType;
import org.jtrfp.trcl.objects.WorldObject;

public class ExplodesOnDeath extends Behavior implements DeathListener {
private boolean dead=false;
private final ExplosionType type;
    public ExplodesOnDeath(ExplosionType type){
	this.type=type;
    }
    @Override
    public void notifyDeath() {
	dead=true;
    }
    @Override
    public void _tick(long tickTimeMillis){
	if(dead){
	    dead=false;
	    final WorldObject p = getParent();
	    p.getTr().getResourceManager().getExplosionFactory().triggerBigExplosion(p.getPosition());
	    }
    }//end _tick()
}//end ExplodesOnDeath
