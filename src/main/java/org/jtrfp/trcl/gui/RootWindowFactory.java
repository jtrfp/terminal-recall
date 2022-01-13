/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.CanvasProvider;
import org.springframework.stereotype.Component;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jtattoo.plaf.noire.NoireLookAndFeel;

@Component
public class RootWindowFactory implements FeatureFactory<TR> {
    static {
	System.setProperty("jogl.disable.openglcore", "false");GLProfile.initSingleton();
	System.setProperty("awt.useSystemAAFontSettings","lcd");
	System.setProperty("swing.aatext", "true");
	}
    public static class RootWindow extends JFrame implements Feature<TR>, CanvasProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2412572500302248185L;
	
	private final GLProfile 	glProfile       = GLProfile.getMaxProgrammable(true);
	private final GLCapabilities 	capabilities 	= new GLCapabilities(glProfile);
	private final GLCanvas 		canvas 		= new GLCanvas(capabilities);
	private static final String         ICON_PATH       = "/ProgramIcon.png";
	private final GLEventListener   glEventListener = new RootWindowGLEventListener();
	
	public RootWindow(){
	    super();
	    affirmLookAndFeel();
	    setSize(800,600);
	    try {SwingUtilities.invokeLater(new Runnable() {
		@Override
		public void run() {
		    canvas.setFocusTraversalKeysEnabled(false);
		    canvas.addGLEventListener(glEventListener);
		    getContentPane().add(canvas);
		    setFocusTraversalKeysEnabled(false);
		    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);//We'll handle the closing process so we don't lose context during a pending shutdown.
		    setTitle("Terminal Recall");
		    try{RootWindow.this.setIconImage(ImageIO.read(this.getClass().getResource(ICON_PATH)));}
		    catch(Exception e){e.printStackTrace();}
		    RootWindow.this.setMinimumSize(new Dimension(100,100));
		}
	    });
	    } catch (Exception e) {
		e.printStackTrace();
	    }//end try/catch Exception
	}//end constructor
	
	protected void affirmLookAndFeel() {
	    try {
		Properties props = new Properties();
		props.put("logoString", "Terminal Recall");
		try {
		    NoireLookAndFeel.setCurrentTheme(props);
		} catch(Exception e) {e.printStackTrace();}
		UIManager.setLookAndFeel(NoireLookAndFeel.class.getName());
		SwingUtilities.updateComponentTreeUI(this);
		pack();
	    } catch(UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException | ClassNotFoundException e) 
	    	{e.printStackTrace();}
	}//end affirmLookAndFeel()

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
	
	private class RootWindowGLEventListener implements GLEventListener {

	    @Override
	    public void init(GLAutoDrawable drawable) {
		final GL gl = drawable.getGL();
		gl.glDisable(GL3.GL_DITHER);
		////gl.glDisable(GL3.GL_POINT_SMOOTH);
		gl.glDisable(GL3.GL_LINE_SMOOTH);
		gl.glDisable(GL3.GL_POLYGON_SMOOTH);
		//gl.glHint(GL3.GL_LINE_SMOOTH, GL3.GL_DONT_CARE);
		//gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_DONT_CARE);
		
		////final int GL_MULTISAMPLE_ARB = 0x809D;
		////gl.glDisable( GL_MULTISAMPLE_ARB);
	    }

	    @Override
	    public void dispose(GLAutoDrawable drawable) {
		System.out.println("RootWindowFactory.RootWindowGLEventListener.dispose()");
	    }

	    @Override
	    public void display(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void reshape(GLAutoDrawable drawable, int x, int y,
		    int width, int height) {
		drawable.getGL().glViewport(0, 0, width, height);
	    }//end reshape()
	    
	}//end DefaultEventListener	
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