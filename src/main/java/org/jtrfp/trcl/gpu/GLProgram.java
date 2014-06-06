package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.Charset;

import javax.media.opengl.GL3;
import javax.media.opengl.GL4;

public class GLProgram {
    private 		GPU 	gpu;
    private final 	GL3 	gl;
    private final 	int 	programID;
    private ValidationHandler 	validationHandler = defaultValidationHandler;

    GLProgram(GPU gpu) {
	this.gpu 	= gpu;
	this.gl 	= gpu.getGl();
	programID 	= gl.glCreateProgram();
	if (programID < 0)
	    throw new RuntimeException("Invalid program ID: " + programID
		    + ". Something went wrong.");
    }//end constructor

    public void attachShader(GLShader shader) {
	gl.glAttachShader(programID, shader.getShaderID());
    }//end attachShader()

    public void link() {
	gl.glLinkProgram(programID);
	if (!validate()) {
	    validationHandler.invalidProgram(this);
	}
    }//end link()

    public void use() {
	gl.glUseProgram(programID);
    }

    public boolean validate() {
	IntBuffer statBuf = IntBuffer.allocate(1);
	gl.glValidateProgram(programID);
	gl.glGetProgramiv(programID, GL4.GL_VALIDATE_STATUS, statBuf);
	return statBuf.get(0) == GL3.GL_TRUE;
    }//end validate()

    public String getInfoLog() {
	IntBuffer statBuf = IntBuffer.allocate(1);
	statBuf.clear();
	gl.glGetProgramiv(programID, GL4.GL_INFO_LOG_LENGTH, statBuf);
	ByteBuffer log = ByteBuffer.allocate(statBuf.get(0));
	gl.glGetProgramInfoLog(programID, statBuf.get(0), null, log);
	return Charset.forName("US-ASCII").decode(log).toString();
    }//end getInfoLog()

    public GLUniform getUniform(String uniformName) {
	final int loc = gl.glGetUniformLocation(programID, uniformName);
	if (loc == -1)
	    throw new RuntimeException("Could not find uniform " + uniformName);
	else if (loc < 0)
	    throw new RuntimeException("Invalid uniform location on lookup: "
		    + loc + " of name " + uniformName);
	return new GLUniform(this, loc);
    }//end getUniform()

    int getProgramID() {
	return programID;
    }//end getProgramID()

    GL3 getGl() {
	return gl;
    }//end getGl()

    public interface ValidationHandler {
	public void invalidProgram(GLProgram p);
    }

    private static final ValidationHandler defaultValidationHandler = new ValidationHandler() {
	@Override
	public void invalidProgram(GLProgram program) {
	    System.out.println("PRIMARY PROGRAM VALIDATION FAILED:");
	    System.out.println(program.getInfoLog());
	}
    };//end anonymous class defaultValidationHandler

    /**
     * @return the validationHandler
     */
    public ValidationHandler getValidationHandler() {
	return validationHandler;
    }//end getvalidationHandler()

    /**
     * @param validationHandler
     *            the validationHandler to set
     */
    public void setValidationHandler(ValidationHandler validationHandler) {
	this.validationHandler = validationHandler;
    }//end setValidationHandler()

}// end GLPRogram
