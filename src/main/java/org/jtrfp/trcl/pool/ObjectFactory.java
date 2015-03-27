/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.pool;

import java.util.Map;

import com.ochafik.util.Adapter;

/**
 * Not related to the JNDI, but likely to be replaced by it in the future.
 * Creates and caches objects of type V from keys of type K.
 * @author Chuck Ritola
 *
 * @param <K>
 * @param <V>
 */
public class ObjectFactory<K,V> {
    private final Map    <K,V> mapToUse;
    private final Adapter<K,V> keyValueAdapter;
    
    public ObjectFactory(Map<K,V> mapToUse, Adapter<K,V> keyValueAdapter){
	this.mapToUse        = mapToUse;
	this.keyValueAdapter = keyValueAdapter;
    }
    
    /**
     * Obtain an instance of V based on the key K, creating one if its key isn't present in the currently-used map.
     * @param key
     * @return Instance of V created with the supplied adapter from K
     * @since Mar 27, 2015
     */
    public V get(K key){
	V result = mapToUse.get(key);
	if(result==null)
	    mapToUse.put(key, result=keyValueAdapter.adapt(key));
	return result;
    }//end get(key)
    
    /**
     * Query the currently-used map for already-created object matching the given key.
     * @param key
     * @return Teh pre-created object, if any, or null if none exists.
     * @since Mar 27, 2015
     */
    public V peek(K key){
	return mapToUse.get(key);
    }//end peek(key)
    
    /**
     * Query the map being used to back this ObjectFactory's caching of already-created objects.
     * @return
     * @since Mar 27, 2015
     */
    public Map<K,V> getMap(){
	return mapToUse;
    }
}//end CachedObjectFactory
