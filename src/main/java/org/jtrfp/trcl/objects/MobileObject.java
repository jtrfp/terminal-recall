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
package org.jtrfp.trcl.objects;

import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.TickListener;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.ai.ObjectBehavior;


public abstract class MobileObject extends WorldObject implements TickListener
	{
	
	private long lastTimeInMillis=-1;//Makes sure if this is read without being set, it's very obvious.
	
	private ObjectBehavior behavior;
	
	public MobileObject(Model model, ObjectBehavior behavior, World world)
		{
		super(world.getTr(),model);
		this.behavior=behavior;
		if(behavior!=null)behavior.setParent(this);
		world.addTickListener(this);
		}//end constructor()
	
	protected boolean translate(){return true;}
	
	@Override
	public void tick(long time)
		{if(lastTimeInMillis==-1)
			{//First time <3 <3 <3
			}
		else
			{//Been around...
			if(behavior!=null)behavior.tick(time);
			}
		lastTimeInMillis=time;
		}//end tick()
	
	@Override
	public void proposeCollision(WorldObject other)
		{if(this.getBehavior()!=null)this.getBehavior().proposeCollision(other);}
	
	@Override
	public String toString()
		{return "MobileObject x="+position.getX()+" y="+position.getY()+" z="+position.getZ();}

	/**
	 * @return the lastTimeInMillis
	 */
	public long getLastTimeInMillis()
		{return lastTimeInMillis;}

	/**
	 * @param lastTimeInMillis the lastTimeInMillis to set
	 */
	public void setLastTimeInMillis(long lastTimeInMillis)
		{this.lastTimeInMillis = lastTimeInMillis;}

	/**
	 * @return the behavior
	 */
	public ObjectBehavior getBehavior()
		{return behavior;}

	/**
	 * @param behavior the behavior to set
	 */
	public void setBehavior(ObjectBehavior behavior)
		{this.behavior = behavior;}
	}//end MobileObject
