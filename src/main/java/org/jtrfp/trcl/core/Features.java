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
import java.util.WeakHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Features {
    // Key represents target-class
    private static final HashMap<Class<?>,FactoryEntry> factories = new HashMap<Class<?>,FactoryEntry>();
    
    private static class FactoryEntry{
	private final Collection<FeatureFactory> factories  = new ArrayList<FeatureFactory>();
	private final Map<Object,Feature>        features   = new WeakHashMap<Object,Feature>();
	public Collection<FeatureFactory> getFactories() {
	    return factories;
	}
	public Map<Object, Feature> getFeatures() {
	    return features;
	}
    }//end FactoryEntry
    
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
	final FactoryEntry entry = getFactoryEntry(factory.getTargetClass());
	entry.getFactories().add(factory);
    }
    
    private static FactoryEntry getFactoryEntry(Class targetClass){
	FactoryEntry fe = factories.get(targetClass);
	if(fe==null)
	    factories.put(targetClass, fe = new FactoryEntry());
	return fe;
    }

    public static void init(Object obj){
     FactoryEntry ent = getFactoryEntry(obj.getClass());
     for(FeatureFactory ff:ent.getFactories()){
	 Feature feature;
	 ent.getFeatures().put(obj,feature = ff.newInstance(obj));
	 feature.apply(obj);
	 }
    }//end init(...)

    public static <T extends Feature> T get(Object target, Class<T> featureClass){
     final FactoryEntry fe = getFactoryEntry(featureClass);
     return (T)fe.getFeatures().get(target);
    }
}//end Features
