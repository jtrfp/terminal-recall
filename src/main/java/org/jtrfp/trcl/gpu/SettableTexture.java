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

public class SettableTexture extends DynamicTexture {
    private int currentTexturePage;

    @Override
    public int getCurrentTexturePage() {
	return currentTexturePage;
    }

    public void setCurrentTexturePage(int currentTexturePage) {
        this.currentTexturePage = currentTexturePage;
    }

}//end SettableTexture
