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

import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.Texture;

/**
 * Partial wrap implementation of OpenGL 1D RGBA32UI CPU-write-optimized Texture
 * Buffer.
 * 
 * @author Chuck Ritola
 * 
 */
public class GLTextureBuffer extends RawGLBuffer {
    private final int textureID;
    private final GPU gpu;
    private static final int DEADBEEF = 0xEFBEADDE;

    /**
     * 
     * @param sizeInBytes
     *            Number of bytes to allocate. NOTE: This will round up to next
     *            multiple of 1024 to reduce probability of segfault in AMD
     *            driver.
     * @param gl
     */
    GLTextureBuffer(int sizeInBytes, GPU gpu) {
	super(roundToNextKB(sizeInBytes), gpu);
	// Allocate a texture id
	final GL3 gl = gpu.getGl();
	textureID = Texture.createTextureID(gl);
	this.gpu=gpu;
	gl.glBindTexture(getBindingTarget(), getTextureID());
	gl.glTexBuffer(getBindingTarget(), GL2.GL_RGBA32UI, this.getBufferID());
	this.map(gl);
	IntBuffer buf = this.getUnderlyingBuffer().asIntBuffer();
	buf.rewind();
	// Fill with empty data
	while (buf.remaining() > 0) {
	    buf.put(DEADBEEF);
	}
	this.flushRange(0, buf.limit());
	this.unmap(gl);
    }//end constructor

    /**
     * This gets around a quirk where AMD driver will segfault with
     * certain-sized texture buffers. No explicit pattern found, however
     * multiples of 1024B appear safe for now.
     * 
     * @param sizeInBytes
     * @return
     * @since Dec 12, 2012
     */
    private static int roundToNextKB(int sizeInBytes) {
	int kb = (sizeInBytes / 1024) + 1;// Round to lower KB, add 1KB
	return kb * 1024;
    }//end roundToNextKB(...)

    @Override
    protected int getBindingTarget() {
	return GL2.GL_TEXTURE_BUFFER;
    }//end getBindingTarget()

    /**
     * Get the OpenGL texture "name" (integer given by OpenGL representing the
     * texture)
     * 
     * @return the textureID
     */
    public final int getTextureID() {
	return textureID;
    }//end getTextureID()

    /**
     * Binds this TextureBuffer to the specified texture unit.
     * 
     * @param gl
     * @param textureUnit
     *            Integer ID of the texture unit to bind to (typically 0-8). Do
     *            not pass a GL enum such as GL_TEXTURE0. It will take care of
     *            this automatically.
     * @return this
     * @since Mar 31, 2014
     */
    public final GLTextureBuffer bindToTextureUnit(GL3 gl, int textureUnit) {
	gl.glActiveTexture(GL2.GL_TEXTURE0 + textureUnit);
	gl.glBindTexture(getBindingTarget(), getTextureID());
	return this;
    }//end bindToTextureUnit

    /**
     * Bind this TextureBuffer to the specified Uniform varible of the specified
     * shader program by binding to a texture unit then passing said texture
     * unit ID to the uniform.
     * 
     * @param gl
     * @param textureUnit
     *            Integer ID of target texture unit,, typ. [0,8]. Do not pass a
     *            GL enum such as GL_TEXTURE0.
     * @param program
     * @param uniform
     * @return this
     * @since Mar 31, 2014
     */
    public final GLTextureBuffer bindToUniform(GL3 gl, int textureUnit,
	    GLProgram program, GLUniform uniform) {
	bindToTextureUnit(gl, textureUnit);
	uniform.set(textureUnit);
	return this;
    }//end bindToUniform
    
    /**
     * Flush the specified range of this buffer, i.e. update the specified range to the GPU.
     * If update is mandatory, flushing must be done manually.
     * It is possible but not guaranteed that the buffer will auto-flush.
     * @param startPointInBytes
     * @param lengthInBytes
     * @since Aug 5, 2014
     */

    public void flushRange(int startPointInBytes, int lengthInBytes) {
	bind((GL2)gpu.getGl());
	gpu.getGl().glFlushMappedBufferRange(getBindingTarget(), startPointInBytes, lengthInBytes);
	unbind((GL2)gpu.getGl());
    }

}// end GLTextureBuffer(...)
