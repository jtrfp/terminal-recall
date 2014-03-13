package org.jtrfp.trcl.gpu;

import java.nio.IntBuffer;

import javax.media.opengl.GL3;

public final class GLFrameBuffer {
    private final int id;
    private GL3 gl;

    GLFrameBuffer(GL3 gl) {
	this.gl=gl;
	IntBuffer result = IntBuffer.wrap(new int[1]);
	gl.glGenFramebuffers(1, result);
	id = result.get(0);
    }
    
    public GLFrameBuffer bindToRead(){
	gl.glBindFramebuffer(GL3.GL_READ_FRAMEBUFFER, id);
	return this;
    }
    public GLFrameBuffer bindToDraw(){
	gl.glBindFramebuffer(GL3.GL_DRAW_FRAMEBUFFER, id);
	return this;
    }
    
    public GLFrameBuffer setGl(GL3 gl){
	this.gl=gl;
	return this;
    }
    
    public GLFrameBuffer attachDepthTexture(GLTexture depthTexture){
	gl.glFramebufferTexture(
		GL3.GL_FRAMEBUFFER, 
		GL3.GL_DEPTH_ATTACHMENT, depthTexture.getTextureID(), 0);
	return this;
    }
    
    public GLFrameBuffer attachDrawTexture(GLTexture texture,
	    int attachmentIndex) {
	gl.glFramebufferTexture(GL3.GL_FRAMEBUFFER, attachmentIndex, texture.getTextureID(), 0);
	return this;
    }

    public GLFrameBuffer attachDepthRenderBuffer(
	    GLRenderBuffer renderBuffer) {
	gl.glFramebufferRenderbuffer(GL3.GL_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER, renderBuffer.getId());
	return this;
    }

    public int getId() {
	return id;
    }

    public GLFrameBuffer setDrawBufferList(int ... attachments) {
	gl.glDrawBuffers(attachments.length, IntBuffer.wrap(attachments));
	return this;
    }
}//end GLFrameBuffer
