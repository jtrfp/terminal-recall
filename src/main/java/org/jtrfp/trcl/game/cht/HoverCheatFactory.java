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
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class HoverCheatFactory extends AbstractCheatFactory {
    private static final String CHEAT_NAME = "Hover! (toggle)";

    protected HoverCheatFactory() {
	super(CHEAT_NAME);
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return HoverCheat.class;
    }
    
    public class HoverCheat extends AbstractCheatItem {
	private double defaultMinPropulsion = Double.NaN;
	
	public HoverCheat() {super();}

	@Override
	protected void invokeCheat() {
	    toggleCheat();
	}//end invokeCheat()
	
	public void toggleCheat() {
	    if(isCheatEnabled())
		disableCheat();
	    else
		enableCheat();
	}//end toggleCheat()
	
	public void enableCheat() {
	    final TVF3Game game = this.getTarget();
	    final Propelled prop = game.getPlayer().probeForBehavior(Propelled.class);
	    if(!isCheatEnabled())
		defaultMinPropulsion = prop.getMinPropulsion();
	    prop.setMinPropulsion(0);
	    conveyStateToUser();
	}//end enableCheat()
	
	public void disableCheat() {
	    final TVF3Game game = this.getTarget();
	    final Propelled prop = game.getPlayer().probeForBehavior(Propelled.class);
	    prop.setMinPropulsion(defaultMinPropulsion);
	    conveyStateToUser();
	}//end disableCheat()
	
	public boolean isCheatEnabled() {
	    final TVF3Game game = this.getTarget();
	    final Propelled prop = game.getPlayer().probeForBehavior(Propelled.class);
	    return prop.getMinPropulsion() == 0;
	}//end isCheatEnabled()
	
	private void conveyStateToUser() {
	    final TR tr = getTarget().getTr();
	    final SoundSystemFeature ssf = Features.get(tr, SoundSystemFeature.class);
	    ssf.enqueuePlaybackEvent(ssf.getPlaybackFactory().create(tr.getResourceManager().soundTextures.get("POWER-1.WAV"), SoundSystem.DEFAULT_SFX_VOLUME_STEREO));
	    final UpfrontDisplay disp = getTarget().getUpfrontDisplay();
	    disp.submitMomentaryUpfrontMessage(isCheatEnabled()?"Hover!":"No hover");
	}//end conveyStateToUser()
	
    }//end HoverCheat

}//end HoverCheatFactory
