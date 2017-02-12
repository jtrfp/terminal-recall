/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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
import org.jtrfp.trcl.beh.phy.AccelleratedByPropulsion;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.SoundSystemFactory.SoundSystemFeature;
import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.Smoke.SmokeType;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundSystem;

public class MissileSiloBehavior extends Behavior {
    private long timeOfLastCycleMillis = -1;
    private long initialRestTimeMillis = 1000L;
    private long launchTimeMillis = 4000L;
    private long resetTimeMillis  = 2000L;
    private long sequenceOffsetMillis = 0;
    private AccelleratedByPropulsion accelleratedByPropulsionBehavior;
    private boolean firstLaunchFrame = true;
    private double initialY;
    protected static final String LAUNCH_SOUND = "MISSILE.WAV";
    @Override
    public void tick(long tickTimeInMillis){
	if(timeOfLastCycleMillis == -1)
	    doFirstTick(tickTimeInMillis);
	final long timeSinceLastCycle = tickTimeInMillis - getTimeOfLastCycleMillis();
	final long restTimeThreshold = getInitialRestTimeMillis();
	final long launchTimeThreshold = getLaunchTimeMillis() + restTimeThreshold;
	final long resetTimeThreshold = getResetTimeMillis() + launchTimeThreshold;
	if(timeSinceLastCycle < restTimeThreshold)
	    doRestBehavior();
	else if(timeSinceLastCycle > restTimeThreshold && timeSinceLastCycle < launchTimeThreshold)
	    doLaunchBehavior();
	else if(timeSinceLastCycle > launchTimeThreshold && timeSinceLastCycle < resetTimeThreshold)
	    doResetBehavior();
	else
	    doReset(tickTimeInMillis);
    }//end MissileSiloBehavior
    
    protected void doFirstTick(long tickTimeMillis){
	timeOfLastCycleMillis = tickTimeMillis+getSequenceOffsetMillis();
	initialY = getParent().getPositionWithOffset()[1];
    }
    
    protected void doRestBehavior(){
	final AccelleratedByPropulsion prop = getAccelleratedByPropulsionBehavior();
	prop.setEnable(false);
	//Set Y value to the terrain height
	final WorldObject parent = getParent();
	parent.setVisible(true);
	final double [] pos = parent.getPosition();
	pos[1] = initialY;
	parent.notifyPositionChange();
    }//end doRestBehavior()
    
    protected void doLaunchBehavior(){
	if(firstLaunchFrame){
	    playLaunchSFX();
	    firstLaunchFrame = false;
	}
	final AccelleratedByPropulsion prop = getAccelleratedByPropulsionBehavior();
	prop.setThrustVector(Vector3D.PLUS_J);
	probeForBehavior(Propelled.class).setPropulsion(1000000);
	prop.setEnable(true);
	if(Math.random() > .9)
	    doSmokePuff();
    }//end doLaunchBehavior
    
    protected void playLaunchSFX(){
	final WorldObject parent = getParent();
	final TR tr = parent.getTr();
	final double [] ppos = parent.getPosition();
	final double [] pos = new double[]{ppos[0],ppos[1],ppos[2]};
	
	Features.get(tr,SoundSystemFeature.class).
	      enqueuePlaybackEvent(Features.get(tr,SoundSystemFeature.class).getPlaybackFactory().
		    create(tr.getResourceManager().soundTextures.get(LAUNCH_SOUND),
			    pos,
			    tr.mainRenderer.getCamera(),
			    SoundSystem.DEFAULT_SFX_VOLUME/2));
    }//end playLaunchSFX
    
    protected void doSmokePuff(){
	final TR tr = getParent().getTr();
	tr.getResourceManager().
	 getSmokeSystem().
	 triggerSmoke(
		 new Vector3D(getParent().getPosition()), 
		 SmokeType.Puff);
    }
    
    protected void doResetBehavior(){
	getParent().setVisible(false);
	final AccelleratedByPropulsion prop = getAccelleratedByPropulsionBehavior();
	prop.setEnable(true);
    }
    
    protected void doReset(long currentTimeMillis){
	firstLaunchFrame = true;
	timeOfLastCycleMillis = currentTimeMillis;
    }
    
    public long getTimeOfLastCycleMillis() {
	return timeOfLastCycleMillis;
    }
    protected void setTimeOfLastCycleMillis(long timeOfLastCycleMillis) {
	this.timeOfLastCycleMillis = timeOfLastCycleMillis;
    }
    public long getInitialRestTimeMillis() {
        return initialRestTimeMillis;
    }
    public void setInitialRestTimeMillis(long initialRestTimeMillis) {
        this.initialRestTimeMillis = initialRestTimeMillis;
    }
    public long getLaunchTimeMillis() {
        return launchTimeMillis;
    }
    public void setLaunchTimeMillis(long launchTimeMillis) {
        this.launchTimeMillis = launchTimeMillis;
    }
    public long getResetTimeMillis() {
        return resetTimeMillis;
    }
    public void setResetTimeMillis(long resetTimeMillis) {
        this.resetTimeMillis = resetTimeMillis;
    }

    public AccelleratedByPropulsion getAccelleratedByPropulsionBehavior() {
	if( accelleratedByPropulsionBehavior == null )
	    accelleratedByPropulsionBehavior = getParent().probeForBehavior(AccelleratedByPropulsion.class);
        return accelleratedByPropulsionBehavior;
    }

    public void setAccelleratedByPropulsionBehavior(
    	AccelleratedByPropulsion accelleratedByPropulsionBehavior) {
        this.accelleratedByPropulsionBehavior = accelleratedByPropulsionBehavior;
    }

    public long getSequenceOffsetMillis() {
        return sequenceOffsetMillis;
    }

    public void setSequenceOffsetMillis(long sequenceOffsetMillis) {
        this.sequenceOffsetMillis = sequenceOffsetMillis;
    }
}//end MissileSiloBehavior
