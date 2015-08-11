/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola and contributors. See CREDITS for details.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.prop;

import java.io.IOException;

import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.obj.Sprite2D;
import org.jtrfp.trcl.snd.MusicPlaybackEvent;

public class IntroScreen extends RenderableSpacePartitioningGrid {
    private final MusicPlaybackEvent bgMusic;
    
    public IntroScreen(TR tr, String backdropResource, String musicResource) throws IllegalAccessException, IOException, FileLoadException {
	super();
	add(new BackdropSprite(tr,backdropResource,musicResource));
	if(musicResource!=null)
	 bgMusic = tr.soundSystem.get().
	  getMusicFactory().
	  create(tr.getResourceManager().gpuResidentMODs.get(musicResource), true);
	else
	 bgMusic = null;
	if(bgMusic!=null)
	    tr.soundSystem.get().enqueuePlaybackEvent(bgMusic);
    }//end IntroScreen(...)

    private class BackdropSprite extends Sprite2D{
	public BackdropSprite(TR tr, String backdropResource, String musicResource) throws IllegalAccessException, IOException, FileLoadException{
		super(tr, .000001, 2, 2, genTexture(backdropResource, tr), false);
	    }
    }//end BackdropSprite(...)
    
    private static TextureDescription [] genTexture(String resourceName, TR tr) throws IllegalAccessException, IOException, FileLoadException{
	final ResourceManager rm = tr.getResourceManager();
	return rm.getSpecialRAWAsTextures(resourceName, rm.getPalette("VGA.ACT"), tr.gpu.get().getGl(), 1, true);
    }//end genTexture
    
    public void startMusic(){
	if(bgMusic!=null)
	 bgMusic.play();
    }
    
    public void stopMusic(){
	if(bgMusic!=null)
	 bgMusic.stop();
    }

    public boolean isMusicPlaying() {
	if(bgMusic==null)
	    return false;
	return bgMusic.isActive();
    }
}//end IntroScreen
