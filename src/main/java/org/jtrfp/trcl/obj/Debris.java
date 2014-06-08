package org.jtrfp.trcl.obj;

import java.util.Arrays;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
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
import org.jtrfp.trcl.beh.phy.VelocityDragBehavior;
import org.jtrfp.trcl.core.TR;
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
    public Debris(TR tr) {
	super(tr);
	try{
	Model m = tr.getResourceManager().getBINModel(TYPES[(int)(Math.random()*TYPES.length)], tr.getGlobalPalette(), tr.gpu.get().getGl());
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
	}catch(Exception e){e.printStackTrace();}
    }//end constructor

    private int lifespan(){
	return (int)(MIN_LIFESPAN_MILLIS+Math.random()*LIFESPAN_RANGE_MILLIS);
    }
    
    public void reset(double[] ds, Vector3D newVelocity){
	getBehavior().probeForBehavior(LimitedLifeSpan.class).reset(lifespan());
	setHeading(newVelocity.normalize());
	setPosition(Arrays.copyOf(ds,3));
	setVisible(true);
	setActive(true);
	getBehavior().probeForBehavior(RotationalMomentumBehavior.class)
		.setEquatorialMomentum(.2*Math.random())
		.setLateralMomentum(.2*Math.random())
		.setPolarMomentum(.2*Math.random());
	getBehavior().probeForBehavior(Velocible.class).setVelocity(newVelocity);
	getBehavior().probeForBehavior(DeathBehavior.class).reset();
    }//end reset()
}//end Debris
