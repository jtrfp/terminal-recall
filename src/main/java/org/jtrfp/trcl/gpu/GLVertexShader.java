package org.jtrfp.trcl.gpu;

import javax.media.opengl.GL3;

public class GLVertexShader extends GLShader
	{
	public GLVertexShader(GPU gpu)
		{super(gpu);}
	@Override
	protected int getShaderType()
		{return GL3.GL_VERTEX_SHADER;}

	}
