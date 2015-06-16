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

    @Test
    public void test() {
	fail("Not yet implemented");
    }

}//end TRIT
