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
package org.jtrfp.trcl.obj;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.Camera;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Vect3D;

public class PortalEntrance extends WorldObject {
    public static final String WITHIN_RANGE         = "withinRange";
    
    private static final double ACTIVATION_DISTANCE = TR.mapSquareSize*8;
    
    private PortalExit portalExit;
    private Camera     cameraToMonitor;
    private boolean    withinRange          = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public PortalEntrance(TR tr, PortalExit exit, Camera cameraToMonitor) {
	super(tr);
	this.portalExit=exit;
	this.cameraToMonitor=cameraToMonitor;
	addBehavior(new PortalEntranceBehavior());
    }
    
    public PortalEntrance(TR tr, Model model, PortalExit exit, Camera cameraToMonitor){
	super(tr,model);
	this.portalExit=exit;
	this.cameraToMonitor=cameraToMonitor;
	addBehavior(new PortalEntranceBehavior());
    }

    public PortalEntrance(TR tr, Camera camera) {
	this(tr,null,camera);
    }

    public double[] getRelativePosition(double [] dest){
	Vect3D.subtract(cameraToMonitor.getPosition(), PortalEntrance.this.getPosition(), dest);
	return dest;
    }
    
    public Rotation getRelativeHeadingTop(){
	return new Rotation(getHeading(),getTop(),portalExit.getHeading(),portalExit.getTop());
    }
    
    private class PortalEntranceBehavior extends Behavior{
	private final double [] relativePosition = new double[3];
	@Override
	public void _tick(long tickTimeMillis){
	    final double dist = Vect3D.distance(cameraToMonitor.getPositionWithOffset(),PortalEntrance.this.getPositionWithOffset());
	    if(dist<ACTIVATION_DISTANCE && !withinRange)
		activation();
	    else if(withinRange && dist>=ACTIVATION_DISTANCE)
		deactivation();
	    
	    if(withinRange){
		portalExit.updateObservationParams(getRelativePosition(relativePosition), getRelativeHeadingTop(),cameraToMonitor.getHeading(),cameraToMonitor.getTop());
		getTr().secondaryRenderer.get().keepAlive();
	    }//end if(isWithinRange)
	}//end _tick(...)
	
	private void activation(){
	    pcs.firePropertyChange(WITHIN_RANGE, withinRange, true);
	    portalExit.activate();
	    withinRange=true;
	    System.out.println("PORTAL ENTRANCE ACTIVATED");
	}
	
	private void deactivation(){
	    pcs.firePropertyChange(WITHIN_RANGE, withinRange, false);
	    portalExit.deactivate();
	    withinRange=false;
	    System.out.println("PORTAL ENTRANCE DE-ACTIVATED");
	}
    }//end PortalEntranceBehavior

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * @return the withinRange
     */
    public boolean isWithinRange() {
        return withinRange;
    }

    /**
     * @return the portalExit
     */
    public PortalExit getPortalExit() {
        return portalExit;
    }

    /**
     * @return the cameraToMonitor
     */
    public Camera getCameraToMonitor() {
        return cameraToMonitor;
    }

    /**
     * @param cameraToMonitor the cameraToMonitor to set
     */
    public void setCameraToMonitor(Camera cameraToMonitor) {
        this.cameraToMonitor = cameraToMonitor;
    }

    /**
     * @param portalExit the portalExit to set
     */
    public void setPortalExit(PortalExit portalExit) {
        this.portalExit = portalExit;
    }

}//end PortalEntrance
