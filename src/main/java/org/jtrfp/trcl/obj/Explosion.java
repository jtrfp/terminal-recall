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

import java.awt.Dimension;
import java.io.IOException;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;

public class Explosion extends OneShotBillboardEvent {
    public static final int NUM_FRAMES=16;
    private final ExplosionType type;
    public Explosion(TR tr, ExplosionType type, String debugName) {
	super(tr,type.getMillisPerFrame(),type.getAnimationFiles().length, debugName);
	final Vector3D origin = type.getOrigin();
	this.setModelOffset(
		origin.getX()*type.getBillboardSize().getWidth()*-.5, 
		origin.getY()*type.getBillboardSize().getHeight()*-.5, 
		origin.getZ());
	this.type=type;
	setBillboardSize(type.getBillboardSize());
	StaticRotationDelegate del = (StaticRotationDelegate)getRotationDelegate();
	if(type.isRandomRotate())
	    del.setRotationAngleRadians(2*Math.PI*Math.random());
	else
	    setRotationDelegate(new UpAlwaysCameraTopDelegate(tr.mainRenderer.get().getCamera()));
	String [] aniFiles = type.getAnimationFiles();
	Texture [] frames = new Texture[aniFiles.length];
	try{for(int i=0; i<aniFiles.length;i++){
	        frames[i]=frame(aniFiles[i]);
	    }
	}//end try{}
	catch(Exception e){e.printStackTrace();}
	setTexture(new AnimatedTexture(getSequencer(),frames),true);
    }//end constructor
    /*
    @Override
    public Explosion notifyPositionChange(){
	final double [] superPos = super.getPosition();
	//Vect3D.subtract(getPosition(),type.getOrigin().scalarMultiply(getBillboardSize().getHeight()/2.).toArray(),superPos);
	super.notifyPositionChange();
	return this;
    }*/
    
    public static enum ExplosionType{
	Blast(new String[]{
		"BLAST1.RAW",
		"BLAST2.RAW",
		"BLAST3.RAW",
		"BLAST4.RAW",
		"BLAST5.RAW",
		"BLAST6.RAW",
		"BLAST7.RAW",
		"BLAST8.RAW",
		"BLAST9.RAW",
		"BLAST10.RAW",
		"BLAST11.RAW",
		"BLAST12.RAW",
		"BLAST13.RAW",
		"BLAST14.RAW",
		"BLAST15.RAW",
		"BLAST16.RAW"
	},new Vector3D(0,0,0),new Dimension(20000,20000),100,true),
	Billow(new String[]{
		"BILLOW1.RAW",
		"BILLOW2.RAW",
		"BILLOW3.RAW",
		"BILLOW4.RAW",
		"BILLOW5.RAW",
		"BILLOW6.RAW",
		"BILLOW7.RAW",
		"BILLOW8.RAW",
		"BILLOW9.RAW",
		"BILLOW10.RAW",
		"BILLOW11.RAW",
		"BILLOW12.RAW",
		"BILLOW13.RAW",
		"BILLOW14.RAW",
		"BILLOW15.RAW",
		"BILLOW16.RAW",
	},new Vector3D(0,0,0),new Dimension(40000,40000),100,true),
	BigExplosion(new String[]{
		"BIGEX1.RAW",
		"BIGEX2.RAW",
		"BIGEX3.RAW",
		"BIGEX4.RAW",
		"BIGEX5.RAW",
		"BIGEX6.RAW",
		"BIGEX7.RAW",
		"BIGEX8.RAW",
		"BIGEX9.RAW",
		"BIGEX10.RAW",
		"BIGEX11.RAW",
		"BIGEX12.RAW",
		"BIGEX13.RAW",
		"BIGEX14.RAW",
		"BIGEX15.RAW",
		"BIGEX16.RAW",
	},new Vector3D(0,-1,0), new Dimension(80000,90000),120,false);
	
	private final String [] animationFiles;
	private final Vector3D origin;
	private final Dimension billboardSize;
	private final int millisPerFrame;
	private final boolean randomRotate;
	ExplosionType(String [] animationFiles,Vector3D origin, Dimension billboardSize, int millisPerFrame, boolean randomRotate){
	    this.animationFiles=animationFiles; 
	    this.origin=origin;
	    this.billboardSize=billboardSize;
	    this.millisPerFrame=millisPerFrame;
	    this.randomRotate=randomRotate;
	}
	public int getMillisPerFrame() {
	    return millisPerFrame;
	}
	public Dimension getBillboardSize() {
	    return billboardSize;
	}
	public String [] getAnimationFiles(){
	    return animationFiles;
	}
	public Vector3D getOrigin(){
	    return origin;}
	/**
	 * @return the randomRotate
	 */
	public boolean isRandomRotate() {
	    return randomRotate;
	}
    }//end ExplosionType

    private Texture frame(String name) throws IllegalAccessException,
	    IOException, FileLoadException {
	return (Texture) getTr().getResourceManager().getRAWAsTexture(name,
		getTr().getDarkIsClearPaletteVL(),null,
		false);
    }

}
