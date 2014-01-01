package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.AfterburnerBehavior;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.file.Powerup;
import org.jtrfp.trcl.file.Weapon;

public class PowerupObject extends BillboardSprite{
	private final Powerup powerupType;
	public PowerupObject(Powerup pt, World world){
		super(world.getTr());
		setBillboardSize(new Dimension(20000,20000));
		addBehavior(new PowerupBehavior());
		TextureDescription desc=Texture.getFallbackTexture();
		if(pt==Powerup.Random){
		    pt=Powerup.values()[(int)Math.random()*(Powerup.values().length-1)];
		}
		powerupType=pt;
		String [] bbFrames = pt.getBillboardFrames();
		Sequencer s = new Sequencer(Powerup.TIME_PER_FRAME_MILLIS,bbFrames.length,false);
		try {
		    Texture [] t = new Texture[pt.getBillboardFrames().length];
			for(int i=0; i<t.length;i++){
			    t[i]=frame(bbFrames[i]);
			}
			desc=new AnimatedTexture(s,t);
			//Do something with desc
			setTexture(desc,true);}//end try{}
		catch(Exception e)
			{e.printStackTrace();}
		}//end constructor

	private class PowerupBehavior extends Behavior{
		@Override
		public void _proposeCollision(WorldObject other){
			if(other.getPosition().distance(getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE)
				{if(other instanceof Player){
				    	Player p=(Player)other;
					applyToPlayer(p);
					destroy();
					}//end if(Player)
				}//end if(close enough)
			}//end proposeCollision()
		
		public void applyToPlayer(Player p){
			if(powerupType.getAfterburnerDelta()!=0){
			    AfterburnerBehavior ab = p.getBehavior().probeForBehavior(AfterburnerBehavior.class);
			    ab.addSupply(powerupType.getAfterburnerDelta());
			}
			if(powerupType.getInvincibilityTimeDeltaMillis()!=0){
			    DamageableBehavior db = p.getBehavior().probeForBehavior(DamageableBehavior.class);
			    db.addInvincibility(powerupType.getInvincibilityTimeDeltaMillis());
			}
			if(powerupType.getInvisibiltyTimeDeltaMillis()!=0){
			    //TODO: Need to re-design invisible vs. inactive.
			}
			if(powerupType.getShieldDelta()!=0){
			    DamageableBehavior db = p.getBehavior().probeForBehavior(DamageableBehavior.class);
			    db.unDamage(powerupType.getAfterburnerDelta());
			}
			//wEAPON DELTAS
			final Weapon pWeapon=powerupType.getWeapon();
			if(pWeapon!=null){p.getWeapons()[pWeapon.ordinal()].addSupply(powerupType.getWeaponSupplyDelta());}
		}//end applyToPlayer()
	}//end PowerupBehavior
	
	private Texture frame(String name) throws IllegalAccessException, IOException, FileLoadException
		{return (Texture)getTr().getResourceManager().getRAWAsTexture(name, getTr().getGlobalPalette(), GammaCorrectingColorProcessor.singleton, getTr().getGPU().takeGL());}

	public Powerup getPowerupType()
		{return powerupType;}
	
	public void reset(Vector3D newPos){
		setPosition(newPos);
		setVisible(true);
	    }//end reset()
	}//end PowerupObject
