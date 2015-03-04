/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.snd;

import org.jtrfp.trcl.gpu.GLTexture;

public interface SoundTexture {
    
    public static final int ROW_LENGTH_SAMPLES = 1024;

    double getLengthInRealtimeSeconds();

    GLTexture getGLTexture();
    
    /**
     * Effectively the height of the texture.
     * @return
     * @since Oct 28, 2014
     */
    int getNumRows();

    double getLengthPerRowSeconds();

}//end SoundTexture
