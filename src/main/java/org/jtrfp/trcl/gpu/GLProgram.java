package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL3;
import javax.media.opengl.GL4;

public class GLProgram
	{
	private GPU gpu;
	private final int programID;
	GLProgram(GPU gpu)
		{this.gpu=gpu;
		GL3 gl = gpu.takeGL();
		programID=gl.glCreateProgram();
		gpu.releaseGL();
		}
	
	public void attachShader(GLShader shader)
		{
		GL3 gl = gpu.takeGL();
		gl.glAttachShader(programID, shader.getShaderID());
		gpu.releaseGL();
		}

	public void link()
		{GL3 gl = gpu.takeGL();
		gl.glLinkProgram(programID);
		gpu.releaseGL();
		}

	public void use()
		{GL3 gl = gpu.takeGL();
		gl.glUseProgram(programID);
		gpu.releaseGL();
		}

	public boolean validate()
		{
		IntBuffer statBuf = IntBuffer.allocate(1);
		GL3 gl = gpu.takeGL();
		gl.glValidateProgram(programID);
		gl.glGetProgramiv(programID, GL4.GL_VALIDATE_STATUS, statBuf);
		gpu.releaseGL();
		return statBuf.get(0)==GL3.GL_TRUE;
		}
	
	public String getInfoLog()
		{
		IntBuffer statBuf = IntBuffer.allocate(1);
		statBuf.clear();
		GL3 gl = gpu.takeGL();
		gl.glGetProgramiv(programID, GL4.GL_INFO_LOG_LENGTH, statBuf);
		ByteBuffer log = ByteBuffer.allocate(statBuf.get(0));
		gl.glGetProgramInfoLog(programID, statBuf.get(0), null, log);
		gpu.releaseGL();
		return Charset.forName("US-ASCII").decode(log)
				.toString();
		}

	public GLUniform getUniform(GL3 gl, String uniformName)
		{return new GLUniform(this,gl.glGetUniformLocation(programID,uniformName));}

	int getProgramID()
		{return programID;}
	}//end GLPRogram
