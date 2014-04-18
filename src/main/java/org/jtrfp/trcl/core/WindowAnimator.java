package org.jtrfp.trcl.core;

import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.Tickable;
import org.jtrfp.trcl.IntTransferFunction;

public class WindowAnimator implements Tickable {
    private int 			indexCounter = 0;
    private final int			numElements, numFrames;
    private final float[]		frames;
    private final boolean		loopInterpolate;
    private 	  String 		debugName;
    
    private final FlatDoubleWindow	w;
    private final Controller 		controller;
    private final IntTransferFunction 	indexXferFun;

    public WindowAnimator(FlatDoubleWindow w, int numElements, int numFrames,
	    boolean loopInterpolate, Controller controller,
	    IntTransferFunction indexXferFun) {
	if (controller == null)
	    throw new NullPointerException("Controller is intolerably null.");
	if (w == null)
	    throw new NullPointerException(
		    "TriangleVertex2FlatDoubleWindow is intolerably null.");
	this.w = w;
	this.numElements = numElements;
	this.numFrames = numFrames;
	this.indexXferFun = indexXferFun;
	frames = new float[numElements * numFrames];
	this.loopInterpolate = loopInterpolate;
	this.controller = controller;
    }//end constructor

    public void addFrames(float[] newFrames) {
	System.arraycopy(newFrames, 0, frames, indexCounter * numFrames,
		numFrames);
	indexCounter++;
    }// end constructor

    @Override
    public void tick() {
	if (indexCounter < numElements) {
	    return;
	}
	final double frame = controller.getCurrentFrame();
	int lowFrame = (int) frame;
	int hiFrame = (lowFrame + 1) % numFrames;
	double interpolation = frame - (double) lowFrame;
	if (hiFrame == 0 && !loopInterpolate) {
	    lowFrame = 0;
	    hiFrame = 1;
	}
	double hI = interpolation;
	double lI = 1. - interpolation;
	final int dHi = hiFrame - lowFrame;
	int indexOffset = lowFrame;
	for (int index = 0; index < numElements; index++) {
	    final double val = frames[indexOffset] * lI
		    + frames[indexOffset + dHi] * hI;
	    w.set(indexXferFun.transfer(index), val);
	    indexOffset += numFrames;
	}// end for(numElements)
    }// update()

    /**
     * @return the debugName
     */
    public String getDebugName() {
	return debugName;
    }

    /**
     * @param debugName
     *            the debugName to set
     */
    public void setDebugName(String debugName) {
	this.debugName = debugName;
    }
}// end WindowAnimator
