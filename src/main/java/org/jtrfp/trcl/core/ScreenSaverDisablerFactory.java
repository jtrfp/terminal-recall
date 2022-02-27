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

package org.jtrfp.trcl.miss;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.springframework.stereotype.Component;

@Component
public class SuspendScreenSaverFactory implements FeatureFactory<Mission> {

    @Override
    public Feature<Mission> newInstance(Mission target) {
	return new SuspendScreenSaver();
    }

    @Override
    public Class<Mission> getTargetClass() {
	return Mission.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SuspendScreenSaver.class;
    }
    
    private class SuspendScreenSaver implements Feature<Mission>{
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
	public void apply(Mission target) {
	    running = true;
	    robotThread = new RobotThread();
	    robotThread.start();
	}

	@Override
	public void destruct(Mission target) {
	    running = false;
	    robotThread.interrupt();
	}
    }//SuspendScreenSaver
}//end SuspendScreenSaverFactory
