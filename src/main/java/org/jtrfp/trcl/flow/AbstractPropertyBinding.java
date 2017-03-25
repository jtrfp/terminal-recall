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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

public abstract class AbstractPropertyBinding<PROPERTY_TYPE> implements PropertyChangeListener {
    private final Method setterMethod, getterMethod;
    private final String propertyName;
    private final Object bindingBean;
    private final Class<? extends PROPERTY_TYPE> propertyType;
    private volatile boolean isModifying = false;
    private Executor executor;
    
    protected AbstractPropertyBinding(String propertyName, Object bindingBean, Class<PROPERTY_TYPE> propertyType) {
	this.propertyName = propertyName;
	this.bindingBean  = bindingBean;
	this.propertyType = propertyType;
	try{
	    final char firstChar = propertyName.charAt(0);
	    final String camelPropertyName = Character.toUpperCase(firstChar)+propertyName.substring(1);
	    final Class beanClass = bindingBean.getClass();
	    final String prefix = (propertyType == Boolean.class || propertyType == boolean.class)?"is":"get";
	    this.getterMethod = beanClass.getMethod(prefix+""+camelPropertyName);
	    this.setterMethod = beanClass.getMethod("set"+camelPropertyName, propertyType);
	    bindingBean.getClass().
	        getMethod("addPropertyChangeListener", String.class, PropertyChangeListener.class).
	        invoke(bindingBean, propertyName, this);
	    //Initial setting for the button
	    final Object initialValue = getterMethod.invoke(bindingBean, null);
	    if( initialValue != null)
	        if( propertyType.isAssignableFrom(initialValue.getClass()) )
	            setUIValue(getPropertyValue());
	    }//end try{}
	catch(Exception e){e.printStackTrace();throw new RuntimeException(e);}
    }//end constructor
    
    @Override
    public void propertyChange(PropertyChangeEvent event){
	final Class<? extends PROPERTY_TYPE> type = getPropertyType();
	final Object newValue = event.getNewValue();
	if( type.isAssignableFrom(newValue.getClass()) ){
	    final PROPERTY_TYPE newValuePT = (PROPERTY_TYPE)newValue;
	    if(!isModifying){
		isModifying = true;
		setUIValue(newValuePT);
		isModifying = false;
	    }//end if(!modifying)
	}//end if(correct class)
    }//end propertyChanged(...)
    
    protected PROPERTY_TYPE getPropertyValue(){
	try{return (PROPERTY_TYPE)getGetterMethod().invoke(getBindingBean(), null);}
	catch(Exception e) {e.printStackTrace();return null;}
    }
    
    protected void setPropertyValue(final PROPERTY_TYPE newValue){
	if(isModifying)
	    return;
	isModifying = true;
	final Executor executor = getExecutor();
	if(executor != null){
	    executor.execute(new Runnable(){
		@Override
		public void run() {
		    setPropertyValueUnsafe(newValue);
		}});
	}else
	    setPropertyValueUnsafe(newValue);
	isModifying = false;
    }//end setPropertyValue(...)

    private void setPropertyValueUnsafe(PROPERTY_TYPE newValue){
	if( executor != null )
	    try{getSetterMethod().invoke(getBindingBean(), newValue);}
	catch(Exception e){e.printStackTrace();}
    }
    
    protected void notifyUIValueChange(PROPERTY_TYPE newValue){
	setPropertyValue(newValue);
    }
    
    protected abstract void setUIValue(PROPERTY_TYPE newValue);

    public String getPropertyName() {
        return propertyName;
    }

    protected Method getSetterMethod() {
        return setterMethod;
    }

    public Object getBindingBean() {
        return bindingBean;
    }

    public Class<? extends PROPERTY_TYPE> getPropertyType() {
        return propertyType;
    }

    public Method getGetterMethod() {
        return getterMethod;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

}//end AbstractPropertyBinding
