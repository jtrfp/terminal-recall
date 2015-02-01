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
package org.jtrfp.trcl.core;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class RootWindow extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -2412572500302248185L;
    static {GLProfile.initSingleton();}
    private final GLProfile 		glProfile 	= GLProfile.get(GLProfile.GL2GL3);
    private final GLCapabilities 	capabilities 	= new GLCapabilities(glProfile);
    private final GLCanvas 		canvas 		= new GLCanvas(capabilities);

    public RootWindow() {
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		@Override
		public void run() {
		    canvas.setFocusTraversalKeysEnabled(false);
		    getContentPane().add(canvas);
		    setVisible(true);
		    setSize(800, 600);
		    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    setFocusTraversalKeysEnabled(false);
		}
	    });
	} catch (Exception e) {
	    e.printStackTrace();
	}//end try/catch Exception
	setTitle("Terminal Recall");
    }//end constructor

    public GLCanvas getCanvas() {
	return canvas;
    }
}// end RootWindow
