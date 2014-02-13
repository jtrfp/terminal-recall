package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.beh.SpinAccellerationBehavior.SpinMode;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;

public class SmartPlaneBehavior extends Behavior {
private final HorizAimAtPlayerBehavior aimAtPlayer;
private final AutoFiring autoFiring;
private final SpinAccellerationBehavior spinBehavior;
private final AdjustAltitudeToPlayerBehavior adjustAlt;
private int sequenceUpdateIntervalMillis = 2000;
private long nextUpdateTimeMillis = 0;
private double ignorePlayerDistanceMin = TR.mapSquareSize*15;
private double attackPlayerDistanceMin = TR.mapSquareSize*5;//Any closer is retreat mode
private BehaviorMode behaviorMode = null;
private int behaviorSubSequence = 0;

    public SmartPlaneBehavior(HorizAimAtPlayerBehavior haapb,
	    AutoFiring afb, SpinAccellerationBehavior sahb, AdjustAltitudeToPlayerBehavior aatpb) {
	adjustAlt=aatpb;
	aimAtPlayer=haapb;
	autoFiring=afb;
	spinBehavior=sahb;
    }//end constructor(...)
    
    @Override
    public void _tick(long timeInMillis){
	if(timeInMillis>=nextUpdateTimeMillis){
	    final WorldObject thisObject = getParent();
	    final Player player = thisObject.getTr().getPlayer();
	    final double [] thisPos = thisObject.getPosition();
	    final double [] playerPos = player.getPosition();
	    final double distFromPlayer = Vect3D.distance(thisPos, playerPos);
	    if(distFromPlayer>ignorePlayerDistanceMin){
		ignorePlayer();
	    }else if(distFromPlayer<ignorePlayerDistanceMin && distFromPlayer>attackPlayerDistanceMin){
		attackPlayer();
	    }else{
		retreat();
	    }//end else{}
	    nextUpdateTimeMillis=timeInMillis+sequenceUpdateIntervalMillis;
	}//end if(time to update)
    }//end _tick(...)
    
    private void retreat(){
	//System.out.println("SmartPlaneBehavior: Retreat.");
	aimAtPlayer.setEnable(true);
	aimAtPlayer.setReverse(true);
	autoFiring.setEnable(false);
	adjustAlt.setEnable(true);
	adjustAlt.setReverse(true);
	adjustAlt.setAccelleration(4000);
	//spinBehavior.setEnable(true);
	//spinBehavior.setSpinMode(SpinMode.LATERAL);
	//spinBehavior.setSpinAccelleration(.007);//Spin away
	aimAtPlayer.setTurnAcceleration(.004+Math.random()*.004);
    }
    
    private void attackPlayer(){//Turn toward player, auto-fire on.
	//System.out.println("SmartPlaneBehavior: Attack.");
	aimAtPlayer.setEnable(true);
	aimAtPlayer.setReverse(false);
	autoFiring.setEnable(true);
	adjustAlt.setEnable(true);
	adjustAlt.setReverse(false);
	adjustAlt.setAccelleration(6000);
	spinBehavior.setEnable(false);
	aimAtPlayer.setTurnAcceleration(.002+Math.random()*.002);
    }
    
    private void ignorePlayer(){//Follow a figure-8 herp-derp.
	//Straight, turn+, straight, turn-
	//System.out.println("SmartPlaneBehavior: Ignore.");
	if(behaviorMode!=BehaviorMode.ignore){
	    behaviorMode=BehaviorMode.ignore;
	    behaviorSubSequence=0;
	    aimAtPlayer.setEnable(false);
	    autoFiring.setEnable(false);
	    adjustAlt.setEnable(false);
	}//end if(!ignore)
	behaviorSubSequence%=4;
	switch(behaviorSubSequence){
	case 0:case 2://Straight
	    spinBehavior.setEnable(false);
	    break;
	case 1://turn+
	    spinBehavior.setEnable(true);
	    spinBehavior.setSpinAccelleration(.004+Math.random()*.002);
	    spinBehavior.setSpinMode(SpinMode.EQUATORIAL);
	    break;
	case 3://turn-
	    spinBehavior.setEnable(true);
	    spinBehavior.setSpinAccelleration(-.004-Math.random()*.002);
	    spinBehavior.setSpinMode(SpinMode.EQUATORIAL);
	    break;
	}//end switch(behaviorSubSequence)
	behaviorSubSequence++;
    }//end ignorePlayer()
    
    private enum BehaviorMode{
	ignore,
	attack,
	retreat
    }
}//end SmartPlaneBehavior
