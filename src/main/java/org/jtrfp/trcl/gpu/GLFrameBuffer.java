/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gpu;

import java.awt.Component;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL3;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jtrfp.trcl.gui.TRBeanUtils;

public final class GLFrameBuffer {
    private final int id;
    private GL3 gl;
    private final ArrayList<GLTexture> attached2DDrawTextures = new ArrayList<GLTexture>();

    GLFrameBuffer(GL3 gl) {
	setGl(gl);
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
	if(gl==null)
	    throw new NullPointerException("Passed GL is intolerably null.");
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
	if(texture==null)
	    throw new NullPointerException("Passed texture is intolerably null.");
	gl.glFramebufferTexture(GL3.GL_FRAMEBUFFER, attachmentIndex, texture.getTextureID(), 0);
	attached2DDrawTextures.add(texture);
	return this;
    }
    
    public GLFrameBuffer attachDrawTexture2D(GLTexture texture,
	    int attachmentIndex, int textureTarget ) {
	gl.glFramebufferTexture2D(GL3.GL_FRAMEBUFFER, attachmentIndex, textureTarget, texture.getTextureID(), 0);
	attached2DDrawTextures.add(texture);
	return this;
    }

    public GLFrameBuffer attachDepthRenderBuffer(
	    GLRenderBuffer renderBuffer) {
	gl.glFramebufferRenderbuffer(GL3.GL_DRAW_FRAMEBUFFER, GL3.GL_DEPTH_ATTACHMENT, GL3.GL_RENDERBUFFER, renderBuffer.getId());
	return this;
    }

    public int getId() {
	return id;
    }

    public GLFrameBuffer setDrawBufferList(int ... attachments) {
	gl.glDrawBuffers(attachments.length, IntBuffer.wrap(attachments));
	return this;
    }

    public GLFrameBuffer attachDepthTexture2D(GLTexture depthTexture) {
	gl.glFramebufferTexture2D(
		GL3.GL_FRAMEBUFFER, 
		GL3.GL_DEPTH_ATTACHMENT, GL3.GL_TEXTURE_2D_MULTISAMPLE, depthTexture.getTextureID(), 0);
	return this;
    }
    public GLFrameBuffer attachStencilTexture2D(GLTexture depthTexture) {
	gl.glFramebufferTexture2D(
		GL3.GL_FRAMEBUFFER, 
		GL3.GL_STENCIL_ATTACHMENT, GL3.GL_TEXTURE_2D_MULTISAMPLE, depthTexture.getTextureID(), 0);
	return this;
    }
    
    public static final class PropertyEditor extends PropertyEditorSupport{
	@Override
	public Component getCustomEditor(){
	    final GLFrameBuffer source = (GLFrameBuffer)getSource();
	    final JPanel result = new JPanel();
	    result.setLayout(new BoxLayout(result,BoxLayout.PAGE_AXIS));
	    result.setAlignmentX(Component.LEFT_ALIGNMENT);
	    for(GLTexture tex:source.getAttached2DDrawTextures()){
		final java.beans.PropertyEditor ed = TRBeanUtils.getDefaultPropertyEditor(tex);
		final JLabel label = new JLabel(tex.getDebugName(),SwingConstants.LEFT);
		//Does this align it left?
		label.setHorizontalAlignment(SwingConstants.LEFT);
		//Nope. How about this one?
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
		//Still nope. Ok, how about this?
		label.setHorizontalTextPosition(JLabel.LEFT);
		//Nope!!!
		result.add(label,Alignment.LEADING);
		final JComponent cEd = (JComponent)ed.getCustomEditor();
		cEd.setAlignmentX(Component.LEFT_ALIGNMENT);
		result.add(cEd);
	    }//end for(textures)
	    return result;
	}//end getCustomEditor()
    }//end PropertyEditor
    
    static{
	PropertyEditorManager.registerEditor(GLFrameBuffer.class, GLFrameBuffer.PropertyEditor.class);
    }//end static{}

    /**
     * @return the attached2DDrawTextures
     */
    public ArrayList<GLTexture> getAttached2DDrawTextures() {
        return attached2DDrawTextures;
    }

    public GLFrameBuffer unbindFromDraw() {
	gl.glBindFramebuffer(GL3.GL_DRAW_FRAMEBUFFER, 0);
	return this;
    }

    public GLFrameBuffer destroy() {
	gl.glDeleteFramebuffers(1, IntBuffer.wrap(new int[]{getId()}));
	return this;
    }

    public GLFrameBuffer attachDrawTexture(GLTexture texture, int layer,
	    int attachmentIndexEnum) {
	int id;
	if(texture == null)
	    id = 0;
	else
	    id = texture.getId();
	gl.glFramebufferTextureLayer(GL3.GL_DRAW_FRAMEBUFFER, attachmentIndexEnum, id, 0, layer);
	return this;
    }
    
    public GLFrameBuffer validate(){
	final int status=gl.glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER);
	if(status!=GL3.GL_FRAMEBUFFER_COMPLETE){
	    switch(status){
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS:
		throw new RuntimeException("Framebuffer Incomplete: Layer targets.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
		throw new RuntimeException("Framebuffer Incomplete: Attachment.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
		throw new RuntimeException("Framebuffer Incomplete: Dimensions.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
		throw new RuntimeException("Framebuffer Incomplete: Draw Buffer.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
		throw new RuntimeException("Framebuffer Incomplete: Formats.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_LAYER_COUNT_ARB:
		throw new RuntimeException("Framebuffer Incomplete: Layer ccount (ARB).");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
		throw new RuntimeException("Framebuffer Incomplete: Missing attachment.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
		throw new RuntimeException("Framebuffer Incomplete: Multisample.");
	    case GL3.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
		throw new RuntimeException("Framebuffer Incomplete: Read Buffer.");
		default:
		    System.out.println("Framebuffer incomplete. glCheckFramebufferStatus returned "+status);
	    }//end switch(status)
	}else System.out.println("Framebuffer OK.");
	return this;
    }
}//end GLFrameBuffer
