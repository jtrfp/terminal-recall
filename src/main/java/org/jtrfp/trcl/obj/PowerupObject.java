package org.jtrfp.trcl.obj;

import java.awt.Dimension;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.file.PUPFile.PowerupLocation;
import org.jtrfp.trcl.file.Powerup;

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
		
		public void applyToPlayer(Player p)
			{switch(powerupType)
				{case RTL:
					p.setRtlQuantity(p.getRtlQuantity()+100);
					break;
				case PAC:
					p.setPacQuantity(p.getPacQuantity()+100);
					break;
				case ION:
					p.setIonQuantity(p.getIonQuantity()+100);
					break;
				case MAM:
					p.setMamQuantity(p.getMamQuantity()+40);
					break;
				case SAD:
					p.setSadQuantity(p.getSadQuantity()+20);
					break;
				case SWT:
					p.setSwtQuantity(p.getSwtQuantity()+20);
					break;
				case shieldRestore:
					p.getBehavior().probeForBehavior(DamageableBehavior.class).unDamage();
					break;
				case invisibility:
					p.setCloakCountdown(Player.CLOAK_COUNTDOWN_START);
					break;
				case invincibility:
					p.setInvincibilityCountdown(Player.INVINCIBILITY_COUNTDOWN_START);
					break;
				case DAM:
					p.setDamQuantity(1);
					break;
				case Afterburner:
					p.setAfterburnerQuantity(p.getAfterburnerQuantity()+20);
					break;
				case PowerCore:
					p.getBehavior().probeForBehavior(DamageableBehavior.class).unDamage(6554);
					break;
				case Random:
				    	applyToPlayer(p);
					break;
				}//end switch(powerupType)
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
