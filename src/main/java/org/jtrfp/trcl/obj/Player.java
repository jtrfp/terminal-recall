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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.AbstractSubmitter;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.Cloakable;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior.SupplyNotNeededException;
import org.jtrfp.trcl.beh.DamagedByCollisionWithDEFObject;
import org.jtrfp.trcl.beh.DamagedByCollisionWithSurface;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DeathListener;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.FacingObject;
import org.jtrfp.trcl.beh.HeadingXAlwaysPositiveBehavior;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.MatchDirection;
import org.jtrfp.trcl.beh.MatchPosition;
import org.jtrfp.trcl.beh.ProjectileFiringBehavior;
import org.jtrfp.trcl.beh.RollLevelingBehavior;
import org.jtrfp.trcl.beh.RollNudgeOnDamage;
import org.jtrfp.trcl.beh.RotateAroundObject;
import org.jtrfp.trcl.beh.SFXOnDamage;
import org.jtrfp.trcl.beh.SpinCrashDeathBehavior;
import org.jtrfp.trcl.beh.UpdatesNAVRadar;
import org.jtrfp.trcl.beh.UpgradeableProjectileFiringBehavior;
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.beh.phy.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.phy.HasPropulsion;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.beh.ui.AfterburnerBehavior;
import org.jtrfp.trcl.beh.ui.RollBehavior;
import org.jtrfp.trcl.beh.ui.UpdatesHealthMeterBehavior;
import org.jtrfp.trcl.beh.ui.UpdatesThrottleMeterBehavior;
import org.jtrfp.trcl.beh.ui.UserInputRudderElevatorControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputThrottleControlBehavior;
import org.jtrfp.trcl.beh.ui.UserInputWeaponSelectionBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.file.Weapon;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.miss.GamePauseFactory.GamePause;
import org.jtrfp.trcl.miss.Mission;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.pool.ObjectFactory;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.snd.SoundTexture;

public class Player extends WorldObject implements RelevantEverywhere{
    public static final int 	CLOAK_COUNTDOWN_START 		= ThreadManager.GAMEPLAY_FPS * 30;// 30sec
    public static final int 	INVINCIBILITY_COUNTDOWN_START 	= ThreadManager.GAMEPLAY_FPS * 30;// 30sec
    private final 		ProjectileFiringBehavior[] weapons = new ProjectileFiringBehavior[Weapon
                  		                                   .values().length];
    private final RunModeListener               runStateListener = new RunModeListener();
    private final WeakPropertyChangeListener    weakRunStateListener;
    private final HeadingXAlwaysPositiveBehavior headingXAlwaysPositiveBehavior;
    private GameShell gameShell;
    
    private static final double LEFT_X = -5000, RIGHT_X = 5000, TOP_Y = 2000, BOT_Y=-2000;

