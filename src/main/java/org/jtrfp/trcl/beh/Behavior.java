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
package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class Behavior{
	private WorldObject parent;
	private boolean enable=true;
	
	public WorldObject getParent(){return parent;}
	public <T> T probeForBehavior(Class<T> type){
	    	return parent.probeForBehavior(type);}
	
	protected void _tick(long tickTimeInMillis){}
	
	public final void tick(long tickTimeInMillis){
	    if(enable)_tick(tickTimeInMillis);}

	public void setParent(WorldObject newParent){
	    this.parent=newParent;}
	
	public <T> void probeForBehaviors(Submitter<T> sub, Class<T> type) {
	    	parent.probeForBehaviors(sub, type);}
	
	public Behavior setEnable(boolean doIt){
	    if(parent!=null){
		if(!doIt&&enable)parent.disableBehavior(this);
	    	if(doIt&&!enable)parent.enableBehavior(this);}
	    enable=doIt;
	    return this;}
	public boolean isEnabled(){return enable;}
	}//end ObjectBehavior
