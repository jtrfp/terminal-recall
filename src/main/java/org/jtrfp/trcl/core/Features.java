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
    private static final FeaturesImpl impl = new FeaturesImpl();
    
    public Features(){
	this(Collections.EMPTY_LIST);
    }
    
    @Autowired(required=true)
    public Features(Collection<FeatureFactory> features){
	for(FeatureFactory ff:features)
	    registerFeature(ff);
    }

    private static void registerFeature(FeatureFactory<?> factory){
	impl.registerFeature(factory);
    }
    
    public static void init(Object obj){
	impl.init(obj);
    }//end init(...)
    
    public static void destruct(Object obj){
	impl.destruct(obj);
    }//end destruct()

    public static <T> T get(Object target, Class<T> featureClass){
     return impl.get(target, featureClass);
    }//end get(...)
}//end Features
