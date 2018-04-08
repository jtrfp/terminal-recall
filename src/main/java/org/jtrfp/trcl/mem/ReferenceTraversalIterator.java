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

import java.lang.ref.PhantomReference;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLContext;

import org.jtrfp.trcl.core.TRFactory.TR;

public class ReferenceTraversalIterator implements Iterator<Object> {
    //private final Set<Object>	 	      alreadyVisited    = Collections.synchronizedSet(new HashSet<Object>());
    private final Set<Object> 			alreadyVisited = 
	    Collections.newSetFromMap(new IdentityHashMap<Object,Boolean>());
    //AlreadyVisited is accessed by a single thread, yet getting co-modification exceptions...!
    private final ArrayBlockingQueue<Object> output		= new ArrayBlockingQueue<Object>(100);
    private static final Object END_OF_TRAVERSAL		= new Object();
    private static final Object EMPTY				= new Object();
    private Object 				currentObject 	= EMPTY;
    private final	Class<?>[]		traversalBlacklist;
    
    
    public ReferenceTraversalIterator(final Object root){
	this(root, new Class<?>[]{
		SoftReference.class,
		TR.class,
		GL.class,
		GLContext.class,
		Integer.class,
		Long.class,
		Float.class,
		Double.class,
		Short.class,
		Byte.class,
		ByteBuffer.class,
		ReferenceTraversalIterator.class,
		PhantomReference.class
	});
    }//end constructor
    
    
    public ReferenceTraversalIterator(final Object root, Class<?> [] traversalBlacklist){
	this.traversalBlacklist=traversalBlacklist;
	new Thread(){
	    @Override
	    public void run(){
		handleElement(root);
		try{output.put(END_OF_TRAVERSAL);}
		catch(InterruptedException e){e.printStackTrace();}
	    }//end run()
	    
	    private void handleElement(Object element){
		if(element==null)
		    return;
		if(element.getClass().isPrimitive())
		    return;
		final Class<?> elementClass = element.getClass();
		for(Class<?> bl:ReferenceTraversalIterator.this.traversalBlacklist){
		    if(bl.isAssignableFrom(elementClass))
			return;
		}
		if(alreadyVisited.contains(element))
		    return;
		try{output.put(element);}
		catch(InterruptedException e){e.printStackTrace();}
		alreadyVisited.add(element);
		final Class cClass = element.getClass();
		if(cClass.isArray()){
		    handleArray(element);
		}else{
		    handleClassObject(element);
		}
	    }//end handleElement(...)
	    
	    private void handleArray(Object element){
		final Class componentType = element.getClass().getComponentType();
		    if(!componentType.isPrimitive()){
			final int size=Array.getLength(element);
			for(int i=0; i<size; i++){
			    Object aElement = Array.get(element, i);
			    handleElement(aElement);
			}//end for(size)
		    }//end !primitive list
		}//end handleArray
	    
	    private void handleClassObject(Object element) {
		Class clazz = element.getClass();
		if(clazz==null || clazz == Object.class)return;
		do {
			final Field[] fields = clazz.getDeclaredFields();
			for (Field f : fields) {
			    // System.out.println("FIELD: "+f.getType().getName()+" "+f.getName());
			    if (!f.getType().isPrimitive()) {
				final boolean wasAccessible = f.isAccessible();
				f.setAccessible(true);
				try {
				    Object obj = f.get(element);
				    handleElement(obj);
				} catch (IllegalAccessException e) {
				    e.printStackTrace();
				}
				f.setAccessible(wasAccessible);
			    }// end if(!isPrimitive)
			}// end for(fields)
		    clazz = clazz.getSuperclass();
		} while (clazz !=Object.class);
	    }// end handleClassObject(...)
	}.start();
    }//end constructor
    
    private Object peekCurrentObject(){
	if(currentObject==EMPTY){
	    try{currentObject = output.take();}
	    catch(InterruptedException e){e.printStackTrace();}
	}//end if(null)
	return currentObject;
    }
    
    private Object popCurrentObject(){
	final Object result = peekCurrentObject();
	currentObject = EMPTY;
	return result;
    }
    
    @Override
    public boolean hasNext() {
	return peekCurrentObject()!=END_OF_TRAVERSAL;
    }//end hasNext()

    @Override
    public Object next() {
	if(hasNext()){
	    return popCurrentObject();
	}else return null;
    }//end next()

    @Override
    public void remove() {
	throw new UnsupportedOperationException(
		"This Iterator represents the state of a running system; items cannot be removed.");
    }//end remove()
}//end ReferenceIterator
