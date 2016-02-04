/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Special thanks to <a href="http://www.jroller.com/santhosh/entry/use_weak_listeners_to_avoid">this page.</a>
 * @author Chuck Ritola
 *
 */

public class WeakPropertyChangeListener implements PropertyChangeListener {
    private final WeakReference<PropertyChangeListener> delegate;
    private final Object                                listened;
    
    public WeakPropertyChangeListener(PropertyChangeListener delegate, Object listened){
	this.delegate=new WeakReference<PropertyChangeListener>(delegate);
	this.listened=listened;
    }
    
    private void remove(Object source){
	if(source instanceof PropertyChangeSupport)
	  ((PropertyChangeSupport)source).removePropertyChangeListener(this);
	else{//do it the sketchy way.
	    try{
	     Method mth = source.getClass().getMethod("removePropertyChangeListener", new Class [] {PropertyChangeListener.class});
	     mth.setAccessible(true);
	     mth.invoke(source, new Object[]{this});}
	    catch(Exception e){e.printStackTrace();}
	}
    }//end remove(...)

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final PropertyChangeListener pcl   = delegate.get();
	final Object                source = listened;
	if(pcl==null){
	    if(source!=null)//Shouldn't be null since the source is expected to invoke this but just in case...
		remove(source);
	    }
	else
	    pcl.propertyChange(evt);
    }//end propertyChange
}//end WeakPropertyChangeListener
