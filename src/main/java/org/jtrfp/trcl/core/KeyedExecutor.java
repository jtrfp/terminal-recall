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

import java.util.List;
import java.util.concurrent.Executor;

public interface KeyedExecutor<K> extends Executor {
    public void execute(Runnable r, K key);
    public void execute(Runnable r);
    
    public void executeAllFromThisThread();
    public void executeFromThisThread(K key);
    public List<Runnable> getNullRunnables();
    public Runnable getRunnable(K key);
    public void clearAll();
    public void clearAllNulls();
    public void clear(K key);
    public void clearAllKeyed();
    public void awaitNextRunnable() throws InterruptedException;
}//end KeyedExecutor
