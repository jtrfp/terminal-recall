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

import org.jtrfp.trcl.TickListener;
import org.jtrfp.trcl.objects.WorldObject;

public abstract class ObjectBehavior<PARENT_TYPE extends WorldObject> 
	{
	private PARENT_TYPE parent;
	private ObjectBehavior wrapped;
	protected ObjectBehavior(ObjectBehavior<?> wrapped)
		{this.wrapped=wrapped;}
	
	public PARENT_TYPE getParent(){return parent;}
	
	protected void _proposeCollision(WorldObject other){}
	
	public final void proposeCollision(WorldObject other)
		{
		_proposeCollision(other);
		if(wrapped!=null)wrapped.proposeCollision(other);
		}
	
	protected void _tick(long tickTimeInMillis){}
	
	public final void tick(long tickTimeInMillis)
		{_tick(tickTimeInMillis);
		if(wrapped!=null)wrapped.tick(tickTimeInMillis);
		}

	public void setParent(PARENT_TYPE newParent)
		{this.parent=newParent;if(wrapped!=null){wrapped.setParent(newParent);}}
	}//end ObjectBehavior
