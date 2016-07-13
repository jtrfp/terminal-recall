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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.CollidesWithTerrain;
import org.jtrfp.trcl.beh.CollidesWithTunnelWalls;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.LimitedLifeSpan;
import org.jtrfp.trcl.beh.LoopingPositionBehavior;
import org.jtrfp.trcl.beh.phy.BouncesOffSurfaces;
import org.jtrfp.trcl.beh.phy.MovesByVelocity;
import org.jtrfp.trcl.beh.phy.PulledDownByGravityBehavior;
import org.jtrfp.trcl.beh.phy.RotationalDragBehavior;
import org.jtrfp.trcl.beh.phy.RotationalMomentumBehavior;
import org.jtrfp.trcl.beh.phy.Velocible;
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;

public class Debris extends WorldObject {
private final int MAX_LIFESPAN_MILLIS=4500;
private final int MIN_LIFESPAN_MILLIS=3000;
private final double LIFESPAN_RANGE_MILLIS=MAX_LIFESPAN_MILLIS-MIN_LIFESPAN_MILLIS;
private static final String [] TYPES = new String[]{
    "CHUNK.BIN",
    "PIPE.BIN",
    "PIPE.BIN",
    "PIPE.BIN",
    "FRAG1.BIN",
    "FRAG2.BIN",
    "FRAG3.BIN",
    "FRAG1.BIN",
    "FRAG2.BIN",
    "FRAG3.BIN",
    "FRAG1.BIN",
    "FRAG2.BIN",
    "FRAG3.BIN"
};
    public Debris() {
	super();
	final TR tr = getTr();
	try{
	    Model m = tr.getResourceManager().getBINModel(TYPES[(int)(Math.random()*TYPES.length)], tr.getGlobalPaletteVL(),null, Features.get(tr, GPUFeature.class).getGl());
	    setModel(m);
	    addBehavior(new MovesByVelocity());
	    addBehavior(new VelocityDragBehavior().setDragCoefficient(.99));
	    addBehavior(new CollidesWithTerrain());
	    addBehavior(new CollidesWithTunnelWalls(false, false));
	    addBehavior(new BouncesOffSurfaces().setReflectHeading(false));
	    addBehavior(new DeathBehavior());
	    addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
	    addBehavior(new LimitedLifeSpan().reset(lifespan()));
	    addBehavior(new LoopingPositionBehavior());
	    addBehavior(new PulledDownByGravityBehavior());
	    addBehavior(new RotationalMomentumBehavior()
	    .setEquatorialMomentum(.2*Math.random())
	    .setLateralMomentum(.2*Math.random())
	    .setPolarMomentum(.2*Math.random()));
	    addBehavior(new RotationalDragBehavior().setDragCoefficient(.99));
	}catch(Exception e){tr.showStopper(e);}
    }//end constructor

    private int lifespan(){
	return (int)(MIN_LIFESPAN_MILLIS+Math.random()*LIFESPAN_RANGE_MILLIS);
    }
    
    public void reset(Vector3D pos, Vector3D newVelocity){
	destroy();
	probeForBehavior(LimitedLifeSpan.class).reset(lifespan());
	setHeading(newVelocity.normalize());
	setPosition(pos.toArray());
	setVisible(true);
	setActive(true);
	probeForBehavior(RotationalMomentumBehavior.class)
		.setEquatorialMomentum(.2*Math.random())
		.setLateralMomentum(.2*Math.random())
		.setPolarMomentum(.2*Math.random());
	probeForBehavior(Velocible.class).setVelocity(newVelocity);
	probeForBehavior(DeathBehavior.class).reset();
    }//end reset()
}//end Debris
