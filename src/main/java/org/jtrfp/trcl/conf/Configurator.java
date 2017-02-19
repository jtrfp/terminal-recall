/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class Configurator<T> {
    private ConfigManager configManager;
    protected abstract Set<String> getPersistentProperties();
    public abstract Class<T>    getConfiguredClass();
    private T configured;

    public ConfigManager getConfigManager() {
	return configManager;
    }
    
    //@Autowired
    public void setConfigManager(ConfigManager configManager) {
	this.configManager = configManager;
    }
    
    public T applyFromMap(Map<String,Object> map){
	final T configured = getConfigured();
	if(configured == null)
	    throw new IllegalStateException("Configured object intolerably null.");
	final Method [] methods = configured.getClass().getMethods();
	for(String propertyName:getPersistentProperties()){
	    try{
	    final Object value = map.get(propertyName);
	    if(value != null){
		final String camelPropertyName = propertyName.toUpperCase().substring(0, 1)+""+propertyName.substring(1);
		final Method setMethod         = findMethodCompatibleWith(methods,"set"+camelPropertyName, value);
		if(setMethod != null)
		 setMethod.invoke(configured, value);
		else
		 System.err.println("Warning: Could not find a method matching set"+camelPropertyName+" with value "+value);
	    }//end if(!null)
	    }catch(InvocationTargetException e){e.printStackTrace();}
	    catch(IllegalAccessException e)   {e.printStackTrace();}
	}//end for(propertyNames)
	return getConfigured();
    }//end applyFromMap()
    
    private Method findMethodCompatibleWith(Method [] methods, String name, Object argument){
	final Class<?> objClass = argument.getClass();
	for(Method m:methods){
	    if(m.getName().contentEquals(name)){
		final Class<?>[] params = m.getParameterTypes();
		if(params.length == 1){
		    if(isCompatible(params[0],objClass))
			return m;
		}//end if(1)
	    }//end if(types == 1)
	}//end for(methods)
	return null;
    }//end findMethodCompatibleWith(...)
    
    private boolean isCompatible(Class class1, Class class2){
	class1 = boxedClassOf(class1);
	class2 = boxedClassOf(class2);
	if(class1 == class2)
	    return true;
	if(class1.isAssignableFrom(class2))
	    return true;
	return false;
    }//end isCompatible(...)
    
    private static Class boxedClassOf(Class original){
	if(original == boolean.class)
	    original = Boolean.class;
	if(original == int.class)
	    original = Integer.class;
	if(original == float.class)
	    original = Float.class;
	if(original == double.class)
	    original = Double.class;
	if(original == long.class)
	    original = Long.class;
	if(original == byte.class)
	    original = Byte.class;
	return original;
    }//end objectify(...)
    
    public Map<String,Object> storeToMap(Map<String,Object> dest){
	final T configured = getConfigured();
	if(configured == null)
	    throw new IllegalStateException("Configured object intolerably null.");
	final Class<T> configuredClass = getConfiguredClass();
	T defaultBean;
	try {
	    defaultBean = configuredClass.newInstance();
	} catch (InstantiationException e) {
	    e.printStackTrace();
	    return dest;
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    return dest;
	}
	final Set<String> props = getPersistentProperties();
	assert props !=null:"Bad implementation: getPersistentProperties() must never be null.";
	final Map<String,Object> outputMap  = dest;
	
	for(String propertyName:props){
	    try{
	    final String camelPropertyName = propertyName.toUpperCase().substring(0, 1)+""+propertyName.substring(1);
	    Method getMethod;
	                                   //In case not boolean
	    try{getMethod = configuredClass.getMethod("get"+camelPropertyName, null);}
	    catch(NoSuchMethodException e){//In case our property is boolean
		getMethod = configuredClass.getMethod("is"+camelPropertyName, null);
	    }
	    final Object value        = getMethod.invoke(configured, null);
	    final Object defaultValue = getMethod.invoke(defaultBean, null);
	    boolean performMap = false;
	    if( value != null )
		performMap = !value.equals(defaultValue);
	    else performMap = value != defaultValue;
	    if( performMap )
		outputMap.put(propertyName, value);
	    }catch(NoSuchMethodException e)   {e.printStackTrace();}
	    catch(InvocationTargetException e){e.printStackTrace();}
	    catch(IllegalAccessException e)   {e.printStackTrace();}
	}//for(propertyNames)
	getConfigManager().setConfiguration(configuredClass,outputMap);
	return dest;
    }//end store()
    
    public T getConfigured() {
        return configured;
    }
    //@Autowired
    public void setConfigured(T configured) {
        this.configured = configured;
    }//end store()
}//end Configurable
