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

import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.obj.Sprite2D;

public class EarlyLoadingScreen extends RenderableSpacePartitioningGrid {
    private static final double FONT_SIZE=.07;
    private static final double Z = -1;
    private final CharLineDisplay 
				startupText;
    private final Sprite2D	startupLogo;

    public EarlyLoadingScreen(final TR tr, GLFont font) throws IOException {
	super();
	InputStream is = null;
	try{startupLogo = new Sprite2D(.000000001, 2, 2, 
		tr.gpu.get().textureManager.get().newTexture(ImageIO.read(is=VQTexture.class
			.getResourceAsStream("/TrclLogo.png")),null, "logoImage", false), true, "EarlyLoadingScreen");}
	finally{if(is!=null)is.close();}
	
	startupText = new CharLineDisplay(tr,this,FONT_SIZE, 32, font);
	startupText.setCentered(true);
	startupText.setPosition(0,0,Z);
	World.relevanceExecutor.submit(new Runnable(){
	    @Override
	    public void run() {
		add(startupLogo);
	    }});
    }//end constructor

    public void setStatusText(String string) {
	startupText.setContent(string);
    }

}//end EarlyLoadingScreen
