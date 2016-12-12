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

package org.jtrfp.trcl.core;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GroupMutex<KEY, VAL> {
  private KEY enabledGroup = null;
  private final Map<KEY, Collection<VAL>> groupMap = new HashMap<KEY, Collection<VAL>>();
  private EnablementEnforcer<VAL> enablementEnforcer;
  private boolean enforcementEnabled = true;
  
  public void addToGroup(KEY group, VAL valueToAdd){
      final Collection<VAL> groupCollection = getGroupCollection(group);
      groupCollection.add(valueToAdd);
      enforce();
  }
  
  public void removeFromGroup(KEY group, VAL valueToRemove){
      final Collection<VAL> groupCollection = getGroupCollection(group);
      if(groupCollection.remove(valueToRemove))
	  enforce();
  }
  
  protected Collection<VAL> getGroupCollection(KEY group){
      if(group == null)
	  return (Collection<VAL>)Collections.EMPTY_SET;
      if(!groupMap.containsKey(group))
	  groupMap.put(group, new HashSet<VAL>());
      Collection<VAL> result = groupMap.get(group);
      return result;
  }
  
  public void enforce(){
      if(!isEnforcementEnabled())
	  return;
      final Collection<VAL> toEnable = getGroupCollection(getEnabledGroup());
      final Set<VAL> toDisable = new HashSet<VAL>();
      for(Collection<VAL> thisGroup : groupMap.values())
	  toDisable.addAll(thisGroup);
      toDisable.removeAll(toEnable);
      final EnablementEnforcer<VAL> enablementEnforcer = getEnablementEnforcer();
      if(enablementEnforcer != null) {
	  for(VAL val : toEnable)
	      enablementEnforcer.enforceEnableOrDisable(val, true);
	  for(VAL val : toDisable)
	      enablementEnforcer.enforceEnableOrDisable(val, false);
      }//end if(!null)
  }

public KEY getEnabledGroup() {
    return enabledGroup;
}

public void setEnabledGroup(KEY enabledGroup) {
    if(enabledGroup == this.enabledGroup)
	return;
    this.enabledGroup = enabledGroup;
    enforce();
}//end enforce()

public static interface EnablementEnforcer<VAL> {
 public abstract void enforceEnableOrDisable(VAL target, boolean enable);
}//end EnablementEnforcer

public EnablementEnforcer<VAL> getEnablementEnforcer() {
    return enablementEnforcer;
}

public void setEnablementEnforcer(EnablementEnforcer<VAL> enablementEnforcer) {
    this.enablementEnforcer = enablementEnforcer;
}

public boolean isEnforcementEnabled() {
    return enforcementEnabled;
}

public void setEnforcementEnabled(boolean enforcementEnabled) {
    this.enforcementEnabled = enforcementEnabled;
}
}//end GroupMutex
