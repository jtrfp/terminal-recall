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
package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;

import javax.media.opengl.GL2;

public class ReallocatableGLTextureBuffer implements ReallocatableGLMemory {
    private GLTextureBuffer buffer;
    private GPU gpu;
    private MemoryUsageHint usageHint;

    public ReallocatableGLTextureBuffer(GPU gpu) {
	this.gpu = gpu;
	buffer = new GLTextureBuffer(1, gpu);
    }
    
    public void flushRange(int startPointInBytes, int lengthInBytes){
	buffer.flushRange(startPointInBytes, lengthInBytes);
    }

    @Override
    public ByteBuffer map() {
	buffer.map(gpu.getGl());
	return buffer.getDuplicateReferenceOfUnderlyingBuffer();
    }

    @Override
    public int getSizeInBytes() {
	return buffer.getUnderlyingBuffer().capacity();
    }

    @Override
    public void setUsageHint(MemoryUsageHint hint) {
	buffer.setUsageHint((usageHint = hint).getGLEnumInt());
    }

    @Override
    public MemoryUsageHint getUsageHint() {
	return usageHint;
    }

    @Override
    public void reallocate(ByteBuffer newData) {
	buffer.free(gpu.getGl());
	buffer = new GLTextureBuffer(newData.capacity(), gpu);
	ByteBuffer bb = map();
	bb.rewind();
	bb.put(newData);
	unmap();
    }

    @Override
    public void unmap() {
	buffer.unmap(gpu.getGl());
    }

    @Override
    public void bind() {
	buffer.bind((GL2) gpu.getGl());
    }

    @Override
    public void unbind() {
	buffer.unbind((GL2) gpu.getGl());
    }

    @Override
    public ByteBuffer getDuplicateReferenceOfUnderlyingBuffer() {
	return buffer.getDuplicateReferenceOfUnderlyingBuffer();
    }

    @Override
    public void bindToUniform(int textureUnit, GLProgram program,
	    GLUniform uniform) {
	buffer.bindToUniform(gpu.getGl(), textureUnit, program, uniform);
    }

    @Override
    public void reallocate(int sizeInBytes) {
	ByteBuffer oldBuffer,newBuffer;
	GLTextureBuffer oldTextureBuffer, newTextureBuffer;
	gpu.getTr().getReporter().report("org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer."+hashCode()+".sizeInBytes", ""+sizeInBytes);
	oldBuffer = buffer.getUnderlyingBuffer();
	oldBuffer.clear();
	oldBuffer.limit(Math.min(sizeInBytes,oldBuffer.capacity()));
	oldTextureBuffer=buffer;
	newTextureBuffer = buffer = new GLTextureBuffer(sizeInBytes, gpu);
	buffer.map(gpu.getGl());
	newBuffer = newTextureBuffer.getUnderlyingBuffer();
	newBuffer.clear();
	newBuffer.put(oldBuffer);
	newBuffer.clear();
	oldTextureBuffer.free(gpu.getGl());
	buffer.unmap(gpu.getGl());
    }

    @Override
    public void putShort(int byteOffset, short val) {
	buffer.getUnderlyingBuffer().putShort(byteOffset, val);
    }

    @Override
    public void putFloat(int byteOffset, float val) {
	buffer.getUnderlyingBuffer().putFloat(byteOffset, val);
    }

    public boolean isMapped() {
	return buffer.isMapped();
    }
    
    public GLTextureBuffer getGLTextureBuffer(){
	return buffer;
    }

}// end ReallocatableGLTextureBuffer
