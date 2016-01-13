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

import javax.media.opengl.GL3;

public class GLVertexShader extends GLShader {
    public GLVertexShader(GPU gpu) {
	super(gpu);
    }

    @Override
    protected int getShaderType() {
	return GL3.GL_VERTEX_SHADER;
    }

}//end GLVertexShader
