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
    
    public V get(K key){
	V result = mapToUse.get(key);
	if(result==null)
	    mapToUse.put(key, result=keyValueAdapter.adapt(key));
	return result;
    }//end public(key)
}//end CachedObjectFactory
