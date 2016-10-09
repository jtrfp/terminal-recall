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
package org.jtrfp.trcl.obj;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class NAVRadarBlipFactory implements NAVRadarBlipFactoryListener {
    private static final int POOL_SIZE=15;
    private static final int RADAR_RANGE=800000;
    private static final double DEFAULT_RADAR_GUI_RADIUS=.17;
    private Double radarGUIRadius;
    //private static final double RADAR_SCALAR=RADAR_GUI_RADIUS/RADAR_RANGE;
    private final Blip [][] blipPool = new Blip[BlipType.values().length][POOL_SIZE];
    private final int []poolIndices = new int[POOL_SIZE];
    private final TR tr;
    //private final DashboardLayout layout;
    private Vector3D topOrigin  = Vector3D.PLUS_J, headingOrigin = Vector3D.PLUS_K;
    private Vector3D positionOrigin = Vector3D.ZERO;
    private Rotation vectorHack = Rotation.IDENTITY;
    private Collection<Blip> activeBlips = new ArrayList<Blip>();
    private Collection<NAVRadarBlipFactoryListener> listeners = new ArrayList<NAVRadarBlipFactoryListener>();
    private boolean radarEnabled = true;
    private GameShell gameShell;
    private final RenderableSpacePartitioningGrid grid;
    final Collection<Blip> newActiveBlips = new ArrayList<Blip>();
    final HashSet<Blip> newlyEnabledBlips = new HashSet<Blip>();
    
    public NAVRadarBlipFactory(TR tr, RenderableSpacePartitioningGrid g, String debugName, boolean ignoreCamera){
	this(tr,g,null,debugName,ignoreCamera);
    }
    
    public NAVRadarBlipFactory(TR tr, RenderableSpacePartitioningGrid g, Double radarGUIRadius, String debugName, boolean ignoreCamera){
	this.tr    =tr;
	this.grid  =g;
	setRadarGUIRadius(radarGUIRadius);
	final BlipType [] types = BlipType.values();
	for(int ti=0; ti<types.length; ti++){
	    InputStream is = null;
	    try{
	     final VQTexture tex = Features.get(tr, GPUFeature.class).textureManager.get().newTexture(ImageIO.read(is = this.getClass().getResourceAsStream("/"+types[ti].getSprite())),null,"",false);
    	     for(int pi=0; pi<POOL_SIZE; pi++){
    		final Blip blip = new Blip(tex,debugName, ignoreCamera, .04*(getRadarGUIRadius()/DEFAULT_RADAR_GUI_RADIUS));
    		blip.setVisible(false);
    		blipPool[ti][pi]= blip;
    		//g.add(blip);
    	     }//end for(pi)
	    }catch(Exception e){e.printStackTrace();}
	    finally{try{if(is!=null)is.close();}catch(Exception e){e.printStackTrace();}}
	}//end for(ti)
    }//end constructor
    
    public static enum BlipType{
	AIR_ABOVE("RedPlus.png"),
	AIR_BELOW("RedMinus.png"),
	GRND_ABOVE("CyanPlus.png"),
	GRND_BELOW("CyanMinus.png"),
	PWR_ABOVE("BluePlus.png"),
	PWR_BELOW("BlueMinus.png"),
	TUN_ABOVE("YellowPlus.png"),
	TUN_BELOW("YellowMinus.png");
	private final String blipSprite;
	private BlipType(String blipSprite){
	    this.blipSprite=blipSprite;
	}//end constructor
	public String getSprite(){return blipSprite;}
    }//end BlipType
    
    private class Blip extends Sprite2D{
	private Positionable representativeObject;
	public Blip(Texture tex, String debugName, boolean ignoreCamera, double diameter) {
	    super(0,diameter,diameter,tex,true,debugName);
	    setImmuneToOpaqueDepthTest(true);
	    if(!ignoreCamera)
	     unsetRenderFlag(RenderFlags.IgnoreCamera);
	}//end constructor

	public void setRepresentativeObject(Positionable representativeObject) {
	    this.representativeObject = representativeObject;
	}

	public void refreshPosition() {
	    setVisible(isRadarEnabled());
	    if(!isRadarEnabled())
		return;
	    final double []blipPos = getPosition();
	    final Game game = getGameShell().getGame();
	    final double [] playerPos=game.getPlayer().getPosition();
	    Vect3D.subtract(representativeObject.getPosition(), playerPos, blipPos);
	    Vect3D.scalarMultiply(blipPos, getRadarScalar(), blipPos);
	    final double [] heading = game.getPlayer().getHeadingArray();
	    double hX=heading[0];
	    double hY=heading[2];
	    double norm = Math.sqrt(hX*hX+hY*hY);
	    if(norm!=0){
		hX/=norm;hY/=norm;
	    }else{hX=1;hY=0;}
	    
	    blipPos[1]=blipPos[2];
	    blipPos[2]=0;
	    
	    double newX=blipPos[0]*hY-blipPos[1]*hX;
	    double newY=blipPos[0]*hX+blipPos[1]*hY;
	    
	    blipPos[0]=-newX;
	    blipPos[1]= newY;
	    
	    Rotation rot = new Rotation(Vector3D.PLUS_J, Vector3D.PLUS_K, getTopOrigin(), getHeadingOrigin());
	    final Vector3D rotationResult = rot.applyTo(getVectorHack().applyTo(new Vector3D(blipPos)));
	    blipPos[0] = rotationResult.getX();
	    blipPos[1] = rotationResult.getY();
	    blipPos[2] = rotationResult.getZ();
	    
	    setHeading(getHeadingOrigin().negate());
	    setTop(getTopOrigin());
	    
	    final Vector3D bp = getPositionOrigin();
	    blipPos[0]+=bp.getX();
	    blipPos[1]+=bp.getY();
	    blipPos[2]+=bp.getZ();
	    
	    notifyPositionChange();
	}//end refreshPosition()
    }//end Blip
    
    private final Collection<Blip> activeBlipBuffer = new CopyOnWriteArrayList<Blip>();
    
    public void refreshActiveBlips(){
	synchronized(activeBlips){
	 activeBlipBuffer.addAll(activeBlips);}
	for(Blip b: activeBlipBuffer)
	    b.refreshPosition();
	activeBlipBuffer.clear();
    }//end refreshActiveBlips()
    
    protected Blip submitRadarBlip(Positionable positionable){
	if(! (positionable instanceof DEFObject || positionable instanceof PowerupObject || positionable instanceof TunnelEntranceObject) )
	    return null;
	final double [] otherPos = positionable.getPosition();
	final double [] playerPos=getGameShell().getGame().getPlayer().getPosition();
	BlipType type=null;
	if(Vect3D.distance(playerPos, otherPos)<RADAR_RANGE){
	    if(positionable instanceof TunnelEntranceObject){
		if(!((TunnelEntranceObject)positionable).isVisible())
		    return null;//Invisible entrances are no-go.
	    }
	    if(otherPos[1]>playerPos[1]){
		//Higher
		if(positionable instanceof DEFObject){
		    final DEFObject def = (DEFObject)positionable;
		    if(!def.isFoliage() && (!def.isMobile())||(def.isGroundLocked())){
			type=BlipType.GRND_ABOVE;
		    }else if(def.isMobile()&&!def.isGroundLocked()){
			type=BlipType.AIR_ABOVE;
		    }
		}else if(positionable instanceof PowerupObject || positionable instanceof Jumpzone || positionable instanceof Checkpoint){
		    type=BlipType.PWR_ABOVE;
		    }else if(positionable instanceof TunnelEntranceObject){
			type=BlipType.TUN_ABOVE;
		    }//end if(TunnelEntranceObject)
	    }else{
		//Lower
		if(positionable instanceof DEFObject){
		final DEFObject def = (DEFObject)positionable;
		if(!def.isFoliage() && (!def.isMobile())||(def.isGroundLocked())){
		  type=BlipType.GRND_BELOW;
		}else if(def.isMobile()&&!def.isGroundLocked()){
		 type=BlipType.AIR_BELOW;
		}
		}else if(positionable instanceof PowerupObject || positionable instanceof Jumpzone || positionable instanceof Checkpoint){
		type=BlipType.PWR_BELOW;
		 }else if(positionable instanceof TunnelEntranceObject){
		  type=BlipType.TUN_BELOW;
		}//end if(TunnelEntranceObject)
	    }//end lower
	    if(type!=null){
		final Blip blip = newBlip(type);
		blip.setRepresentativeObject(positionable);
		blip.refreshPosition();//TODO: Use listeners instead?
		return blip;
	    }//end if(type == null)
	}//end if(RADAR_RANGE)
	return null;
    }//end submitRadarBlip(...)
    
    public void refreshBlips(Collection<WorldObject> newBlipObjects){
	resetBlipCounters();
	final Collection<Blip> oldActiveBlips = activeBlipBuffer;
	synchronized(activeBlips){
	    	 activeBlipBuffer.clear();
		 activeBlipBuffer.addAll(activeBlips);
		 activeBlips.clear();}
	
	newActiveBlips.clear();
	
	for(WorldObject wo : newBlipObjects){
	    final Blip blip = submitRadarBlip(wo);
	    if(blip != null)
		newActiveBlips.add(blip);
	    }
	synchronized(activeBlips){
	    activeBlips.clear();
	    activeBlips.addAll(newActiveBlips);
	}
	if(!newActiveBlips.equals(activeBlips))
	    throw new RuntimeException("Activeblips mismatch!");
	newlyEnabledBlips.clear();
	newlyEnabledBlips.addAll(newActiveBlips);
	newlyEnabledBlips.removeAll(oldActiveBlips);
	//final Collection<Blip> enabledBlips  = CollectionUtils.subtract(newActiveBlips, oldActiveBlips);
	final Collection<Blip> disabledBlips = CollectionUtils.subtract(oldActiveBlips, newActiveBlips);
	if(CollectionUtils.containsAny(newlyEnabledBlips, disabledBlips))
	    throw new RuntimeException("Enabled and disabled contain same");
	if(CollectionUtils.containsAny(newlyEnabledBlips, oldActiveBlips))
	    throw new RuntimeException("Old and enabled contain same");
	enableNewBlips(newlyEnabledBlips);
	disableBlips(disabledBlips);
	for(NAVRadarBlipFactoryListener l:listeners)
	    l.refreshBlips(newBlipObjects);
    }//end refreshBlips(...)
    
    protected void enableNewBlips(Collection<Blip> newBlipsToActivate){
	grid.addAll(newBlipsToActivate);
    }
    
    protected void disableBlips(Collection<Blip> newBlipsToDeactivate){
	grid.removeAll(newBlipsToDeactivate);
    }
    
    private Blip newBlip(BlipType t){
	Blip result = blipPool[t.ordinal()][poolIndices[t.ordinal()]++];
	poolIndices[t.ordinal()]%=poolIndices.length;
	return result;
    }
    
    protected void resetBlipCounters(){
	for( int i = 0; i < poolIndices.length; i++ )
	    poolIndices[i]=0;//reset index
    }//end resetBlipCounters()
    
    protected void clearUnusedRadarBlips(Collection<Blip> previouslyActiveBlips){
	final ArrayList<Blip> blipsToRemove = new ArrayList<Blip>();
	final int numPools = blipPool.length;
	for(int poolTypeIndex = 0; poolTypeIndex < numPools; poolTypeIndex++){
	    final Blip [] pool = blipPool[poolTypeIndex];
	    int blipStartIndex = poolIndices[poolTypeIndex];
	    for(int blipIndex = blipStartIndex; blipIndex < POOL_SIZE; blipIndex++){
		final Blip blip = pool[blipIndex];
		if(previouslyActiveBlips.contains(blip)){//Off transient
		    blipsToRemove.add(blip);
		}//end (off transient)
	    }//end for(blips)
	}//end for(pools)
	for(Blip blip : blipsToRemove)
	    blip.setVisible(false);//TODO: Remove from grid
    }//end clearUnusedRadarBlips()
    
    public void clearRadarBlips(){
	final Collection<Blip> activeBlips = getActiveBlips();
	synchronized(activeBlips){
	    activeBlips.clear();
	}
	int i=0;
	for(Blip [] pool:blipPool){
	    poolIndices[i++]=0;//reset index
	    for(Blip blip : pool)
		blip.setVisible(false);
	}//end for(pool)
    }//end clearRadarBlips()

    public Vector3D getTopOrigin() {
        return topOrigin;
    }

    public void setTopOrigin(Vector3D topOrigin) {
        this.topOrigin = topOrigin;
    }

    public Vector3D getHeadingOrigin() {
        return headingOrigin;
    }

    public void setHeadingOrigin(Vector3D headingOrigin) {
        this.headingOrigin = headingOrigin;
    }

    public Rotation getVectorHack() {
        return vectorHack;
    }

    public void setVectorHack(Rotation vectorHack) {
        this.vectorHack = vectorHack;
    }

    protected Collection<Blip> getActiveBlips() {
        return activeBlips;
    }

    protected Vector3D getPositionOrigin() {
        return positionOrigin;
    }

    public void setPositionOrigin(Vector3D positionOrigin) {
        this.positionOrigin = positionOrigin;
        refreshActiveBlips();
    }

    public Double getRadarGUIRadius() {
	if(radarGUIRadius == null)
	    radarGUIRadius = DEFAULT_RADAR_GUI_RADIUS;
        return radarGUIRadius;
    }

    protected void setRadarGUIRadius(Double radarGUIRadius) {
        this.radarGUIRadius = radarGUIRadius;
    }
    
    protected double getRadarScalar(){
	return getRadarGUIRadius()/RADAR_RANGE;
    }

    public void addBlipListener(NAVRadarBlipFactoryListener listener) {
	listeners.add(listener);
    }
    
    public void removeBlipListener(NAVRadarBlipFactoryListener listener) {
	listeners.remove(listener);
    }

    public boolean isRadarEnabled() {
        return radarEnabled;
    }

    public void setRadarEnabled(boolean radarEnabled) {
        this.radarEnabled = radarEnabled;
        refreshActiveBlips();
    }
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }

    public TR getTr(){
	return tr;
    }
}//end NAVRadarBlipFactory
