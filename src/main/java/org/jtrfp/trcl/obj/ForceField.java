/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.obj;

import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.CollidesWithPlayer;
import org.jtrfp.trcl.beh.DamageListener;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.PlayerCollisionListener;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class ForceField extends WorldObject {
    private final CollidesWithPlayer cwp;
    public ForceField(int tunnelDia, int wallThickness) throws IOException, FileLoadException, IllegalAccessException {
	super();
	final TR tr = getTr();
	final Texture eTex = new AnimatedTexture(new Sequencer(100, 4, false).setTimeOffset((long)(Math.random()*500)), 
		    new VQTexture[]{
		    (VQTexture)tr.getResourceManager().getRAWAsTexture("ELECTRI0.RAW", tr.getDarkIsClearPaletteVL(),null, false, true),
		    (VQTexture)tr.getResourceManager().getRAWAsTexture("ELECTRI1.RAW", tr.getDarkIsClearPaletteVL(),null, false, true),
		    (VQTexture)tr.getResourceManager().getRAWAsTexture("ELECTRI2.RAW", tr.getDarkIsClearPaletteVL(),null, false, true),
		    (VQTexture)tr.getResourceManager().getRAWAsTexture("ELECTRI3.RAW", tr.getDarkIsClearPaletteVL(),null, false, true)}
		    );
	    setModel(GL33Model.buildCube(tunnelDia, tunnelDia, wallThickness,
		    eTex,
		    new double[] { tunnelDia / 4., tunnelDia / 4., 0 }, 0, 0,
		    1, 1, true, false, tr));
	    addBehavior(new ForceFieldBehavior());
	    addBehavior(cwp = new CollidesWithPlayer());
    }//end constructor
    
    private final class ForceFieldBehavior extends Behavior implements PlayerCollisionListener{
	private static final long FLASH_INTERVAL  = 2000L;
	private static final long FLASH_ON_THRESH = FLASH_INTERVAL/2L;
	private final long timeOffset = (long)(Math.random()*5000);
	private final SoundTexture st;
	
	public ForceFieldBehavior(){
	    super();
	    st = getTr().getResourceManager().soundTextures.get("ELECTRIC.WAV");
	}
	@Override
	public void tick(long tickTimeMillis){
	    tickTimeMillis+=timeOffset;
	    tickTimeMillis%=FLASH_INTERVAL;
	    if(isVisible() && tickTimeMillis<FLASH_ON_THRESH){
		setVisible(false);
		cwp.setEnable(false);
	    }else if(!isVisible() && tickTimeMillis>=FLASH_ON_THRESH){
		setVisible(true);
		cwp.setEnable(true);
		final SoundSystem ss = Features.get(getTr(),SoundSystemFeature.class);
		ss.enqueuePlaybackEvent(
		 ss.
		 getPlaybackFactory().
		 create(st, 
			ForceField.this, 
			getTr().
			 mainRenderer.
			 getCamera(), 
		SoundSystem.DEFAULT_SFX_VOLUME));
	    }//end turned on
	}//end _tick()
	@Override
	public void collidedWithPlayer(Player player) {
	    final DamageListener.ElectrocutionDamage dmg = 
			new DamageListener.ElectrocutionDamage();
		dmg.setDamageAmount(2048);
		player.probeForBehavior(DamageableBehavior.class).proposeDamage(dmg);
	}
    }//end ForceFieldBehavior
}//end ForceField
