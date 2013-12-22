package org.jtrfp.trcl.ai;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.objects.Velocible;
import org.jtrfp.trcl.objects.WorldObject;

public class MovesByVelocity extends ObjectBehavior implements Velocible {
	private Vector3D velocity=Vector3D.ZERO;
	@Override
	public void _tick(long tickTimeMillis){
		final WorldObject p = getParent();
		p.setPosition(p.getPosition().add(getVelocity()));
		}

	@Override
	public void setVelocity(Vector3D vel)
		{velocity=vel;}

	@Override
	public Vector3D getVelocity()
		{return velocity;}

	@Override
	public void accellerate(Vector3D scalarMultiply)
		{velocity=velocity.add(scalarMultiply);}
	}//end MovesWithVelocity
