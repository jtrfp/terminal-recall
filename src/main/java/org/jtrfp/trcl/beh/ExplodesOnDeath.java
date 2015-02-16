/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundSystem;

public class ExplodesOnDeath extends Behavior implements DeathListener {
private ExplosionType type;
private String explosionSound;

    public ExplodesOnDeath(ExplosionType type){
	this(type,null);
    }
    public ExplodesOnDeath(ExplosionType type, String explosionSound){
	this.explosionSound=explosionSound;
	this.type=type;
    }
    @Override
    public synchronized void notifyDeath() {
	    final WorldObject p = getParent();
	    final TR tr = p.getTr();
	    final Vector3D loc = p.probeForBehavior(DeathBehavior.class).getLocationOfLastDeath();
	    tr.
	     getResourceManager().
	     getExplosionFactory().
	     triggerExplosion(loc,type);
	    String explosionSound = this.explosionSound;
	    if(explosionSound!=null)
	     tr.soundSystem.get().
	      enqueuePlaybackEvent(tr.soundSystem.get().getPlaybackFactory().
		    create(tr.getResourceManager().soundTextures.get(explosionSound),
			    loc.toArray(),
			    tr.mainRenderer.get().getCamera(),
			    SoundSystem.DEFAULT_SFX_VOLUME*2));
    }
    @Override
    public void _tick(long tickTimeMillis){
	
    }//end _tick()
    public ExplodesOnDeath setExplosionType(ExplosionType type) {
	this.type=type;
	return this;
    }//end setExplosionType
    /**
     * @return the explosionSound
     */
    public String getExplosionSound() {
        return explosionSound;
    }
    /**
     * @param explosionSound the explosionSound to set
     */
    public ExplodesOnDeath setExplosionSound(String explosionSound) {
        this.explosionSound = explosionSound;
        return this;
    }
}//end ExplodesOnDeath
