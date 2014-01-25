package org.jtrfp.trcl.core;

import org.jtrfp.trcl.Controller;
import org.jtrfp.trcl.Tickable;
import org.jtrfp.trcl.IntTransferFunction;

public class WindowAnimator implements Tickable {
    private final TriangleVertex2FlatDoubleWindow w;
    private final int numElements, numFrames;
    private final float[][] frames;
    private final boolean loopInterpolate;
    private final Controller controller;
    private int indexCounter=0;
    private final IntTransferFunction indexXferFun;
    public WindowAnimator(TriangleVertex2FlatDoubleWindow w, 
	    int numElements, int numFrames,
	    boolean loopInterpolate, Controller controller, IntTransferFunction indexXferFun){
	this.w=w;
	this.numElements=numElements;
	this.numFrames=numFrames;
	this.indexXferFun=indexXferFun;
	//this.startElement=startElement;
	frames = new float[numElements*numFrames][];
	this.loopInterpolate=loopInterpolate;
	this.controller=controller;
    }
    public void addFrames(float[] newFrames) {
	//indices[indexCounter]=globalFlatID;
	frames[indexCounter]=(newFrames);
	indexCounter++;
    }//end constructor
    
    @Override
    public void tick(){
	if(indexCounter<numElements){
	    return;}
	final double frame=controller.getCurrentFrame();
	controller.unstale();
	int lowFrame = (int)frame;
	int hiFrame = (lowFrame+1)%numFrames;
	double interpolation = frame-(double)lowFrame;
	if(hiFrame==0 && !loopInterpolate){lowFrame=0;hiFrame=1;}
	double hI=interpolation;
	double lI=1.-interpolation;
	for(int index=0; index<numElements; index++){
	    final double val = frames[index][lowFrame]*lI+frames[index][hiFrame]*hI;
	    //System.out.println("Element index="+index+" location="+indexXferFun.transfer(index));
	    w.set(indexXferFun.transfer(index), val);
	}//end for(numElements)
    }//update()
}//end WindowAnimator
