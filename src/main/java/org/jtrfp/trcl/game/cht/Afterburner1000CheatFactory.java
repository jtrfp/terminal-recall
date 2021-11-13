/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
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
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class Afterburner1000CheatFactory extends AbstractCheatFactory {
    private static final String CHEAT_NAME = "Turbo (1000)";

    protected Afterburner1000CheatFactory() {
	super(CHEAT_NAME);
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return Afterburner1000Cheat.class;
    }

    public class Afterburner1000Cheat extends AbstractCheatItem {

	public Afterburner1000Cheat() {super();}

	@Override
	protected void invokeCheat() {
	    System.out.println(CHEAT_NAME+" cheat invoked.");
	    final UpfrontDisplay disp = getTarget().getUpfrontDisplay();
	    disp.submitMomentaryUpfrontMessage("Watusi!");
	    getTarget().getPlayer().probeForBehavior(AfterburnerBehavior.class).addSupply(1000);
	    //Ding
	    final TR tr = getTarget().getTr();
	    final SoundSystemFeature ssf = Features.get(tr, SoundSystemFeature.class);
	    ssf.enqueuePlaybackEvent(ssf.getPlaybackFactory().create(tr.getResourceManager().soundTextures.get("POWER-1.WAV"), SoundSystem.DEFAULT_SFX_VOLUME_STEREO));
	}//end invokeCheat

    }//end Afterburner1000Cheat

}//end Afterburner1000CheatFactory
