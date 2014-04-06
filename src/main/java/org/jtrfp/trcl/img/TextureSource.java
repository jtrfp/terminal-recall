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
