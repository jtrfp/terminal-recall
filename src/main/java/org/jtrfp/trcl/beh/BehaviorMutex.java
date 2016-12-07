/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import org.jtrfp.trcl.core.GroupMutex;
import org.jtrfp.trcl.core.GroupMutex.EnablementEnforcer;

public class BehaviorMutex<KEY> extends Behavior {
    private final GroupMutex<KEY, Behavior> groupMutex = new GroupMutex<KEY, Behavior>();
    
    public BehaviorMutex(){
	super();
	groupMutex.setEnablementEnforcer(new BehaviorEnablementEnforcer());
    }
    
    private class BehaviorEnablementEnforcer implements EnablementEnforcer<Behavior> {
	@Override
	public void enforceEnableOrDisable(Behavior target, boolean enable) {
	    if(target.isEnabled() != enable)
		target.setEnable(enable);
	}
    }//end BehaviorEnablementEnforcer
    
    /**
     * Performs nothing if Behavior is disabled
     * 
     * @since Nov 22, 2016
     */
    public void enforce(){
	groupMutex.enforce();
    }
    
    /**
     * When re-enabled, the mutex state will be immediately enforced.
     */
    @Override
    public Behavior setEnable(boolean enable){ 
	groupMutex.setEnforcementEnabled(enable);
	return super.setEnable(enable);
    }

 public void addToMutexGroup     (KEY group, Behavior behaviorToAdd){
     groupMutex.addToGroup(group, behaviorToAdd);
 }
 public void removeFromMutexGroup(KEY group, Behavior behaviorToRemove){
     groupMutex.removeFromGroup(group, behaviorToRemove);
 }
 
 public void setEnabledGroup(KEY group){
     groupMutex.setEnabledGroup(group);
 }

public KEY getEnabledGroup() {
    return groupMutex.getEnabledGroup();
}
}//end BehaviorMutex
