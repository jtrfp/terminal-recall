/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.obj.Positionable;
import org.jtrfp.trcl.tools.Util;

import com.ochafik.util.Adapter;
import com.ochafik.util.listenable.Pair;

public abstract class CachedAdapter<U,V> implements Adapter<U, V> {
    private final BidiReferenceMap<U,V> cache;
    private volatile boolean tolerateNull = false;
    
    public CachedAdapter(){
	this(new BidiReferenceMap<U,V>(ReferenceStrength.WEAK,ReferenceStrength.WEAK, 64,.75f,true));
    }
    
    public CachedAdapter(BidiReferenceMap<U,V> cache){
	this.cache=cache;
    }
    
    public com.ochafik.util.listenable.Adapter<U,V> toForward(){
	return Util.bidi2Forward(this);
    }
    
    public com.ochafik.util.listenable.Adapter<V,U> toBackward(){
	return Util.bidi2Backward(this);
    }

    @Override
    public final V adapt(U key) {
	if(key==null){
	    if(tolerateNull)
		return null;
	    else throw new NullPointerException("Key is intolerably null.");
	}//end if(key null)
	synchronized(cache){
	 V result = cache.get(key);
	 if(result==null){
	    V v = _adapt(key);
	    if(v==null){
		if(tolerateNull)
		    return null;
		else throw new NullPointerException("Adapted value is intolerably null.");
	    }//end if(key null)
	    cache.put(key, v);
	    result = v;
	 }//end if(null)
	 return result;
	}//end sync(cache)
    }//end adapt()

    @Override
    public final U reAdapt(V value) {
	if(value==null){
	    if(tolerateNull)
		return null;
	    else throw new NullPointerException("Value is intolerably null.");
	}//end if(key null)
	synchronized(cache){
	 U result = cache.getKey(value);
	 if(result==null){
	    U key = _reAdapt(value);
	    if(key==null){
		if(tolerateNull)
		    return null;
		else throw new NullPointerException("Re-Adapted key is intolerably null.");
	    }//end if(key null)
	    cache.put(key, value);
	    result = key;
	 }//end if(null)
	 return result;
	}//end sync(cache)
    }//end reAdapt(...)
    
    protected abstract V _adapt  (U value) throws UnsupportedOperationException;
    protected abstract U _reAdapt(V value) throws UnsupportedOperationException;
    
    public static <U,V> CachedAdapter<U,V> decorate(final Adapter<U,V> adapter, BidiReferenceMap<U,V> mapToUse){
	return new CachedAdapter<U,V>(mapToUse){

	    @Override
	    protected V _adapt(U value) throws UnsupportedOperationException {
		return adapter.adapt(value);
	    }

	    @Override
	    protected U _reAdapt(V value) throws UnsupportedOperationException {
		return adapter.reAdapt(value);
	    }};
    }
    
    public static <U,V> CachedAdapter<U,V> decorate(final Adapter<U,V> adapter){
	return new CachedAdapter<U,V>(){

	    @Override
	    protected V _adapt(U value) throws UnsupportedOperationException {
		return adapter.adapt(value);
	    }

	    @Override
	    protected U _reAdapt(V value) throws UnsupportedOperationException {
		return adapter.reAdapt(value);
	    }};
    }

    /**
     * @return the tolerateNull
     */
    public boolean isTolerateNull() {
        return tolerateNull;
    }

    /**
     * @param tolerateNull the tolerateNull to set
     */
    public CachedAdapter<U,V> setTolerateNull(boolean tolerateNull) {
        this.tolerateNull = tolerateNull;
        return this;
    }

    public Adapter<V, U> inverse() {
	return Util.inverse(this);
    }

}//end CachedAdapter
