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
import java.util.concurrent.Callable;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.coll.ObjectTallyCollection;
import org.jtrfp.trcl.core.PortalTexture;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.RendererFactory.PortalNotAvailableException;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.prop.SkyCubeGen;
import org.jtrfp.trcl.shell.GameShell;

import com.ochafik.util.listenable.Pair;

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
    private SkyCubeGen skyCubeGen = GameShell.DEFAULT_GRADIENT;
    private boolean    portalUnavailable = false;
    private volatile long timeOfLastTick = 0L;
    private TRFuture<Void> relevanceFuture;

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
    
    @Override
    public boolean supportsLoop(){
	return false;
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
		    getTr().gpu.get().rendererFactory.get().releasePortalRenderer(portalRenderer);
		    portalExit.deactivate();
		    getPortalExit().setControlledCamera(null);
		    setPortalRenderer(null);
		    setRelevant(false);
		    System.out.println("Done de-activating portal entrance.");
		}catch(Exception e){e.printStackTrace();}
		return null;
	    }});
    }

    private void becameRelevant() {
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
		    final Pair<Renderer, Integer> newRendererPair = getTr().gpu.get().rendererFactory
			    .get().acquirePortalRenderer();
		    System.out.println("Activating portal entrance... "+PortalEntrance.this);
		    getPortalExit().setControlledCamera(
			    newRendererPair.getFirst().getCamera());
		    newRendererPair.getFirst().getSkyCube().setSkyCubeGen(skyCubeGen);
		    setPortalRenderer(newRendererPair.getFirst());
		    setRelevant(true);
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
	    timeOfLastTick = tickTimeMillis;
	    if(isRelevant()){
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
	final boolean oldValue = this.relevant;
        this.relevant = relevant;
        pcs.firePropertyChange(RELEVANT, relevant, oldValue);
    }

    public Renderer getPortalRenderer() {
	if(portalRenderer == null){
	    System.out.println("WARNING: Portal renderer is null. isRelevant? "+isRelevant()+" RelevanceTally: "+getTr().gpu.get().rendererFactory.get().getRelevanceTallyOf(this));
	    final Player player = getTr().getGame().getPlayer();
	    final int dist = (int)Vect3D.distance(getPosition(), player.getPosition());
	    final double [] posMapSq = new double[3];
	    Vect3D.scalarMultiply(getPosition(), 1./TR.mapSquareSize, posMapSq);
	    System.out.println("Portal position in mapSquares: "+posMapSq[0]+" "+posMapSq[1]+" "+posMapSq[2]+" ");
	    Vect3D.scalarMultiply(player.getPosition(), 1./TR.mapSquareSize, posMapSq);
	    System.out.println("Player position in mapSquares: "+posMapSq[0]+" "+posMapSq[1]+" "+posMapSq[2]+" ");
	    System.out.println("Distance from player in mapSquares "+(dist/TR.mapSquareSize));
	    System.out.println("Millis since last tick: "+(System.currentTimeMillis()-timeOfLastTick));
	}
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
