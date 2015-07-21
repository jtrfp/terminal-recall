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
package org.jtrfp.trcl;
import static org.junit.Assert.*;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.obj.WorldObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TRIT {
    protected TR tr;
    protected double errorThreshold = 0;

    @Before
    public void setUp() throws Exception{
	tr = new TR(new TRConfiguration());
	tr.getDefaultGrid().removeAll();
	tr.getThreadManager().setPaused(false);
	final Renderer renderer = tr.mainRenderer.get();
	renderer.setAmbientLight(Color.gray);
	renderer.setSunVector(new Vector3D(1,2,.25).normalize());
	Camera c = tr.mainRenderer.get().getCamera();
	c.setHeading(Vector3D.PLUS_K);
	c.setTop(Vector3D.PLUS_J);
	c.setPosition(new double[]{0,0,-50000});
	c.notifyPositionChange();
	c.setActive(true);
    }

    @After
    public void tearDown() throws Exception {
	tr.threadManager.setPaused(true);
	tr.soundSystem.get().setPaused(true);
	tr.getRootWindow().setVisible(false);
    }
    
    protected void spawnCubes(){//Ordering not guaranteed so we will keep it at one cube per primitive type
	final TextureDescription texture = tr.gpu.get().textureManager.get().getFallbackTexture();
	//OPAQUE
	WorldObject obj;
	obj = new WorldObject(tr);
	obj.setModel(Model.buildCube(10000, 10000, 10000, texture, new double[]{5000,5000,5000}, false, tr));
	obj.setHeading(new Vector3D(1,1,1).normalize());
	obj.setTop(new Vector3D(-1,1,1).normalize());
	obj.setPosition(new double[]
		{-10000,0,0});
	obj.notifyPositionChange();
	obj.setVisible(true);
	obj.setActive (true);
	tr.getDefaultGrid().add(obj);
	//TRANSPARENT
	obj = new WorldObject(tr);
	obj.setModel(Model.buildCube(10000, 10000, 10000, texture, new double[]{5000,5000,5000}, true, tr));
	obj.setHeading(new Vector3D(1,1,1).normalize());
	obj.setTop(new Vector3D(-1,1,1).normalize());
	obj.setPosition(new double[]
		{10000,0,0});
	obj.notifyPositionChange();
	obj.setVisible(true);
	obj.setActive (true);
	tr.getDefaultGrid().add(obj);
    }//end spawnCubes()
    
    protected boolean testFloatResults(ByteBuffer bb, ByteBuffer refBB){
	double error=0;
	boolean result = true;
	while(bb.hasRemaining()){
	    float ref = refBB.getFloat();
	    float res = bb.getFloat();
	    error = Math.abs(ref-res);
	    if(error > errorThreshold){
		System.out.println("ref="+ref+" res="+res+" pos="+bb.position());
		result = false;
		}
	    }
	return result;
    }
    
    protected boolean testByteResults(ByteBuffer bb, ByteBuffer refBB){
	boolean result = true;
	while(bb.hasRemaining()){
	    byte ref = refBB.get();
	    byte res = bb.get();
	    if(ref!=res){
		System.out.println("ref="+ref+" res="+res+" pos="+bb.position());
		result = false;
		}
	    }
	return result;
    }
    
    protected void clearOldResult(String file){
	File f = new File(file+".result");
	if(f.exists())f.delete();
    }
    
    protected void dumpContentsToFile(ByteBuffer bb, String file) throws Exception{
	bb.clear();
	final File f = new File(file);
	if(f.exists())f.delete();
	FileOutputStream fos = new FileOutputStream(file);
	FileChannel channel = fos.getChannel();
	channel.write(bb);
	channel.close();
	fos    .close();
	System.out.println("Dumped contents to "+file);
    }
    
    /**
     * The target ByteBuffer's position is cleared after writing.
     * @param bb
     * @param file
     * @throws Exception
     * @since Jun 14, 2015
     */
    protected void readContentsFromResource(ByteBuffer bb, String file) throws Exception{
	InputStream is = Class.class.getResourceAsStream("/"+file);
	assertNotNull("Failed to load resource /"+file,is);
	int val;
	while((val=is.read())!=-1)
	    bb.put((byte)val);
	is.close();
	bb.clear();
    }

}//end TRIT
