/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.prop;

import java.awt.Color;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.math.Misc;

public class HorizGradientCubeGen implements SkyCubeGen {
    private static final HashMap<String,WeakReference<ByteBuffer>> resourceMap = new HashMap<String,WeakReference<ByteBuffer>>();
    private ByteBuffer  top,bottom,east,west,north,south;
    private static final String NULL = ""; // This is to make sure null has a hashcode.
    private String      topTexture=NULL,bottomTexture=NULL,eastTexture=NULL,westTexture=NULL,northTexture=NULL,southTexture=NULL;
    private final Color topColor,bottomColor;
    private float       verticalBias = 0;

    public HorizGradientCubeGen(Color bottomColor, Color topColor) {
	this.topColor   =topColor;
	this.bottomColor=bottomColor;
    }
    
    private ByteBuffer getResource(String resourceName){
	ByteBuffer bb=null;
	if(resourceMap.get(resourceName)!=null)
	 bb = resourceMap.get(resourceName).get();
	if(bb==null){
	    InputStream is = null;
	    try    {bb = Texture.RGBA8FromPNG(is = Class.class.getResourceAsStream(resourceName));}
	    finally{try{if(is!=null)is.close();}catch(Exception e){e.printStackTrace();}}
	    if(bb==null) throw new NullPointerException("Failed to get resource "+resourceName);
	    resourceMap.put(resourceName, new WeakReference<ByteBuffer>(bb));
	}//end if(null)
	return bb;
    }//end getResource(...)

    @Override
    public ByteBuffer getTop() {
	return (top = ensureGrad(top,topColor,topColor,topTexture)).duplicate();
    }

    @Override
    public ByteBuffer getBottom() {
	return (bottom = ensureGrad(bottom,bottomColor,bottomColor,bottomTexture)).duplicate();
    }

    @Override
    public ByteBuffer getNorth() {
	return (north = ensureGrad(north,bottomColor,topColor,northTexture)).duplicate();
    }

    @Override
    public ByteBuffer getSouth() {
	return (south = ensureGrad(south,bottomColor,topColor,southTexture)).duplicate();
    }

    @Override
    public ByteBuffer getWest() {
	return (west = ensureGrad(west,bottomColor,topColor,westTexture)).duplicate();
    }

    @Override
    public ByteBuffer getEast() {
	return (east = ensureGrad(east,bottomColor,topColor,topTexture)).duplicate();
    }
    
    private ByteBuffer ensureGrad(ByteBuffer bb, Color low, Color hi, String textureName){
	if(bb==null){
	    ByteBuffer tBuf = null;
	    int sideWidth = getSideWidth();
	    if(textureName!=null && textureName != NULL){
		tBuf = getResource(textureName);
		if(tBuf==null)
		    throw new RuntimeException("Failed to find resource '"+textureName+"'.");
		sideWidth =  (int)Math.sqrt(tBuf.capacity()/4);
		tBuf.clear();
	    }
	    bb = ByteBuffer.allocate(sideWidth*sideWidth*4);
	    bb.clear();
	    if(tBuf==null){
		for(int y=0; y<sideWidth; y++){
		    final float lW = ((float)y)/(float)sideWidth;
		    final float hW = 1f-lW;
		    final Color c = new Color(
			    (low.getRed()*lW+hi.getRed()*hW)/256f,
			    (low.getGreen()*lW+hi.getGreen()*hW)/256f,
			    (low.getBlue()*lW+hi.getBlue()*hW)/256f,
			    (low.getAlpha()*lW+hi.getAlpha()*hW)/256f);
		    final byte r = (byte)c.getRed();
		    final byte g = (byte)c.getGreen();
		    final byte b = (byte)c.getBlue();
		    for(int x=0; x<sideWidth; x++)
		     {bb.put(r);bb.put(g);bb.put(b);bb.put((byte)1);}
		}//end for(y)
	    }else { // Mix a texture
		for(int y=0; y<sideWidth; y++){
		    final float lW = (float)Misc.clamp(((((float)y)/(float)sideWidth)/(1-verticalBias))-verticalBias,0,1);
		    final float hW = 1f-lW;
		    final Color c = new Color(
			    (low.getRed()*lW+hi.getRed()*hW)/256f,
			    (low.getGreen()*lW+hi.getGreen()*hW)/256f,
			    (low.getBlue()*lW+hi.getBlue()*hW)/256f,
			    (low.getAlpha()*lW+hi.getAlpha()*hW)/256f);

		    final float a = (float)c.getAlpha()/255f;
		    final float aI = 1f-a;
		    for(int x=0; x<sideWidth; x++){
			byte r = (byte)((float)c.getRed()*a + aI * (float)(tBuf.get()&0xFF));
			byte g = (byte)((float)c.getGreen()*a + aI * (float)(tBuf.get()&0xFF));
			byte b = (byte)((float)c.getBlue()*a + aI * (float)(tBuf.get()&0xFF));
			bb.put(r);bb.put(g);bb.put(b);bb.put(tBuf.get());
			}//end for(x)
		}//end for(y)
	    }//end if(tBuf!=null)
	}//end if(null)
	bb.clear();
	return bb;
    }//end ensureGrad(...)
    
