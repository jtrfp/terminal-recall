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

package org.jtrfp.trcl;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.obj.MeterBar;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class LevelLoadingScreen extends RenderableSpacePartitioningGrid {
    private static final double[] LOADING_POS = new double[] { 0,.1,0};
    private static final double LOADING_WIDTH =.04;
    private static final double LOADING_LENGTH=.7;
    ManuallySetController loadingMeter;
    MeterBar		  loadingMeterBar;

    public LevelLoadingScreen(SpacePartitioningGrid<PositionedRenderable> parent, TR tr) throws IOException {
	super();
	InputStream is = null;
	try{add(loadingMeterBar = new MeterBar( 
		Features.get(tr, GPUFeature.class).textureManager.get().newTexture(ImageIO.read(is = VQTexture.class
			.getResourceAsStream("/BlueWhiteGradient.png")),null,
			"LoadingBar blackBlue",false, false), LOADING_WIDTH, LOADING_LENGTH,
		true, "LevelLoadingScreen"));}
	finally{if(is!=null)try{is.close();}catch(Exception e){e.printStackTrace();}}
	loadingMeterBar.setPosition(LOADING_POS);
	loadingMeter = loadingMeterBar.getController();
    }//end constructor
    
    public LevelLoadingScreen setLoadingProgress(double unitPercent){
	loadingMeter.setFrame(1.-unitPercent);
	return this;
    }
}//end LevelLoadingScreen
