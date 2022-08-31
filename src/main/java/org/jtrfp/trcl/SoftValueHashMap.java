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

package org.jtrfp.trcl;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//http://www.codeinstructions.com/2008/09/weakhashmap-is-not-cache-understanding.html

public final class SoftValueHashMap<K, V> implements Map<K,V> {
    private final HashMap<K,SoftReference<V>> intrinsic = new HashMap<K,SoftReference<V>>();

    public V put(K key, V value){
	intrinsic.put(key, new SoftReference<V>(value));
	return value;
    }//end put(...)
    
    public V get(Object key){
	if(intrinsic.containsKey(key)){
	    return intrinsic.get(key).get();
	}//end if(containsKey)
	return null;
    }//end get(...)

    @Override
    public void clear() {
	intrinsic.clear();
    }

    @Override
    public boolean containsKey(Object key) {
	if(intrinsic.containsKey(key)){
	    return intrinsic.get(key).get()!=null;
	}//end if(intrinsic.containsKey())
	return false;
    }//end containsKey(...)

    @Override
    public boolean containsValue(Object arg0) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public boolean isEmpty() {
	return intrinsic.isEmpty();
    }

    @Override
    public Set<K> keySet() {
	return intrinsic.keySet();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> arg0) {
	throw new RuntimeException("Not implemented.");
    }

    @Override
    public V remove(Object key) {
	return intrinsic.remove(key).get();
    }

    @Override
    public int size() {
	return intrinsic.size();
    }

    @Override
    public Collection<V> values() {
	throw new RuntimeException("Not implemented.");
    }
}//end SoftValueHashMap
