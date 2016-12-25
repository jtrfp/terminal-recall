/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola and contributors.
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gpu;

import java.awt.geom.Point2D;

public class SettableTexture extends DynamicTexture {
    //private int currentTexturePage;
    private Texture currentTexture;
/*
    @Override
    public int getCurrentTexturePage() {
	final Texture texture = getCurrentTexture();
	if(texture instanceof VQTexture){
	    final VQTexture vq = (VQTexture)texture;
	    return vq.getTexturePage();
	}
	throw new IllegalStateException("Texture for this settable texture must be a VQTexture for this method to work. Texture is  set to "+getCurrentTexture());
    }//end getcurrentTexturePage()
*/
    /*public void setCurrentTexturePage(int currentTexturePage) {
        this.currentTexturePage = currentTexturePage;
    }*/
    
    public void setCurrentTexture(Texture newCurrentTexture){
	this.currentTexture = newCurrentTexture;
    }

    public Texture getCurrentTexture() {
        return currentTexture;
    }

    @Override
    public Point2D.Double getSize() {
	final Texture texture = getCurrentTexture();
	if(texture == null)
	    throw new IllegalStateException("Current texture is intolerably null.");
	return texture.getSize();
    }

}//end SettableTexture
