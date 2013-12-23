/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.objects.CollisionManager;
import org.jtrfp.trcl.objects.Damageable;
import org.jtrfp.trcl.objects.Player;
import org.jtrfp.trcl.objects.WorldObject;

public class DamagedByCollisionWithGameplayObject extends Behavior
	{
	protected void _proposeCollision(WorldObject other)
		{
		if(other.getPosition().distance(getParent().getPosition())<CollisionManager.SHIP_COLLISION_DISTANCE)
			{if(other instanceof Player)
				{getParent().getBehavior().probeForBehavior(Damageable.class).damage(65535/10);}
			}//end if(nearby)
		}//end proposeCollision
	}//end TVBehavior
