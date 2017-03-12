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
import javax.swing.SwingUtilities;
/**
 * A bidirectional binding between an AbstractButton (JCheckbox, JRadioButton, etc) and 
 * a bean property.
 * @author Chuck Ritola
 *
 */
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
	try{
	    final Class beanClass = bindingBean.getClass();
	    final Method getterMethod = beanClass.getMethod("is"+camelPropertyName);
	    this.setterMethod = beanClass.getMethod("set"+camelPropertyName, boolean.class);
	    bindingBean.getClass().
	        getMethod("addPropertyChangeListener", String.class, PropertyChangeListener.class).
	        invoke(bindingBean, propertyName, this);
	    button.addItemListener(this);
	    //Initial setting for the button
	    final Object initialValue = getterMethod.invoke(bindingBean, null);
	    if( initialValue instanceof Boolean )
	        setSelected((boolean)initialValue);
	    }
	catch(Exception e){e.printStackTrace();throw new RuntimeException(e);}
    }//end constructor
    
    private void setSelected(final boolean newValue){
	SwingUtilities.invokeLater(new Runnable(){

	    @Override
	    public void run() {
		getButton().setSelected(newValue);
	    }});
    }//end setSelected()

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
	    if( evt.getNewValue() instanceof Boolean )
	        setSelected((boolean)evt.getNewValue());
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
