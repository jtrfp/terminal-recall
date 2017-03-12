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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;

public class CheckboxPropertyBinding implements ItemListener, PropertyChangeListener {
    private final Method setterMethod;
    private final String propertyName;
    private final AbstractButton button;
    private final Object bindingBean;
    
    public CheckboxPropertyBinding(AbstractButton button, Object bindingBean, String propertyName){
	final char firstChar = propertyName.charAt(0);
	final String camelPropertyName = Character.toUpperCase(firstChar)+propertyName.substring(1);
	this.propertyName = propertyName;
	this.button       = button;
	this.bindingBean  = bindingBean;
	try{this.setterMethod = bindingBean.getClass().getMethod("set"+camelPropertyName, boolean.class);}
	catch(Exception e){throw new RuntimeException(e);}
	
    }//end constructor

    @Override
    public void itemStateChanged(ItemEvent evt) {
	final int stateChange = evt.getStateChange();
	try{switch(stateChange){
	    case ItemEvent.SELECTED:
		getSetterMethod().invoke(getBindingBean(), true);
		break;
	    case ItemEvent.DESELECTED:
		getSetterMethod().invoke(getBindingBean(), false);
		break;
	    default://Do nothing
	    }
	}catch(Exception e){throw new RuntimeException(e);}
    }//end itemStateChanged(...)

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final String evtPropertyName = evt.getPropertyName();
	if( evtPropertyName.equals(getPropertyName()) )
	    getButton().setSelected(evt.getNewValue() == Boolean.TRUE);
    }

    public String getPropertyName() {
        return propertyName;
    }

    protected Method getSetterMethod() {
        return setterMethod;
    }

    public AbstractButton getButton() {
        return button;
    }

    public Object getBindingBean() {
        return bindingBean;
    }

}//end CheckboxPropertyBinding
