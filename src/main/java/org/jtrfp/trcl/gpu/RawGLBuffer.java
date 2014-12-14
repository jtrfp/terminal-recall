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
import java.nio.IntBuffer;
import javax.media.opengl.GL3;

public class RawGLBuffer {
    private int bufferID;
    protected ByteBuffer localBuffer;
    private boolean mapped = false;
    private int memoryUsageHint = MemoryUsageHint.DymamicDraw.getGLEnumInt();
    private final int sizeInBytes;

    protected RawGLBuffer(int sizeInBytes, GPU gpu) {
	if (sizeInBytes == 0)
	    throw new RuntimeException("Cannot allocate a buffer of size zero.");
	this.sizeInBytes=sizeInBytes;
	final GL3 gl = gpu.getGl();
	final IntBuffer iBuf = IntBuffer.allocate(1);
	gl.glGenBuffers(1, iBuf);

	bufferID = iBuf.get();
	gl.glBindBuffer(getBindingTarget(), bufferID);
	gl.glBufferData(getBindingTarget(), sizeInBytes, null,
		getReadWriteParameter());
	gl.glBindBuffer(getBindingTarget(), 0);
    }// end constructor

    void setUsageHint(int hint) {
	memoryUsageHint = hint;
    }

    protected int getReadWriteParameter() {
	return memoryUsageHint;
    }

    protected int getBindingTarget() {
	return GL3.GL_ARRAY_BUFFER;
    }

    public void rewind() {
	localBuffer.rewind();
    }

    public void map(GL3 gl) {
	if (mapped)
	    return;
	gl.glBindBuffer(getBindingTarget(), bufferID);
	localBuffer = gl.glMapBufferRange(getBindingTarget(), 0, sizeInBytes, GL3.GL_MAP_WRITE_BIT|GL3.GL_MAP_UNSYNCHRONIZED_BIT|GL3.GL_MAP_FLUSH_EXPLICIT_BIT);
	if (localBuffer == null) {
	    throw new NullPointerException("Failed to map buffer.");
	}
	gl.glBindBuffer(getBindingTarget(), 0);
	mapped = true;
    }

    public void unmap(GL3 gl) {
	if (!mapped)
	    return;
	gl.glBindBuffer(getBindingTarget(), bufferID);
	gl.glUnmapBuffer(getBindingTarget());
	gl.glBindBuffer(getBindingTarget(), 0);
	mapped = false;
    }

    public void free(GL3 gl) {
	unmap(gl);
	gl.glDeleteBuffers(1, IntBuffer.wrap(new int[] { bufferID }));
	localBuffer = null;
    }

    public void bind(GL3 gl3) {
	gl3.glBindBuffer(getBindingTarget(), getBufferID());
    }

    public void bindAsElementArrayBuffer(final GL3 gl) {
	// gl.glEnableClientState(GL2.GL_ELEMENT_ARRAY_BUFFER);
	gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, getBufferID());
    }

    public void unbind(GL3 gl) {
	gl.glBindBuffer(getBindingTarget(), 0);
    }

    public void position(int pos) {
	localBuffer.position(pos);
    }

    public int position() {
	return localBuffer.position();
    }

    /**
     * @return the bufferID
     */
    public int getBufferID() {
	return bufferID;
    }

    /**
     * @param bufferID
     *            the bufferID to set
     */
    public void setBufferID(int bufferID) {
	this.bufferID = bufferID;
    }

    public ByteBuffer getUnderlyingBuffer() {
	return localBuffer;
    }

    public ByteBuffer getDuplicateReferenceOfUnderlyingBuffer() {
	final ByteBuffer result = localBuffer.duplicate();
	result.order(localBuffer.order()).clear();
	return result;
    }
    
    public boolean isMapped(){
	return mapped;
    }
}// end GLBuffer
