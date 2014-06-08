package org.jtrfp.trcl;

import java.util.concurrent.Future;

import org.jtrfp.trcl.core.Texture;
import org.jtrfp.trcl.core.TriangleVertexWindow;

public class TexturePageAnimator implements Tickable{
    private final TriangleVertexWindow 	vertexWindow;
    private final int 			gpuTVIndex;
    private 	  String 		debugName = "[not set]";
    private final Controller		controller;
    private final Future<Texture>[]	frames;

    public TexturePageAnimator(AnimatedTexture at, TriangleVertexWindow vw, int gpuTVIndex) {
	this.vertexWindow=vw;
	this.gpuTVIndex=gpuTVIndex;
	this.controller=at.getTextureSequencer();
	frames = at.getFrames();
    }//end constructor

    @Override
    public void tick() {
	try{
	final int texturePage = frames[(int)controller.getCurrentFrame()].get().getNodeForThisTexture().getTexturePage();
	//final int texturePage = 100;
	vertexWindow.textureIDLo .set(gpuTVIndex, (byte)(texturePage & 0xFF));
	vertexWindow.textureIDMid.set(gpuTVIndex, (byte)((texturePage >> 8) & 0xFF));
	vertexWindow.textureIDHi .set(gpuTVIndex, (byte)((texturePage >> 16) & 0xFF));}
	catch(Exception e){e.printStackTrace();}
    }//end tick()

    public TexturePageAnimator setDebugName(String debugName) {
	this.debugName=debugName;
	return this;
    }//end setDebugName(...)

    /**
     * @return the debugName
     */
    public String getDebugName() {
        return debugName;
    }//end getDebugName()

}//end TextureIDAnimator
