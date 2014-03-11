package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL3;
import javax.media.opengl.GL4;

public class GLProgram
	{
	private GPU gpu;
	private final GL3 gl;
	private final int programID;
	GLProgram(GPU gpu)
		{this.gpu=gpu;
		this.gl=gpu.getGl();
		programID=gl.glCreateProgram();
		if(programID<0)throw new RuntimeException("Invalid program ID: "+programID+". Something went wrong.");
		}
	
	public void attachShader(GLShader shader)
		{gl.glAttachShader(programID, shader.getShaderID());}

	public void link(){
		gl.glLinkProgram(programID);
		}

	public void use(){
		gl.glUseProgram(programID);
		}

	public boolean validate()
		{
		IntBuffer statBuf = IntBuffer.allocate(1);
		gl.glValidateProgram(programID);
		gl.glGetProgramiv(programID, GL4.GL_VALIDATE_STATUS, statBuf);
		return statBuf.get(0)==GL3.GL_TRUE;
		}
	
	public String getInfoLog()
		{
		IntBuffer statBuf = IntBuffer.allocate(1);
		statBuf.clear();
		gl.glGetProgramiv(programID, GL4.GL_INFO_LOG_LENGTH, statBuf);
		ByteBuffer log = ByteBuffer.allocate(statBuf.get(0));
		gl.glGetProgramInfoLog(programID, statBuf.get(0), null, log);
		return Charset.forName("US-ASCII").decode(log)
				.toString();
		}

	public GLUniform getUniform(String uniformName){
		final int loc = gl.glGetUniformLocation(programID,uniformName);
		if(loc==-1)throw new RuntimeException("Could not find unifrom "+uniformName);
		else if(loc<0)throw new RuntimeException("Invalid uniform location on lookup: "+loc+" of name "+uniformName);
		return new GLUniform(this,loc);}

	int getProgramID()
		{return programID;}
	
	GL3 getGl(){return gl;}
	}//end GLPRogram
