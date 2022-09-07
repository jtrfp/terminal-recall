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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GL4;

import org.apache.commons.io.IOUtils;

public abstract class GLShader {
    private final int shaderID;
    private final GPU gpu;

    GLShader(GPU gpu) {
	this.gpu = gpu;
	shaderID = gpu.getGl().glCreateShader(getShaderType());
	if (shaderID < 0)
	    throw new RuntimeException(
		    "Invalid shader ID "
			    + shaderID
			    + ". Something went wrong somewhere in putting the shader together.");
    }//end constructor

    protected abstract int getShaderType();

    public GLShader setSourceFromResource(String resourceURI)
	    throws IOException {
	InputStream is = null;
	try    {setSource(IOUtils.toString(is = getClass().getResourceAsStream(resourceURI),Charset.defaultCharset()));}
	finally{if(is!=null)is.close();}
	return this;
    }// end setSourceFromResource(...)

    public void setSource(String source) {
	GL3 gl = gpu.getGl();
	gl.glShaderSource(shaderID, 1, new String[] { source },
		(IntBuffer) null);
	gl.glCompileShader(shaderID);
	printStatusInfo(gl, shaderID);
    }// end setSource

    private void printStatusInfo(GL3 gl, int shaderID) {
	IntBuffer statBuf = IntBuffer.allocate(1);
	gl.glGetShaderiv(shaderID, GL4.GL_COMPILE_STATUS, statBuf);
	if (statBuf.get(0) == GL4.GL_FALSE) {
	    statBuf.clear();
	    gl.glGetShaderiv(shaderID, GL4.GL_INFO_LOG_LENGTH, statBuf);
	    ByteBuffer log = ByteBuffer.allocate(statBuf.get(0));
	    gl.glGetShaderInfoLog(shaderID, statBuf.get(0), null, log);
	    System.out.println(Charset.forName("US-ASCII").decode(log)
		    .toString());
	    System.exit(1);
	}
    }// end printStatusInfo(...)

    int getShaderID() {
	return shaderID;
    }
}//end GLShader
