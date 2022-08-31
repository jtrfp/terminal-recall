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
package org.jtrfp.trcl;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.springframework.stereotype.Component;

@Component
public class KeyStatusFactory implements FeatureFactory<TR> {
    public class KeyStatus implements KeyEventDispatcher, Feature<TR>{
	boolean [] keyStates = new boolean[256];

	public KeyStatus(){
	    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);}

	@Override
	public boolean dispatchKeyEvent(KeyEvent evt){
	    if(evt.getID()==KeyEvent.KEY_PRESSED)
	    {keyStates[evt.getKeyCode()]=true;return false;}
	    else if(evt.getID()==KeyEvent.KEY_RELEASED)
	    {keyStates[evt.getKeyCode()]=false;return false;}
	    return false;
	}

	public boolean isPressed(int index){
	    return keyStates[index];}

	public void keyTyped(KeyEvent evt){}

	public void waitForSequenceTyped(final int ... keys){
	    final boolean [] keyTypeObject 	= new boolean[]{false};
	    final KeyEventDispatcher dispatcher = new KeyEventDispatcher(){
		private volatile int keyArrayIndex = 0;
		@Override
		public synchronized boolean dispatchKeyEvent(KeyEvent evt) {//one at a time please...
		    if(evt.getID()==KeyEvent.KEY_RELEASED){
			if(evt.getKeyCode()==keys[keyArrayIndex]){
			    keyArrayIndex++;
			    if(keyArrayIndex>=keys.length){
				synchronized(keyTypeObject){
				    keyTypeObject[0]=true;
				    keyTypeObject.notifyAll();
				}//end sync(keyTypeObject)
				return false;
			    }//end if(>keys.length)
			}else keyArrayIndex=0;//!target key
		    }//end if(KEY_RELEASED)
		    return false;
		}};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
		synchronized(keyTypeObject){
		    while(!keyTypeObject[0]){
			try{keyTypeObject.wait();}
			catch(InterruptedException e){break;}
		    }//end while(!keyTypeObject)
		}//end sync(keyTypeObject)
		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
	}//end waitForKeyTyped

	@Override
	public void apply(TR target) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub

	}
    }//end KeyStatus

    @Override
    public Feature<TR> newInstance(TR target) {
	return new KeyStatus();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature<?>> getFeatureClass() {
	return KeyStatus.class;
    }
}//end KeyStatusFactory
