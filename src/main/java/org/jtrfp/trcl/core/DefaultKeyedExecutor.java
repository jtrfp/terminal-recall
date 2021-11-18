/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;

public class DefaultKeyedExecutor<K> implements KeyedExecutor<K> {
    private List<Runnable>  nullRunnables      = new ArrayList<Runnable>();
    private Map<K,Runnable> keyedRunnables     = new HashMap<K,Runnable>();
    
    private List<Runnable>  nullRunnablesSwap  = new ArrayList<Runnable>();
    private Map<K,Runnable> keyedRunnablesSwap = new HashMap<K,Runnable>();

    @Override
    public synchronized void execute(Runnable r, K key) {
	if( key != null )
	    keyedRunnables.put(key, r);
	else
	    nullRunnables.add(r);
	notifyAll();
    }//end submit(...)

    @Override
    public void execute(Runnable r) {
	execute(r, null);
    }//end submit(...)

    @Override
    public void executeAllFromThisThread() {
	final List<Runnable> nullRunnablesToUse   = nullRunnables;
	final Map<K,Runnable> keyedRunnablesToUse = keyedRunnables;
	
	synchronized(this) {
	    nullRunnables  = nullRunnablesSwap;
	    keyedRunnables = keyedRunnablesSwap;
	    
	    nullRunnablesSwap  = nullRunnablesToUse;
	    keyedRunnablesSwap = keyedRunnablesToUse;
	}
	
	//Keyed runnables first
	for(Runnable r : keyedRunnablesToUse.values())
	    r.run();
	keyedRunnablesToUse.clear();
	
	//Then null runnables
	for( Runnable r : nullRunnablesToUse )
	    r.run();
	nullRunnablesToUse.clear();
    }//end executeAllFromThisThread

    @Override
    public void executeFromThisThread(K key) {
	Runnable r;
	synchronized(this){
	    r = keyedRunnables.remove(key);
	}
	if( r != null )
	    r.run();
    }//end executeFromThisThread(...)

    @Override
    public List<Runnable> getNullRunnables() {
	return ListUtils.unmodifiableList(nullRunnables);
    }

    @Override
    public Runnable getRunnable(K key) {
	return keyedRunnables.get(key);
    }

    @Override
    public void clearAll() {
	clearAllNulls();
	clearAllKeyed();
    }

    @Override
    public void clearAllNulls() {
	nullRunnables.clear();
    }

    @Override
    public void clear(K key) {
	keyedRunnables.remove(key);
    }

    @Override
    public void clearAllKeyed() {
	keyedRunnables.clear();
    }

    @Override
    public void awaitNextRunnable() throws InterruptedException {
	synchronized(this) {
	    while( keyedRunnables.isEmpty() && nullRunnables.isEmpty() )
		wait();
	}
    }

}//end DefaultKeyedExecutor
