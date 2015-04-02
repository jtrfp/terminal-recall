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

import com.ochafik.util.Adapter;

public abstract class CachedAdapter<U,V> implements Adapter<U, V> {
    private final BidiReferenceMap<U,V> cache = new BidiReferenceMap<U,V>(ReferenceStrength.WEAK);

    @Override
    public final V adapt(U key) {
	if(key==null)
	    throw new NullPointerException("key to adapt is intolerably null.");
	V result = cache.get(key);
	if(result==null){
	    V v = _adapt(key);
	    cache.put(key, v);
	    result = v;
	}//end if(null)
	return result;
    }//end adaot()

    @Override
    public final U reAdapt(V value) {
	if(value==null)
	    throw new NullPointerException("value to adapt is intolerably null.");
	U result = cache.getKey(value);
	if(result==null){
	    U key = _reAdapt(value);
	    cache.put(key, value);
	    result = key;
	}//end if(null)
	return result;
    }//end reAdapt(...)
    
    protected abstract V _adapt(U value);
    protected abstract U _reAdapt(V value);

}//end CachedAdapter
