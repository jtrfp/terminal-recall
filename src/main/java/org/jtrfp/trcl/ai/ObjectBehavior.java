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

import org.jtrfp.trcl.objects.WorldObject;

public abstract class ObjectBehavior
	{
	private WorldObject parent;
	private ObjectBehavior wrapped;
	
	public WorldObject getParent(){return parent;}
	public <T> T probeForBehavior(Class<T> type)
		{if(type.isAssignableFrom(this.getClass())){return (T)this;}
		if(wrapped!=null)return wrapped.probeForBehavior(type);
		throw new BehaviorNotFoundException("Cannot find behavior of type "+type.getName()+" in behavior sandwich owned by "+parent);
		}
	
	protected void _proposeCollision(WorldObject other){}
	
	public final void proposeCollision(WorldObject other)
		{_proposeCollision(other);
		if(wrapped!=null)wrapped.proposeCollision(other);
		}
	
	protected void _tick(long tickTimeInMillis){}
	
	public final void tick(long tickTimeInMillis)
		{_tick(tickTimeInMillis);
		if(wrapped!=null)wrapped.tick(tickTimeInMillis);
		}

	public void setParent(WorldObject newParent)
		{this.parent=newParent;if(wrapped!=null){wrapped.setParent(newParent);}}
	
	public void setDelegate(ObjectBehavior delegate){wrapped=delegate;}
	}//end ObjectBehavior
