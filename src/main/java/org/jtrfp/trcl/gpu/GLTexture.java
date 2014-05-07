package org.jtrfp.trcl.gpu;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TRFuture;

public final class GLTexture {
    private final GPU gpu;
    private final TRFuture<Integer> textureID;
    private int rawSideLength;
    private final GL3 gl;
    private int bindingTarget = GL3.GL_TEXTURE_2D;
    private int internalColorFormat = GL3.GL_RGBA4;

    public GLTexture(final GPU gpu) {
	System.out.println("Creating GL Texture...");
	this.gpu = gpu;
	textureID = gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
	    @Override
	    public Integer call() throws Exception {
		return gpu.newTextureID();
	    }});
	gl = gpu.getGl();
	// Setup the empty rows
	System.out.println("...Done.");
    }// end constructor
    
    public GLTexture setImage(int internalOrder, int width, int height, int colorOrder, int numericalFormat, Buffer pixels){
	gl.glTexImage2D(bindingTarget, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setParameteri(int parameterName, int value){
	gl.glTexParameteri(textureID.get(), parameterName, value);
	return this;
    }

    /**
     * Takes a square texture in RGBA 8888 format. Automatically determines
     * dimensions from buffer size.
     * 
     * @param buf
     *            Directly-allocated buffer containing the image data.
     * @since Dec 11, 2013
     */
    public void setTextureImageRGBA(final ByteBuffer buf) {
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		rawSideLength = (int) Math.sqrt(buf.capacity() / 4);
		buf.rewind();
		GL3 gl = gpu.getGl();
		gl.glBindTexture(bindingTarget, textureID.get());
		/*FloatBuffer isoSize = FloatBuffer.wrap(new float[] { 0 });
		gl.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, isoSize);*/
		System.out.println("Uploading texture...");
		gl.glTexImage2D(bindingTarget, 0, internalColorFormat, rawSideLength,
			rawSideLength, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buf);
		gl.glGenerateMipmap(bindingTarget);
		System.out.println("\t...Done.");
		return null;
	    }}).get();
    }//end setTextureImageRGBA
    
    public GLTexture configure(int [] sideLengthsInTexels, int numLevels ){
	switch(sideLengthsInTexels.length){
	case 3:{
	 gl.glTexStorage3D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0], sideLengthsInTexels[1], sideLengthsInTexels[2]);
	    break;
	}case 2:{
	    gl.glTexStorage2D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0], sideLengthsInTexels[1]);
	    break;
	}case 1:{
	    gl.glTexStorage1D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0]);
	    break;
	}
	default:{
	    throw new RuntimeException("Invalid number of dimensions in specified sideLength: "+sideLengthsInTexels.length);
	}}
	return this;
    }//end configureEmpty(...)
    
    public GLTexture subImage(int [] texelCoordinates, int [] sideLengthsInTexels, int format, int level, ByteBuffer texels){
	if(texelCoordinates.length!=sideLengthsInTexels.length)
	    throw new RuntimeException("Texel coordinate dims ("+texelCoordinates.length+") must match sideLength dims ("+sideLengthsInTexels.length+").");
	switch(texelCoordinates.length){
	case 1:{
	    gl.glTexSubImage1D(bindingTarget, level, texelCoordinates[0], sideLengthsInTexels[0], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 2:{
	    gl.glTexSubImage2D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1], sideLengthsInTexels[0], sideLengthsInTexels[1], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 3:{
	    if(level<0)throw new RuntimeException("Level is intolerably negative: "+level);
	    gl.glTexSubImage3D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1],texelCoordinates[2], sideLengthsInTexels[0], sideLengthsInTexels[1],sideLengthsInTexels[2], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}
	default:{
	    throw new RuntimeException("Invalid number of dimensions in specified coordinates: "+texelCoordinates.length);
	}}
	return this;
    }

    public void delete() {
	gl.glBindTexture(bindingTarget, textureID.get());
	gl.glDeleteTextures(1, IntBuffer.wrap(new int[] { textureID.get() }));
    }

    int getTextureID() {
	return textureID.get();
    }

    public static void specifyTextureUnit(GL3 gl, int unitNumber) {
	gl.glActiveTexture(GL3.GL_TEXTURE0 + unitNumber);
    }
    
    public GLTexture bind(){
	return bind(gl);
    }
    
    public GLTexture bind(GL3 gl) {
	gl.glBindTexture(bindingTarget, getTextureID());
	return this;
    }

    public int getCurrentSideLength() {
	return rawSideLength;
    }

    public GLTexture setMagFilter(int mode) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_MAG_FILTER, mode);
	return this;
    }
    public GLTexture setMinFilter(int mode){
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_MIN_FILTER, mode);
	return this;
    }

    public GLTexture setWrapS(int val) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_S, val);
	return this;
    }
    public GLTexture setWrapT(int val) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_T, val);
	return this;
    }

    /**
     * @return the bindingTarget
     */
    public int getBindingTarget() {
        return bindingTarget;
    }

    /**
     * @param bindingTarget the bindingTarget to set
     */
    public GLTexture setBindingTarget(int bindingTarget) {
        this.bindingTarget = bindingTarget;
        return this;
    }

    /**
     * @return the internalColorFormat
     */
    public int getInternalColorFormat() {
        return internalColorFormat;
    }

    /**
     * @param internalColorFormat the internalColorFormat to set
     */
    public GLTexture setInternalColorFormat(int internalColorFormat) {
        this.internalColorFormat = internalColorFormat;
        return this;
    }
}// end GLTexture
