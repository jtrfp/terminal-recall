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

import static org.junit.Assert.*;

import org.jtrfp.trcl.core.GroupMutex.EnablementEnforcer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GroupMutexTest {
    private GroupMutex<String, String> subject;
    
    private static final String [] GROUPS = new String [] {"1","2","3"};
    private static final String [] VALUES = new String [] {"A","B","C","D"};
    
    @Before
    public void setUp(){
	
    }

    @Test
    public void testAddToGroup() {
	new GroupMutex().addToGroup(GROUPS[0], VALUES[0]);
    }

    @Test
    public void testRemoveFromGroup() {
	new GroupMutex().removeFromGroup(GROUPS[1], VALUES[1]);
    }

    @Test
    public void testEnforce() {
	getSubject().enforce();
    }
    
    @Test
    public void testEnforceEmptyWithResults(){
	final GroupMutex<String,String> subject = getSubject();
	final EnablementEnforcer enablementEnforcer = Mockito.mock(EnablementEnforcer.class);
	subject.setEnablementEnforcer(enablementEnforcer);
	subject.enforce();
	for(int i = 0; i < VALUES.length; i++)
	 Mockito.verify(enablementEnforcer).enforceEnableOrDisable(VALUES[i],false);
    }
    
    @Test
    public void testEnforceGroupWithResults(){
	final GroupMutex<String,String> subject = getSubject();
	final EnablementEnforcer enablementEnforcer = Mockito.mock(EnablementEnforcer.class);
	subject.setEnablementEnforcer(enablementEnforcer);
	subject.setEnabledGroup(GROUPS[0]);
	Mockito.verify(enablementEnforcer).enforceEnableOrDisable(VALUES[0],true);
	Mockito.verify(enablementEnforcer).enforceEnableOrDisable(VALUES[1],true);
	Mockito.verify(enablementEnforcer).enforceEnableOrDisable(VALUES[2],false);
	Mockito.verify(enablementEnforcer).enforceEnableOrDisable(VALUES[3],false);
    }
    
    @Test
    public void testEnforceNewGroupWithResults(){
	final GroupMutex<String,String> subject = getSubject();
	final EnablementEnforcer enablementEnforcer = Mockito.mock(EnablementEnforcer.class);
	subject.setEnablementEnforcer(enablementEnforcer);
	subject.setEnabledGroup(GROUPS[0]);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[0],true);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[0],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[1],true);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[1],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[2],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[2],true);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[3],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[3],true);
	subject.setEnabledGroup(GROUPS[1]);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[0],true);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[0],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[1],true);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[1],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[2],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[2],true);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[3],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[3],true);
	subject.setEnabledGroup(GROUPS[2]);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[0],true);
	Mockito.verify(enablementEnforcer,Mockito.times(2)).enforceEnableOrDisable(VALUES[0],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[1],true);
	Mockito.verify(enablementEnforcer,Mockito.times(2)).enforceEnableOrDisable(VALUES[1],false);
	Mockito.verify(enablementEnforcer,Mockito.times(2)).enforceEnableOrDisable(VALUES[2],false);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[2],true);
	Mockito.verify(enablementEnforcer,Mockito.times(1)).enforceEnableOrDisable(VALUES[3],false);
	Mockito.verify(enablementEnforcer,Mockito.times(2)).enforceEnableOrDisable(VALUES[3],true);
    }
    
    @Test
    public void testEnforcementOffGroupWithResults(){
	final GroupMutex<String,String> subject = getSubject();
	final EnablementEnforcer enablementEnforcer = Mockito.mock(EnablementEnforcer.class);
	subject.setEnablementEnforcer(enablementEnforcer);
	subject.setEnforcementEnabled(false);
	subject.setEnabledGroup(GROUPS[0]);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[0],true);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[0],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[1],true);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[1],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[2],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[2],true);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[3],false);
	Mockito.verify(enablementEnforcer,Mockito.times(0)).enforceEnableOrDisable(VALUES[3],true);
    }
    
    @Test
    public void testGetEnabledGroup() {
	assertNull(getSubject().getEnabledGroup());
    }

    @Test
    public void testSetEnabledGroup() {
	getSubject().setEnabledGroup(GROUPS[0]);
	assertEquals(GROUPS[0], getSubject().getEnabledGroup());
    }

    @Test
    public void testGetEnablementEnforcer() {
	assertNull(getSubject().getEnablementEnforcer());
    }

    @Test
    public void testSetEnablementEnforcer() {
	getSubject().setEnablementEnforcer(Mockito.mock(EnablementEnforcer.class));
    }

    @Test
    public void testIsEnforcementEnabled() {
	assertTrue(getSubject().isEnforcementEnabled());
    }

    @Test
    public void testSetEnforcementEnabled() {
	getSubject().setEnforcementEnabled(false);
	assertFalse(getSubject().isEnforcementEnabled());
    }

    public GroupMutex getSubject() {
	if(subject == null){
	    final GroupMutex<String,String> groupMutex;
	    setSubject(groupMutex = new GroupMutex<String, String>());

	    groupMutex.addToGroup(GROUPS[0], VALUES[0]);
	    groupMutex.addToGroup(GROUPS[0], VALUES[1]);

	    groupMutex.addToGroup(GROUPS[1], VALUES[2]);
	    groupMutex.addToGroup(GROUPS[1], VALUES[3]);

	    groupMutex.addToGroup(GROUPS[2], VALUES[3]);
	    
	    subject = groupMutex;
	}
	return subject;
    }

    public void setSubject(GroupMutex subject) {
        this.subject = subject;
    }

}//end GroupMutexTest
