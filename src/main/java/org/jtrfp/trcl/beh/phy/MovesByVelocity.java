package org.jtrfp.trcl.beh.phy;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;

public class MovesByVelocity extends Behavior implements Velocible {
	private Vector3D velocity=Vector3D.ZERO;
	@Override
	public void _tick(long tickTimeMillis){
		final WorldObject p = getParent();
		final double progressionInSeconds = (double)p.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;
		p.movePositionBy(getVelocity().scalarMultiply(progressionInSeconds));
		//p.setPosition(p.getPosition().add();
		}

	@Override
	public void setVelocity(Vector3D vel)
		{velocity=vel;}

	@Override
	public Vector3D getVelocity()
		{return velocity;}

	@Override
	public void accellerate(Vector3D accelVector)
		{velocity=velocity.add(accelVector);}
	}//end MovesWithVelocity
