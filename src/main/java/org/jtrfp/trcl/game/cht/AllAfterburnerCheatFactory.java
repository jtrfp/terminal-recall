/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2020 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.game.cht;

import org.jtrfp.trcl.UpfrontDisplay;
import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class AllAfterburnerCheatFactory extends AbstractCheatFactory {
    private static final String CHEAT_NAME = "Infinite Afterburner (Toggle)";

    protected AllAfterburnerCheatFactory() {
	super(CHEAT_NAME);
    }

    @Override
    public Class<AllAfterburnerCheat> getFeatureClass() {
	return AllAfterburnerCheat.class;
    }
    
    public class AllAfterburnerCheat extends AbstractCheatItem {
	
	public AllAfterburnerCheat() {super();}

	@Override
	protected void invokeCheat() {
	    System.out.println(CHEAT_NAME+" cheat invoked.");
	    final UpfrontDisplay disp = getTarget().getUpfrontDisplay();
	    
	    final Player player = getTarget().getPlayer();
	    final AfterburnerBehavior ab = player.probeForBehavior(AfterburnerBehavior.class);
	    
	    //Ding
	    final TR tr = getTarget().getTr();
	    final SoundSystemFeature ssf = Features.get(tr, SoundSystemFeature.class);
	    ssf.enqueuePlaybackEvent(ssf.getPlaybackFactory().create(tr.getResourceManager().soundTextures.get("POWER-1.WAV"), SoundSystem.DEFAULT_SFX_VOLUME_STEREO));
	    
	    //Toggle
	    ab.setInfiniteFuel(!ab.isInfiniteFuel());
	    //Inform
	    disp.submitMomentaryUpfrontMessage("Afterburner "+(ab.isInfiniteFuel()?"Infinite":"Normal"));
	}//end invokeCheat()
	
    }//end AllAfterburnerCheat

}//end AllAfterburnerFactory
