/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import static org.junit.Assert.fail;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GLTestUtils {
    public static JFrame newMiniFrame(){
	return newFrame(128,128);
    }
    public static JFrame newFrame(int width, int height){
	final JFrame result = new JFrame("Terminal Recall Integration Test");
	result.setSize(new Dimension(width,height));
	return result;
    }//end newFrame(...)
    
    public static GLCanvas newCompleteMiniCanvas() throws Exception {
	return newCompleteCanvas(128,128,new GLCapabilities(GLProfile.getGL2GL3()));
    }
    
    public static GLCanvas newCompleteCanvas(final int width, final int height, GLCapabilities capabilities) throws Exception{
	final JFrame frame = newFrame(width,height);
	final GLCanvas canvas = new GLCanvas(capabilities);
	SwingUtilities.invokeAndWait(new Runnable(){
	    @Override
	    public void run() {
		canvas.setPreferredSize(new Dimension(width,height));
		frame.getContentPane().add(canvas);
		frame.setVisible(true);
		canvas.setRealized(true);
		frame.pack();
		frame.invalidate();
	    }});
	return canvas;
    }//end newCompleteCanvas(...)
    
    public static BufferedImage runAttributelessShader(
	    final GLCanvas canvas,
	    final String vertexShaderCode, final String fragmentShaderCode, 
	    int primitiveType, int numVerticies) throws Exception{
	final boolean [] success = new boolean[1];
	final BufferedImage [] result = new BufferedImage[1];
	
	if(!canvas.invoke(true, new GLRunnable(){
	    @Override
	    public boolean run(GLAutoDrawable drawable) {
		try{
		final GL3 gl = drawable.getGL().getGL3();
		final int program = gl.glCreateProgram();
		final int vShader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
		final int fShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
		gl.glShaderSource(vShader, 1, new String[]{vertexShaderCode   }, IntBuffer.wrap(new int []{vertexShaderCode  .length()}));
		gl.glShaderSource(fShader, 1, new String[]{fragmentShaderCode }, IntBuffer.wrap(new int []{fragmentShaderCode.length()}));
		gl.glCompileShader(vShader); 
		final IntBuffer ib       = IntBuffer.wrap(new int[]{5});
		final ByteBuffer infoLog = ByteBuffer.allocate(4096);
		gl.glGetShaderiv(vShader, GL3.GL_COMPILE_STATUS, ib);
		if(ib.get(0)!=1){
		    StringBuilder log = new StringBuilder();
		    gl.glGetShaderInfoLog(vShader, 4096, ib, infoLog);
		    log.append(new CharSequence(){
			@Override
			public char charAt(int index) {
			    return (char)infoLog.get(index);
			}
			@Override
			public int length() {
			    return infoLog.capacity();
			}
			@Override
			public CharSequence subSequence(int arg0, int arg1) {
			    throw new UnsupportedOperationException();
			}});
		    fail("Vertex Shader invalid. Returned "+ib.get(0)+" log follows:\n"+log);
		    }
		gl.glCompileShader(fShader);
		gl.glAttachShader(program, vShader);
		gl.glAttachShader(program, fShader);
		gl.glLinkProgram (program);
		gl.glValidateProgram(program);
		gl.glGetProgramiv(program, GL3.GL_VALIDATE_STATUS, ib);
		if(ib.get(0)!=1){
		    StringBuilder log = new StringBuilder();
		    gl.glGetProgramInfoLog(program, 4096, ib, infoLog);
		    log.append(new CharSequence(){
			@Override
			public char charAt(int index) {
			    return (char)infoLog.get(index);
			}
			@Override
			public int length() {
			    return infoLog.capacity();
			}
			@Override
			public CharSequence subSequence(int arg0, int arg1) {
			    throw new UnsupportedOperationException();
			}});
		    fail("Program invalid. Returned "+ib.get(0)+" log follows:\n"+log);
		    }
		gl.glUseProgram(program);
		gl.glEnableVertexAttribArray(0);
		gl.glDisable(GL3.GL_DEPTH_TEST);
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 5);
		gl.glUseProgram(0);
		gl.glFinish();
		result[0] = GLTestUtils.screenshot(gl, canvas);
		}
		catch(Exception e){e.printStackTrace();fail("Failure due to exception "+e);}
		success[0] = true;
		return true;
	    }}))
	    fail("Failed to submit GL Task");
	if(!success[0])
	    fail("runAttributeShader failed to complete.");
	return result[0];
    }//end runAttributelessShader
    
    /**
     * Adapted from <a href="http://gamedev.stackexchange.com/a/72915">
     * @param gl
     * @param canvas
     * @return
     * @since May 28, 2016
     */
    public static BufferedImage screenshot(GL gl, Canvas canvas){
	final int bytesPerPixel         = 3;
	final int width = canvas.getWidth(), height = canvas.getHeight();
	final int totalPixels           = width * height;
	final int totalBytes            = totalPixels * bytesPerPixel;
	final ByteBuffer capturedPixels = ByteBuffer.allocateDirect(totalBytes);
	final BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	final Graphics graphics        = screenshot.getGraphics();
	gl.glReadPixels(0, 0, width, height, GL3.GL_RGB, GL3.GL_UNSIGNED_BYTE, capturedPixels);
	capturedPixels.clear();
	
	for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            graphics.setColor(new Color( capturedPixels.get() & 0xFF, capturedPixels.get() & 0xFF, capturedPixels.get() & 0xFF ));
	            graphics.drawRect(x,height - y, 1, 1);
	        }
	    }
	    return screenshot;
    }//end screenshot()
    
    
    public static class ImageSizeMismatchException extends Exception {
	private static final long serialVersionUID = -3356941712113763184L;
	public ImageSizeMismatchException()          {super();}
	public ImageSizeMismatchException(String msg){super(msg);}
    }
    
    public static void resizeChildWithParent(Component parent, Component child, Dimension newChildSize) {
	final double dw = newChildSize.getWidth()-child.getWidth(), dh = newChildSize.getHeight()-child.getHeight();
	final Dimension oldSize = parent.getSize();
	parent.setSize((int)(oldSize.getWidth()+dw), (int)(oldSize.getHeight()+dh));
    }//end resizeChildWithParent(...)
    
    public static double compareImage(BufferedImage reference,
	    BufferedImage test, int threshold) throws ImageSizeMismatchException {
	final int    DEV_THRESHOLD     = threshold;
	//final double TOT_DEV_THRESHOLD = .0001;
	final int    rWidth          = reference.getWidth(), 
		     rHeight         = reference.getHeight(),
		     tWidth          = test.getWidth(), 
		     tHeight         = test.getHeight();
	if( rWidth != tWidth || rHeight != tHeight )
	    throw new ImageSizeMismatchException("ref="+rWidth+"x"+rHeight+" test="+tWidth+"x"+tHeight);
	double       deviationTally    = 0;
	final double deviationDivisor  = rWidth*rHeight;
	
	for( int y = 0; y <  rHeight; y++ )
	    for( int x = 0; x < rWidth; x++ ){
		final Color rColor  = new Color(reference.getRGB(x, y));
		//final int rgbR      = reference.getRGB(x, y);
		final int rR        = rColor.getRed(), gR = rColor.getGreen(), bR = rColor.getBlue();
		final Color tColor  = new Color(test.getRGB(x, y));
		//final int rgbT      = test.getRGB(x, y);
		final int rT        = tColor.getRed(), gT = tColor.getGreen(), bT = tColor.getBlue();
		//final int rT        = rgbT & 0xFF, gT = ( rgbT >> 8 ) & 0xFF, bT = ( rgbT >> 16 ) & 0xFF;
		final int dR        = rR - rT, dG = gR - gT, dB = bR - bT;
		final int deviation = Math.abs(dR)+Math.abs(dG)+Math.abs(dB);
		if( deviation > DEV_THRESHOLD )
		    deviationTally++;
	    }//end for(x) for(y)
	return deviationTally / deviationDivisor;
    }//end compareImage(...)
}//end GLTestUtils
