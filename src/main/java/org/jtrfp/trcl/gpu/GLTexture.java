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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.media.opengl.GL3;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.jtrfp.trcl.core.RootWindow;
import org.jtrfp.trcl.core.TRFuture;
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
    private int width,height,numComponents;

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
    
    private int numComponentsFromEnum(int glEnum){
	switch(glEnum){
	case GL3.GL_RGBA:
		return 4;
	case GL3.GL_RGB:
	    return 3;
	case GL3.GL_RG:
		return 2;
	case GL3.GL_RED:
	    return 1;
	case GL3.GL_RED_INTEGER:
	    return 1;
	default:
	    return 0;
	}//end switch(glEnum)
    }
    
    public GLTexture setImage(int internalOrder, int width, int height, int colorOrder, int numericalFormat, Buffer pixels){
	this.width=width; this.height=height; this.internalColorFormat=numericalFormat;
	setNumComponents(numComponentsFromEnum(colorOrder));
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
		GLTexture.this.width=rawSideLength; GLTexture.this.height=rawSideLength;
		setNumComponents(numComponentsFromEnum(GL3.GL_RGBA));
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
	gl.glBindTexture(bindingTarget, getId());
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
    public GLTexture setWrapR(int wrappingMode) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_R, wrappingMode);
	return this;
    }
    public GLTexture setWrapS(int wrappingMode) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_S, wrappingMode);
	return this;
    }
    public GLTexture setWrapT(int wrappingMode) {
	gl.glTexParameteri(bindingTarget, GL3.GL_TEXTURE_WRAP_T, wrappingMode);
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
	this.width=width; this.height=height;
	//TODO: Num components
	gl.glTexImage2DMultisample(bindingTarget, samples, internalFormat, width, height, fixedSampleLocations);
	return this;
    }

    public GLTexture setImage1D(int internalFormat, int width, int internalOrder, int numericalFormat,
	    FloatBuffer pixels) {
	this.width=width; this.height=1;
	setNumComponents(numComponentsFromEnum(internalOrder));
	gl.glTexImage1D(bindingTarget, 0, internalFormat, width, 0, internalOrder, numericalFormat, pixels);
	return this;
    }

    public GLTexture readPixels(PixelReadOrder pixelReadOrder, PixelReadDataType pixelReadDataType, ByteBuffer buffer) {
	gl.glGetTexImage(bindingTarget, 0, pixelReadOrder.getGlEnum(), pixelReadDataType.getGlEnum(), buffer);
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
	private GLTexture colorTexture,targetTexture;
	private GLFrameBuffer frameBuffer;
	private static final Dimension PANEL_SIZE = new Dimension(200,100);
	private final ByteBuffer rgbaBytes = ByteBuffer.allocate((int)((PANEL_SIZE.getWidth()*PANEL_SIZE.getHeight()*4*4)))
		.order(ByteOrder.nativeOrder());
	private final FloatBuffer rgbaFloats = rgbaBytes.asFloatBuffer();
	private final Thread updateThread;
	private final JPopupMenu popupMenu = new JPopupMenu();
	private final JMenuItem exportToCSV = new JMenuItem("Export To CSV");
	private final ThreadManager threadManager;
	public TextureViewingPanel(final GLTexture parent, RootWindow root){
	    super();
	    this.targetTexture=parent;
	    this.setSize(PANEL_SIZE);
	    this.setPreferredSize(PANEL_SIZE);
	    this.setMinimumSize(PANEL_SIZE);
	    this.setAlignmentX(Component.LEFT_ALIGNMENT);
	    frame = parent.getGPU().getTr().getRootWindow();
	    threadManager = parent.getGPU().getTr().getThreadManager();
	    final GPU gpu = parent.getGPU();
	    final Canvas canvas = frame.getCanvas();
	    popupMenu.add(exportToCSV);
	    updateThread = new Thread(){
		@Override
		public void run(){
		    while(true){
			try{Thread.currentThread().sleep(parent.getPreferredUpdateIntervalMillis());}
			catch(InterruptedException e){e.printStackTrace();}
			Window ancestor = SwingUtilities.getWindowAncestor(TextureViewingPanel.this);
			if(ancestor!=null)
			    if(ancestor.isVisible()){
			 threadManager.submitToGL(new Callable<Void>(){
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
				colorTexture.bind().readPixels(PixelReadOrder.RGBA, PixelReadDataType.FLOAT, rgbaBytes).unbind();
				frameBuffer.bindToDraw();
				parent.bindToTextureUnit(0, gpu.getGl());
				gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
				rgbaBytes.clear();
				//Cleanup
				gpu.defaultViewport();
				gpu.defaultProgram();
				gpu.defaultFrameBuffers();
				TextureViewingPanel.this.repaint();
				return null;
			    }//end call()
			}).get();}
		    }//end while(true)
		}//end run()
	    };
	    threadManager.submitToGL(new Callable<Void>(){
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
	    this.addMouseListener(new MouseAdapter(){
		@Override
		public void mouseClicked(MouseEvent evt) {
		    if(evt.getButton()==MouseEvent.BUTTON3)
			popupMenu.show(evt.getComponent(),evt.getX(),evt.getY());
		    else popupMenu.setVisible(false);
		}//end mouseClicked(...)
		});
	    exportToCSV.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent evt) {
		    final JFileChooser fc = new JFileChooser();
		    fc.setSelectedFile(
			    new File(gpu.getTr().
				    getTrConfig()[0].
				    getFileDialogStartDir()
				    +"/"+parent.
				    getDebugName()+".csv"));
		    fc.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
			    return f.getAbsolutePath().toUpperCase().endsWith(".CSV")||f.isDirectory();
			}
			@Override
			public String getDescription() {
			    return "Comma-Separated Values (.CSV)";
			}});
		    final int result = fc.showSaveDialog(TextureViewingPanel.this);
		    if(result==JFileChooser.APPROVE_OPTION){
			final File selectedFile = fc.getSelectedFile();
			if(selectedFile.isDirectory())
			    return;//Abort
			gpu.getTr().getTrConfig()[0].setFileDialogStartDir(selectedFile.getParentFile().getAbsolutePath());
			writeTextureToCSV(ensureEndsWithCSV(fc.getSelectedFile()));}
		}});
	}//end constructor
	
	final float [] val = new float[4];
	
	private File ensureEndsWithCSV(File f){
	    if(f.getName().toUpperCase().endsWith(".CSV"))
		return f;
	    else return new File(f.getAbsolutePath()+".csv");
	}//end ensureEndsWithCSV(...)
	
	private void writeTextureToCSV(final File destFile){
	    threadManager.submitToThreadPool(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    final boolean intFormat = targetTexture.internalColorFormat==GL3.GL_UNSIGNED_INT;
		    final ByteBuffer dest = ByteBuffer.allocate(
			    4*4*targetTexture.getNumComponents()*
			    targetTexture.getWidth()*targetTexture.getHeight()).order(ByteOrder.nativeOrder());
		    threadManager.submitToGL(new Callable<Void>(){
			@Override
			public Void call() throws Exception {
			    targetTexture.
			    	bind().
			    	readPixels(
			    		intFormat?PixelReadOrder.RGBA_INT:PixelReadOrder.RGBA, 
			    		intFormat?PixelReadDataType.UINT:PixelReadDataType.FLOAT, 
			    		dest).
			    	unbind();
			    return null;
			}}).get();
		    final FileOutputStream fos = new FileOutputStream(destFile);
		    final PrintStream printStream = new PrintStream(fos);
		    final int numCols = targetTexture.getWidth();
		    final int numRows = targetTexture.getHeight();
		    for(int col=0; col<numCols; col++){
			printStream.print("R,G,B,A");
			if(col<numCols-1)
			    printStream.print(",");
		    }
		    dest.clear();
		    printStream.println();
		    for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
			    if (intFormat) {
				printStream.print(dest.getInt() + ",");
				printStream.print(dest.getInt() + ",");
				printStream.print(dest.getInt() + ",");
				printStream.print(dest.getInt());
			    } else {
				printStream.print(dest.getFloat() + ",");
				printStream.print(dest.getFloat() + ",");
				printStream.print(dest.getFloat() + ",");
				printStream.print(dest.getFloat());
			    }
			    
			    if (col < numCols - 1)
				printStream.print(",");
			}// end for(cols)
			printStream.println();
		    }// end for(rows)
		    
		    printStream.close();
		    fos.close();
		    return null;
		}});
	}//end writeTextureToCSV(...)
	
	@Override
	public void paint(Graphics g){
	    super.paint(g);
	    rgbaFloats.clear();
	    for(int y=getHeight()-1; y>=0;y--)
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

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return the numComponents
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * @param numComponents the numComponents to set
     */
    private void setNumComponents(int numComponents) {
        this.numComponents = numComponents;
    }
    
    public static abstract class GLEnumWrapper{
	private final int glEnum;
	public GLEnumWrapper(int glEnum){
	    this.glEnum=glEnum;
	}
	/**
	 * @return the glEnum
	 */
	public int getGlEnum() {
	    return glEnum;
	}
    }//end GLEnumWrapper
    
    public static class Format extends InternalFormat{//enums can't be extended. ):
	public static final Format
		RED = new Format(GL3.GL_RED,R,8),
		RG  = new Format(GL3.GL_RG,R,8,G,8),
		RGB = new Format(GL3.GL_RGB,R,8,G,8,B,8),
		BGR = new Format(GL3.GL_BGR,B,8,G,8,R,8),
		RGBA= new Format(GL3.GL_RGBA,R,8,G,8,B,8,A,8),
		BGRA= new Format(GL3.GL_BGRA,B,8,G,8,R,8,A,8);
	
	public Format(int glEnum, int ... order) {
	    super(glEnum, order);
	}
    }//end Format()
    
    public static class InternalFormat extends GLEnumWrapper {
	protected static final int R=0,G=1,B=2,A=3,N=-1;
	public static final InternalFormat
		R8=new InternalFormat(GL3.GL_R8,R,8),
		R8_SNORM=new InternalFormat(GL3.GL_R8_SNORM,R,8),
		R16=new InternalFormat(GL3.GL_R16,16,0,0,0,R,16),
		R16_SNORM=new InternalFormat(GL3.GL_R16_SNORM,R,16),
		RG8=new InternalFormat(GL3.GL_RG8,R,8,G,8),
		RG8_SNORM=new InternalFormat(GL3.GL_RG8_SNORM,R,8,G,8),
		RG16=new InternalFormat(GL3.GL_RG16,R,16,G,16),
		RG16_SNORM=new InternalFormat(GL3.GL_RG16_SNORM,R,16,G,16),
		R3_G3_B2=new InternalFormat(GL3.GL_R3_G3_B2,R,3,G,3,B,2),
		RGB4=new InternalFormat(GL3.GL_RGB4,R,4,G,4,B,4),
		RGB5=new InternalFormat(GL3.GL_RGB5,R,5,G,5,B,5),
		RGB8=new InternalFormat(GL3.GL_RGB8,R,8,G,8,B,8),
		RGB8_SNORM=new InternalFormat(GL3.GL_RGB8_SNORM,R,8,G,8,B,8),
		RGB10=new InternalFormat(GL3.GL_RGB10,R,10,G,10,B,10),
		RGB12=new InternalFormat(GL3.GL_RGB12,R,12,G,12,B,12),
		RGB16=new InternalFormat(GL3.GL_RGB16,R,16,G,16,B,16),
		RGB16_SNORM=new InternalFormat(GL3.GL_RGB16_SNORM,R,16,G,16,B,16),
		RGBA2=new InternalFormat(GL3.GL_RGBA2,R,2,G,2,B,2,A,2),
		RGBA4=new InternalFormat(GL3.GL_RGBA4,R,4,G,4,B,4,A,4),
		RGB5_A1=new InternalFormat(GL3.GL_RGB5_A1,R,5,G,5,B,5,A,1),
		RGBA8=new InternalFormat(GL3.GL_RGBA8,R,8,G,8,B,8,A,8),
		RGBA8_SNORM=new InternalFormat(GL3.GL_RGBA8_SNORM,R,8,G,8,B,8,A,8),
		RGB10_A2UI=new InternalFormat(GL3.GL_RGB10_A2UI,R,10,G,10,B,10,A,2),
		RGBA12=new InternalFormat(GL3.GL_RGBA12,R,12,G,12,B,12,A,12),
		RGBA16=new InternalFormat(GL3.GL_RGBA16,R,16,G,16,B,16,A,16),
		RGBA16_SNORM=new InternalFormat(GL3.GL_RGBA16_SNORM,R,16,G,16,B,16,A,16),
		SRGB8=new InternalFormat(GL3.GL_SRGB8,R,8,G,8,B,8),
		SRGB8_ALPHA8=new InternalFormat(GL3.GL_SRGB8_ALPHA8,R,8,G,8,B,8),
		R16F=new InternalFormat(GL3.GL_R16F,R,16),
		RG16F=new InternalFormat(GL3.GL_RG16F,R,16,G,16),
		RGB16F=new InternalFormat(GL3.GL_RGB16F,R,16,G,16,B,16),
		R32F=new InternalFormat(GL3.GL_R32F,R,32),
		RG32F=new InternalFormat(GL3.GL_RG32F,R,32,G,32),
		RGB32F=new InternalFormat(GL3.GL_RGB32F,R,32,G,32,B,32),
		RGBA32F=new InternalFormat(GL3.GL_RGBA32F,R,32,G,32,B,32,A,32),
		R11F_G11F_B10F=new InternalFormat(GL3.GL_R11F_G11F_B10F,R,11,G,11,B,10),
		RGB9_E5=new InternalFormat(GL3.GL_RGB9_E5,R,9,G,9,B,9),//TODO
		R8I=new InternalFormat(GL3.GL_R8I,R,8),
		R8UI=new InternalFormat(GL3.GL_R8UI,R,8),
		R16I=new InternalFormat(GL3.GL_R16I,R,16),
		R16UI=new InternalFormat(GL3.GL_R16UI,R,16),
		R32I=new InternalFormat(GL3.GL_R32I,R,32),
		R32UI=new InternalFormat(GL3.GL_R32UI,R,32),
		RG8I=new InternalFormat(GL3.GL_RG8I,R,8,G,8),
		RG8UI=new InternalFormat(GL3.GL_RG8UI,R,8,G,8),
		RG16I=new InternalFormat(GL3.GL_RG16I,R,16,G,16),
		RG16UI=new InternalFormat(GL3.GL_RG16UI,R,16,G,16),
		RG32I=new InternalFormat(GL3.GL_RG32I,R,32,G,32),
		RG32UI=new InternalFormat(GL3.GL_RG32UI,R,32,G,32),
		RGB8I=new InternalFormat(GL3.GL_RGB8I,R,8,G,8,B,8),
		RGB8UI=new InternalFormat(GL3.GL_RGB8UI,R,8,G,8,B,8),
		RGB16I=new InternalFormat(GL3.GL_RGB16I,R,16,G,16,B,16),
		RGB16UI=new InternalFormat(GL3.GL_RGB16UI,R,16,G,16,B,16),
		RGB32I=new InternalFormat(GL3.GL_RGB32I,R,32,G,32,B,32),
		RGB32UI=new InternalFormat(GL3.GL_RGB32UI,R,32,G,32,B,32),
		RGBA8I=new InternalFormat(GL3.GL_RGBA8I,R,8,G,8,B,8,A,8),
		RGBA8UI=new InternalFormat(GL3.GL_RGBA8UI,R,8,G,8,B,8,A,8),
		RGBA16I=new InternalFormat(GL3.GL_RGBA16I,R,16,G,16,B,16,A,16),
		RGBA16UI=new InternalFormat(GL3.GL_RGBA16UI,R,16,G,16,B,16,A,16),
		RGBA32I=new InternalFormat(GL3.GL_RGBA32I,R,32,G,32,B,32,A,32),
		RGBA32UI=new InternalFormat(GL3.GL_RGBA32UI,R,32,G,32,B,32,A,32);

	private final int [] order;
	public InternalFormat(int glEnum, int ... order){
	    super(glEnum);
	    this.order = order;
	}//end constructor
	
	public int getDestComponent(int index){
	    return order[index*2];
	}
	public int getComponentSizeBits(int index){
	    return order[index*2+1];
	}
	/**
	 * @return the order
	 */
	public int[] getOrder() {
	    return order;
	}
    };
    
    public static class PixelReadOrder extends GLEnumWrapper{
	public static final PixelReadOrder
		RED =new PixelReadOrder(GL3.GL_RED),
		GREEN=new PixelReadOrder(GL3.GL_GREEN),
		BLUE=new PixelReadOrder(GL3.GL_BLUE),
		RG=new PixelReadOrder(GL3.GL_RG),
		RGB=new PixelReadOrder(GL3.GL_RGB),
		RGBA=new PixelReadOrder(GL3.GL_RGBA),
		BGR=new PixelReadOrder(GL3.GL_BGR),
		BGRA=new PixelReadOrder(GL3.GL_BGRA),
		RED_INT=new PixelReadOrder(GL3.GL_RED_INTEGER),
		GREEN_INT=new PixelReadOrder(GL3.GL_GREEN_INTEGER),
		BLUE_INT=new PixelReadOrder(GL3.GL_BLUE_INTEGER),
		RG_INT=new PixelReadOrder(GL3.GL_RG_INTEGER),
		RGB_INT=new PixelReadOrder(GL3.GL_RGB_INTEGER),
		RGBA_INT=new PixelReadOrder(GL3.GL_RGBA_INTEGER),
		BGR_INT=new PixelReadOrder(GL3.GL_BGR_INTEGER),
		BGRA_INT=new PixelReadOrder(GL3.GL_BGRA_INTEGER);

	public PixelReadOrder(int glEnum) {
	    super(glEnum);
	}
	
    }//end PixelReadOrder
    
    public static class PixelReadDataType extends GLEnumWrapper{
	public PixelReadDataType(int glEnum) {
	    super(glEnum);
	}

	public static final PixelReadDataType
		UBYTE=new PixelReadDataType(GL3.GL_UNSIGNED_BYTE),
		BYTE=new PixelReadDataType(GL3.GL_BYTE),
		USHORT=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT),
		SHORT=new PixelReadDataType(GL3.GL_SHORT),
		UINT=new PixelReadDataType(GL3.GL_UNSIGNED_INT),
		INT=new PixelReadDataType(GL3.GL_INT),
		HALF_FLOAT=new PixelReadDataType(GL3.GL_HALF_FLOAT),
		FLOAT=new PixelReadDataType(GL3.GL_FLOAT),
		UBYTE_3_3_2=new PixelReadDataType(GL3.GL_UNSIGNED_BYTE_3_3_2),
		UBYTE_2_3_3_REV=new PixelReadDataType(GL3.GL_UNSIGNED_BYTE_2_3_3_REV),
		USHORT_5_6_5=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_5_6_5),
		USHORT_5_6_5_REV=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_5_6_5_REV),
		USHORT_4_4_4_4=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_4_4_4_4),
		USHORT_4_4_4_4_REV=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_4_4_4_4_REV),
		USHORT_5_5_5_1=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_5_5_5_1),
		USHORT_1_5_5_5_REV=new PixelReadDataType(GL3.GL_UNSIGNED_SHORT_1_5_5_5_REV),
		UINT_8_8_8_8=new PixelReadDataType(GL3.GL_UNSIGNED_INT_8_8_8_8),
		UINT_8_8_8_8_REV=new PixelReadDataType(GL3.GL_UNSIGNED_INT_8_8_8_8_REV),
		UINT_10_10_10_2=new PixelReadDataType(GL3.GL_UNSIGNED_INT_10_10_10_2),
		UINT_2_10_10_10_REV=new PixelReadDataType(GL3.GL_UNSIGNED_INT_2_10_10_10_REV),
		UINT_24_8=new PixelReadDataType(GL3.GL_UNSIGNED_INT_24_8),
		UINT_10F_11F_11F_REV=new PixelReadDataType(GL3.GL_UNSIGNED_INT_10F_11F_11F_REV),
		UINT_5_9_9_9_REV=new PixelReadDataType(GL3.GL_UNSIGNED_INT_5_9_9_9_REV),
		FLOAT32_UINT_24_8_REV=new PixelReadDataType(GL3.GL_FLOAT_32_UNSIGNED_INT_24_8_REV);
    }//end PixelReadDataType

    public GLTexture unbind() {
	gl.glBindTexture(getBindingTarget(), 0);
	return this;
    }

    public GLTexture setImagePositiveX(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setImageNegativeX(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setImagePositiveY(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setImageNegativeY(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setImagePositiveZ(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }
    public GLTexture setImageNegativeZ(int internalOrder, int width, int height, int colorOrder,
	    int numericalFormat, ByteBuffer pixels) {
	gl.glTexImage2D(GL3.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, internalOrder, width, height, 0, colorOrder, numericalFormat, pixels);
	return this;
    }

    public int getId() {
	return textureID.get();
    }
}// end GLTexture
