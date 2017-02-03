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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.AutoLeveling.LevelingAxis;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.FeaturesImpl.FeatureNotFoundException;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.RollPitchYawBehavior;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.tools.Util;

public class NewSmartPlaneBehavior extends Behavior {
    private volatile double safeFromPlayerRadius = TRFactory.mapSquareSize*10;
    private volatile double tooCloseRadius       = TRFactory.mapSquareSize*5;
    private long timeOfCurrentMode = System.currentTimeMillis();
    private Player player;
    private GameShell gameShell;
    private double heightThreshold = TRFactory.mapSquareSize * 4;
    private RollPitchYawBehavior rollPitchYawBehavior;
    private AutoFiring autoFiringBehavior;
    private final BehaviorMutex<SmartPlaneMode> behaviorSelector = new BehaviorMutex<SmartPlaneMode>();
    private boolean firstRun = true;
    private PlaneTurnBehavior planeTurnBehavior;
    private RotationalMomentumBehavior rotationalMomentumBehavior;
    private RotationalDragBehavior	rotationalDragBehavior;
    private double rotationScalar = 1;
    
    protected enum SmartPlaneMode{
	TURN_TOWARD_PLAYER,
	TURN_AWAY_PLAYER,
	TURN_PERP_PLAYER,
	FLY_STRAIGHT_UPRIGHT,
	FLY_STRAIGHT_UPSIDE_DOWN,
	FLIP_UP,
	UNFLIP_UP,
	UNFLIP_SPIN,
	SPIN,
	TURN_LEFT,
	TURN_RIGHT,
	FREEHWEEL//Everything off
    }
    
    public NewSmartPlaneBehavior(){
    }
    
    protected void init(){
	firstRun = false;
	//Make sure we have prereqs and that existing behaviors aren't present to interfere.
	final WorldObject parent = getParent();
	
	try{while(true)parent.removeBehavior(parent.probeForBehavior(AutoLeveling.class));}
	catch(BehaviorNotFoundException e){}//That's fine.
	
	getRotationalDragBehavior();
	
	getParent().addBehavior(new BuzzByPlayerSFX().setBuzzSounds(new String[]{
		"FLYBY56.WAV","FLYBY60.WAV","FLYBY80.WAV","FLYBY81.WAV"}));
	
	initTurnTowardPlayer();
	initAwayFromPlayer();
	initPerpendicularToPlayer();
	initFlyStraightUpright();
	initFlyStraightUpsideDown();
	initFlipUp();
	initUnflipUp();
	initUnflipSpin();
	initTurnLeft();
	initTurnRight();
	initSpin();
	
	//Initial state
	setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
    }//end ensureInit()
    
    private void setCurrentMode(SmartPlaneMode mode){
	timeOfCurrentMode = System.currentTimeMillis();
	behaviorSelector.setEnabledGroup(mode);
    }
    
    @Override
    public Behavior setEnable(boolean enabled) {
	if(!enabled)
	    behaviorSelector.setEnabledGroup(SmartPlaneMode.FREEHWEEL);
	behaviorSelector.setEnable(enabled);
	return super.setEnable(enabled);
    }
    
    private SmartPlaneMode getCurrentMode(){
	return behaviorSelector.getEnabledGroup();
    }
    
