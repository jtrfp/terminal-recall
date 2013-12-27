package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.obj.Propelled;
import org.jtrfp.trcl.obj.Velocible;
import org.jtrfp.trcl.obj.WorldObject;


public class AccelleratedByPropulsion extends Behavior{
	@Override
	public void _tick(long timeInMillis)
		{WorldObject wo = getParent();
		Propelled p = wo.getBehavior().probeForBehavior(Propelled.class);
		Velocible v = wo.getBehavior().probeForBehavior(Velocible.class);
		final double progressionInSeconds = (double)wo.getTr().getThreadManager().getElapsedTimeInMillisSinceLastGameTick()/1000.;
		v.accellerate(wo.getHeading().scalarMultiply(p.getPropulsion()*progressionInSeconds));
		}//end _tick(...)
	}//end AccelleratedByPropulsion
