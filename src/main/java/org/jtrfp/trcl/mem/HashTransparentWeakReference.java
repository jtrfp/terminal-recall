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

package org.jtrfp.trcl.mem;

import java.lang.ref.WeakReference;

public class HashTransparentWeakReference<T> extends WeakReference<T> {
    public HashTransparentWeakReference(T val){
	super(val);
    }
    
    @Override
    public int hashCode(){
	final Object obj = get();
	if(obj!=null){
	    return obj.hashCode();
	}else return 0;
    }
    
    @Override
    public boolean equals(Object other){
	final Object thisObj = get();
	if(thisObj!=null){
	    return thisObj.equals(other);
	}return other==null;
    }
}//end HashTransparentWeakReference
