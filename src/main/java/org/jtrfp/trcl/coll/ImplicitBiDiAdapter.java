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

import com.ochafik.util.listenable.Adapter;

/**
 * A CachedAdapter which is null-toleraent for both forward and backward adapters
 * for faux-converting a UniDi adapter to a BiDi adapter by using cached results;
 * @author Chuck Ritola
 *
 * @param <U>
 * @param <V>
 */
public class ImplicitBiDiAdapter<U,V> extends CachedAdapter<U, V> {
    private final Adapter<U,V> read;
    private final Adapter<V,U> write;
    public ImplicitBiDiAdapter(Adapter<U,V> read, Adapter<V,U> write){
	this.read=read;
	this.write=write;
    }

    @Override
    protected V _adapt(U value) throws UnsupportedOperationException {
	if(read!=null)
	 return read.adapt(value);
	throw new UnsupportedOperationException("No cached value available for reverse-operation value "+value);
    }

    @Override
    protected U _reAdapt(V value) throws UnsupportedOperationException {
	if(write!=null)
	    return write.adapt(value);
	throw new UnsupportedOperationException("No cached value available for reverse-operation value "+value);
    }

}//end ImplicitBiDiAdapter
