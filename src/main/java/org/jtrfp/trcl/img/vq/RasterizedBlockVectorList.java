package org.jtrfp.trcl.img.vq;

public final class RasterizedBlockVectorList implements VectorList {
    private final VectorList 	rasterizedVectorList;
    private final int 		rasterWidthInVectors, 
    /*			*/	blockWidthInVectors,
    /*			*/	vectorsPerBlock,
    /*			*/	blocksPerRow;

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
	final int sx = componentIndex % blockWidthInVectors;
	final int sy = (componentIndex % vectorsPerBlock) / blockWidthInVectors;
	final int x = col * blockWidthInVectors + sx;
	final int y = (row * blockWidthInVectors) + sy;

	return rasterizedVectorList.componentAt(y * rasterWidthInVectors + x,
		componentIndex);
    }// end componentAt(...)

    @Override
    public void setComponentAt(int vectorIndex, int componentIndex, double value) {
	final int row = vectorIndex / blocksPerRow;
	final int col = vectorIndex % blocksPerRow;
	final int sx = componentIndex % blockWidthInVectors;
	final int sy = (componentIndex % vectorsPerBlock) / blockWidthInVectors;
	final int x = col * blockWidthInVectors + sx;
	final int y = (row * blockWidthInVectors) + sy;

	rasterizedVectorList.setComponentAt(y * rasterWidthInVectors + x,
		componentIndex, value);
    }// end setComponentAt(...)

}// end RasterizedBlockVectorList
