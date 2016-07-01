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

import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.obj.Player;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SamplePlaybackEvent;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundTexture;

public class BuzzByPlayerSFX extends Behavior implements CollisionBehavior {
    private int             minTimeBetweenBuzzesMillis = 1000;
    private double          promixityThreshold = TRFactory.mapSquareSize*2;
    private long            timeOfLastBuzzMillis = 0L;
    private double          previousDot = 0;
    private final double [] workDouble = new double[3];
    private String []       buzzSounds;
    
 @Override
 public void proposeCollision(WorldObject other){
     if(other instanceof Player){
	 final long currentTimeMillis             = System.currentTimeMillis();
	 final long timePassedSinceLastBuzzMillis = currentTimeMillis - timeOfLastBuzzMillis;
	 
	 if(timePassedSinceLastBuzzMillis>minTimeBetweenBuzzesMillis){
	     checkForBuzz(other);
	 }//end if(timePassed)
     }//end if(Player)
 }
 
 private void checkForBuzz(WorldObject other){
     final Player player  = (Player)other;
     Vect3D.subtract(getParent().getPositionWithOffset(),player.getPositionWithOffset(),workDouble);
     final double  dot    = Vect3D.dot3(workDouble, player.getHeading().toArray());
     final boolean inVicinity = Vect3D.norm(workDouble) <= promixityThreshold;
     final boolean buzzed = Math.signum(dot) != Math.signum(previousDot) && inVicinity;
     if(buzzed)
	 performBuzz();
     previousDot = dot;
 }//end checkForBuzz
 
 private void performBuzz(){
     timeOfLastBuzzMillis = System.currentTimeMillis();
     if(buzzSounds == null)
	 return;
     final String sfxToUse = getBuzzSounds()[(int)(Math.random()*getBuzzSounds().length)];
     final WorldObject parent = getParent();
     final TR              tr = parent.getTr();
     final SoundSystem     ss = tr.soundSystem.get();
     final ResourceManager rm = tr.getResourceManager();
     final SoundTexture texture = rm.soundTextures.get(sfxToUse);
     final SamplePlaybackEvent evt = ss.getPlaybackFactory().create(texture, parent, tr.mainRenderer.getCamera(), SoundSystem.DEFAULT_SFX_VOLUME);
     ss.enqueuePlaybackEvent(evt);
 }//end performBuzz

public String [] getBuzzSounds() {
    return buzzSounds;
}

public BuzzByPlayerSFX setBuzzSounds(String [] buzzSounds) {
    this.buzzSounds = buzzSounds;
    return this;
}

public int getMinTimeBetweenBuzzesMillis() {
    return minTimeBetweenBuzzesMillis;
}

public BuzzByPlayerSFX setMinTimeBetweenBuzzesMillis(int nimTimeBetweenBuzzesMillis) {
    this.minTimeBetweenBuzzesMillis = nimTimeBetweenBuzzesMillis;
    return this;
}

public double getPromixityThreshold() {
    return promixityThreshold;
}

public BuzzByPlayerSFX setPromixityThreshold(double promixityThreshold) {
    this.promixityThreshold = promixityThreshold;
    return this;
}
}//end BuzzByPlayerSFX
