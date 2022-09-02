/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.coll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CollectionMap<K, V> implements Map<K,Collection<V>> {
    private final Map<K, Collection<V>> collectionMap = new HashMap<K, Collection<V>>();
    private final Class<? extends Collection<V>> collectionClass;
    
    public CollectionMap(){
	this(ArrayList.class);
    }
    
    @SuppressWarnings("unchecked")
    public CollectionMap(@SuppressWarnings("rawtypes") Class<? extends Collection> collectionClass){
	this.collectionClass = (Class<? extends Collection<V>>)collectionClass;
    }
    
    protected Collection<V> newCollection(){
	try{return collectionClass.getConstructor().newInstance();}
	catch(Exception e){throw new RuntimeException(e);}
    }
    
    @Override
    public void clear() {
	collectionMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
	return collectionMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
	return collectionMap.containsValue(value);
    }

    @Override
    public Set<java.util.Map.Entry<K, Collection<V>>> entrySet() {
	return collectionMap.entrySet();
    }

    @Override
    public boolean equals(Object o) {
	return collectionMap.equals(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<V> get(Object key) {//TODO: Safety
	Collection<V> result = collectionMap.get(key);
	if( result == null )
	    put((K)key, result = newCollection());
	return result;
    }//end get(...)

    @Override
    public int hashCode() {
	return collectionMap.hashCode();
    }

    @Override
    public boolean isEmpty() {
	return collectionMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
	return collectionMap.keySet();
    }

    @Override
    public Collection<V> put(K key, Collection<V> value) {
	return collectionMap.put(key, value);
    }
    
    public boolean putInCollection(K key, V value){
	return get(key).add(value);
    }
    
    public boolean removeFromCollection(K key, V value){
	return get(key).remove(value);
    }

    public void putAll(
	    Map<? extends K, ? extends Collection<V>> m) {
	collectionMap.putAll(m);
    }

    @Override
    public Collection<V> remove(Object key) {
	return collectionMap.remove(key);
    }

    @Override
    public int size() {
	return collectionMap.size();
    }

    @Override
    public Collection<Collection<V>> values() {
	return collectionMap.values();
    }
 
}//end CollectoinMap
