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
package org.jtrfp.trcl.core;

import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.Reporter;
import org.jtrfp.trcl.mem.MemoryWindow;

public class SubTextureWindow extends MemoryWindow {
    public static final int BYTES_PER_CODE=1;
    public static final int SIDE_LENGTH_CODES = 36;
    public static final int SIDE_LENGTH_CODES_WITH_BORDER = SIDE_LENGTH_CODES+2;
    public static final int BYTES_PER_SUBTEXTURE = SIDE_LENGTH_CODES_WITH_BORDER*SIDE_LENGTH_CODES_WITH_BORDER*BYTES_PER_CODE;
    //38 tiles squared = 1444
    public final ByteArrayVariable codeIDs 		= new ByteArrayVariable(BYTES_PER_SUBTEXTURE);
    public final ByteArrayVariable unusedCID 		= new ByteArrayVariable(12);// 3 VEC4s
    //91VEC4
    public final IntArrayVariable codeStartOffsetTable 	= new IntArrayVariable(6);// 6x4B = 24B.
    //1480
    public final ByteArrayVariable unused 		= new ByteArrayVariable(56);
    
    public SubTextureWindow(GPU gpu){
	this.init(gpu,"SubTextureWindow");
    }
}//end SubTextureWindow
