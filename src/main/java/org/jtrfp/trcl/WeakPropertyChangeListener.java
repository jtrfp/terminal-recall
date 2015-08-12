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

/**
 * Special thanks to <a href="http://www.jroller.com/santhosh/entry/use_weak_listeners_to_avoid">this page.</a>
 * @author Chuck Ritola
 *
 */

public class WeakPropertyChangeListener implements PropertyChangeListener {
    private final PropertyChangeListener                delegate;
    private final WeakReference<PropertyChangeSupport>  listened;
    
    public WeakPropertyChangeListener(PropertyChangeListener delegate, PropertyChangeSupport source){
	this.delegate=delegate;
	this.listened=new WeakReference<PropertyChangeSupport> (source);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final PropertyChangeListener pcl   = delegate;
	final PropertyChangeSupport source = this.listened.get();
	if(pcl==null){
	    if(source!=null)//Shouldn't be null since the source is expected to invoke this but just in case...
		source.removePropertyChangeListener(this);
	    }
	else
	    pcl.propertyChange(evt);
    }//end propertyChange

}//end WeakPropertyChangeListener
