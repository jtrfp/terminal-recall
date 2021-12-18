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

import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.FeatureConfigurationPrivilegeData;
import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.PropertyKey;
import org.jtrfp.trcl.core.Feature;

public abstract class FeatureConfigurator<TARGET_CLASS> implements Feature<TARGET_CLASS>{
    protected abstract Set<String> getPersistentProperties();
    protected ConfigManager configManager;
    protected TARGET_CLASS target;

    @Override
    public void apply(TARGET_CLASS target) {
	setTarget(target);
    }//end apply(...)

    @Override
    public void destruct(TARGET_CLASS target) {
	// TODO Auto-generated method stub
	
    }
/*
    public ConfigManager getConfigManager() {
	if(configManager == null)
	    Features.get(Features.getSingleton(), ConfigManager.class);
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }//end destruct(...)
    */
    public Map<String,Object> storeToMap(final Map<String,Object> propertiesToStore, Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap, int privilegeLevel){
	final TARGET_CLASS target = getTarget();
	if(target == null)
	    throw new IllegalStateException("Configured object intolerably null.");
	final Class<TARGET_CLASS> targetClass = (Class<TARGET_CLASS>)target.getClass();
	TARGET_CLASS defaultBean;
	try {
	    defaultBean = targetClass.newInstance();
	} catch (InstantiationException e) {
	    e.printStackTrace();
	    return propertiesToStore;
	} catch (IllegalAccessException e) {
	    e.printStackTrace();
	    return propertiesToStore;
	}
	final Set<String> props = getPersistentProperties();
	assert props !=null:"Bad implementation: getPersistentProperties() must never be null.";
	final Map<String,Object> outputMap  = propertiesToStore;
	
	for(String propertyName:props){
	    boolean allowSave = true;
	    final FeatureConfigurationPrivilegeData fcpData = privMap.get(new PropertyKey((Class<? extends Feature>)target.getClass(), propertyName));
	    if( fcpData != null )
		allowSave &= fcpData.getPrivilegeLevel() <= privilegeLevel;
	    if(allowSave) {
		try{
		    final String camelPropertyName = propertyName.toUpperCase().substring(0, 1)+""+propertyName.substring(1);
		    Method getMethod;
		    //In case not boolean
		    try{getMethod = targetClass.getMethod("get"+camelPropertyName, null);}
		    catch(NoSuchMethodException e){//In case our property is boolean
			try{getMethod = targetClass.getMethod("is"+camelPropertyName, null);}
			catch(NoSuchMethodException ex){
			    throw new RuntimeException("Cannot find property of name `"+propertyName+"` in class "+targetClass.getName()+".",ex);
			}
		    }
		    final Object value        = getMethod.invoke(target, null);
		    final Object defaultValue = getMethod.invoke(defaultBean, null);
		    boolean performMap = false;
		    if( value != null )
			performMap = !value.equals(defaultValue);
		    else performMap = value != defaultValue;
		    if( performMap )
			outputMap.put(propertyName, value);
		}catch(InvocationTargetException e){e.printStackTrace();}
		catch(IllegalAccessException e)   {e.printStackTrace();}
	    }//end if(allSave)
	}//for(propertyNames)
	//getConfigManager().setConfiguration(targetClass,outputMap);
	return propertiesToStore;
    }//end store()

    public TARGET_CLASS applyFromMap(Map<String,Object> map, Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap, int privilegeLevel){
	final TARGET_CLASS target = getTarget();
	if(target == null)
	    throw new IllegalStateException("Configured object intolerably null. This FeatureConfigurator class: "+getClass().getName());
	final Method [] methods = target.getClass().getMethods();
	for(String propertyName:getPersistentProperties()){
	    boolean allowApply = true;
	    final FeatureConfigurationPrivilegeData fcpData = privMap.get(new PropertyKey((Class<? extends Feature>)target.getClass(), propertyName));
	    if( fcpData != null )
		allowApply &= fcpData.getPrivilegeLevel() <= privilegeLevel;
	    if(allowApply) {
		try{
		    final Object value = map.get(propertyName);
		    if(value != null){
			final String camelPropertyName = propertyName.toUpperCase().substring(0, 1)+""+propertyName.substring(1);
			final Method setMethod         = findMethodCompatibleWith(methods,"set"+camelPropertyName, value);
			if(setMethod != null)
			    setMethod.invoke(target, value);
			else
			    System.err.println("Warning: Could not find a method matching set"+camelPropertyName+" with value "+value);
		    }//end if(!null)
		}catch(InvocationTargetException e){e.printStackTrace();}
		catch(IllegalAccessException e)   {e.printStackTrace();}
	    }//end if(allowApply)
	}//end for(propertyNames)
	return getTarget();
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
	class1 = objectify(class1);
	class2 = objectify(class2);
	if(class1 == class2)
	    return true;
	if(class1.isAssignableFrom(class2))
	    return true;
	return false;
    }//end isCompatible(...)
    
    private Class objectify(Class original){
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

    public TARGET_CLASS getTarget() {
        return target;
    }

    public void setTarget(TARGET_CLASS target) {
        this.target = target;
    }
    
}//end FeatureConfigurator
