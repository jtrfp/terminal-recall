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
    /*
    public static void main(String [] args){
	final String string = "content";
	final WeakReference<String> ref = new WeakReference<String>(string);
	System.out.println("equal? "+string.equals(ref));
	System.out.println("string.hash="+string.hashCode()+" ref.hash="+ref.hashCode());
	final HashMap<String,Object> map = new HashMap<String,Object>();
	map.put(string, new Object());
	System.out.println("Contains string? "+map.containsKey(string));
	System.out.println("Contains ref? "+map.containsKey(ref));
    }*/
    
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
	    return thisObj.hashCode()==other.hashCode();
	}return other==null;
    }
}//end HashTransparentWeakReference
