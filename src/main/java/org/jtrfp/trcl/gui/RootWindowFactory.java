/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2018 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gui;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.imageio.ImageIO;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.CanvasProvider;
import org.springframework.stereotype.Component;

@Component
public class RootWindowFactory implements FeatureFactory<TR> {
    static {GLProfile.initSingleton();}
    public static class RootWindow extends JFrame implements Feature<TR>, CanvasProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2412572500302248185L;
	
	private final GLProfile 		glProfile 	= GLProfile.get(GLProfile.GL2GL3);
	private final GLCapabilities 	capabilities 	= new GLCapabilities(glProfile);
	private final GLCanvas 		canvas 		= new GLCanvas(capabilities);
	private static final String         ICON_PATH       = "/ProgramIcon.png";

	public RootWindow(){
	    super();
	    setSize(800,600);
	    try {SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    canvas.setFocusTraversalKeysEnabled(false);
		    getContentPane().add(canvas);
		    //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    setFocusTraversalKeysEnabled(false);
		    setTitle("Terminal Recall");
		    try{RootWindow.this.setIconImage(ImageIO.read(this.getClass().getResource(ICON_PATH)));}
		    catch(Exception e){e.printStackTrace();}
		    RootWindow.this.setMinimumSize(new Dimension(100,100));
		}
	    });
	    } catch (Exception e) {
		e.printStackTrace();
	    }//end try/catch Exception
	}

	public void initialize(){
	    try {SwingUtilities.invokeAndWait(new Runnable() {
		@Override
		public void run() {
		    setVisible(true);
		}
	    });
	    } catch (Exception e) {
		e.printStackTrace();
	    }//end try/catch Exception
	}

	public GLCanvas getCanvas() {
	    return canvas;
	}
/*
	@Override
	public <T> GLFutureTask<T> submitToGL(Callable<T> c){
	    final GLCanvas canvas = getCanvas();
	    final GLContext context = canvas.getContext();
	    final GLFutureTask<T> result = new GLFutureTask<T>(canvas,c);
	    if(context.isCurrent())
		if(context.isCurrent()){
		    result.run();
		    return result;
		}else{
		    context.makeCurrent();
		    result.run();
		    context.release();
		}
	    result.enqueue();
	    return result;
	}//end submitToGL(...)
*/
	@Override
	public void apply(TR target) {
	    //target.setRootWindow(this);
	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub
	}
	
	private class RootWindowCloseListener implements WindowListener {

	    @Override
	    public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	    }
	    
	}//end RootWindowCloseListener
    }// end RootWindow

    @Override
    public Feature<TR> newInstance(TR target) {
	final RootWindow result = new RootWindow();
	result.initialize();
	return result;
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return RootWindow.class;
    }
}// end RootWindowFactory