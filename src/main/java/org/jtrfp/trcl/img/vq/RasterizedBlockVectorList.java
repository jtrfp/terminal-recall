package org.jtrfp.trcl.img.vq;

public final class RasterizedBlockVectorList implements VectorList {
    private final VectorList rasterizedVectorList;
    private final int rasterWidthInVectors, blockWidthInVectors;
    private final int vectorsPerBlock, blocksPerRow;

    public RasterizedBlockVectorList(VectorList rasterizedVectorList,
	    int rasterWidthInVectors, int blockWidthInVectors) {
	this.rasterWidthInVectors = rasterWidthInVectors;
	this.blockWidthInVectors = blockWidthInVectors;
	this.rasterizedVectorList = rasterizedVectorList;
	this.vectorsPerBlock = blockWidthInVectors * blockWidthInVectors;
	this.blocksPerRow = rasterWidthInVectors / blockWidthInVectors;
    }// end constructor

    @Override
    public int getNumVectors() {
	return rasterizedVectorList.getNumVectors() / vectorsPerBlock;
    }

    @Override
    public int getNumComponentsPerVector() {
	return rasterizedVectorList.getNumComponentsPerVector()
		* vectorsPerBlock;
    }

    @Override
    public double componentAt(int vectorIndex, int componentIndex) {
	final int row = vectorIndex / blocksPerRow;
	final int col = vectorIndex % blocksPerRow;
	final int x = col * blockWidthInVectors;
	final int y = row * blockWidthInVectors;
	return rasterizedVectorList.componentAt(y * rasterWidthInVectors + x
		* blockWidthInVectors, componentIndex);
    }// end componentAt(...)

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	final int row = vectorIndex / blocksPerRow;
	final int col = vectorIndex % blocksPerRow;
	final int x = col * blockWidthInVectors;
	final int y = row * blockWidthInVectors;
	rasterizedVectorList.setComponentAt(y * rasterWidthInVectors + x
		* blockWidthInVectors, componentIndex, value);
    }// end setComponentAt(...)

}// end RasterizedBlockVectorList
