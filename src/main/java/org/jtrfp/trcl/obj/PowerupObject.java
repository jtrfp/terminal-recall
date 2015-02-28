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
package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Arrays;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.AfterburnerBehavior;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.Cloakable;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.beh.TunnelRailed;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class PowerupObject extends BillboardSprite{
	private final Powerup powerupType;
	private final SoundTexture powerupSound;
	public PowerupObject(Powerup pt, World world){
		super(world.getTr());
		setBillboardSize(new Dimension(20000,20000));
		addBehavior(new PowerupBehavior());
		addBehavior(new TunnelRailed(getTr()));
		TextureDescription desc=getTr().gpu.get().textureManager.get().getFallbackTexture();
		if(pt==Powerup.Random)
		    pt=Powerup.values()[(int)Math.random()*(Powerup.values().length-1)];
		powerupType=pt;
		String [] bbFrames;
		if(getTr().config.getGameVersion()!=GameVersion.TV)
		    bbFrames = pt.getF3BillboardFrames();
		else
		    bbFrames = pt.getTvBillboardFrames();
		Sequencer s = new Sequencer(Powerup.TIME_PER_FRAME_MILLIS,bbFrames.length,false);
		try {
		    Texture [] t;
			t = new Texture[bbFrames.length];
		    
			for(int i=0; i<t.length;i++){
			    t[i]=frame(bbFrames[i]);
			}
			desc=new AnimatedTexture(s,t);
			//Do something with desc
			setTexture(desc,true);}//end try{}
		catch(Exception e)
			{e.printStackTrace();}
		powerupSound=world.getTr().getResourceManager().soundTextures.get("POWER-1.WAV");
		}//end constructor

	private class PowerupBehavior extends Behavior implements CollisionBehavior{
		@Override
		public void proposeCollision(WorldObject other){
			if(TR.twosComplimentDistance(other.getPosition(), getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE)
				{if(other instanceof Player){
				    	Player p=(Player)other;
					try{applyToPlayer(p);
					destroy();}
					catch(SupplyNotNeededException e){}
					}//end if(Player)
				}//end if(close enough)
			}//end proposeCollision()
		
		public void applyToPlayer(Player p) throws SupplyNotNeededException{
			if(powerupType.getAfterburnerDelta()!=0){
			    AfterburnerBehavior ab = p.getBehavior().probeForBehavior(AfterburnerBehavior.class);
			    ab.addSupply(powerupType.getAfterburnerDelta());
			}
			if(powerupType.getInvincibilityTimeDeltaMillis()!=0){
			    DamageableBehavior db = p.getBehavior().probeForBehavior(DamageableBehavior.class);
			    db.addInvincibility(powerupType.getInvincibilityTimeDeltaMillis());
			}
			if(powerupType.getInvisibiltyTimeDeltaMillis()!=0){
			    p.getBehavior().probeForBehavior(Cloakable.class).addSupply(powerupType.getInvisibiltyTimeDeltaMillis());
			}
			if(powerupType.getShieldDelta()!=0){
			    DamageableBehavior db = p.getBehavior().probeForBehavior(DamageableBehavior.class);
			    db.unDamage(powerupType.getShieldDelta());
			}
			//wEAPON DELTAS
			final Weapon pWeapon=powerupType.getWeapon();
			if(pWeapon!=null){
			    if(pWeapon.getButtonToSelect()!=-1){
			    p.getWeapons()[pWeapon.getButtonToSelect()-1].
			    addSupply(powerupType.
				    getWeaponSupplyDelta());}}
			final TR tr = getParent().getTr();
			tr.getGame().getUpfrontDisplay().submitMomentaryUpfrontMessage(
				tr.config.getGameVersion()!=GameVersion.TV?
					powerupType.getF3Description():
					powerupType.getTvDescription());
			//SOUND FX
			tr.soundSystem.get()
			 .enqueuePlaybackEvent(tr
				 .soundSystem.get().getPlaybackFactory()
				 .create(powerupSound, new double []{.5*SoundSystem.DEFAULT_SFX_VOLUME*3,.5*SoundSystem.DEFAULT_SFX_VOLUME*3}));
		}//end applyToPlayer()
	}//end PowerupBehavior

    private Texture frame(String name) throws IllegalAccessException,
	    IOException, FileLoadException {
	return (Texture) getTr().getResourceManager().getRAWAsTexture(name,
		getTr().getGlobalPaletteVL(),null, false);
    }

	public Powerup getPowerupType()
		{return powerupType;}
	
	public void reset(double[] ds){
		setPosition(Arrays.copyOf(ds,3));
		setActive(true);
		setVisible(true);
	    }//end reset()
	}//end PowerupObject
