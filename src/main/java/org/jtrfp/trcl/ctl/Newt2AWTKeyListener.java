/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.ctl;

import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.JLabel;

import com.jogamp.newt.event.KeyListener;

public class Newt2AWTKeyListener implements KeyListener {

    private static final Map<Short,Integer> newt2AWT = new HashMap<>();
    static {
	for(Field awtField : java.awt.event.KeyEvent.class.getFields()) {
	    final int modifiers = awtField.getModifiers();
	    if( Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers)) {
		final String name = awtField.getName();
		if( name.startsWith("VK_") ) {
		    //Find a field with that name on the NEWT end of things.
		    Optional<Field> found = 
			    Stream.of(com.jogamp.newt.event.KeyEvent.class.getFields()).filter(x->x.getName().contentEquals(name)).findAny();
		    if(found.isPresent()){
			final Field newtField = found.get();
			try {
			    final short newtValue  = newtField.getShort(null);
			    final Integer awtValue = awtField .getInt(awtField);
			    newt2AWT.put(newtValue, awtValue);
			} catch(IllegalAccessException e) {}
		    }//end if(found)
		}//end VK_
	    }//end if(modifiers)
	}//end for(fields)
    }//end static

    private final Component targetComponent;
    private final JLabel dummy = new JLabel();

    public Newt2AWTKeyListener(Component target) {
	this.targetComponent = target;
    }

    @Override
    public void keyPressed(com.jogamp.newt.event.KeyEvent newt) {
	if(newt.isAutoRepeat())
	    return;
	final java.awt.event.KeyEvent awt = new java.awt.event.KeyEvent(
		dummy,
		java.awt.event.KeyEvent.KEY_PRESSED,
		newt.getWhen(),
		newt.getModifiers(),
		newt2AWT.get(newt.getKeyCode()),
		newt.getKeyChar());
	targetComponent.dispatchEvent(awt);
    }//end keyPressed(...)

    @Override
    public void keyReleased(com.jogamp.newt.event.KeyEvent newt) {
	if(newt.isAutoRepeat())
	    return;
	final java.awt.event.KeyEvent awt = new java.awt.event.KeyEvent(
		dummy,
		java.awt.event.KeyEvent.KEY_RELEASED,
		newt.getWhen(),
		newt.getModifiers(),
		newt2AWT.get(newt.getKeyCode()),
		newt.getKeyChar());
	targetComponent.dispatchEvent(awt);
    }//end keyReleased(...)
}//end New2AWTKeyListener