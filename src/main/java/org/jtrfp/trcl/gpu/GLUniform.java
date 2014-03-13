package org.jtrfp.trcl.gpu;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

import org.apache.commons.math3.linear.RealMatrix;

public class GLUniform {
    private final GLProgram prg;
    private final int uniformID;
    private static GL3 gl;

    GLUniform(GLProgram prg, int uniformID) {
	this.prg = prg;
	this.uniformID = uniformID;
	gl = prg.getGl();
    }

    public GLUniform set(float value) {
	gl.glUniform1f(uniformID, value);
	return this;
    }

    public GLUniform set(int value) {
	gl.glUniform1i(uniformID, value);
	return this;
    }

    public GLUniform setui(int value) {
	gl.glUniform1ui(uniformID, value);
	return this;
    }

    public GLUniform set(float float1, float float2, float float3) {
	gl.glUniform3f(uniformID, float1, float2, float3);
	return this;
    }

    public GLUniform setArrayui(int[] vals) {
	gl.glUniform1uiv(uniformID, vals.length, IntBuffer.wrap(vals));
	return this;
    }

    public GLUniform set4x4Matrix(float[] elements, boolean transpose) {
	gl.glUniformMatrix4fv(uniformID, 1, transpose, elements, 0);
	return this;
    }
}// end GLUniform
