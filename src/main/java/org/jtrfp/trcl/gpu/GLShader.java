package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL3;
import javax.media.opengl.GL4;

public abstract class GLShader
	{
	private final int shaderID;
	private final GPU gpu;
	GLShader(GPU gpu)
		{
		this.gpu=gpu;
		GL3 gl =gpu.takeGL();
		shaderID= gl.glCreateShader(getShaderType());
		gpu.releaseGL();
		}
	
	protected abstract int getShaderType();
	
	public void setSource(String source)
		{
		GL3 gl = gpu.takeGL();
		gl.glShaderSource(shaderID, 1, new String[]
			{ source }, (IntBuffer) null);
		gl.glCompileShader(shaderID);
		printStatusInfo(gl, shaderID);
		gpu.releaseGL();
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
