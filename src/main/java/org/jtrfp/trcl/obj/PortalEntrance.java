/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2021 Chuck Ritola
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
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.RendererConfigurator;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.coll.ObjectTallyCollection;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.PortalTexture;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gpu.RendererFactory.PortalNotAvailableException;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.prop.SkyCubeGen;
import org.jtrfp.trcl.shell.GameShellFactory;

import com.ochafik.util.listenable.Pair;

import lombok.Getter;
import lombok.Setter;

public class PortalEntrance extends WorldObject {
    //// PROPERTIES
    public static final String RELEVANT         = "withinRange";
    public static final String PORTAL_TEXTURE_ID    = "portalTextureID";
    
    private PortalExit portalExit;
    private Renderer   portalRenderer;
    private boolean    relevant          = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final PropertyChangeListener relevanceTallyListener = new RelevanceTallyListener();
    //private final PropertyChangeListener weakRelevanceTallyListener;
    private int        portalTextureID = -1;
    private PortalTexture
                       portalTexture = null;
    private WorldObject approachingObject;
    private SkyCubeGen skyCubeGen = GameShellFactory.DEFAULT_GRADIENT;
    private boolean    portalUnavailable = false;
    private TRFuture<Void> relevanceFuture;
    private boolean dotRelevant = false, rendering = false, nearRelevant=false;
    private double maxDistance = TRFactory.mapSquareSize * 4;
    private boolean portalStateStale = true;
    @Getter @Setter
    private RendererConfigurator rendererConfigurator;

    public PortalEntrance(GL33Model model, PortalExit exit, WorldObject approachingObject){
	this(exit,approachingObject);
	setModel(model);
    }
    
    public PortalEntrance(PortalExit exit, WorldObject approachingObject) {
	this();
	this.portalExit=exit;
	addBehavior(new PortalEntranceBehavior());
	setApproachingObject(approachingObject);
    }//end constructor
    
