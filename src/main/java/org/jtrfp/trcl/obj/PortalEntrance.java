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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.coll.ObjectTallyCollection;
import org.jtrfp.trcl.core.PortalTexture;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.RendererFactory.PortalNotAvailableException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.prop.SkyCubeGen;
import org.jtrfp.trcl.shell.GameShell;

import com.ochafik.util.listenable.Pair;

public class PortalEntrance extends WorldObject {
    //// PROPERTIES
    public static final String WITHIN_RANGE         = "withinRange";
    public static final String PORTAL_TEXTURE_ID    = "portalTextureID";
    
    private PortalExit portalExit;
    private Renderer   portalRenderer;
    private boolean    withinRange          = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PropertyChangeListener relevanceTallyListener = new RelevanceTallyListener();
    //private final PropertyChangeListener weakRelevanceTallyListener;
    private int        portalTextureID = -1;
    private PortalTexture
                       portalTexture = null;
    private WorldObject approachingObject;
    private SkyCubeGen skyCubeGen = GameShell.DEFAULT_GRADIENT;
    private boolean    portalUnavailable = false;

    public PortalEntrance(TR tr, Model model, PortalExit exit, WorldObject approachingObject){
	this(tr,exit,approachingObject);
	setModel(model);
    }
    
    public PortalEntrance(TR tr, PortalExit exit, WorldObject approachingObject) {
	this(tr);
	this.portalExit=exit;
	addBehavior(new PortalEntranceBehavior());
	setApproachingObject(approachingObject);
    }//end constructor
    
    private PortalEntrance(TR tr) {
	super(tr);
	final ObjectTallyCollection<Positionable> allRelevant = tr.gpu.get().rendererFactory.get().getAllRelevant();
	//weakRelevanceTallyListener = new WeakPropertyChangeListener(relevanceTallyListener, allRelevant);
	allRelevant.addObjectTallyListener(this, relevanceTallyListener);
    }
    
    private class RelevanceTallyListener implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final int newTally = (Integer)evt.getNewValue();
	    final int oldTally = (Integer)evt.getOldValue();
	    if(newTally == 0 && oldTally != 0)
		becameIrrelevant();
	    else if(newTally != 0 && oldTally == 0)
		becameRelevant();
	}//end propertyChange(...)
    }//end RelevanceTallyListener
    
    private void becameIrrelevant(){
	if(isPortalUnavailable())
	    return;//Do nothing, failed to get portal in the first place.
	final Renderer portalRenderer = getPortalRenderer();
	if(portalRenderer == null)
	    throw new IllegalStateException("cameraToMonitor is intolerably null.");
	System.out.println("De-activating portal entrance...");
	//Release the Camera
	getTr().gpu.get().rendererFactory.get().releasePortalRenderer(portalRenderer);
	portalExit.deactivate();
	getPortalExit().setControlledCamera(null);
	setPortalRenderer(null);
	setWithinRange(false);
	System.out.println("Done de-activing portal entrance.");
    }

    private void becameRelevant() {
	if (getPortalRenderer() != null)
	    throw new IllegalStateException(
		    "portalRenderer is intolerably non-null.");
	try {
	    final Pair<Renderer, Integer> newRendererPair = getTr().gpu.get().rendererFactory
		    .get().acquirePortalRenderer();
	    System.out.println("Activating portal entrance...");
	    getPortalExit().setControlledCamera(
		    newRendererPair.getFirst().getCamera());
	    newRendererPair.getFirst().getSkyCube().setSkyCubeGen(skyCubeGen);
	    setPortalRenderer(newRendererPair.getFirst());
	    setWithinRange(true);
	    setPortalTextureID(newRendererPair.getSecond());
	    portalExit.activate();
	    setPortalUnavailable(false);
	    System.out.println("Done activating portal entrance.");
	} catch (PortalNotAvailableException e) {
	    System.out.println("Portal acquisition rejected: All are in use. PortalEntrance hash="+this.hashCode());
	    setPortalUnavailable(true);
	}// end catch(PortalNotAvailableException)
    }// end becameRelevant()

    public double[] getRelativePosition(double [] dest){
	Vect3D.subtract(getApproachingObject().getPosition(), PortalEntrance.this.getPosition(), dest);
	return dest;
    }
    
    public Rotation getRelativeHeadingTop(){
	return new Rotation(getHeading(),getTop(),portalExit.getHeading(),portalExit.getTop());
    }
    
    @Override
    public WorldObject notifyPositionChange(){
	final double [] pos = getPosition();
	//System.out.println("PortalEntrance. "+hashCode()+" notifyPositionChange() "+pos[0]+" "+pos[1]+" "+pos[2]);
	super.notifyPositionChange();
	return this;
    }
    
    private class PortalEntranceBehavior extends Behavior{
	private final double [] relativePosition = new double[3];
	@Override
	public void tick(long tickTimeMillis){
	    if(isWithinRange()){
		getRelativePosition(relativePosition);
		final WorldObject approachingObject = getApproachingObject();
		portalExit.updateObservationParams(relativePosition, getRelativeHeadingTop(),approachingObject.getHeading(),approachingObject.getTop());
	    }//end if(isWithinRange)
	    
	}//end _tick(...)
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
     * @param portalExit the portalExit to set
     */
    public void setPortalExit(PortalExit portalExit) {
        this.portalExit = portalExit;
    }

    public void setWithinRange(boolean withinRange) {
	final boolean oldValue = this.withinRange;
        this.withinRange = withinRange;
        pcs.firePropertyChange(WITHIN_RANGE, withinRange, oldValue);
    }

    public Renderer getPortalRenderer() {
        return portalRenderer;
    }

    public void setPortalRenderer(Renderer rendererToUse) {
        this.portalRenderer = rendererToUse;
    }

    public int getPortalTextureID() {
        return portalTextureID;
    }

    public void setPortalTextureID(int portalTextureID) {//TODO: This needs to be set!
	final int oldValue = this.portalTextureID;
	this.portalTextureID = portalTextureID;
	pcs.firePropertyChange(PORTAL_TEXTURE_ID, oldValue, portalTextureID);
	getPortalTexture().setPortalFramebufferNumber(portalTextureID);
    }

    public PortalTexture getPortalTexture() {
        return portalTexture;
    }

    public void setPortalTexture(PortalTexture portalTexture) {
        this.portalTexture = portalTexture;
    }

    public WorldObject getApproachingObject() {
        return approachingObject;
    }

    public void setApproachingObject(WorldObject objectToMonitor) {
        this.approachingObject = objectToMonitor;
    }

    public SkyCubeGen getSkyCubeGen() {
        return skyCubeGen;
    }

    public void setSkyCubeGen(SkyCubeGen skyCubeGen) {
        this.skyCubeGen = skyCubeGen;
    }

    private boolean isPortalUnavailable() {
        return portalUnavailable;
    }

    private void setPortalUnavailable(boolean portalWasUnavailable) {
        this.portalUnavailable = portalWasUnavailable;
    }

}//end PortalEntrance