    @Override
    public int hashCode(){
	return this.getClass().hashCode()
		+topColor.getRed()*1
		+topColor.getGreen()*256
		+topColor.getBlue()*1024
		+topColor.getAlpha()*65535
		+65535*256*(
		 bottomColor.getRed()*1
		+bottomColor.getGreen()*256
		+bottomColor.getBlue()*1024
		+bottomColor.getAlpha()*65535)
		+65535*1024*(
		 northTexture.hashCode()*1
		+southTexture.hashCode()*256
		+eastTexture.hashCode()*1024
		+westTexture.hashCode()*65535)+
		(int)(65535f*65535f*verticalBias);
    }//end hashCode()

    /**
     * @param topTexture the topTexture to set
     */
    public HorizGradientCubeGen setTopTexture(String topTexture) {
        this.topTexture = topTexture==null?NULL:topTexture;
        ackResource(topTexture);
        return this;
    }

    /**
     * @param bottomTexture the bottomTexture to set
     */
    public HorizGradientCubeGen setBottomTexture(String bottomTexture) {
        this.bottomTexture = bottomTexture==null?NULL:bottomTexture;
        ackResource(bottomTexture);
        return this;
    }

    /**
     * @param eastTexture the eastTexture to set
     */
    public HorizGradientCubeGen setEastTexture(String eastTexture) {
        this.eastTexture = eastTexture==null?NULL:eastTexture;
        ackResource(eastTexture);
        return this;
    }

    /**
     * @param westTexture the westTexture to set
     */
    public HorizGradientCubeGen setWestTexture(String westTexture) {
        this.westTexture = westTexture==null?NULL:westTexture;
        ackResource(westTexture);
        return this;
    }

    /**
     * @param northTexture the northTexture to set
     */
    public HorizGradientCubeGen setNorthTexture(String northTexture) {
        this.northTexture = northTexture==null?NULL:northTexture;
        ackResource(northTexture);
        return this;
    }

    /**
     * @param southTexture the southTexture to set
     */
    public HorizGradientCubeGen setSouthTexture(String southTexture) {
        this.southTexture = southTexture==null?NULL:southTexture;
        ackResource(southTexture);
        return this;
    }
    
    private void ackResource(String rsc){
	resourceMap.put(rsc, null);
    }

    @Override
    public int getSideWidth() {
	int maxSideWidth = 0;
	maxSideWidth = processSideWidth(topTexture,maxSideWidth);
	maxSideWidth = processSideWidth(bottomTexture,maxSideWidth);
	maxSideWidth = processSideWidth(northTexture,maxSideWidth);
	maxSideWidth = processSideWidth(southTexture,maxSideWidth);
	maxSideWidth = processSideWidth(eastTexture,maxSideWidth);
	maxSideWidth = processSideWidth(westTexture,maxSideWidth);
	if(maxSideWidth==0)
	    return 32;
	else 
	    return maxSideWidth;
    }//end getSideWidth()
    
    private int processSideWidth(String rc, int max){
	if(rc==null || rc==NULL)return max;
	final int w = (int)Math.sqrt((getResource(rc).capacity()/4));
	return w>max?w:max;
    }

    /**
     * @param verticalBias the verticalBias to set
     */
    public HorizGradientCubeGen setVerticalBias(float verticalBias) {
        this.verticalBias = verticalBias;
        return this;
    }
    
    @Override
    public String toString(){
	return this.getClass().toString()+" verticalBias="+verticalBias+" sideWidth="+getSideWidth();
    }
}//end HorizGradientCubeGen