    public Player() {
	super();
	setVisible(false);
	DamageableBehavior db = new DamageableBehavior();
	addBehavior(db);
	String godMode = System.getProperty("org.jtrfp.trcl.godMode");
	if (godMode != null) {
	    if (godMode.toUpperCase().contains("TRUE")) {
		db.setEnable(false);
	    }
	}
	final TR tr = getTr();
	final ResourceManager rm = tr.getResourceManager();
	final ObjectFactory<String,SoundTexture> soundTextures = rm.soundTextures;
	
	addBehavior(new AccelleratedByPropulsion());
	addBehavior(new MovesByVelocity());
	addBehavior(new HasPropulsion());
	addBehavior(new CollidesWithTunnelWalls(true, true));
	addBehavior(new UserInputThrottleControlBehavior(tr.getControllerInputs()));
	addBehavior(new VelocityDragBehavior());
	addBehavior(new RollLevelingBehavior());
	addBehavior(new UserInputRudderElevatorControlBehavior(tr.getControllerInputs()));
	addBehavior(new RotationalMomentumBehavior());
	addBehavior(new RotationalDragBehavior());
	addBehavior(new CollidesWithTerrain().setTunnelEntryCapable(true).setIgnoreHeadingForImpact(false));
	addBehavior(new AfterburnerBehavior(tr.getControllerInputs()).
		setIgnitionSound  (soundTextures.get(AfterburnerBehavior.IGNITION_SOUND)).
		setExtinguishSound(soundTextures.get(AfterburnerBehavior.EXTINGUISH_SOUND)).
		setLoopSound      (soundTextures.get(AfterburnerBehavior.LOOP_SOUND)));
	addBehavior(new LoopingPositionBehavior());
	addBehavior(headingXAlwaysPositiveBehavior = (HeadingXAlwaysPositiveBehavior)new HeadingXAlwaysPositiveBehavior().setEnable(false));
	//Add a listener to control HeadingXAlwaysPositive\
	weakRunStateListener = new WeakPropertyChangeListener(runStateListener, tr);
	tr.addPropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener);
	addBehavior(new UpdatesThrottleMeterBehavior().setController(((TVF3Game)getGameShell().getGame()).getHUDSystem().getThrottleMeter()));
	addBehavior(new UpdatesHealthMeterBehavior().setController(((TVF3Game)getGameShell().getGame()).getHUDSystem().getHealthMeter()));
	addBehavior(new DamagedByCollisionWithDEFObject());
	addBehavior(new DamagedByCollisionWithSurface());
	addBehavior(new BouncesOffSurfaces());
	addBehavior(new UpdatesNAVRadar());
	addBehavior(new Cloakable());
	addBehavior(new RedFlashOnDamage());
	addBehavior(new RollNudgeOnDamage());
	final SpinCrashDeathBehavior scb = new SpinCrashDeathBehavior();
	scb.addPropertyChangeListener(SpinCrashDeathBehavior.TRIGGERED, new SpinCrashTriggerBehaviorListener());
	addBehavior(scb);
	addBehavior(new DeathBehavior());
	addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
	addBehavior(new PlayerDeathListener());
	addBehavior(new SFXOnDamage());
	addBehavior(new RollBehavior(tr.getControllerInputs()));
	
	final Weapon[] allWeapons = Weapon.values();
	
	for (int i = 0; i < allWeapons.length; i++) {
	    final Weapon w = allWeapons[i];
	    if (w.getButtonToSelect() != -1) {
		final ProjectileFiringBehavior pfb;
		if (w.isLaser()) {// LASER
		    pfb = new UpgradeableProjectileFiringBehavior()
			    .setProjectileFactory(tr.getResourceManager()
				    .getProjectileFactories()[w.ordinal()]);
		    ((UpgradeableProjectileFiringBehavior) pfb)
			    .setMaxCapabilityLevel(2)
			    .setCapabilityLevel(i==0?0:-1)
			    .setFiringMultiplexMap(
				    new Vector3D[][] {
					    new Vector3D[] {
						    new Vector3D(RIGHT_X, BOT_Y, 0),
						    new Vector3D(LEFT_X, BOT_Y,
							    0) },// Level 0,
								 // single
					    new Vector3D[] {
						    new Vector3D(RIGHT_X, BOT_Y, 0),
						    new Vector3D(LEFT_X, BOT_Y,
							    0) },// Level 1,
								 // double
					    new Vector3D[] {
						    new Vector3D(RIGHT_X, BOT_Y, 0),
						    new Vector3D(LEFT_X, BOT_Y,
							    0),// Level 2 quad
						    new Vector3D(RIGHT_X, TOP_Y, 0),
						    new Vector3D(LEFT_X, TOP_Y, 0) } })//Level 2 cont'd
				.setTimeBetweenFiringsMillis(w.getFiringIntervalMS())
				.setSumProjectorVelocity(w.isSumWithProjectorVel());
		}// end if(isLaser)
		else {// NOT LASER
		    pfb = new ProjectileFiringBehavior().setFiringPositions(
			    new Vector3D[] { new Vector3D(RIGHT_X, BOT_Y, 0),
				    new Vector3D(LEFT_X, BOT_Y, 0) })
			    .setProjectileFactory(
				    tr.getResourceManager()
					    .getProjectileFactories()[w
					    .ordinal()])
		            .setTimeBetweenFiringsMillis(w.getFiringIntervalMS())
		            .setSumProjectorVelocity(w.isSumWithProjectorVel());
		    if (w == Weapon.DAM)
			pfb.setAmmoLimit(1);
		    if(w == Weapon.ION){
			pfb.setFiringDirections( new Vector3D [] {
				new Vector3D(-.5,0,1).normalize(),//LEFT
				Vector3D.PLUS_K, //CENTER
				new Vector3D(.5,0,1).normalize(),//RIGHT
				new Vector3D(0,-.3,1).normalize(),//DOWN
				new Vector3D(0,.3,1).normalize() //UP
			});
			pfb.setFiringPositions(new Vector3D[]{
				new Vector3D(0,BOT_Y,0),
				new Vector3D(0,BOT_Y,0),
				new Vector3D(0,BOT_Y,0),
				new Vector3D(0,BOT_Y,0),
				new Vector3D(0,BOT_Y,0)
				});
		    }//end if(ION)
		}
		addBehavior(pfb);
		weapons[w.getButtonToSelect() - 1] = pfb;
		if(System.getProperties().containsKey("org.jtrfp.trcl.allAmmo")){
		    if(System.getProperty("org.jtrfp.trcl.allAmmo").toUpperCase().contains("TRUE")){
			System.out.println("allAmmo cheat active for weapon "+w.getButtonToSelect());
			pfb.setAmmoLimit(Integer.MAX_VALUE);
			try{pfb.addSupply(Double.POSITIVE_INFINITY);}catch(SupplyNotNeededException e){}
		    }//end if(property=true)
		}//end if(allAmmo)
	    }// end if(hasButton)
	}//end for(Weapons)
	addBehavior(new UserInputWeaponSelectionBehavior(tr.getControllerInputs()).setBehaviors(weapons));
	
