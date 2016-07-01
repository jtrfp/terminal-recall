/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.beh;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.obj.TerrainChunk;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.snd.SoundSystem;

public class SurfaceImpactSFXBehavior extends Behavior implements
	SurfaceImpactListener {
    final ResourceManager rm;
    final SoundSystem ss;
    final String [] CRASH_SOUNDS = new String[]{"EXP3.WAV","EXP4.WAV"};
    private static final double [] VOLUME = new double[]{SoundSystem.DEFAULT_SFX_VOLUME,SoundSystem.DEFAULT_SFX_VOLUME};
    private long timeOfLastSFXMillis = 0L;
    
    public SurfaceImpactSFXBehavior(TR tr){
	this.rm=tr.getResourceManager();
	this.ss=tr.soundSystem.get();
    }

    @Override
    public void collidedWithSurface(WorldObject src, double[] surfaceNormal) {
	final double collDot = Math.abs(
		getParent().getHeading().normalize().
		dotProduct(new Vector3D(surfaceNormal).normalize()));
	if(System.currentTimeMillis()<timeOfLastSFXMillis+500)
	    return;
	timeOfLastSFXMillis=System.currentTimeMillis();
	if(src instanceof TerrainChunk){
	    //Calculate if this impact is severe or glazing
	    if(collDot<.6){
		ss.enqueuePlaybackEvent(
			ss.getPlaybackFactory().create(
				rm.soundTextures.get("GROUND.WAV"), VOLUME));
	    }else{//Hard crash
		//Boom
		ss.enqueuePlaybackEvent(
			ss.getPlaybackFactory().create(
				rm.soundTextures.get(CRASH_SOUNDS[(int)(Math.random()*2)]), VOLUME));
	    }//end hard Crash
	}//end terrain
	else if(src instanceof TunnelSegment){
	    //Shearing noise
	    ss.enqueuePlaybackEvent(
			ss.getPlaybackFactory().create(
				rm.soundTextures.get("SCRAPE.WAV"), VOLUME));
	    ss.enqueuePlaybackEvent(
			ss.getPlaybackFactory().create(
				rm.soundTextures.get(CRASH_SOUNDS[(int)(Math.random()*2)]), VOLUME));
	}//end if(TunnelSegment)
    }//end collidedWithSurface(...)
}//end SurfaceImpactSFXBehavior
