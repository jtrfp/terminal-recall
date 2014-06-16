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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.Callable;

import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.mem.GPUMemDump;

public class RootWindow extends JFrame {
    /**
     * 
     */
    private static final long serialVersionUID = -2412572500302248185L;
    static {GLProfile.initSingleton();}
    private final TR 			tr;
    private final GLProfile 		glProfile 	= GLProfile.get(GLProfile.GL2GL3);
    private final GLCapabilities 	capabilities 	= new GLCapabilities(glProfile);
    private final GLCanvas 		canvas 		= new GLCanvas(capabilities);

    public RootWindow(TR tr) {
	this.tr = tr;
	try {
	    SwingUtilities.invokeAndWait(new Runnable() {
		@Override
		public void run() {
		    configureMenuBar();
		    // frame.setBackground(Color.black);
		    getContentPane().add(canvas);
		    setVisible(true);
		    setSize(800, 600);
		    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	    });
	} catch (Exception e) {
	    e.printStackTrace();
	}//end try/catch Exception
	setTitle("Terminal Recall");
    }//end constructor

    private void configureMenuBar() {
	setJMenuBar(new JMenuBar());
	JMenu file = new JMenu("File"), window = new JMenu("Window");

	// And menus to menubar
	JMenuItem file_exit = new JMenuItem("Exit");
	JMenuItem debugStatesMenuItem = new JMenuItem("Debug States");
	JMenuItem gpuMemDump = new JMenuItem("Dump GPU Memory");
	// Menu item behaviors
	file_exit.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		System.exit(1);
	    }
	});
	debugStatesMenuItem.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ev) {
		tr.getReporter().setVisible(true);
	    };
	});
	gpuMemDump.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent ev) {
		tr.getThreadManager().submitToThreadPool(new Callable<Void>(){

		    @Override
		    public Void call() throws Exception {
			new GPUMemDump(tr);
			return null;
		    }});
	    };
	});
	final String showDebugStatesOnStartup = System
		.getProperty("org.jtrfp.trcl.showDebugStates");
	if (showDebugStatesOnStartup != null) {
	    if (showDebugStatesOnStartup.toUpperCase().contains("TRUE")) {
		tr.getReporter().setVisible(true);
	    }
	}
	file.add(file_exit);
	file.add(gpuMemDump);
	window.add(debugStatesMenuItem);
	getJMenuBar().add(file);
	getJMenuBar().add(window);
    }//end configureMenuBar()

    public GLCanvas getCanvas() {
	return canvas;
    }
}// end RootWindow