	defaultConfiguration();
    }//end constructor
    
    private class RunModeListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final Object newValue = evt.getNewValue();
	    final Object oldValue = evt.getOldValue();
	    if(newValue instanceof Mission.TunnelState         && !(oldValue instanceof Mission.TunnelState))
		headingXAlwaysPositiveBehavior.setEnable(true);
	    else if(!(newValue instanceof Mission.TunnelState) && (oldValue instanceof Mission.TunnelState))
		headingXAlwaysPositiveBehavior.setEnable(false);
	}//end propertyChange(...)
    }//end RunModeListener
    
    private void defaultConfiguration(){
		probeForBehavior(VelocityDragBehavior.class)
			.setDragCoefficient(.86);
		probeForBehavior(Propelled.class).setMinPropulsion(0);
		probeForBehavior(Propelled.class)
			.setMaxPropulsion(900000);
		probeForBehavior(RotationalDragBehavior.class)
			.setDragCoefficient(.86);
		setActive(false);
    }//end defaultConfiguration()
    
    public void resetVelocityRotMomentum(){
	probeForBehavior(RotationalMomentumBehavior.class).
		setEquatorialMomentum(0).
		setLateralMomentum(0).
		setPolarMomentum(0);
	probeForBehavior(MovesByVelocity.class).setVelocity(new double[3]);
    }
    
    private class PlayerDeathListener extends Behavior implements DeathListener{
	@Override
	public void notifyDeath() {
	    new Thread(){
		@Override
		public void run(){
		    final Player thisPlayer = Player.this;
		    setName("Player Death Sequence Thread");
		    System.out.println("Player has died.");
		    try{Thread.sleep(3000);}
		    catch(InterruptedException e){}
		    //Reset player
		    final DamageableBehavior db = Player.this.probeForBehavior(DamageableBehavior.class);
		    db.setHealth(db.getMaxHealth());
		    Player.this.defaultConfiguration();
		    thisPlayer.probeForBehavior(SpinCrashDeathBehavior.class).
		      reset().
		      setEnable(true);
		    probeForBehaviors(new AbstractSubmitter<ProjectileFiringBehavior>(){
			@Override
			public void submit(ProjectileFiringBehavior item) {
			    item.setEnable(true);
			}}, ProjectileFiringBehavior.class);
		    probeForBehavior(ProjectileFiringBehavior.class).setEnable(true);
		    thisPlayer.probeForBehavior(DeathBehavior.class).reset();
		    //Reset camera
		    final Camera camera = Player.this.getTr().mainRenderer.getCamera(); 
		    Player.this.setVisible(false);
		    camera.probeForBehavior(MatchPosition.class) .setEnable(true);
		    camera.probeForBehavior(MatchDirection.class).setEnable(true);
		    camera.probeForBehavior(RotateAroundObject.class).
		     setEnable(false);
		    camera.probeForBehavior(FacingObject.class).
		     setEnable(false);
		    //Reset game
	            final TVF3Game game = (TVF3Game)Player.this.getGameShell().getGame();
	            final Mission mission = game.getCurrentMission();
	            Features.get(mission, GamePause.class).setPaused(true);
		    mission.abort();
		    final SpacePartitioningGrid grid = thisPlayer.probeForBehavior(DeathBehavior.class).getGridOfLastDeath();
		    grid.add(thisPlayer);
		    thisPlayer.setActive(true);
		    
		    try{game.setLevelIndex(game.getLevelIndex());
		        game.getCurrentMission().setNavSubObjects(null);//Ensure they are repopulated
		        game.getCurrentMission().setShowIntro(false);
		        game.getCurrentMission().go();
		    }catch(Exception e){e.printStackTrace();}
		}//end run()
	    }.start();
	}//end notifyDeath()
    }//end PlayerDeathListener

    /**
     * @return the weapons
     */
    public ProjectileFiringBehavior[] getWeapons() {
	return weapons;
    }
    
    private class SpinCrashTriggerBehaviorListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
	    if(pce.getNewValue()==Boolean.TRUE){
		System.out.println("Player death sequence triggered.");
		final Camera camera = Player.this.getTr().mainRenderer.getCamera(); 
		Player.this.setVisible(true);
		    camera.probeForBehavior(MatchPosition.class) .setEnable(false);
		    camera.probeForBehavior(MatchDirection.class).setEnable(false);
		    camera.probeForBehavior(RotateAroundObject.class).
		            setTarget(Player.this).
		    	    setDistance(TRFactory.mapSquareSize*1).
			    setAngularVelocityRPS(.1).
			    setEnable(true);
		    camera.probeForBehavior(FacingObject.class).
		      setTarget(Player.this).
		      setEnable(true);
	    }//end if(triggered)
	}//end propertyChange(...)
    }//end PropertyChangeListener
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }
    
    public PlayerSaveState getPlayerSaveState(){
	final PlayerSaveState result = new PlayerSaveState();
	result.readFrom(this);
	return result;
    }//end getPlayerSaveState()
    
    public void setPlayerSaveState(PlayerSaveState pss){
	pss.writeTo(this);
    }//end setPlayerSaveState()
    
    public static class PlayerSaveState {
	private int [] ammoQuantities;
	private int [] ammoCapabilityLevels;
	private int health;
	private double[] position, heading, top;
	boolean invincible, cloaked;
	private int selectedWeapon = 0;
	//TODO: Turbo quantity
	
	public void readFrom(Player source){
	    final ProjectileFiringBehavior [] weapons = source.getWeapons();
	    final int numPFBs                  = weapons.length;
	    final int [] ammoCapabilityLevels  = new int[numPFBs];
	    final int [] ammoQuantities        = new int[numPFBs];
	    
	    for( int i = 0; i < numPFBs; i++ ){
		ProjectileFiringBehavior pfb = weapons[i];
		if( pfb instanceof UpgradeableProjectileFiringBehavior ) {
		    final UpgradeableProjectileFiringBehavior upfb = (UpgradeableProjectileFiringBehavior)pfb;
		       ammoCapabilityLevels [i]  = upfb.getCapabilityLevel();
		} else ammoCapabilityLevels [i]  = -1;// -1 means 'ignore'
		if(pfb != null)
		    ammoQuantities      [i]  = pfb.getAmmo();
	    }//end for(numPFBs)
	    
	    setAmmoCapabilityLevels(ammoCapabilityLevels);
	    setAmmoQuantities     (ammoQuantities);
	    
	    final DamageableBehavior db = source.probeForBehavior(DamageableBehavior.class);
	    
	    setHealth    (db    .getHealth());
	    setInvincible(db    .isInvincible());
	    setCloaked   (source.probeForBehavior(Cloakable.class).isCloaked());
	    setPosition  (source.getPosition());
	    setHeading   (source.getHeading().toArray());
	    setTop       (source.getTop().toArray());
	    
	    setSelectedWeapon(source.probeForBehavior(UserInputWeaponSelectionBehavior.class).getActiveBehaviorByIndex());
	}//end readFrom(...)
	
	public void writeTo(Player target){
	    final ProjectileFiringBehavior [] weapons = target.getWeapons();
	    final int numPFBs                  = weapons.length;
	    final int [] ammoCapabilityLevels  = getAmmoCapabilityLevels();
	    final int [] ammoQuantities        = getAmmoQuantities();
	    
	    for( int i = 0; i < numPFBs; i++ ){
		ProjectileFiringBehavior pfb = weapons[i];
		if( pfb instanceof UpgradeableProjectileFiringBehavior ) {
		    final UpgradeableProjectileFiringBehavior upfb = (UpgradeableProjectileFiringBehavior)pfb;
		    upfb.setCapabilityLevel(ammoCapabilityLevels[i]);
		}
		if(pfb != null)
		    pfb.setAmmo(ammoQuantities[i]);
	    }//end for(numPFBs)
	    
	    final DamageableBehavior db = target.probeForBehavior(DamageableBehavior.class);
	    
	    db.setHealth       (getHealth());
	    if(isInvincible())
		db.addInvincibility(INVINCIBILITY_COUNTDOWN_START);
	    if(isCloaked())
	       try {target.probeForBehavior(Cloakable.class).addSupply(CLOAK_COUNTDOWN_START);}
	       catch(SupplyNotNeededException e){}
	    target.setPosition(getPosition());
	    target.setHeadingArray(getHeading());
	    target.setTopArray    (getTop());
	    target.notifyPositionChange();
	    
	    target.probeForBehavior(UserInputWeaponSelectionBehavior.class).setActiveBehaviorByIndex(getSelectedWeapon());
	}//end writeTo(...)
	
	public int[] getAmmoQuantities() {
	    return ammoQuantities;
	}
	public void setAmmoQuantities(int[] ammoQuantities) {
	    this.ammoQuantities = ammoQuantities;
	}
	public int[] getAmmoCapabilityLevels() {
	    return ammoCapabilityLevels;
	}
	public void setAmmoCapabilityLevels(int[] ammoDegrees) {
	    this.ammoCapabilityLevels = ammoDegrees;
	}
	public int getHealth() {
	    return health;
	}
	public void setHealth(int health) {
	    this.health = health;
	}

	public double [] getPosition() {
	    return position;
	}

	public void setPosition(double [] position) {
	    this.position = position;
	}

	public double [] getHeading() {
	    return heading;
	}

	public void setHeading(double [] heading) {
	    this.heading = heading;
	}

	public double [] getTop() {
	    return top;
	}

	public void setTop(double [] top) {
	    this.top = top;
	}

	public boolean isInvincible() {
	    return invincible;
	}

	public void setInvincible(boolean remainingInvincibility) {
	    this.invincible = remainingInvincibility;
	}

	public boolean isCloaked() {
	    return cloaked;
	}

	public void setCloaked(boolean remainingCloak) {
	    this.cloaked = remainingCloak;
	}

	public int getSelectedWeapon() {
	    return selectedWeapon;
	}

	public void setSelectedWeapon(int selectedWeapon) {
	    this.selectedWeapon = selectedWeapon;
	}
	
    }//end DefaultPlayerSaveState
    
}// end Player
