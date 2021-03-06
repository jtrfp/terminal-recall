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

import org.jtrfp.trcl.gpu.VQTexture;

public class SelectableTexture extends AnimatedTexture{
	public SelectableTexture(VQTexture[] frames)
		{super(new Selector(), frames);}
	
	public void setFrame(int frame){((Selector)getTextureSequencer()).set(frame);}

	public int getCurrentFrameNumber() {
	    return (int)((Selector)getTextureSequencer()).getCurrentFrame();
	}
}//end SelectableTexture
