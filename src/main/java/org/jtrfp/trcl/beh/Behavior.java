/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.beh;

import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.obj.WorldObject;

public abstract class Behavior{
	private volatile WorldObject parent;
	private volatile boolean     enabled=true;
	
	public WorldObject getParent(){return parent;}
	public <T> T probeForBehavior(Class<T> type){
	    	return parent.probeForBehavior(type);}
	
	protected void tick(long tickTimeInMillis){}
	
	public final void proposeTick(long tickTimeInMillis){
	    if(enabled) tick(tickTimeInMillis);}

	public void setParent(WorldObject newParent){
	    this.parent=newParent;}
	
	public <T> void probeForBehaviors(Submitter<T> sub, Class<T> type) {
	    	parent.probeForBehaviors(sub, type);}
	
	public Behavior setEnable(boolean proposedState){
	    if(parent!=null){
		if(!proposedState&&enabled)parent.disableBehavior(this);
	    	if(proposedState&&!enabled)parent.enableBehavior(this);}
	    enabled=proposedState;
	    return this;}
	public boolean isEnabled(){return enabled;}
	}//end Behavior
