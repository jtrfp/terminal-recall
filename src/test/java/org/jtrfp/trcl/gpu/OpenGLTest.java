/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * A test to see what TravisCI does with GL tests.
 * @author Chuck Ritola
 *
 */
public class OpenGLTest {
    protected JFrame    frame;
    protected GLCanvas  canvas;
    protected GLContext context;
    protected volatile boolean   success = false;
    
    static {GLProfile.initSingleton();}
    protected static final String VERTEX_SOURCE = 
	    "#version 330\n" +
	    "void main(){\n" +
	    " gl_Position.x=gl_VertexID%2;\n" +
	    " gl_Position.y=(gl_VertexID/2)%2;\n" +
	    " gl_Position.z=1;\n"+
	    "}\n";
    protected static final String FRAG_SOURCE = 
	    "#version 330\n" +
	    "void main(){\n" +
	    " gl_FragColor.rgb = vec3(0,0,1);\n" +
	    "}\n";

    @Before
    public void setUp() throws Exception {
	if(GraphicsEnvironment.isHeadless())
	    System.exit(0);
	frame  = new JFrame("test");
	canvas = new GLCanvas(new GLCapabilities(GLProfile.getGL2GL3()));
	frame.getContentPane().add(canvas);
	frame.setSize(640, 480);
	frame.setVisible(true);
	canvas.setRealized(true);
    }

    @After
    public void tearDown() throws Exception {
	frame.setVisible(false);
    }

    @Test
    public void test() {
	assertTrue(canvas.isRealized());
	assertNotNull(canvas.getContext());
	if(!canvas.invoke(true, new GLRunnable(){
	    @Override
	    public boolean run(GLAutoDrawable drawable) {
		try{
		final GL3 gl = drawable.getGL().getGL3();
		final int program = gl.glCreateProgram();
		final int vShader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
		final int fShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
		gl.glShaderSource(vShader, 1, new String[]{VERTEX_SOURCE}, IntBuffer.wrap(new int []{VERTEX_SOURCE.length()}));
		gl.glShaderSource(fShader, 1, new String[]{FRAG_SOURCE  }, IntBuffer.wrap(new int []{FRAG_SOURCE  .length()}));
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
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 3);
		gl.glUseProgram(0);}
		catch(Exception e){e.printStackTrace();Assert.fail("Failure due to exception "+e);}
		success=true;
		return true;
	    }}))
	    fail("Failed to submit GL Task");
	assertTrue(success);
    }//end test()
}//end OpenGLTest
