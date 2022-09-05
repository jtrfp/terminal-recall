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
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.jtrfp.trcl.conf.ui.CheckboxUI;
import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ctl.Newt2AWTKeyListener;
import org.jtrfp.trcl.gpu.GLAutoDrawableProvider;
import org.springframework.stereotype.Component;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;

import lombok.Getter;

@Component
public class RootWindowFactory implements FeatureFactory<TR> {
    static {
	System.setProperty("jogl.disable.openglcore", "false");GLProfile.initSingleton();
	System.setProperty("awt.useSystemAAFontSettings","lcd");
	System.setProperty("swing.aatext", "true");
	}
    public static class RootWindow extends JFrame implements Feature<TR>, GLAutoDrawableProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2412572500302248185L;
	
	@Getter(lazy=true)
	private final GLProfile 	glProfile       = createGLProfile();
	@Getter(lazy=true)
	private final GLCapabilities 	glCapabilities 	= createGLCapabilities();
	@Getter(lazy=true)
	private final GLWindow glWindow = createGLWindow();
	@Getter(lazy=true)
	private final NewtCanvasAWT 		canvas 		= createCanvas();
	private static final String         ICON_PATH       = "/ProgramIcon.png";
	
	private final GLEventListener   glEventListener = new RootWindowGLEventListener();
	private boolean fullScreen = false;
	private Rectangle normalBounds = null;
	
	public RootWindow(){
	    super();
	    final GLWindow w = getGlWindow();
	    w.addKeyListener(new Newt2AWTKeyListener(RootWindow.this));
	    
	    try {
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
			affirmLookAndFeel();
			setSize(800,600);
			getGlWindow().addGLEventListener(glEventListener);
			NewtCanvasAWT canvas = getCanvas();
			getContentPane().add(canvas);
			setFocusTraversalKeysEnabled(false);
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);//We'll handle the closing process so we don't lose context during a pending shutdown.
			setTitle("Terminal Recall");
			try{RootWindow.this.setIconImage(ImageIO.read(this.getClass().getResource(ICON_PATH)));}
			catch(Exception e){e.printStackTrace();}
			RootWindow.this.setMinimumSize(new Dimension(100,100));
		    }
		});
	    } catch(Exception e) {e.printStackTrace();}
	}//end constructor
	
	private GLWindow createGLWindow() {
	    final GLWindow result = GLWindow.create(getGlCapabilities());
	    return result;
	}
	
	private GLCapabilities createGLCapabilities() {
	    final GLCapabilities result = new GLCapabilities(getGlProfile());
	    return result;
	}
	
	private GLProfile createGLProfile() {
	    final GLProfile result = GLProfile.getMaxProgrammable(true);
	    return result;
	}
	
	private NewtCanvasAWT createCanvas() {
	    final NewtCanvasAWT result = new NewtCanvasAWT(getGlWindow());
	    result.setFocusTraversalKeysEnabled(false);
	    return result;
	}

	@ConfigByUI(editorClass=CheckboxUI.class)
	public void setFullScreen(boolean newState) {
	    if(newState == fullScreen)
		return;
	    fullScreen = newState;
	    SwingUtilities.invokeLater(()->{
		GraphicsEnvironment graphics =
			GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphics.getDefaultScreenDevice();
		final DisplayMode mode = device.getDisplayMode();
		setVisible(false);
		dispose();
		setUndecorated(newState);
		
		if(newState) {
		    setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		    normalBounds = getBounds();
		    setBounds(new Rectangle(0,0,mode.getWidth(), mode.getHeight()));
		} else {
		    setExtendedState(this.getExtendedState() ^ JFrame.MAXIMIZED_BOTH);
		    if(normalBounds != null)
			setBounds(normalBounds);
		}
		setVisible(true);
	    });//end invokeLater()
	}//end setFullScreen(...)

	public boolean isFullScreen() {
	    return fullScreen;
	}
	
	@Override
	public void setBounds(Rectangle newBounds) {
	    super.setBounds(newBounds);
	}
	
	@Override
	public Rectangle getBounds() {
	    if(isFullScreen() && normalBounds != null)
		return normalBounds;
	    else
		return super.getBounds();
	}
/*
	@ConfigByUI(editorClass=CheckboxUI.class)
	public void setMaximized(boolean maximized) {
	    setResizable(true);
	    if(maximized)
		setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	    else
		setExtendedState(this.getExtendedState() ^ JFrame.MAXIMIZED_BOTH);
	}
	
	public boolean isMaximized() {
	    return ((this.getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
	}
	*/
	protected void affirmLookAndFeel() {
	    try {
		Properties props = new Properties();
		props.put("logoString", "Terminal Recall");
		try {
		    HiFiLookAndFeel.setCurrentTheme(props);
		} catch(Exception e) {e.printStackTrace();}
		UIManager.setLookAndFeel(HiFiLookAndFeel.class.getName());
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

	public GLAutoDrawable getAutoDrawable() {
	    return getGlWindow();
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
	/*
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
	*/
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
    public Class<RootWindow> getFeatureClass() {
	return RootWindow.class;
    }
}// end RootWindowFactory