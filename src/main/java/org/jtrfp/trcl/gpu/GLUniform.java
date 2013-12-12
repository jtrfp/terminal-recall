package org.jtrfp.trcl.gpu;

import javax.media.opengl.GL3;

public class GLUniform
	{
	private final GLProgram prg;
	private final int uniformID;
	GLUniform(GLProgram prg, int uniformID)
		{
		this.prg=prg;
		this.uniformID=uniformID;
		}
	
		public void set(GL3 gl,float value)
			{gl.glUniform1f(uniformID,value);}
		public void set(GL3 gl,int value)
			{gl.glUniform1i(uniformID,value);}
		public void setui(GL3 gl,int value)
			{gl.glUniform1ui(uniformID,value);}
	
		public void set(GL3 gl, float float1, float float2,
				float float3)
			{gl.glUniform3f(uniformID,
					float1, float2, float3);
		}
	}//end GLUniform
