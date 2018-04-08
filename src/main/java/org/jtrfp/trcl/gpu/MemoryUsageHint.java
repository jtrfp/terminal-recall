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

import com.jogamp.opengl.GL3;

public enum MemoryUsageHint {
    DymamicDraw(GL3.GL_DYNAMIC_DRAW), StaticDraw(GL3.GL_STATIC_DRAW), StreamDraw(
	    GL3.GL_STREAM_DRAW), StreamRead(GL3.GL_STREAM_READ), StaticRead(
	    GL3.GL_STATIC_READ), DynamicRead(GL3.GL_DYNAMIC_READ), DynamicCopy(
	    GL3.GL_DYNAMIC_COPY), StaticCopy(GL3.GL_STATIC_COPY), StreamCopy(
	    GL3.GL_STREAM_COPY);

    private final int glEnum;

    private MemoryUsageHint(int glEnum) {
	this.glEnum = glEnum;
    }

    public int getGLEnumInt() {
	return glEnum;
    }
}//end MemoryUsageHint
