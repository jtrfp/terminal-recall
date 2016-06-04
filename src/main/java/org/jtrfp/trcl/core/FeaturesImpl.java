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

package org.jtrfp.trcl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;

public class FeaturesImpl {
 // Key represents target-class
       final Map<Object,Map<Class<? extends Feature>,Feature>> targetMap 
    	= new ReferenceMap<Object,Map<Class<? extends Feature>,Feature>>(ReferenceStrength.WEAK, ReferenceStrength.HARD, true);
       final HashMap<Class<?>,Collection<FeatureFactory>> featureFactoriesByTargetClass          = new HashMap<Class<?>,Collection<FeatureFactory>>();
       final HashMap<Class<?>,FeatureFactory> featureFactoriesByFeature = new HashMap<Class<?>,FeatureFactory>();
    
      void registerFeature(FeatureFactory<?> factory){
	Collection<FeatureFactory> factoryCollection = getFactoryCollection(factory.getTargetClass());
	factoryCollection.add(factory);
	registerFeatureRecursive(factory.getFeatureClass(),factory);
	//featureFactoriesByFeature.put(factory.getFeatureClass(),factory);
    }
    
       void registerFeatureRecursive(Class featureClass, FeatureFactory factory){
	if(featureClass == Object.class || featureClass == null)
	    return;
	featureFactoriesByFeature.put(featureClass,factory);
	for(Class iface:featureClass.getInterfaces())
	    registerFeatureRecursive(iface,factory);
	registerFeatureRecursive(featureClass.getSuperclass(), factory);
    }
    
       Collection<FeatureFactory> getFactoryCollection(Class targetClass){
	Collection<FeatureFactory> result = featureFactoriesByTargetClass.get(targetClass);
	if(result==null)
	    featureFactoriesByTargetClass.put(targetClass, result = new ArrayList<FeatureFactory>());
	return result;
    }
    
    public  void init(Object obj){
	//Traverse the type hierarchy
	Class tClass = obj.getClass();
	//For its interfaces
	for(Class iFace:tClass.getInterfaces())
	    for(FeatureFactory ff:getFactoryCollection(iFace))
		get(obj,ff.getFeatureClass());
	while(tClass!=Object.class){
	    //First for the class itself
	    for(FeatureFactory ff:getFactoryCollection(tClass))
		    get(obj,ff.getFeatureClass());
	    tClass=tClass.getSuperclass();
	}//end while(hierarchy)
    }//end init(...)
    
    public  void destruct(Object obj){
	for(Feature f : targetMap.get(obj).values())
	    if(f!=null)
	     f.destruct(obj);
	targetMap.remove(obj);
    }//end destruct()
    
       Map<Class<? extends Feature>,Feature> getFeatureMap(Object targ){
	Map<Class<? extends Feature>,Feature> result = targetMap.get(targ);
	if(result==null)
	    targetMap.put(targ, result = new HashMap<Class<? extends Feature>,Feature>());
	assert result!=null;
	return result;
    }//end getFeatureMap()
    
       Feature getFeature(Map<Class<? extends Feature>,Feature> map, Class<? extends Feature> featureClass, Object target){
	Feature result = map.get(featureClass);
	if(result==null){
	    final FeatureFactory ff = featureFactoriesByFeature.get(featureClass);
	    assert ff!=null:""+featureClass.getName();
	    map.put(featureClass, result = ff.newInstance(target));
	    result.apply(target);
	    }
	return result;
    }//end getFeature()

    public  <T> T get(Object target, Class<T> featureClass){
     final Map<Class<? extends Feature>,Feature> fMap = getFeatureMap(target);
     return (T)getFeature(fMap,(Class<Feature>)featureClass,target);
    }//end get(...)
}//end FeaturesImpl
