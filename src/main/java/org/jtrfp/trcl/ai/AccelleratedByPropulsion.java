package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.Propelled;
import org.jtrfp.trcl.objects.Velocible;
import org.jtrfp.trcl.objects.WorldObject;


public class AccelleratedByPropulsion extends ObjectBehavior
	{
	
	@Override
	public void _tick(long timeInMillis)
		{WorldObject wo = getParent();
		Propelled p = wo.getBehavior().probeForBehavior(Propelled.class);
		Velocible v = wo.getBehavior().probeForBehavior(Velocible.class);
		v.accellerate(wo.getHeading().scalarMultiply(p.getPropulsion()));
		}
	}//end AccelleratedByPropulsion
