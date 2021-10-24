package org.jtrfp.trcl.gpu;

import java.util.ArrayList;
import java.util.List;

import org.jtrfp.trcl.coll.Decorator;
import org.jtrfp.trcl.gpu.BasicModelTarget.PrimitiveType;

public class InterpolatedAnimatedModelSource implements BasicModelSource {
    private double                      currentFrame = 0;
    private final List<BasicModelSource>frames = new ArrayList<BasicModelSource>();
    private Integer                     delayBetweenFramesMillis;

    /**
     * @return the currentFrame
     */
    public double getCurrentFrame() {
	if(delayBetweenFramesMillis!=null)
	    return ((double)System.currentTimeMillis())/delayBetweenFramesMillis.doubleValue() % frames.size();
        return currentFrame;
    }

    /**
     * @param currentFrame the currentFrame to set
     */
    public void setCurrentFrame(double currentFrame) {
        this.currentFrame = currentFrame;
    }
    
    public void setNormalizedCurrentFrame(double nCurrentFrame){
	setCurrentFrame(nCurrentFrame*(double)frames.size());
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getVertex(int)
     */
    public double[] getVertex(int index) {
	final double [] result = new double[8];
	final int nFrames   = frames.size();
	final double frame = getCurrentFrame();
	int lowFrame = (int) frame;
	int hiFrame = (int)(Math.ceil(frame) % nFrames);
	final double mFrame = frame%1.;
	final double imFrame= 1-mFrame;
	final double [] dR     = frames.get(hiFrame).getVertex(index);
	final double [] dL     = frames.get(lowFrame).getVertex(index);
	final int n = Math.min(dR.length,dL.length);
	for(int i=0; i<n; i++)
	    result[i]=mFrame*dR[i]+imFrame*dL[i];
	return result;
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getPrimitiveVertexIDs(int)
     */
    public int[] getPrimitiveVertexIDs(int index) {
	return frames.get((int)getCurrentFrame()).getPrimitiveVertexIDs(index);
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getPrimitiveType(int)
     */
    public PrimitiveType getPrimitiveType(int index) {
	return frames.get((int)getCurrentFrame()).getPrimitiveType(index);
    }
    
    public int addModelFrame(BasicModelSource frameToAdd){
	frames.add(frameToAdd);
	return frames.size()-1;
    }

    /**
     * 
     * @param delay Time in milliseconds between each frame, or null if manually animated with setCurrentFrame()
     * @since Jul 30, 2015
     */
    public void setDelayBetweenFramesMillis(Integer delay) {
	delayBetweenFramesMillis=delay;
    }

}//end InterpolatedAnimatedModelSource
