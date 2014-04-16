package org.jtrfp.trcl.img.vq;

public interface VectorList {
    public int getNumVectors();

    public int getNumComponentsPerVector();

    public double componentAt(int vectorIndex, int componentIndex);

    public void setComponentAt(int vectorIndex, int componentIndex, double value);
}
