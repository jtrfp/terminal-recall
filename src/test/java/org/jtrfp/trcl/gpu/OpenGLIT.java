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

import java.awt.Component;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;

import javax.media.opengl.GL3;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A test to see what TravisCI does with GL tests.
 * @author Chuck Ritola
 *
 */
public class OpenGLIT {
    //protected JFrame    frame;
    protected GLCanvas  canvas;
    protected GLContext context;
    protected volatile boolean   success = false;
    
    static {
	if(!GraphicsEnvironment.isHeadless())
	 GLProfile.initSingleton();}
    protected static final String VERTEX_SOURCE = 
	    "#version 330\n" +
	    "void main(){\n" +
	    " gl_Position.x=gl_VertexID%2;\n" +
	    " gl_Position.y=(gl_VertexID/2)%2;\n" +
	    " gl_Position.z=0;\n"+
	    " gl_Position.w=1;\n"+
	    "}\n";
    protected static final String FRAG_SOURCE = 
	    "#version 330\n" +
	    "void main(){\n" +
	    " gl_FragColor.rgba = vec4(0,0,1,1);\n" +
	    "}\n";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
	if(canvas != null){
	    Component cmp = canvas;
	    while(!(cmp instanceof Frame))
		cmp = cmp.getParent();
	    Frame frame = (Frame)cmp;
	    canvas.setVisible(false);
	    frame.setVisible (false);
	}
    }//end tearDown()

    @Test
    public void test() throws Exception {
	if(getCanvas()==null)
	    return;//Headless
	GLTestUtils.runAttributelessShader(getCanvas(), VERTEX_SOURCE, FRAG_SOURCE, GL3.GL_TRIANGLES, 3);
    }//end test()

    public GLCanvas getCanvas() throws Exception {
	if(canvas == null){
	    if(!GraphicsEnvironment.isHeadless())
		canvas = GLTestUtils.newCompleteMiniCanvas();
	}//end if(null)
	return canvas;
    }//end getCanvas()
}//end OpenGLTest
