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

import javax.imageio.ImageIO;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.math.Vect3D;

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
    
    public NAVRadarBlipFactory(TR tr, RenderableSpacePartitioningGrid g, String debugName, boolean ignoreCamera){
	this(tr,g,null,debugName,ignoreCamera);
    }
    
    public NAVRadarBlipFactory(TR tr, RenderableSpacePartitioningGrid g, Double radarGUIRadius, String debugName, boolean ignoreCamera){
	this.tr    =tr;
	setRadarGUIRadius(radarGUIRadius);
	final BlipType [] types = BlipType.values();
	for(int ti=0; ti<types.length; ti++){
	    InputStream is = null;
	    try{
	     final VQTexture tex = tr.gpu.get().textureManager.get().newTexture(ImageIO.read(is = this.getClass().getResourceAsStream("/"+types[ti].getSprite())),null,"",false);
    	     for(int pi=0; pi<POOL_SIZE; pi++){
    		final Blip blip = new Blip(tex,debugName, ignoreCamera, .04*(getRadarGUIRadius()/DEFAULT_RADAR_GUI_RADIUS));
    		blipPool[ti][pi]= blip;
    		g.add(blip);
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
	    super(tr,0,diameter,diameter,tex,true,debugName);
	    setImmuneToOpaqueDepthTest(true);
	    if(!ignoreCamera)
	     unsetRenderFlag(RenderFlags.IgnoreCamera);
	}//end constructor

	public void setRepresentativeObject(Positionable representativeObject) {
	    this.representativeObject = representativeObject;
	}

	public void refreshPosition() {
	    final double []blipPos = getPosition();
	    final double [] playerPos=tr.getGame().getPlayer().getPosition();
	    Vect3D.subtract(representativeObject.getPosition(), playerPos, blipPos);
	    Vect3D.scalarMultiply(blipPos, getRadarScalar(), blipPos);
	    final double [] heading = tr.getGame().getPlayer().getHeadingArray();
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
    
    public void refreshActiveBlips(){
	for(Blip b: activeBlips)
	    b.refreshPosition();
    }//end refreshActiveBlips()
    
    @Override
    public void submitRadarBlip(Positionable positionable){
	if(! (positionable instanceof DEFObject || positionable instanceof PowerupObject || positionable instanceof TunnelEntranceObject) )return;
	final double [] otherPos = positionable.getPosition();
	final double [] playerPos=tr.getGame().getPlayer().getPosition();
	BlipType type=null;
	if(Vect3D.distance(playerPos, otherPos)<RADAR_RANGE){
	    if(positionable instanceof TunnelEntranceObject){
		if(!((TunnelEntranceObject)positionable).isVisible())return;//Invisible entrances are no-go.
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
		getActiveBlips().add(blip);
		blip.setRepresentativeObject(positionable);
		blip.refreshPosition();//TODO: Use listeners instead?
	    }//end if(type == null)
	}//end if(RADAR_RANGE)
	for(NAVRadarBlipFactoryListener l:listeners)
	    l.submitRadarBlip(positionable);
    }//end submitRadarBlip(...)
    
    private Blip newBlip(BlipType t){
	Blip result = blipPool[t.ordinal()][poolIndices[t.ordinal()]++];
	poolIndices[t.ordinal()]%=poolIndices.length;
	return result;
    }
    
    @Override
    public void clearRadarBlips(){
	getActiveBlips().clear();
	int i=0;
	for(Blip [] pool:blipPool){
	    poolIndices[i++]=0;//reset index
	    for(Blip blip: pool){
		final double [] pos = blip.getPosition();
		pos[0]=2; pos[1]=2;blip.notifyPositionChange();
	    }//end for(blip)
	}//end for(pool)
	for(NAVRadarBlipFactoryListener l:listeners)
	    l.clearRadarBlips();
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
}//end NAVRadarBlipFactory
