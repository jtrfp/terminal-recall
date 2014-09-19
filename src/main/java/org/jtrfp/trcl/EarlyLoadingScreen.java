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

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.Sprite2D;

public class EarlyLoadingScreen extends RenderableSpacePartitioningGrid {
    private static final double FONT_SIZE=.07;
    private static final double Z = -1;
    private final CharLineDisplay 
				startupText;
    private final Sprite2D	
				startupLogo;

    public EarlyLoadingScreen(SpacePartitioningGrid<PositionedRenderable> parent, final TR tr, GLFont font) {
	super(parent);
	startupLogo = new Sprite2D(tr, .000000001, 2, 2, 
		tr.gpu.get().textureManager.get().newTexture(Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/TrclLogo.png")), "logoImage", false), true);
	add(startupLogo);
	startupText = new CharLineDisplay(tr,this,FONT_SIZE, 32, font);
	startupText.setCentered(true);
	startupText.setPosition(0,0,Z);
    }//end constructor

    public void setStatusText(String string) {
	startupText.setContent(string);
    }

}//end EarlyLoadingScreen
