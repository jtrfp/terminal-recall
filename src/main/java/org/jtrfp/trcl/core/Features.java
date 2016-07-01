/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015-2016 Chuck Ritola
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

import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Features {
    private static FeaturesImpl impl = new FeaturesImpl();
    private static Features singleton;
    
    public Features(){
	this(Collections.EMPTY_LIST);
    }
    
    @Autowired(required=true)
    public Features(Collection<FeatureFactory> features){
	for(FeatureFactory ff:features)
	    registerFeature(ff);
	singleton = this;
    }
    
    public synchronized static Features getSingleton(){
	return singleton;
    }
    
    public synchronized static void resetImpl(){
	impl = new FeaturesImpl();
    }

    public synchronized static void registerFeature(FeatureFactory<?> factory){
	impl.registerFeature(factory);
    }
    
    public synchronized static void init(Object obj){
	impl.init(obj);
    }//end init(...)
    
    public synchronized static void destruct(Object obj){
	impl.destruct(obj);
    }//end destruct()

    public synchronized static <T> T get(Object target, Class<T> featureClass){
     return impl.get(target, featureClass);
    }//end get(...)
    
    public synchronized static void getAllFeaturesOf(Object target, Collection dest){
	impl.getAllFeaturesOf(target,dest);
    }

    public static void setSingleton(Features singleton) {
        Features.singleton = singleton;
    }
}//end Features
