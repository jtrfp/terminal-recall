/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.util.Collection;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.snd.SoundSystem;

public class SFXOnDamage extends Behavior implements DamageListener {

    @Override
    public void damageEvent(Event ev) {
	Collection<String> suggestedSoundEffects = ev.getSuggestedSFX();
	for(String suggestedSoundEffect:suggestedSoundEffects){
	    final TR tr = getParent().getTr();
	    final SoundSystem ss = Features.get(tr,SoundSystemFeature.class);
	    ss.enqueuePlaybackEvent(
		    ss.getPlaybackFactory().create(
			    tr.getResourceManager().soundTextures.get(suggestedSoundEffect), 
			    new double [] {SoundSystem.DEFAULT_SFX_VOLUME, SoundSystem.DEFAULT_SFX_VOLUME})
		   );
	}//end if(!null)
    }//end damageEvent(...)
}//end SFXOnDamage
