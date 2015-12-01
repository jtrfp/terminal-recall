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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.gpu.RawGLBuffer.MapMode;
import org.jtrfp.trcl.gui.Reporter;

public class ReallocatableGLTextureBuffer implements ReallocatableGLMemory {
    private GLTextureBuffer buffer;
    private GPU gpu;
    private MemoryUsageHint usageHint;
    private final Reporter reporter;
    private static final MapMode [] DEFAULT_MAP_FLAGS = new MapMode[]{MapMode.FLUSH_EXPLICIT,MapMode.WRITE,MapMode.UNSYNCHRONIZED};

    public ReallocatableGLTextureBuffer(GPU gpu, Reporter reporter) {
	this.gpu = gpu;
	this.reporter = reporter;
	buffer = new GLTextureBuffer(1, gpu);
    }
    
    public void flushRange(int startPointInBytes, int lengthInBytes){
	buffer.flushRange(startPointInBytes, lengthInBytes);
    }

    @Override
    public ByteBuffer map() {
	buffer.map(gpu.getGl(),DEFAULT_MAP_FLAGS);
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
	buffer.flushRange(0, newData.capacity());
	unmap();
    }

    @Override
    public void unmap() {
	buffer.unmap(gpu.getGl());
    }

    @Override
    public void bind() {
	buffer.bind((GL3) gpu.getGl());
    }

    @Override
    public void unbind() {
	buffer.unbind((GL3) gpu.getGl());
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
	final GL3 gl = gpu.getGl();
	ByteBuffer oldBuffer,newBuffer;
	GLTextureBuffer oldTextureBuffer, newTextureBuffer;
	reporter.report("org.jtrfp.trcl.gpu.ReallocatableGLTextureBuffer."+hashCode()+".sizeInBytes", ""+sizeInBytes);
	oldTextureBuffer=buffer;
	oldTextureBuffer.unmap(gl);
	oldTextureBuffer.map(gl, MapMode.READ);
	oldBuffer = oldTextureBuffer.getUnderlyingBuffer();
	oldBuffer.clear();
	oldBuffer.limit(Math.min(sizeInBytes,oldBuffer.capacity()));
	newTextureBuffer = buffer = new GLTextureBuffer(sizeInBytes, gpu);
	newTextureBuffer.map(gl,MapMode.WRITE);
	newBuffer = newTextureBuffer.getUnderlyingBuffer();
	newBuffer.clear();
	newBuffer.put(oldBuffer);
	newBuffer.clear();
	oldTextureBuffer.free(gl);
	newTextureBuffer.unmap(gl);
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
