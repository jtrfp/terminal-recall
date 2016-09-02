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
package org.jtrfp.trcl.gpu;

import org.jtrfp.trcl.mem.MemoryWindow;

public class TextureTOCWindow extends MemoryWindow {
    public static final int WIDTH_IN_SUBTEXTURES=19;
    //19^2 * 4 = 1444 bytes
    public final IntArrayVariable 	subtextureAddrsVec4	= new IntArrayVariable(361);
    public final ByteArrayVariable 	filler0			= new ByteArrayVariable(12);//12 bytes to quantize to next VEC4
    //Offset 1456B, 91VEC4
    public final IntVariable 		width			= new IntVariable();//4B
    public final IntVariable 		height			= new IntVariable();//4B
    public final IntVariable		renderFlags		= new IntVariable();//4B
    	/**
    	 * BIT 0: Wrapping on/off (true=on)
    	 */
    public final IntVariable		magic			= new IntVariable();//4B
    //Tally: 1472B
    public final ByteArrayVariable 	filler1			= new ByteArrayVariable(64);//64B
    
    public TextureTOCWindow(GPU gpu){
	this.init(gpu, "TextureTOCWindow");
    }//end constructor
    
    public TextureTOCWindow(){}
}//end TextureTOCWindow
