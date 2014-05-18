package org.jtrfp.trcl.img.vq;

public final class RGBA8888VectorList implements VectorList {
    private final VectorList 	source;
    private final int 		singletOffset;

    public RGBA8888VectorList(VectorList source, int singletOffset) {
	this.source = source;
	this.singletOffset = singletOffset;
    }

    public RGBA8888VectorList(VectorList source) {
	this(source, 0);
    }

    @Override
    public int getNumVectors() {
	return source.getNumVectors() / 4;
    }

    @Override
    public int getNumComponentsPerVector() {
	return 4;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	return source.componentAt(vectorIndex * 4 + componentIndex,
		singletOffset);
    }

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	source.setComponentAt(vectorIndex * 4 + componentIndex, singletOffset,
		value);
    }
}// end RGBA8888VectorList
