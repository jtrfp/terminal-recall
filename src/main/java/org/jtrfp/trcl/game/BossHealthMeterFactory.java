/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.game;

import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.springframework.stereotype.Component;

@Component
public class BossHealthMeterFactory implements FeatureFactory<TVF3Game> {

    @Override
    public Feature<TVF3Game> newInstance(TVF3Game target)
	    throws FeatureNotApplicableException {
	return new BossHealthMeterFeature();
    }

    @Override
    public Class<TVF3Game> getTargetClass() {
	return TVF3Game.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return BossHealthMeterFeature.class;
    }
    
    public static class BossHealthMeterFeature extends BossHealthMeter implements Feature<TVF3Game>{

	@Override
	public void apply(TVF3Game target) {
	    target.getBriefingScreen();
	    this.setFont(target.getGameShell().getGreenFont());
	    this.setGame(target);
	    InputStream is;
	    try{final Texture meterTexture = 
			Features.get(target.getTr(), GPUFeature.class).textureManager.get().newTexture(ImageIO.read(is = VQTexture.class
			.getResourceAsStream("/BlueWhiteGradient.png")),null,
			"BossHealthBar BlueBlack",false, false);
		if(is != null)
		    is.close();
		this.setMeterTexture(meterTexture);
	    }
	    catch(IOException e){e.printStackTrace();}
	    this.setTargetSPG(target.getPartitioningGrid());
	}//end apply(...)

	@Override
	public void destruct(TVF3Game target) {
	    // TODO Auto-generated method stub
	}
	
    }//end BossHEalthMeterFeature

}//end BossHealthMeterFactory
