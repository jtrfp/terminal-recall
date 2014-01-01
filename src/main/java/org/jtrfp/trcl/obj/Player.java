package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.beh.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.AfterburnerBehavior;
import org.jtrfp.trcl.beh.AutoLeveling;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.BouncesOffTunnelWalls;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.HasPropulsion;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MovesByVelocity;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.RotationalDragBehavior;
import org.jtrfp.trcl.beh.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.UpdatesHealthMeterBehavior;
import org.jtrfp.trcl.beh.UpdatesThrottleMeterBehavior;
import org.jtrfp.trcl.beh.UpgradeableProjectileFiringBehavior;
import org.jtrfp.trcl.beh.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.VelocityDragBehavior;
import org.jtrfp.trcl.beh.WeaponSelectionBehavior;
import org.jtrfp.trcl.core.Camera;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.Weapon;

public class Player extends WorldObject
	{
	private final Camera camera;
	private int cameraDistance=0;
	private static final int SINGLE_SKL=0;
	public static final int CLOAK_COUNTDOWN_START=ThreadManager.GAMEPLAY_FPS*30;//30sec
	public static final int INVINCIBILITY_COUNTDOWN_START=ThreadManager.GAMEPLAY_FPS*30;//30sec
	//private final ProjectileFiringBehavior pacFiringBehavior;
	//private final ProjectileFiringBehavior rtlFiringBehavior;
	private final ProjectileFiringBehavior [] weapons = new ProjectileFiringBehavior[Weapon.values().length];

	public Player(TR tr,Model model)
		{super(tr,model);
		addBehavior(new PlayerBehavior());
		addBehavior(new DamageableBehavior());
		addBehavior(new MovesByVelocity());
		addBehavior(new HasPropulsion());
		addBehavior(new AccelleratedByPropulsion());
		addBehavior(new BouncesOffTunnelWalls(true,true));
		addBehavior(new UserInputThrottleControlBehavior());
		addBehavior(new VelocityDragBehavior());
		addBehavior(new AutoLeveling());
		addBehavior(new UserInputRudderElevatorControlBehavior());
		addBehavior(new RotationalMomentumBehavior());
		addBehavior(new RotationalDragBehavior());
		addBehavior(new CollidesWithTerrain());
		addBehavior(new AfterburnerBehavior());
		addBehavior(new LoopingPositionBehavior());
		addBehavior(new UpdatesThrottleMeterBehavior().setController(tr.getThrottleMeter()));
		addBehavior(new UpdatesHealthMeterBehavior().setController(tr.getHealthMeter()));
		addBehavior(new BouncesOffSurfaces());
		final Weapon [] allWeapons = Weapon.values();
		final ProjectileFactory [] projectileFactories=tr.getResourceManager().getProjectileFactories();
		for(int i=0; i<allWeapons.length; i++){
		    final Weapon w=allWeapons[i];
		    if(w.getButtonToSelect()!=-1){
			final ProjectileFiringBehavior pfb;
			if(w.isLaser()){//LASER
			    pfb = new UpgradeableProjectileFiringBehavior().
				setProjectileFactory(tr.getResourceManager().getProjectileFactories()[w.ordinal()]);
			    ((UpgradeableProjectileFiringBehavior)pfb).setMaxCapabilityLevel(2).setFiringMultiplexMap(
				    new Vector3D[][]{new Vector3D[]{new Vector3D(5000,-3000,0),new Vector3D(-5000,-3000,0)},//Level 0, single
					    new Vector3D[]{new Vector3D(5000,-3000,0),new Vector3D(-5000,-3000,0)},//Level 1, double
					    new Vector3D[]{new Vector3D(5000,-3000,0),new Vector3D(-5000,-3000,0),//Level 2 quad
					    new Vector3D(5000,3000,0),new Vector3D(-5000,3000,0)}});// Level 2 cont'd
			}//end if(isLaser)
			else{//NOT LASER
			    pfb = new ProjectileFiringBehavior()
				.setFiringPositions(new Vector3D[]{new Vector3D(5000,-3000,0),new Vector3D(-5000,-3000,0)}).
				setProjectileFactory(tr.getResourceManager().getProjectileFactories()[w.ordinal()]);
			}
			addBehavior(pfb);
			weapons[w.getButtonToSelect()-1]=pfb;
		    }//end if(hasButton)
		}
		addBehavior(new WeaponSelectionBehavior().setBehaviors(weapons));
		camera = tr.getRenderer().getCamera();
		getBehavior().probeForBehavior(VelocityDragBehavior.class).setDragCoefficient(.86);
		getBehavior().probeForBehavior(Propelled.class).setMinPropulsion(0);
		getBehavior().probeForBehavior(Propelled.class).setMaxPropulsion(900000);
		getBehavior().probeForBehavior(RotationalDragBehavior.class).setDragCoefficient(.86);
		
		}
	
	private class PlayerBehavior extends Behavior{
		@Override
		public void _tick(long tickTimeInMillis){
			updateCountdowns();
		}//end _Tick
	}//end PlayerBehavior
	
	public void updateCountdowns(){//TODO: BUG - setting visibility to false causes Player to no longer receive ticks!
		/*if(cloakCountdown>0){
			if(--cloakCountdown==0)
				{setVisible(true);}
			else setVisible(false);}
		if(invincibilityCountdown>0){
			--invincibilityCountdown;}*/
		}

	@Override
	public void setHeading(Vector3D lookAt){
		camera.setLookAtVector(lookAt);
		camera.setPosition(getPosition().subtract(lookAt.scalarMultiply(cameraDistance)));
		super.setHeading(lookAt);
		}
	@Override
	public void setTop(Vector3D top){
		camera.setUpVector(top);
		super.setTop(top);
		}
	@Override
	public Player setPosition(Vector3D pos){
		camera.setPosition(pos.subtract(getLookAt().scalarMultiply(cameraDistance)));
		super.setPosition(pos);
		return this;
		}

	/**
	 * @return the weapons
	 */
	public ProjectileFiringBehavior[] getWeapons() {
	    return weapons;
	}
	}//end Player
