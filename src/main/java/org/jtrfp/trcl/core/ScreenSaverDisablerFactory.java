/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2022 Chuck Ritola and contributors.
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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jtrfp.trcl.conf.ui.CheckboxUI;
import org.jtrfp.trcl.conf.ui.ComboBoxUI;
import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.conf.ui.IntegerFormat;
import org.jtrfp.trcl.conf.ui.IntegerSpinnerUI;
import org.springframework.stereotype.Component;

@Component
public class ScreenSaverDisablerFactory implements FeatureFactory<Features> {
    //private static final Map<String, Integer> KEY_EVENTS;
    private static final DefaultMutableTreeNode KEY_EVENT_ROOT;
    private static final DefaultMutableTreeNode KEY_EVENT_DEFAULT;
    static {
	final int MODS = Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC;
	final HashMap<String, Integer> result = new HashMap<>();
	final Field [] fields = KeyEvent.class.getFields();

	KEY_EVENT_ROOT = new DefaultMutableTreeNode();
	DefaultMutableTreeNode defaultKey = null;

	for(Field field : fields) {
	    if(((field.getModifiers() & MODS) != 0 && field.getName().startsWith("VK_"))) {
		try {
		    final int val = field.getInt(null);
		    final String key = field.getName().substring(3);
		    result.put(key, val);
		    final DefaultMutableTreeNode node = new DefaultMutableTreeNode(key); 
		    KEY_EVENT_ROOT.add(node);
		    if( key.contentEquals("SHIFT"))
			defaultKey = node;
		} catch(IllegalAccessException e) {e.printStackTrace();}
	    }//end if(matches)
	}//end for(fields)
	KEY_EVENT_DEFAULT = defaultKey;
	//KEY_EVENTS = Collections.unmodifiableMap(result);
    }//end static

    @Override
    public Feature<Features> newInstance(Features target) {
	return new ScreenSaverDisabler();
    }

    @Override
    public Class<Features> getTargetClass() {
	return Features.class;
    }

    @Override
    public Class<? extends Feature<Features>> getFeatureClass() {
	return ScreenSaverDisabler.class;
    }
    
    public static class ScreenSaverDisabler implements Feature<Features>{
	private volatile boolean running = true;
	private volatile int keyIntervalInSeconds = 60;
	private volatile DefaultMutableTreeNode keyEventToUse = KEY_EVENT_DEFAULT;
	private volatile boolean enabled = true;
	
	public PackedTreeNode getPackedKeyEventToUse() {
	    PackedTreeNode result = new PackedTreeNode();
	    result.setNode(keyEventToUse);
	    result.setRoot((DefaultMutableTreeNode)keyEventToUse.getRoot());
	    return result;
	}//end getPackedKeyEventToUse(...)
	
	public void setPackedKeyEventToUse(PackedTreeNode newEvent) {
	    if( newEvent == null )
		keyEventToUse = KEY_EVENT_DEFAULT;
	    else
		keyEventToUse = newEvent.getNode();
	}//end setPakedKeyEventToUse(...)
	
	/**
	 * Adapted from <a href='http://willcode4beer.com/tips.jsp?set=disable_screensaver'>This page</a>
	 */
	private class RobotThread extends Thread{
	    @Override
	    public void run(){
		try{
		    this.setName("ScreenSaverDisabler");
		  final Robot robot = new Robot();
		  while (running) {
		      Thread.sleep(getKeyIntervalInSeconds()*1000);
		      if(isEnabled()) {
			  robot.keyPress(KeyEvent.VK_SHIFT);
			  Thread.sleep(5);
			  robot.keyRelease(KeyEvent.VK_SHIFT);
		      }//end if(enabled)
		  }//end while(running)
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

	@ConfigByUI(editorClass = IntegerSpinnerUI.class)
	@IntegerFormat(min=30,max=3600,stepSize=30)
	public int getKeyIntervalInSeconds() {
	    return keyIntervalInSeconds;
	}

	public void setKeyIntervalInSeconds(int keyIntervalInSeconds) {
	    this.keyIntervalInSeconds = keyIntervalInSeconds;
	}

	@ConfigByUI(editorClass = CheckboxUI.class)
	public boolean isEnabled() {
	    return enabled;
	}

	public void setEnabled(boolean enabled) {
	    this.enabled = enabled;
	}

	@ConfigByUI(editorClass = ComboBoxUI.class)
	public DefaultMutableTreeNode getKeyEventToUse() {
	    return keyEventToUse;
	}

	public void setKeyEventToUse(DefaultMutableTreeNode keyEventToUse) {
	    this.keyEventToUse = keyEventToUse;
	}
	
	public PackedTreeNode getKeyEventToUsePacked() {
	    final PackedTreeNode result = new PackedTreeNode();
	    result.setNode(keyEventToUse);
	    result.setRoot((DefaultMutableTreeNode)(keyEventToUse.getRoot()));
	    return result;
	}
	
	public void setKeyEventToUsePacked(PackedTreeNode node) {
	    keyEventToUse = node.getNode();
	}
    }//ScreenSaverDisabler
}//end ScreenSaverDisablerFactory
