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

package org.jtrfp.trcl.flow;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeListenerProxy;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Yet another implementation of a property proxy object, though not quite as cool as those used
 * with ProxyObjects, this doesn't need an skeletal interface and simply forwards property change
 * events.<br>
 * CAVEAT: General (nameless) property change listeners will not be notified of an indirection change
 * since this class depends on the name of the property to send the refresh. Blindly refreshing all
 * properties with each indirection change would be potentially wasteful of resources as it could trigger 
 * unneeded lazy allocations which don't end up being used.
 * @author Chuck Ritola
 *
 */
public class IndirectProperty<PROPERTY_TYPE> implements PropertyChangeListener{
 private final PropertyChangeSupport
  targetPCS = new PropertyChangeSupport(this),
  thisPCS   = new PropertyChangeSupport(this);
 private WeakReference<PROPERTY_TYPE> target = new WeakReference<PROPERTY_TYPE>(null);
/**
 * @return the target
 */
public PROPERTY_TYPE getTarget() {
    return target.get();
}
/**
 * @param target the target to set
 */
public void setTarget(PROPERTY_TYPE target) {
    if(this.target!=target){
	if(this.target!=null)
	    disconnectAllFrom(this.target.get());
	if(target!=null)
	    connectAllTo(target);
	thisPCS.firePropertyChange("target", this.target, target);
	this.target = new WeakReference<PROPERTY_TYPE>(target);
	fireAllPropertiesChanged();
    }//end if(changed)
}//end setTarget(...)

private void connectAllTo(PROPERTY_TYPE tgt) {
    for(PropertyChangeListener l:targetPCS.getPropertyChangeListeners()){
	if(l instanceof PropertyChangeListenerProxy){
	    final PropertyChangeListenerProxy proxy = (PropertyChangeListenerProxy)l;
	    try{tgt.getClass().getMethod("addPropertyChangeListener", String.class,PropertyChangeListener.class).invoke(tgt, proxy.getPropertyName(), proxy);}
   		catch(Exception e){e.printStackTrace();}}
	else{try{tgt.getClass().getMethod("addPropertyChangeListener", PropertyChangeListener.class).invoke(tgt, l);}
   	 catch(Exception e){e.printStackTrace();}}
       }//end for(listener)
}
private void disconnectAllFrom(PROPERTY_TYPE tgt) {
    if(tgt==null)
	return;
    for(PropertyChangeListener l:targetPCS.getPropertyChangeListeners()){
   	try{tgt.getClass().getMethod("removePropertyChangeListener", PropertyChangeListener.class).invoke(tgt, l);}
   	catch(Exception e){e.printStackTrace();}
       }//end for(listener)
}
public IndirectProperty<PROPERTY_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l){
	thisPCS.addPropertyChangeListener(propertyName, l);
	return this;
}

public IndirectProperty<PROPERTY_TYPE> removePropertyChangeListener(PropertyChangeListener l){
	thisPCS.removePropertyChangeListener(l);
	return this;
}

public IndirectProperty<PROPERTY_TYPE> addTargetPropertyChangeListener(String propertyName, PropertyChangeListener l){
	targetPCS.addPropertyChangeListener(propertyName, l);
	return this;
}

public IndirectProperty<PROPERTY_TYPE> removeTargetPropertyChangeListener(PropertyChangeListener l){
	targetPCS.removePropertyChangeListener(l);
	return this;
}

private void fireAllPropertiesChanged(){
    PROPERTY_TYPE target = this.target.get();
    if(target==null)
	return;
    HashSet<String> propertiesToFireChanged = new HashSet<String>();
    PropertyChangeListener []listeners = targetPCS.getPropertyChangeListeners();
    for(PropertyChangeListener l:listeners){
	if(l instanceof PropertyChangeListenerProxy){
	    final PropertyChangeListenerProxy prox = (PropertyChangeListenerProxy)l;
	    propertiesToFireChanged.add(prox.getPropertyName());
	}//end if(...)
    }//end for(listener)
    for(String propertyName:propertiesToFireChanged){
	try{targetPCS.firePropertyChange(propertyName, null, PropertyUtils.getProperty(target, propertyName));}
	catch(NoSuchMethodException e){e.printStackTrace();}
	catch(InvocationTargetException e){e.printStackTrace();}
	catch(IllegalAccessException e){e.printStackTrace();}
    }//end for(propertyNames)
 }//end fireAllPropertiesChanged()
@Override
public void propertyChange(PropertyChangeEvent evt) {
    final Object newVal = evt.getNewValue();
    if(newVal!=null)
     setTarget((PROPERTY_TYPE)newVal);
}//end propertyChange(...)
 
}//end IndirectBean
