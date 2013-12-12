package org.jtrfp.trcl.gpu;

import javax.media.opengl.GL3;

public class GLFragmentShader extends GLShader
	{
	public GLFragmentShader(GPU gpu)
		{super(gpu);}
	@Override
	protected int getShaderType()
		{return GL3.GL_FRAGMENT_SHADER;}
	}//end GLFragmentShader
