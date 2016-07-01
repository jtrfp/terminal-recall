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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
    
    public  void init(Object target){
	//Traverse the type hierarchy
	Class tClass = target.getClass();
	Set<Class> featureClassSet = new HashSet<Class>();
	//For its interfaces
	for(Class iFace:tClass.getInterfaces())
	    for(FeatureFactory ff:getFactoryCollection(iFace))
		featureClassSet.add(ff.getFeatureClass());
	while(tClass!=Object.class){
	    //First for the class itself
	    for(FeatureFactory ff:getFactoryCollection(tClass))
		    featureClassSet.add(ff.getFeatureClass());
	    tClass=tClass.getSuperclass();
	}//end while(hierarchy)
	
	for(Class c:featureClassSet)
	    get(target,c);
    }//end init(...)
    
    public  void destruct(Object obj){
	final Collection<Feature> rawFeatures = targetMap.get(obj).values();
	final Set<Feature> featureSet = new HashSet<Feature>();
	featureSet.addAll(rawFeatures);
	for(Feature f:featureSet){
	    if(f!=null)
		f.destruct(obj);
	    targetMap.remove(obj);   
	}//end for(entries)
    }//end destruct()
    
       Map<Class<? extends Feature>,Feature> getFeatureMap(Object targ){
	if(targ == null)
	    throw new NullPointerException("Target intolerably null.");
	Map<Class<? extends Feature>,Feature> result = targetMap.get(targ);
	if(result==null)
	    targetMap.put(targ, result = new HashMap<Class<? extends Feature>,Feature>());
	assert result!=null;
	return result;
    }//end getFeatureMap()
    
       Feature getFeature(Map<Class<? extends Feature>,Feature> featuresByClass, Class<? extends Feature> featureClass, Object target){
	Feature result = featuresByClass.get(featureClass);
	if(result==null)
	    result = newFeatureInstance(featuresByClass, featureClass, target);
	return result;
    }//end getFeature()

       private Feature newFeatureInstance(Map<Class<? extends Feature>,Feature> featuresByClass, Class<? extends Feature> featureClass, Object target){
	   final Feature result;
	   final FeatureFactory ff = featureFactoriesByFeature.get(featureClass);
	   if(ff == null)
	       throw new RuntimeException("Could not find Feature of type "+featureClass.getName());
	   assert ff!=null:""+featureClass.getName();
	   registerFeatureByClassRecursively(featureClass, result = ff.newInstance(target), featuresByClass);
	   result.apply(target);
	   init(result);
	   return result;
       }//end newFeatureInstance()

       private void registerFeatureByClassRecursively(Class featureClass, Feature toRegister, Map<Class<? extends Feature>,Feature> featuresByClass){
	   featuresByClass.put(featureClass, toRegister);
	   for(Class iFace:featureClass.getInterfaces())
	       registerFeatureByClassRecursively(iFace, toRegister, featuresByClass);
	   Class fc = featureClass.getSuperclass();
	   if(fc != null)
	    while(fc != Object.class){
	       registerFeatureByClassRecursively(fc, toRegister, featuresByClass);
	       fc = fc.getSuperclass();
	    }//end while(!Object.class)
       }//end registerFeatureByClassRecursively()

    public  <T> T get(Object target, Class<T> featureClass){
     final Map<Class<? extends Feature>,Feature> fMap = getFeatureMap(target);
     return (T)getFeature(fMap,(Class<Feature>)featureClass,target);
    }//end get(...)

    public void getAllFeaturesOf(Object target, Collection dest) {
	final Map<Class<? extends Feature>,Feature> fMap = getFeatureMap(target);
	for(Entry<Class<? extends Feature>,Feature> entry:fMap.entrySet())
	    dest.add(entry.getValue());
    }//end getAllFeaturesOf(...)
}//end FeaturesImpl
