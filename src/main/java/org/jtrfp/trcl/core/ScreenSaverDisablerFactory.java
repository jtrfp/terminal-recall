/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola and contributors.
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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.springframework.stereotype.Component;

@Component
public class ScreenSaverDisablerFactory implements FeatureFactory<Features> {

    @Override
    public Feature<Features> newInstance(Features target) {
	return new ScreenSaverDisabler();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ScreenSaverDisabler.class;
    }
    
    private class ScreenSaverDisabler implements Feature<Features>{
	private volatile boolean running = true;
	
	/**
	 * Adapted from <a href='http://willcode4beer.com/tips.jsp?set=disable_screensaver'>This page</a>
	 */
	private class RobotThread extends Thread{
	    @Override
	    public void run(){
		try{
		    this.setName("SuspendScreenSaver");
		  final Robot robot = new Robot();
		  while (running) {
		    Thread.sleep(60 * 1000);
		    robot.keyPress(KeyEvent.VK_SHIFT);
		    Thread.sleep(50);
		    robot.keyRelease(KeyEvent.VK_SHIFT);
		  }
		}catch(InterruptedException e){}
		catch(AWTException e){e.printStackTrace();}
	    }//end run()
	}//end RobotThread
	
	private RobotThread robotThread;

	@Override
	public void apply(Features target) {
	    running = true;
	    robotThread = new RobotThread();
	    robotThread.start();
	}

	@Override
	public void destruct(Features target) {
	    running = false;
	    robotThread.interrupt();
	}
    }//ScreenSaverDisabler
}//end ScreenSaverDisablerFactory
