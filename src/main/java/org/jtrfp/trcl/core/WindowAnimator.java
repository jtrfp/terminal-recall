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
package org.jtrfp.trcl.core;

import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.IntTransferFunction;
import org.jtrfp.trcl.Tickable;

public class WindowAnimator implements Tickable {
    private int 			indexCounter = 0;
    private final int			numElements, numFrames;
    private final float[]		frames;
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
	this.controller = controller;
    }//end constructor

    public void addFrames(float[] newFrames) {
	System.arraycopy(newFrames, 0, frames, indexCounter * numFrames,
		numFrames);
	indexCounter++;
    }// end constructor

    @Override
    public void tick() {
	if (indexCounter < numElements)
	    return;
	
	final double frame = controller.getCurrentFrame();
	final int fL       = (int)Math.floor(frame);
	final int fH       = (int)Math.ceil(frame) % numFrames;
	final int dF	   = fH-fL;
	final double xFade = frame%1.;
	
	double wL = 1-xFade;
	double wH = xFade;
	
	int indexOffset    = fL;
	final int nFrames  = numFrames;
	final int nElements= numElements;
	for (int counter = 0; counter < nElements; counter++) {
	    final double vH = frames[indexOffset+dF];
	    final double vL = frames[indexOffset];
	    final double val= vH*wH + vL*wL;
	    w.set(indexXferFun.transfer(counter), val);
	    indexOffset += nFrames;
	}// end for(numElements)
    }// end tick()

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
