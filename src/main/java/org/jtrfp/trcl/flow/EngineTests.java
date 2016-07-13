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
package org.jtrfp.trcl.flow;

import java.io.InputStream;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class EngineTests {

    private static void preClean(TR tr){
	Features.get(tr, GameShell.class).abortCurrentGame();
	tr.getDefaultGrid().blockingRemoveAll();
    }
    public static void singlet(TR tr, int numInstances) {
	try{
	final Texture test = tr.getResourceManager().getTestTexture();
	preClean(tr);
	final int sideLen = (int)Math.ceil(Math.sqrt(numInstances));
	final double diameter = 2./(double)sideLen;
	final double off = diameter/2;
	for (int i = 0; i < numInstances; i++) {
	    WorldObject wo = new Sprite2D(0, diameter, diameter, test, false,"EngineTest.singlet");
	    wo.setPosition(new double[] { (i%sideLen)*diameter-1+off, (i/sideLen)*diameter-1+off, .01 });
	    wo.setActive(true);
	    wo.setVisible(true);
	    tr.getDefaultGrid().add(wo);
	}//end for(numInstances)
	}catch(Exception e){e.printStackTrace();}
    }//end singlet(tr)
    
    public static void depthQueueTest(TR tr){
	preClean(tr);
	InputStream is = null;
	try{
	 final Texture test = Features.get(tr, GPUFeature.class).textureManager.get().newTexture(
		    VQTexture.RGBA8FromPNG(is = tr.getClass().getResourceAsStream("/dqTestTexture.png")),null, "dqTestTexture", true);
	 final int NUM_LAYERS=8;
	 final double INCREMENT = .1;
	 final double OFF=-.5;
	 for (int i = 0; i < NUM_LAYERS; i++) {
	    WorldObject wo = new Sprite2D(0, 1, 1, test, true,"EngineTest.depthQueueTest");
	    wo.setPosition(new double[] { OFF+((double)i)*INCREMENT,OFF+((double)i)*INCREMENT, .01*(double)i });
	    wo.setActive(true);
	    wo.setVisible(true);
	    tr.getDefaultGrid().add(wo);
	}//end for(numInstances)
	}finally{try{if(is!=null)is.close();}catch(Exception e){e.printStackTrace();}}
    }
}//end EngineTests
