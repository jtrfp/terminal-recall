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
package org.jtrfp.trcl;


public final class AttribAnimator implements Tickable {
    Settable 	attrib;
    Controller 	controller;
    double[] 	frames;
    private final boolean loopInterpolate;

    public AttribAnimator(Settable attrib, Controller sequencer, double[] frames) {
	this(attrib, sequencer, frames, true);
    }//end constructor

    public AttribAnimator(Settable attrib, Controller sequencer,
	    double[] frames, boolean loopInterpolate) {
	this.loopInterpolate = loopInterpolate;
	this.attrib = attrib;
	this.controller = sequencer;
	this.frames = frames;
    }//end constructor

    public void tick() {
	updateAnimation();
    }//end tick()

    public void updateAnimation() {
	double frame = controller.getCurrentFrame();
	int lowFrame = (int) frame;
	int hiFrame = (int)(Math.ceil(frame) % frames.length);
	double interpolation = frame % 1.;
	if (hiFrame == 0 && !loopInterpolate) {
	    lowFrame = 0;
	    hiFrame = 1;
	}
	double hI = interpolation;
	double lI = 1. - interpolation;
	attrib.set(frames[lowFrame] * lI + frames[hiFrame] * hI);
    }//end updateAnimation()
}// end AttribAnimator
