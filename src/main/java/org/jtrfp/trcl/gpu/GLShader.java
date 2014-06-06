package org.jtrfp.trcl.gpu;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL3;
import javax.media.opengl.GL4;

import org.apache.commons.io.IOUtils;

public abstract class GLShader
	{
	private final int shaderID;
	private final GPU gpu;
	GLShader(GPU gpu)
		{
		this.gpu=gpu;
		shaderID= gpu.getGl().glCreateShader(getShaderType());
		if(shaderID<0)throw new RuntimeException("Invalid shader ID "+shaderID+". Something went wrong somewhere in putting the shader together.");
		}
	
	protected abstract int getShaderType();
	
	public GLShader setSourceFromResource(String resourceURI) throws IOException{
	    setSource(IOUtils.toString(getClass()
		    .getResourceAsStream(resourceURI)));
	    return this;
	}//end setSourceFromResource(...)
	
	public void setSource(String source)
		{
		GL3 gl = gpu.getGl();
		gl.glShaderSource(shaderID, 1, new String[]
			{ source }, (IntBuffer) null);
		gl.glCompileShader(shaderID);
		printStatusInfo(gl, shaderID);
		}
	
	private void printStatusInfo(GL3 gl, int shaderID)
		{
		IntBuffer statBuf = IntBuffer.allocate(1);
		gl.glGetShaderiv(shaderID, GL4.GL_COMPILE_STATUS, statBuf);
		if (statBuf.get(0) == GL4.GL_FALSE)
			{
			statBuf.clear();
			gl.glGetShaderiv(shaderID, GL4.GL_INFO_LOG_LENGTH, statBuf);
			ByteBuffer log = ByteBuffer.allocate(statBuf.get(0));
			gl.glGetShaderInfoLog(shaderID, statBuf.get(0), null, log);
			System.out.println(Charset.forName("US-ASCII").decode(log)
					.toString());
			System.exit(1);
			}
		}// end printStatusInfo(...)
	
	int getShaderID(){return shaderID;}
	}
