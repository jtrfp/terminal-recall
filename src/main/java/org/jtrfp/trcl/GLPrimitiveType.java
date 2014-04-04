/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import javax.media.opengl.GL2;

public abstract class GLPrimitiveType {
    public static final class GL_FLOAT extends GLPrimitiveType {
	public GL_FLOAT() {
	    super(4, true, true, GL2.GL_FLOAT);
	}
    }

    public static final class GL_DOUBLE extends GLPrimitiveType {
	public GL_DOUBLE() {
	    super(8, true, true, GL2.GL_DOUBLE);
	}
    }

    public static final class GL_INT extends GLPrimitiveType {
	public GL_INT() {
	    super(4, true, false, GL2.GL_INT);
	}
    }

    public static final class GL_UNSIGNED_INT extends GLPrimitiveType {
	public GL_UNSIGNED_INT() {
	    super(4, false, false, GL2.GL_UNSIGNED_INT);
	}
    }

    public static final class GL_SHORT extends GLPrimitiveType {
	public GL_SHORT() {
	    super(4, true, true, GL2.GL_SHORT);
	}
    }

    private final int sizeInBytes;
    private final boolean signed;
    private final boolean floating;
    private final int glEnumID;

    public GLPrimitiveType(int size, boolean isSigned, boolean isFloating,
	    int glID) {
	sizeInBytes = size;
	signed = isSigned;
	floating = isFloating;
	glEnumID = glID;
    }

    /**
     * @return the sizeInBytes
     */
    public int getSizeInBytes() {
	return sizeInBytes;
    }

    /**
     * @return the signed
     */
    public boolean isSigned() {
	return signed;
    }

    /**
     * @return the floating
     */
    public boolean isFloating() {
	return floating;
    }

    /**
     * @return the glEnumID
     */
    public int getGlEnumID() {
	return glEnumID;
    }
}
