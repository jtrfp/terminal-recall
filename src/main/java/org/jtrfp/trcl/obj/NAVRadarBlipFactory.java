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

import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.DummyTRFutureTask;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.math.Vect3D;

public class NAVRadarBlipFactory {
    private static final int POOL_SIZE=15;
    private static final int RADAR_RANGE=800000;
    private static final double RADAR_GUI_RADIUS=.17;
    private static final double RADAR_SCALAR=RADAR_GUI_RADIUS/RADAR_RANGE;
    private final Blip [][] blipPool = new Blip[BlipType.values().length][POOL_SIZE];
    private final int []poolIndices = new int[POOL_SIZE];
    private final TR tr;
    
    public NAVRadarBlipFactory(TR tr, RenderableSpacePartitioningGrid g){
	this.tr=tr;
	final BlipType [] types = BlipType.values();
	for(int ti=0; ti<types.length; ti++){
	    try{
	     final Texture tex = tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(this.getClass().getResourceAsStream("/"+types[ti].getSprite())),"",false);
    	     for(int pi=0; pi<POOL_SIZE; pi++){
    		blipPool[ti][pi]=new Blip(new DummyTRFutureTask<TextureDescription>(tex),g);
    	     }//end for(pi)
	    }catch(Exception e){e.printStackTrace();}
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
	public Blip(TRFutureTask<TextureDescription> tex, RenderableSpacePartitioningGrid g) {
	    super(tr,-1,.02,.02,tex,true);
	    g.add(this);
	}//end constructor
    }//end Blip
    
    public void submitRadarBlip(WorldObject wo){
	if(! (wo instanceof DEFObject || wo instanceof PowerupObject || wo instanceof TunnelEntranceObject) )return;
	final double [] otherPos = wo.getPosition();
	final double [] playerPos=tr.getPlayer().getPosition();
	BlipType type=null;
	if(Vect3D.distance(playerPos, otherPos)<RADAR_RANGE){
	    if(wo instanceof TunnelEntranceObject){
		if(!((TunnelEntranceObject)wo).isVisible())return;//Invisible entrances are no-go.
	    }
	    if(otherPos[1]>playerPos[1]){
		//Higher
		if(wo instanceof DEFObject){
		    final DEFObject def = (DEFObject)wo;
		    if(!def.isFoliage() && (!def.isMobile())||(def.isGroundLocked())){
			type=BlipType.GRND_ABOVE;
		    }else if(def.isMobile()&&!def.isGroundLocked()){
			type=BlipType.AIR_ABOVE;
		    }
		}else if(wo instanceof PowerupObject){
		    type=BlipType.PWR_ABOVE;
		    }else if(wo instanceof TunnelEntranceObject){
			type=BlipType.TUN_ABOVE;
		    }//end if(TunnelEntranceObject)
	    }else{
		//Lower
		if(wo instanceof DEFObject){
		final DEFObject def = (DEFObject)wo;
		if(!def.isFoliage() && (!def.isMobile())||(def.isGroundLocked())){
		  type=BlipType.GRND_BELOW;
		}else if(def.isMobile()&&!def.isGroundLocked()){
		 type=BlipType.AIR_BELOW;
		}
		}else if(wo instanceof PowerupObject){
		type=BlipType.PWR_BELOW;
		 }else if(wo instanceof TunnelEntranceObject){
		  type=BlipType.TUN_BELOW;
		}//end if(TunnelEntranceObject)
	    }//end lower
	    if(type!=null){
		final Blip blip = newBlip(type);
		final double []blipPos = blip.getPosition();
		Vect3D.subtract(otherPos, playerPos, blipPos);
		Vect3D.scalarMultiply(blipPos, RADAR_SCALAR, blipPos);
		final double [] heading = tr.getPlayer().getHeadingArray();
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
		blipPos[1]=newY;
		
		blipPos[0]+=.825;
		blipPos[1]+=.8;
		
		blip.notifyPositionChange();
	    }
	}//end if(RADAR_RANGE)
	
    }//end submitRadarBlip(...)
    
    private Blip newBlip(BlipType t){
	Blip result = blipPool[t.ordinal()][poolIndices[t.ordinal()]++];
	poolIndices[t.ordinal()]%=poolIndices.length;
	return result;
    }
    
    public void clearRadarBlips(){
	int i=0;
	for(Blip [] pool:blipPool){
	    poolIndices[i++]=0;//reset index
	    for(Blip blip: pool){
		final double [] pos = blip.getPosition();
		pos[0]=2; pos[1]=2;//TODO Putting it out of view is faster than making it invisible?!
	    }//end for(blip)
	}//end for(pool)
    }//end clearRadarBlips()
}//end NAVRadarBlipFactory
