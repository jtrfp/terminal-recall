package org.jtrfp.trcl.gpu;

import javax.media.opengl.GL3;

public class GLUniform
	{
	private final GLProgram prg;
	private final int uniformID;
	private static GL3 gl;
	GLUniform(GLProgram prg, int uniformID)
		{
		this.prg=prg;
		this.uniformID=uniformID;
		gl=prg.getGl();
		}
	
		public void set(float value)
			{gl.glUniform1f(uniformID,value);}
		public void set(int value)
			{gl.glUniform1i(uniformID,value);}
		public void setui(int value)
			{gl.glUniform1ui(uniformID,value);}
	
		public void set(float float1, float float2,
				float float3)
			{gl.glUniform3f(uniformID,
					float1, float2, float3);
		}
	}//end GLUniform
