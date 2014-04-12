package org.jtrfp.trcl.gpu;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public final class GLTexture {
    private final GPU gpu;
    private final int textureID;
    private int rawSideLength;
    private GL3 gl;
    private int bindingTarget = GL3.GL_TEXTURE_2D;
    private int internalColorFormat = GL3.GL_RGBA4;

    public GLTexture(GPU gpu) {
	System.out.println("Creating GL Texture...");
	this.gpu = gpu;
	textureID = gpu.newTextureID();
	gl = gpu.getGl();
	// Setup the empty rows
	System.out.println("...Done.");
    }// end constructor
    
    public GLTexture setImage(int internalOrder, int width, int height, int colorOrder, int numericalFormat, Buffer pixels){
	gl.glTexImage2D(bindingTarget, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setParameteri(int parameterName, int value){
	gl.glTexParameteri(textureID, parameterName, value);
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
    public void setTextureImageRGBA(ByteBuffer buf) {
	rawSideLength = (int) Math.sqrt(buf.capacity() / 4);
	buf.rewind();
	GL3 gl = gpu.getGl();
	gl.glBindTexture(bindingTarget, textureID);
	/*FloatBuffer isoSize = FloatBuffer.wrap(new float[] { 0 });
	gl.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, isoSize);*/
	System.out.println("Uploading texture...");
	gl.glTexImage2D(bindingTarget, 0, internalColorFormat, rawSideLength,
		rawSideLength, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buf);
	gl.glGenerateMipmap(bindingTarget);
	System.out.println("\t...Done.");
    }
    
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
	switch(texelCoordinates.length){
	case 3:{
	    gl.glTexSubImage1D(bindingTarget, level, texelCoordinates[0], sideLengthsInTexels[0], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 2:{
	    gl.glTexSubImage2D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1], sideLengthsInTexels[0], sideLengthsInTexels[1], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 1:{
	    gl.glTexSubImage3D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1],texelCoordinates[2], sideLengthsInTexels[0], sideLengthsInTexels[1],sideLengthsInTexels[2], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}
	default:{
	    throw new RuntimeException("Invalid number of dimensions in specified coordinates: "+texelCoordinates.length);
	}}
	return this;
    }

    public void delete() {
	gl.glBindTexture(bindingTarget, textureID);
	gl.glDeleteTextures(1, IntBuffer.wrap(new int[] { textureID }));
    }

    int getTextureID() {
	return textureID;
    }
    
    public GLTexture setGl(GL3 gl){
	this.gl=gl;
	return this;
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
