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

import com.jogamp.opengl.GL3;

public class GLRenderBuffer {
    private GL3 gl;
    private int id;
    GLRenderBuffer(GL3 gl){
	this.gl=gl;
	IntBuffer result = IntBuffer.wrap(new int[1]);
	gl.glGenRenderbuffers(1, result);
	id = result.get(0);
    }//end constructor
    public GLRenderBuffer bind() {
	gl.glBindRenderbuffer(GL3.GL_RENDERBUFFER, id);
	return this;
    }
    public GLRenderBuffer setStorage(int internalFormat, int width, int height) {
	gl.glRenderbufferStorage(GL3.GL_RENDERBUFFER, internalFormat, width, height);
	return this;
    }
    public GLRenderBuffer setGl(GL3 gl){
	this.gl=gl;
	return this;
    }
    public int getId() {
	return id;
    }
}//end GLRenderBuffer
