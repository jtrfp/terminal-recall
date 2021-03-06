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

public interface GLMemory {
    public ByteBuffer map();

    public void unmap();

    public void bind();

    public void unbind();

    public int getSizeInBytes();

    public void setUsageHint(MemoryUsageHint hint);

    public MemoryUsageHint getUsageHint();

    public ByteBuffer getDuplicateReferenceOfUnderlyingBuffer();

    public void bindToUniform(int textureUnit, GLProgram program,
	    GLUniform uniform);
}//end GLMemory
