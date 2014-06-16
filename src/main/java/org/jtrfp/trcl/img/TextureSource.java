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
package org.jtrfp.trcl.img;

import java.nio.ByteBuffer;

public interface TextureSource {
    /**
     * Get derasterized texture data as Red, Green, Blue, Alpha, Caustic,
     * Emissiveness and two unknowns, each in 4-bit format.
     * 
     * @return
     * @since Apr 6, 2014
     */
    public ByteBuffer getAsRGBACEnn4b();

    public int getWidth();

    public int getHeight();
}// end TextureSource