    private void initTurnLeft(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_LEFT, al);
	final PlaneTurnBehavior ptb = new PlaneTurnBehavior();
	ptb.setTurnFactor(.003);
	ptb.setRotationalMomentumBehavior(getRotationalMomentumBehavior());
	parent.addBehavior(ptb);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_LEFT, ptb);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(3500);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_LEFT, ttre);
    }//end initTurnLeft()
    
    private void initTurnRight(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_RIGHT, al);
	final PlaneTurnBehavior ptb = new PlaneTurnBehavior();
	ptb.setTurnFactor(-.003);
	ptb.setRotationalMomentumBehavior(getRotationalMomentumBehavior());
	parent.addBehavior(ptb);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_RIGHT, ptb);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(3500);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_RIGHT, ttre);
    }//end initTurnRight()
    
    private void initSpin(){
	final WorldObject parent = getParent();
	final Spin spin = new Spin();
	parent.addBehavior(spin);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.SPIN, spin);
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(3000, SmartPlaneMode.UNFLIP_SPIN);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.SPIN, ttre);
    }//end initUnflipSpin()
    
    private class Spin extends Behavior {
	private static final double ACCEL_FACTOR = .015;
	private final double direction = Math.signum(Math.random()-.5);
	@Override
	public void tick(long tickTimeMillis){
	    final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
	    rotationalMomentumBehavior.accellerateLateralMomentum(ACCEL_FACTOR*direction);
	}//end tick(...)
    }//end Spin
    
    private void initUnflipSpin(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_SPIN, al);
	final UnflipSpin flipUp = new UnflipSpin();
	parent.addBehavior(flipUp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_SPIN, flipUp);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(2000);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_SPIN, ttre);
    }//end initUnflipSpin()
    
    private class UnflipSpin extends Behavior {
	private static final double ACCEL_FACTOR = .003;
	private final double direction = Math.signum(Math.random()-.5);
	@Override
	public void tick(long tickTimeMillis){
	    if(getParent().getTopArray()[1] > 0)
		return;//Already upside down
	    final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
	    rotationalMomentumBehavior.accellerateLateralMomentum(ACCEL_FACTOR*direction);
	}//end tick(...)
    }//end UnflipSpin
    
    private void initFlipUp(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.MINUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLIP_UP, al);
	final FlipUp flipUp = new FlipUp();
	parent.addBehavior(flipUp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLIP_UP, flipUp);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(2500, SmartPlaneMode.FLY_STRAIGHT_UPSIDE_DOWN);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLIP_UP, ttre);
    }//end initFlipUp()
    
    private class FlipUp extends Behavior {
	private static final double ACCEL_FACTOR = .003;
	@Override
	public void tick(long tickTimeMillis){
	    if(getParent().getTopArray()[1] < 0)
		return;//Already upside down
	    final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
	    rotationalMomentumBehavior.accelleratePolarMomentum(ACCEL_FACTOR);
	}//end tick(...)
    }//end FlipUp
    
    private void initUnflipUp(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_UP, al);
	final UnflipUp unflipUp = new UnflipUp();
	parent.addBehavior(unflipUp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_UP, unflipUp);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(2500);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.UNFLIP_UP, ttre);
    }//end initUnflipUp()
    
    private class UnflipUp extends Behavior {
	private static final double ACCEL_FACTOR = .003;
	@Override
	public void tick(long tickTimeMillis){
	    if(getParent().getTopArray()[1] > 0)
		return;//Already rightside up
	    final RotationalMomentumBehavior rotationalMomentumBehavior = getRotationalMomentumBehavior();
	    rotationalMomentumBehavior.accelleratePolarMomentum(ACCEL_FACTOR);
	}//end tick(...)
    }//end unFlipUp
    
    private void initFlyStraightUpright(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLY_STRAIGHT_UPRIGHT, al);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(1750);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLY_STRAIGHT_UPRIGHT, ttre);
    }
    
    private void initFlyStraightUpsideDown(){
	final WorldObject parent = getParent();
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.MINUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLY_STRAIGHT_UPSIDE_DOWN, al);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(1750, SmartPlaneMode.UNFLIP_UP);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.FLY_STRAIGHT_UPSIDE_DOWN, ttre);
    }
    
    private void initPerpendicularToPlayer(){
	final WorldObject parent = getParent();
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_PERP_PLAYER, getPlaneTurnBehavior());
	
	final PlaneTurnTowardTarget pttt = new PlaneTurnTowardTarget();
	pttt.setTarget(getPlayer());
	pttt.setTurnFactor(.003 * getRotationScalar());
	pttt.setDesiredRelativeVector(Vector3D.PLUS_I);
	pttt.setSymmetricalRelativeVector(true);
	parent.addBehavior(pttt);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_PERP_PLAYER, pttt);
	
	final AdjustAltitudeToPlayerBehavior aatp = new AdjustAltitudeToPlayerBehavior(getPlayer());
	aatp.setAccelleration(10000);
	parent.addBehavior(aatp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_PERP_PLAYER, aatp);
	
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_PERP_PLAYER, al);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(4000);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_PERP_PLAYER, ttre);
    }//end initTurnTowardPlayer
    
    private void initAwayFromPlayer(){
	final WorldObject parent = getParent();
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_AWAY_PLAYER, getPlaneTurnBehavior());
	
	final PlaneTurnTowardTarget pttt = new PlaneTurnTowardTarget();
	pttt.setTarget(getPlayer());
	pttt.setTurnFactor(.003 * getRotationScalar());
	pttt.setDesiredRelativeVector(Vector3D.MINUS_K);
	parent.addBehavior(pttt);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_AWAY_PLAYER, pttt);
	
	final AdjustAltitudeToPlayerBehavior aatp = new AdjustAltitudeToPlayerBehavior(getPlayer());
	aatp.setAccelleration(10000);
	aatp.setReverse(true);
	parent.addBehavior(aatp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_AWAY_PLAYER, aatp);
	
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_AWAY_PLAYER, al);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(4000);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_AWAY_PLAYER, ttre);
    }
    
    private void initTurnTowardPlayer(){
	final WorldObject parent = getParent();
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_TOWARD_PLAYER, getPlaneTurnBehavior());
	
	final PlaneTurnTowardTarget pttt = new PlaneTurnTowardTarget();
	pttt.setTarget(getPlayer());
	pttt.setTurnFactor(.003 * getRotationScalar());
	pttt.setDesiredRelativeVector(Vector3D.PLUS_K);
	parent.addBehavior(pttt);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_TOWARD_PLAYER, pttt);
	
	final AdjustAltitudeToPlayerBehavior aatp = new AdjustAltitudeToPlayerBehavior(getPlayer());
	aatp.setAccelleration(10000);
	parent.addBehavior(aatp);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_TOWARD_PLAYER, aatp);
	
	final AutoLeveling al = new AutoLeveling();
	al.setLevelingAxis(LevelingAxis.TOP);
	al.setLevelingVector(Vector3D.PLUS_J);
	al.setRetainmentCoeff(.985, .985, .985);
	parent.addBehavior(al);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_TOWARD_PLAYER, al);
	
	final TimeoutThenReEvaluate ttre = new TimeoutThenReEvaluate(4000);
	parent.addBehavior(ttre);
	behaviorSelector.addToMutexGroup(SmartPlaneMode.TURN_TOWARD_PLAYER, ttre);
    }//end initTurnTowardPlayer
    
    private class AlwaysReEvaluate extends Behavior {
	@Override
	public void tick(long tickTimeMillis){
	    reEvaluateState();
	}
    }//end AlwaysReEvaluate
    
    private class TimeoutThenReEvaluate extends Behavior {
	private final long timeout;
	private SmartPlaneMode nextMode;
	
	public TimeoutThenReEvaluate(long timeout){
	    this(timeout, null);
	}//end constructor
	
	public TimeoutThenReEvaluate(long timeout, SmartPlaneMode nextMode){
	    super();
	    this.timeout = timeout;
	    this.nextMode = nextMode;
	}//end constructor
	
	@Override
	public void tick(long tickTimeMillis){
	    if(currentModeTimer() > getTimeout())
		if(nextMode == null)
	            reEvaluateState();
		else
		    setCurrentMode(nextMode);
	}
	public long getTimeout() {
	    return timeout;
	}
    }//end AlwaysReEvaluate
    
    private void reEvaluateState(){
	final double distanceFromTarget = getDistanceFromTarget();
	if(distanceFromTarget >= this.getSafeFromPlayerRadius()){//SAFE
	    reEvaluateSafe();
	}else if(distanceFromTarget <= this.getTooCloseRadius()){//TOO CLOSE
	    reEvaluateTooClose();
	}else{ //ATTACK RADIUS
	    reEvaluateAttack();
	}
    }//end reEvaluateState()
    
    private void reEvaluateSafe(){
	if(isFacedByPlayer()){
	    final double r = Math.random();
	    if(r > .5)
	        this.setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
	    else
		if(r < .25)
		    this.setCurrentMode(SmartPlaneMode.TURN_LEFT);
		else
		    this.setCurrentMode(SmartPlaneMode.TURN_RIGHT);
	}else{
	    final double r = Math.random();
	    if( r > .33 )
	        this.setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
	    else
		this.setCurrentMode(SmartPlaneMode.SPIN);
	}
    }//end reEvaluateSafe()
    
    private void reEvaluateAttack(){
	if(isFacedByPlayer()){
	    if(isFacingPlayer()){
		final double r = Math.random();
		if( r > .2 )
		    this.setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
		else
		    this.setCurrentMode(SmartPlaneMode.TURN_AWAY_PLAYER);
	    }else{
		this.setCurrentMode(SmartPlaneMode.TURN_PERP_PLAYER);
	    }
	}else{
	    final double r = Math.random();
	    if( r > .5 )
	        this.setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
	    else
		this.setCurrentMode(SmartPlaneMode.SPIN);
	}
    }//end reEvaluateAttack()
    
    private void reEvaluateTooClose(){
	if( isFacedByPlayer() ){
	    if( isFacingPlayer() ){
		final double r = Math.random();
		if( r > .3 )
		    this.setCurrentMode(SmartPlaneMode.TURN_AWAY_PLAYER);
		else
		    this.setCurrentMode(SmartPlaneMode.SPIN);
	    }else{
		final double r = Math.random();
		if( r > .75 )
		    this.setCurrentMode(SmartPlaneMode.FLIP_UP);
		else if( r > .5)
		    this.setCurrentMode(SmartPlaneMode.TURN_LEFT);
		else if( r > .25 )
		    this.setCurrentMode(SmartPlaneMode.TURN_RIGHT);
		else
		    this.setCurrentMode(SmartPlaneMode.SPIN);
	    }
	}else{
	    this.setCurrentMode(SmartPlaneMode.TURN_TOWARD_PLAYER);
	}
    }//end reEvaluateTooClose()
    
    private double getDistanceFromTarget(){
	return Vect3D.distance(getParent().getPosition(), getPlayer().getPosition());
    }
    
    private boolean isFacedByPlayer(){
	final WorldObject parent = getParent();
	final WorldObject player = getPlayer();
	final double [] relativeHeadingVector = new double[3]; //TODO: Cache as field.
	Util.relativeHeadingVector(player.getPosition(), player.getHeadingArray(), parent.getPosition(), relativeHeadingVector);
	return relativeHeadingVector[2] > 0;
    }//end isFacingPlayer
    
    private boolean isFacingPlayer(){
	final WorldObject parent = getParent();
	final WorldObject player = getPlayer();
	final double [] relativeHeadingVector = new double[3]; //TODO: Cache as field.
	Util.relativeHeadingVector(parent.getPosition(), parent.getHeadingArray(), player.getPosition(), relativeHeadingVector);
	return relativeHeadingVector[2] > 0;
    }//end isFacingPlayer
    
    @Override
    public void tick(long tickTimeMillis){
	if(firstRun)
	 init();
    }//end tick(...)
    
    public long currentModeTimer(){
	return System.currentTimeMillis() - timeOfCurrentMode;
    }
    
    public boolean isSafeDistance(){
	return Vect3D.distance(getParent().getPosition(), getPlayer().getPosition()) > this.getSafeFromPlayerRadius();
    }
    
    public boolean isTooCloseDistance(){
	return Vect3D.distance(getParent().getPosition(), getPlayer().getPosition()) < this.getTooCloseRadius();
    }

    public Player getPlayer() {
	if(player == null)
	    player = getGameShell().getGame().getPlayer();
        return player;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getParent().getTr(), GameShell.class);
	return gameShell;
    }

    public NewSmartPlaneBehavior setPlayer(Player player) {
        this.player = player;
        return this;
    }

    public double getHeightThreshold() {
        return heightThreshold;
    }

    public NewSmartPlaneBehavior setHeightThreshold(double heightThreshold) {
        this.heightThreshold = heightThreshold;
        return this;
    }

    public RollPitchYawBehavior getRollPitchYawBehavior() {
	if(rollPitchYawBehavior == null)
	    rollPitchYawBehavior = getParent().probeForBehavior(RollPitchYawBehavior.class);
        return rollPitchYawBehavior;
    }

    public NewSmartPlaneBehavior setRollPitchYawBehavior(RollPitchYawBehavior rollPitchYawBehavior) {
        this.rollPitchYawBehavior = rollPitchYawBehavior;
        return this;
    }

    public AutoFiring getAutoFiringBehavior() {
	if(autoFiringBehavior == null)
	    autoFiringBehavior = getParent().probeForBehavior(AutoFiring.class);
        return autoFiringBehavior;
    }

    public NewSmartPlaneBehavior setAutoFiringBehavior(AutoFiring autoFiringBehavior) {
        this.autoFiringBehavior = autoFiringBehavior;
        return this;
    }

    public PlaneTurnBehavior getPlaneTurnBehavior() {
	if(planeTurnBehavior == null){
	    final PlaneTurnBehavior result = new PlaneTurnBehavior();
	    result.setNoseUpCompensation(1);//TODO
	    result.setRotationalMomentumBehavior(getRotationalMomentumBehavior());
	    getParent().addBehavior(result);
	    planeTurnBehavior = result;
	    }
        return planeTurnBehavior;
    }

    public void setPlaneTurnBehavior(PlaneTurnBehavior planeTurnBehavior) {
        this.planeTurnBehavior = planeTurnBehavior;
    }

    public BehaviorMutex<SmartPlaneMode> getBehaviorSelector() {
        return behaviorSelector;
    }

    public double getSafeFromPlayerRadius() {
        return safeFromPlayerRadius;
    }

    public void setSafeFromPlayerRadius(double safeFromPlayerRadius) {
        this.safeFromPlayerRadius = safeFromPlayerRadius;
    }

    public double getTooCloseRadius() {
        return tooCloseRadius;
    }

    public void setTooCloseRadius(double tooCloseRadius) {
        this.tooCloseRadius = tooCloseRadius;
    }

    public RotationalMomentumBehavior getRotationalMomentumBehavior() {
	if( rotationalMomentumBehavior == null )
	    rotationalMomentumBehavior = getParent().probeForBehavior(RotationalMomentumBehavior.class);
        return rotationalMomentumBehavior;
    }

    public void setRotationalMomentumBehavior(
    	RotationalMomentumBehavior rotationalMomentumBehavior) {
        this.rotationalMomentumBehavior = rotationalMomentumBehavior;
    }

    public RotationalDragBehavior getRotationalDragBehavior() {
	if(rotationalDragBehavior == null){
	    final RotationalDragBehavior rotationalDragBehavior = new RotationalDragBehavior();
	    getParent().addBehavior(rotationalDragBehavior);
	    return this.rotationalDragBehavior = rotationalDragBehavior;
	}
        return rotationalDragBehavior;
    }

    public void setRotationalDragBehavior(
    	RotationalDragBehavior rotationalDragBehavior) {
        this.rotationalDragBehavior = rotationalDragBehavior;
    }

    public double getRotationScalar() {
        return rotationScalar;
    }

    public void setRotationScalar(double rotationScalar) {
        this.rotationScalar = rotationScalar;
    }
}//end NewSmartPlaneBehavior
