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

package org.jtrfp.trcl.flow;

import org.jtrfp.trcl.core.DefaultKeyedExecutor;
import org.jtrfp.trcl.core.KeyedExecutor;

public class TransientExecutor {
    public static final int TRANSIENT_THREAD_PRIORITY = 
	    Thread.MIN_PRIORITY + (Thread.MAX_PRIORITY - Thread.MIN_PRIORITY) / 3;
 private static KeyedExecutor<?> singleton;
 private static Thread transientThread;
 
 static {
     transientThread = new TransientThread();
     singleton       = new DefaultKeyedExecutor<>();
     transientThread.start();
 }
 
 public static KeyedExecutor<?> getSingleton(){
     return singleton;
 }
 
 private static class TransientThread extends Thread {
     @Override
     public void run() {
	 setName("Transient Thread");
	 setPriority(TRANSIENT_THREAD_PRIORITY);
	 boolean running = true;
	 try{
	     while(running) { //Currently has no escape other than InterruptedException
		 singleton.executeAllFromThisThread();
		 singleton.awaitNextRunnable();
	     }//end while(running)
	 }//end try{}
     catch(InterruptedException e){e.printStackTrace();}
     }//end run()
 }//end TransientThread
}//end TransientExecutor
