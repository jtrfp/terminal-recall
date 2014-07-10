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

import java.util.TimerTask;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.Sprite2D;

public class BriefingScreen extends RenderableSpacePartitioningGrid {
    private static final double Z = -1;
    private final Sprite2D	  briefingScreen;
    private final CharAreaDisplay briefingChars;

    public BriefingScreen(SpacePartitioningGrid<PositionedRenderable> parent, final TR tr, GLFont font) {
	super(parent);
	briefingScreen = new Sprite2D(tr,.000000001, 2, 2,
		tr.getResourceManager().getSpecialRAWAsTextures("BRIEF.RAW", tr.getGlobalPalette(),
		tr.gpu.get().getGl(), 0,false),true);
	add(briefingScreen);
	
	briefingChars = new CharAreaDisplay(this,32,7,tr,font);
	briefingChars.activate();
	briefingChars.setContent("The quick\nbrown\nfox\njumps over\nthe lazy\ndog.\nAnd a bag of chips.");
	tr.getThreadManager().getLightweightTimer().scheduleAtFixedRate(new TimerTask(){
	    @Override
	    public void run() {
		briefingChars.setScollPosition(((double)System.currentTimeMillis()/1000.)%14.);
	    }}, 0, 20);
	briefingChars.setPosition(-.7, -.5, 0);
	
	briefingScreen.setPosition(0,0,Z);
	briefingScreen.notifyPositionChange();
	briefingScreen.setActive(true);
	briefingScreen.setVisible(true);
    }//end constructor

}//end BriefingScreen
