/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Features {
    // Key represents target-class
    private static final Map<Object,Map<Class<? extends Feature>,Feature>> targetMap = new HashMap<Object,Map<Class<? extends Feature>,Feature>>();
    private static final HashMap<Class<?>,Collection<FeatureFactory>> featureFactoriesByTargetClass          = new HashMap<Class<?>,Collection<FeatureFactory>>();
    private static final HashMap<Class<?>,FeatureFactory> featureFactoriesByFeature = new HashMap<Class<?>,FeatureFactory>();

    
    public Features(){
	this(Collections.EMPTY_LIST);
    }
    
    @Autowired(required=false)
    public Features(Collection<FeatureFactory> features){
	for(FeatureFactory ff:features)
	    registerFeature(ff);
    }

    private static void registerFeature(FeatureFactory<?> factory){
	System.out.println("Registering feature factory: "+factory.getClass().getName());
	Collection<FeatureFactory> factoryCollection = getFactoryCollection(factory.getTargetClass());
	factoryCollection.add(factory);
	featureFactoriesByFeature.put(factory.getFeatureClass(),factory);
    }
    
    private static Collection<FeatureFactory> getFactoryCollection(Class targetClass){
	Collection<FeatureFactory> result = featureFactoriesByTargetClass.get(targetClass);
	if(result==null)
	    featureFactoriesByTargetClass.put(targetClass, result = new ArrayList<FeatureFactory>());
	return result;
    }
    
    public static void init(Object obj){
	//Traverse the type hierarchy
	Class tClass = obj.getClass();
	while(tClass!=Object.class){
	    for(Class iFace:tClass.getInterfaces())
		for(FeatureFactory ff:getFactoryCollection(iFace))
		    get(obj,ff.getFeatureClass());
	    tClass=tClass.getSuperclass();
	}//end while(hierarchy)
    }//end init(...)
    
    public static void destruct(Object obj){
	for(Feature f : targetMap.get(obj).values())
	    f.destruct(obj);
	targetMap.put(obj, null);
    }//end destruct()
    
    private static Map<Class<? extends Feature>,Feature> getFeatureMap(Object targ){
	Map<Class<? extends Feature>,Feature> result = targetMap.get(targ);
	if(result==null)
	    targetMap.put(targ, result = new HashMap<Class<? extends Feature>,Feature>());
	assert result!=null;
	return result;
    }//end getFeatureMap()
    
    private static Feature getFeature(Map<Class<? extends Feature>,Feature> map, Class<? extends Feature> featureClass, Object target){
	Feature result = map.get(featureClass);
	if(result==null){
	    final FeatureFactory ff = featureFactoriesByFeature.get(featureClass);
	    assert ff!=null;
	    map.put(featureClass, result = ff.newInstance(target));
	    result.apply(target);
	    }
	return result;
    }//end getFeature()

    public static <T extends Feature> T get(Object target, Class<T> featureClass){
     final Map<Class<? extends Feature>,Feature> fMap = getFeatureMap(target);
     return (T)getFeature(fMap,featureClass,target);
    }//end get(...)
}//end Features
