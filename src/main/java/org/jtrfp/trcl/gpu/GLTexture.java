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

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.RootWindow;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.mem.MemoryManager;

public final class GLTexture {
    private final GPU gpu;
    private final TRFuture<Integer> textureID;
    private int rawSideLength;
    private final GL3 gl;
    private int bindingTarget = GL3.GL_TEXTURE_2D;
    private int internalColorFormat = GL3.GL_RGBA4;
    private boolean deleted=false;
    private static GLProgram textureRenderProgram;
    private String debugName="UNNAMED";
    private final double [] expectedMaxValue = new double[]{1,1,1,1};
    private final double [] expectedMinValue = new double[]{0,0,0,0};
    private int preferredUpdateIntervalMillis = 500;

    public GLTexture(final GPU gpu) {
	System.out.println("Creating GL Texture...");
	this.gpu = gpu;
	textureID = gpu.getTr().getThreadManager().submitToGL(new Callable<Integer>(){
	    @Override
	    public Integer call() throws Exception {
		return gpu.newTextureID();
	    }});
	gl = gpu.getGl();
	// Setup the empty rows
	System.out.println("...Done.");
    }// end constructor
    
    public GLTexture setImage(int internalOrder, int width, int height, int colorOrder, int numericalFormat, Buffer pixels){
	if(pixels==null && width*height*16 < MemoryManager.ZEROES.capacity()){
	    pixels=MemoryManager.ZEROES;
	    synchronized(pixels){
		pixels.clear();gl.glTexImage2D(bindingTarget, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);}
	    }//end if(null)
	else gl.glTexImage2D(bindingTarget, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setParameteri(int parameterName, int value){
	gl.glTexParameteri(textureID.get(), parameterName, value);
	return this;
    }

    /**
     * Takes a square texture in RGBA 8888 format. Automatically determines
     * dimensions from buffer size.
     * 
     * @param buf
     *            Directly-allocated buffer containing the image data.
     * @since Dec 11, 2013
     */
    public void setTextureImageRGBA(final ByteBuffer buf) {
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		rawSideLength = (int) Math.sqrt(buf.capacity() / 4);
		buf.rewind();
		GL3 gl = gpu.getGl();
		gl.glBindTexture(bindingTarget, textureID.get());
		/*FloatBuffer isoSize = FloatBuffer.wrap(new float[] { 0 });
		gl.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, isoSize);*/
		System.out.println("Uploading texture...");
		gl.glTexImage2D(bindingTarget, 0, internalColorFormat, rawSideLength,
			rawSideLength, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buf);
		gl.glGenerateMipmap(bindingTarget);
		System.out.println("\t...Done.");
		return null;
	    }}).get();
    }//end setTextureImageRGBA
    
    public void getTextureImageRGBA(final ByteBuffer buf) {
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		rawSideLength = (int) Math.sqrt(buf.capacity() / 4);
		buf.rewind();
		GL3 gl = gpu.getGl();
		gl.glBindTexture(bindingTarget, textureID.get());
		System.out.println("Downloading texture...");
		gl.glGetTexImage(bindingTarget, 0, GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buf);
		System.out.println("\t...Done.");
		return null;
	    }}).get();
    }//end setTextureImageRGBA
    
    public GLTexture configure(int [] sideLengthsInTexels, int numLevels ){
	switch(sideLengthsInTexels.length){
	case 3:{
	 gl.glTexStorage3D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0], sideLengthsInTexels[1], sideLengthsInTexels[2]);
	    break;
	}case 2:{
	    gl.glTexStorage2D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0], sideLengthsInTexels[1]);
	    break;
	}case 1:{
	    gl.glTexStorage1D(bindingTarget, numLevels, internalColorFormat, sideLengthsInTexels[0]);
	    break;
	}
	default:{
	    throw new RuntimeException("Invalid number of dimensions in specified sideLength: "+sideLengthsInTexels.length);
	}}
	return this;
    }//end configureEmpty(...)
    
    public GLTexture subImage(int [] texelCoordinates, int [] sideLengthsInTexels, int format, int level, ByteBuffer texels){
	if(texelCoordinates.length!=sideLengthsInTexels.length)
	    throw new RuntimeException("Texel coordinate dims ("+texelCoordinates.length+") must match sideLength dims ("+sideLengthsInTexels.length+").");
	switch(texelCoordinates.length){
	case 1:{
	    gl.glTexSubImage1D(bindingTarget, level, texelCoordinates[0], sideLengthsInTexels[0], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 2:{
	    gl.glTexSubImage2D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1], sideLengthsInTexels[0], sideLengthsInTexels[1], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}case 3:{
	    if(level<0)throw new RuntimeException("Level is intolerably negative: "+level);
	    gl.glTexSubImage3D(bindingTarget, level, texelCoordinates[0],texelCoordinates[1],texelCoordinates[2], sideLengthsInTexels[0], sideLengthsInTexels[1],sideLengthsInTexels[2], GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, texels);
	    break;
	}
	default:{
	    throw new RuntimeException("Invalid number of dimensions in specified coordinates: "+texelCoordinates.length);
	}}
	return this;
    }

    public void delete() {
	if(isDeleted())
	    return;
	gl.glBindTexture(bindingTarget, textureID.get());
	gl.glDeleteTextures(1, IntBuffer.wrap(new int[] { textureID.get() }));
	deleted=true;
    }

    int getTextureID() {
	return textureID.get();
    }

    public static void specifyTextureUnit(GL3 gl, int unitNumber) {
	gl.glActiveTexture(GL3.GL_TEXTURE0 + unitNumber);
    }
    
    public GLTexture bind(){
	return bind(gl);
    }
    
    public GLTexture bind(GL3 gl) {
	gl.glBindTexture(bindingTarget, getTextureID());
	return this;
    }
    
    public GLTexture bindToTextureUnit(int unitNumber, GL3 gl){
	GLTexture.specifyTextureUnit(gl, unitNumber);
	bind(gl);
	return this;
    }

    public int getCurrentSideLength() {
	return rawSideLength;
    }

    public GLTexture setMagFilter(int mode) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_MAG_FILTER, mode);
	return this;
    }
    public GLTexture setMinFilter(int mode){
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_MIN_FILTER, mode);
	return this;
    }

    public GLTexture setWrapS(int val) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_S, val);
	return this;
    }
    public GLTexture setWrapT(int val) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_T, val);
	return this;
    }

    /**
     * @return the bindingTarget
     */
    public int getBindingTarget() {
        return bindingTarget;
    }

    /**
     * @param bindingTarget the bindingTarget to set
     */
    public GLTexture setBindingTarget(int bindingTarget) {
        this.bindingTarget = bindingTarget;
        return this;
    }

    /**
     * @return the internalColorFormat
     */
    public int getInternalColorFormat() {
        return internalColorFormat;
    }

    /**
     * @param internalColorFormat the internalColorFormat to set
     */
    public GLTexture setInternalColorFormat(int internalColorFormat) {
        this.internalColorFormat = internalColorFormat;
        return this;
    }

    public GLTexture setImage2DMultisample(int samples,
	    int internalFormat, int width, int height, boolean fixedSampleLocations) {
	gl.glTexImage2DMultisample(bindingTarget, samples, internalFormat, width, height, fixedSampleLocations);
	return this;
    }

    public GLTexture setImage1D(int internalFormat, int width, int internalOrder, int numericalFormat,
	    FloatBuffer pixels) {
	gl.glTexImage1D(bindingTarget, 0, internalFormat, width, 0, internalOrder, numericalFormat, pixels);
	return this;
    }

    public GLTexture readPixels(int pixelFormat, int pixelDataType, ByteBuffer buffer) {
	gl.glGetTexImage(bindingTarget, 0, pixelFormat, pixelDataType, buffer);
	return this;
    }
    
    @Override
    public void finalize() throws Throwable{
	gpu.getTr().getThreadManager().submitToGL(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		delete();
		return null;
	    }}).get();
	super.finalize();
    }

    /**
     * @return the deleted
     */
    public boolean isDeleted() {
        return deleted;
    }
    
    public GPU getGPU() {
	return gpu;
    }
    
    private static GLProgram getTextureRenderProgram(GPU gpu){
	if(textureRenderProgram!=null)
	    return textureRenderProgram;
	try{
	    GLShader vs = gpu.newVertexShader().setSourceFromResource("/shader/fullScreenQuadVertexShader.glsl");
	    GLShader fs = gpu.newFragmentShader().setSourceFromResource("/shader/fullScreenTextureFragShader.glsl");
	    textureRenderProgram = gpu.newProgram().attachShader(vs).attachShader(fs).link();
	    textureRenderProgram.validate();
	    textureRenderProgram.use();
	    textureRenderProgram.getUniform("textureToUse").set((int)0);
	}catch(IOException e){gpu.getTr().showStopper(e);}
	return textureRenderProgram;
    }//end getTextureRenderProgram(...)
    
    public static final class PropertyEditor extends PropertyEditorSupport{
	@Override
	public Component getCustomEditor(){
	    final GLTexture source = (GLTexture)getSource();
	    final JPanel result = new JPanel();
	    if(source.getBindingTarget()==GL3.GL_TEXTURE_2D){
		result.add(new TextureViewingPanel(source, source.getGPU().getTr().getRootWindow()));
	    }//TODO: Texture 1D
	    return result;
	}//end getCustomEditor()
    }//end PropertyEditor
    
    static{
	PropertyEditorManager.registerEditor(GLTexture.class, GLTexture.PropertyEditor.class);
    }//end static{}
    
    private static class TextureViewingPanel extends JPanel{
	private static final long serialVersionUID = 4580039742312228700L;
	private final RootWindow frame;
	private GLTexture colorTexture;
	private GLFrameBuffer frameBuffer;
	private static final Dimension PANEL_SIZE = new Dimension(200,100);
	private TRFutureTask future;
	private final ByteBuffer rgbaBytes = ByteBuffer.allocate((int)((PANEL_SIZE.getWidth()*PANEL_SIZE.getHeight()*4*4)))
		.order(ByteOrder.nativeOrder());
	private final FloatBuffer rgbaFloats = rgbaBytes.asFloatBuffer();
	private final Thread updateThread;
	public TextureViewingPanel(final GLTexture parent, RootWindow root){
	    super();
	    this.setSize(PANEL_SIZE);
	    this.setPreferredSize(PANEL_SIZE);
	    this.setMinimumSize(PANEL_SIZE);
	    this.setAlignmentX(Component.LEFT_ALIGNMENT);
	    frame = parent.getGPU().getTr().getRootWindow();
	    final ThreadManager tm = parent.getGPU().getTr().getThreadManager();
	    final GPU gpu = parent.getGPU();
	    final Canvas canvas = frame.getCanvas();
	    updateThread = new Thread(){
		@Override
		public void run(){
		    while(true){
			try{Thread.currentThread().sleep(parent.getPreferredUpdateIntervalMillis());}
			catch(InterruptedException e){e.printStackTrace();}
			Window ancestor = SwingUtilities.getWindowAncestor(TextureViewingPanel.this);
			if(ancestor!=null)
			    if(ancestor.isVisible()){
			 tm.submitToGL(new Callable<Void>(){
			    @Override
			    public Void call() throws Exception {
				GL3 gl = gpu.getGl();
				gl.glDepthMask(false);
				gl.glViewport(0, 0, getWidth(), getHeight());
				gl.glDepthFunc(GL3.GL_ALWAYS);
				final double [] min = parent.getExpectedMinValue();
				final double [] max = parent.getExpectedMaxValue();
				final GLProgram prg = getTextureRenderProgram(gpu);
				prg.use();
				prg.getUniform("scalar").set(
					1f/(float)(max[0]-min[0]), 
					1f/(float)(max[1]-min[1]), 
					1f/(float)(max[2]-min[2]), 
					1f/(float)(max[3]-min[3]));
				prg.getUniform("offset").set(
					(float)min[0]/(float)(max[0]-min[0]), 
					(float)min[1]/(float)(max[1]-min[1]), 
					(float)min[2]/(float)(max[2]-min[2]), 
					(float)min[3]/(float)(max[3]-min[3]));
				colorTexture.bind().readPixels(GL3.GL_RGBA, GL3.GL_FLOAT, rgbaBytes);
				frameBuffer.bindToDraw();
				parent.bindToTextureUnit(0, gpu.getGl());
				gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
				rgbaBytes.clear();
				//Cleanup
				gl.glViewport(0, 0, canvas.getWidth(), canvas.getHeight());
				gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
				TextureViewingPanel.this.repaint();
				return null;
			    }//end call()
			}).get();}
		    }//end while(true)
		}//end run()
	    };
	    tm.submitToGL(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    colorTexture = gpu
			    .newTexture()
			    .bind()
			    .setMinFilter(GL3.GL_NEAREST)
			    .setMagFilter(GL3.GL_NEAREST)
			    .setWrapS(GL3.GL_CLAMP_TO_EDGE)
			    .setWrapT(GL3.GL_CLAMP_TO_EDGE)
			    .setImage(GL3.GL_RGBA32F, 
				    (int)PANEL_SIZE.getWidth(), 
				    (int)PANEL_SIZE.getHeight(), 
				    GL3.GL_RGBA, 
				    GL3.GL_FLOAT, null);
		    frameBuffer = gpu
			    .newFrameBuffer()
			    .bindToDraw()
			    .attachDrawTexture(colorTexture, GL3.GL_COLOR_ATTACHMENT0)
			    .setDrawBufferList(GL3.GL_COLOR_ATTACHMENT0);
		    if(gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER) != GL3.GL_FRAMEBUFFER_COMPLETE){
			    throw new RuntimeException("Texture display frame buffer setup failure. OpenGL code "+gpu.getGl().glCheckFramebufferStatus(GL3.GL_FRAMEBUFFER));
			}
		    updateThread.start();
		    return null;
		}});
	}//end constructor
	
	final float [] val = new float[4];
	
	@Override
	public void paint(Graphics g){
	    super.paint(g);
	    rgbaFloats.clear();
	    for(int y=0; y<getHeight();y++)
		for(int x=0; x<getWidth();x++){
		    rgbaFloats.get(val);
		    g.setColor(new Color(val[0],val[1],val[2],1));
		    g.fillRect(x, y, 1, 1);}
	}//end paint(...)
    }//end TextureViewingCanvas

    /**
     * @return the debugName
     */
    public String getDebugName() {
        return debugName;
    }

    /**
     * @param debugName the debugName to set
     */
    public GLTexture setDebugName(String debugName) {
        this.debugName = debugName;
        return this;
    }

    /**
     * @return the expectedMaxValue
     */
    public double[] getExpectedMaxValue() {
        return expectedMaxValue;
    }

    /**
     * @return the expectedMinValue
     */
    public double[] getExpectedMinValue() {
        return expectedMinValue;
    }

    /**
     * @param expectedMaxValue the expectedMaxValue to set
     */
    public GLTexture setExpectedMaxValue(double r, double g, double b, double a) {
        this.expectedMaxValue[0]=r;
        this.expectedMaxValue[1]=g;
        this.expectedMaxValue[2]=b;
        this.expectedMaxValue[3]=a;
        return this;
    }

    /**
     * @param expectedMinValue the expectedMinValue to set
     */
    public GLTexture setExpectedMinValue(double r, double g, double b, double a) {
	this.expectedMaxValue[0]=r;
        this.expectedMaxValue[1]=g;
        this.expectedMaxValue[2]=b;
        this.expectedMaxValue[3]=a;
        return this;
    }

    /**
     * @return the preferredUpdateIntervalMillis
     */
    public int getPreferredUpdateIntervalMillis() {
        return preferredUpdateIntervalMillis;
    }

    /**
     * @param preferredUpdateIntervalMillis the preferredUpdateIntervalMillis to set
     */
    public GLTexture setPreferredUpdateIntervalMillis(int preferredUpdateIntervalMillis) {
        this.preferredUpdateIntervalMillis = preferredUpdateIntervalMillis;
        return this;
    }
}// end GLTexture