    private PortalEntrance() {
	super();
	final ObjectTallyCollection<Positionable> allRelevant = Features.get(getTr(), GPUFeature.class).rendererFactory.get().getAllRelevant();
	//weakRelevanceTallyListener = new WeakPropertyChangeListener(relevanceTallyListener, allRelevant);
	allRelevant.addObjectTallyListener(this, relevanceTallyListener);
    }
  /*  
    @Override
    public boolean supportsLoop(){
	return false;
    }
    */
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
	setRelevant(false);
	reEvaluatePortalState();
    }

    private void becameRelevant() {
	setRelevant(true);
    }// end becameRelevant()
    
    private void reEvaluatePortalState(){
	if(isRelevant() && isDotRelevant() && isNearRelevant())
	    setRendering(true);
	else
	    setRendering(false);
	setPortalStateStale(false);
    }

    public double[] getRelativePosition(double [] dest){
	Vect3D.subtract(getApproachingObject().getPosition(), PortalEntrance.this.getPosition(), dest);
	for(int i=0; i<3; i++)
	 dest[i] = TRFactory.deltaRollover(dest[i]);
	return dest;
    }
    
    public Rotation getRelativeHeadingTop(){
	return new Rotation(getHeading(),getTop(),portalExit.getHeading(),portalExit.getTop());
    }
    
    @Override
    public WorldObject notifyPositionChange(){
	super.notifyPositionChange();
	return this;
    }
    
    private void updateDotState(double dot){
	setDotRelevant(dot>=0);
    }
    
    private class PortalEntranceBehavior extends Behavior{
	private final double [] relativePosition = new double[3];
	@Override
	public void tick(long tickTimeMillis){
	    if(isRelevant()){
		final WorldObject approachingObject = getApproachingObject();
		final double dot = PortalEntrance.this.getHeading().dotProduct(approachingObject.getHeading());
		updateDotState(dot);
		getRelativePosition(relativePosition);
		updateNearState(relativePosition);
		portalExit.updateObservationParams(relativePosition, getRelativeHeadingTop(),approachingObject.getHeading(),approachingObject.getTop());
	    }//end if(isWithinRange)
	    if(isPortalStateStale())
		    reEvaluatePortalState();
	}//end _tick(...)
    }//end PortalEntranceBehavior
    
    private void updateNearState(double [] relativePos) {
	setNearRelevant(Vect3D.norm(relativePos) < getMaxDistance());
    }

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
    public boolean isRelevant() {
        return relevant;
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

    public void setRelevant(boolean relevant) {
	if(relevant == this.relevant)
	    return;
	final boolean oldValue = this.relevant;
        this.relevant = relevant;
        pcs.firePropertyChange(RELEVANT, relevant, oldValue);
        //reEvaluatePortalState();
        setPortalStateStale(true);
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

    public void setPortalTextureID(int portalTextureID) {
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

    protected boolean isDotRelevant() {
        return dotRelevant;
    }

    protected void setDotRelevant(boolean dotRelevant) {
	if(this.dotRelevant == dotRelevant)
	    return;
        this.dotRelevant = dotRelevant;
        //reEvaluatePortalState();
        setPortalStateStale(true);
    }

    protected boolean isRendering() {
        return rendering;
    }

    protected void setRendering(boolean rendering) {
	if(this.rendering == rendering)
	    return;
	this.rendering = rendering;
	if(rendering){
	    final ThreadManager tm = getTr().getThreadManager();
	    final TRFuture<Void> oldRelevanceFuture = relevanceFuture;
	    relevanceFuture = tm.submitToThreadPool(new Callable<Void>(){//Cannot call GPU ops directly from a realtime thread; use a pool thread.
		@Override
		public Void call() throws Exception {
		    try {
			if(oldRelevanceFuture!=null)
			    oldRelevanceFuture.get();
			if (getPortalRenderer() != null)
			    throw new IllegalStateException(
				    "portalRenderer is intolerably non-null.");
			final Pair<Renderer, Integer> newRendererPair = Features.get(getTr(), GPUFeature.class).rendererFactory
				.get().acquirePortalRenderer();
			final Renderer newRenderer = newRendererPair.getFirst();
			System.out.println("Activating portal entrance... "+PortalEntrance.this);
			getPortalExit().setControlledCamera(
				newRenderer.getCamera());
			assert newRenderer != getTr().mainRenderer;
			if(rendererConfigurator != null)
			    rendererConfigurator.applyToRenderer(newRenderer);
			else
			    newRenderer.getSkyCube().setSkyCubeGen(skyCubeGen);
			setPortalRenderer(newRenderer);
			setPortalTextureID(newRendererPair.getSecond());
			portalExit.activate();
			setPortalUnavailable(false);
			System.out.println("Done activating portal entrance.");
		    } catch (PortalNotAvailableException e) {
			System.out.println("Portal acquisition rejected: All are in use. PortalEntrance hash="+this.hashCode());
			setPortalUnavailable(true);
		    }// end catch(PortalNotAvailableException)
		    catch(Exception e){e.printStackTrace();}
		    
		    return null;
		}});
	}else{//Not rendering
	    final ThreadManager tm = getTr().getThreadManager();
	    final TRFuture<Void> oldRelevanceFuture = relevanceFuture;
	    relevanceFuture = tm.submitToThreadPool(new Callable<Void>(){//Cannot call GPU ops directly from a realtime thread; use a pool thread.
		@Override
		public Void call() throws Exception {
		    try{
			if(oldRelevanceFuture!=null)
			    oldRelevanceFuture.get();//Don't double-access
			if(isPortalUnavailable())
			    return null;//Do nothing, failed to get portal in the first place.
			final Renderer portalRenderer = getPortalRenderer();
			if(portalRenderer == null)
			    throw new IllegalStateException("portalRenderer is intolerably null.");
			System.out.println("De-activating portal entrance: "+PortalEntrance.this);
			//Release the Camera
			Features.get(getTr(), GPUFeature.class).rendererFactory.get().releasePortalRenderer(portalRenderer);
			portalExit.deactivate();
			getPortalExit().setControlledCamera(null);
			setPortalRenderer(null);
			setPortalTextureID(-1);
			System.out.println("Done de-activating portal entrance.");
		    }catch(Exception e){e.printStackTrace();}
		    return null;
		}});
	}//end if(!rendering)
    }//end setRendering(...)

    public double getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public boolean isNearRelevant() {
        return nearRelevant;
    }

    public void setNearRelevant(boolean nearRelevant) {
        this.nearRelevant = nearRelevant;
        setPortalStateStale(true);
        //reEvaluatePortalState();
    }

    public boolean isPortalStateStale() {
        return portalStateStale;
    }

    public void setPortalStateStale(boolean portalStateStale) {
        this.portalStateStale = portalStateStale;
    }
}//end PortalEntrance
