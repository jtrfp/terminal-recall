package org.jtrfp.trcl.img.vq;

import java.nio.ByteBuffer;

import org.jtrfp.trcl.math.Misc;

public class ByteBufferVectorList implements VectorList {
    private final ByteBuffer bb;

    public ByteBufferVectorList(ByteBuffer bb) {
	this.bb = bb;
    }

    @Override
    public int getNumVectors() {
	return bb.capacity();
    }

    @Override
    public int getNumComponentsPerVector() {
	return 1;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return (double) (bb.get(vectorIndex) & 0xFF) / 255.;
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	value = Misc.clamp(value * 255, 0, 255);
	bb.put(vectorIndex, (byte) value);
    }

}// end ByteBufferVectorList
