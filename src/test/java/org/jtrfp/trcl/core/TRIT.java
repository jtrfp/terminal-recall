/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2017 Chuck Ritola
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFactory.TRRunState;
import org.jtrfp.trcl.flow.RunMe;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.gpu.GLTestUtils;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.gui.GLExecutable;
import org.jtrfp.trcl.gui.MenuSystem;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.jtrfp.trcl.gui.SwingMenuSystemFactory;
import org.jtrfp.trcl.shell.GameShellFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import com.jogamp.opengl.GL3;

public class TRIT {
    private static final Integer [] COMPLETION_BARRIER = new Integer[1];
    private static final int        SECONDS            = 1000;
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Before
    public void before(){
	Features.resetForTesting();
    }

    //@Test
    public void testRegisterFeature() {
	Features.registerFeature(new SwingMenuSystemFactory());
	Features.get(Features.getSingleton(), MenuSystem.class);
    }
    
    @Test
    public void testLoadToBootScreen() throws Throwable {
	exit.expectSystemExitWithStatus(0);
	final String SCREENSHOT_PATH = "org.jtrfp.trcl.core.TRIT.TestTRCL.FAIL_SCREENSHOT.png";
	final String SCREENSHOT_REF  = "/org.jtrfp.trcl.core.TRIT.TestTRCL.TITLE_SCREENSHOT_REF.png";
	Executor transientExecutor   = TransientExecutor.getSingleton();
	final LoadCompletionListener loadCompletionListener = new LoadCompletionListener();
	
	synchronized(transientExecutor){
	    transientExecutor.execute(RunMe.BOOTSTRAP);
	    transientExecutor.execute(new Runnable(){
		@Override
		public void run() {
		    final TR tr = Features.get(Features.getSingleton(), TR.class);
		    tr.addPropertyChangeListener(TRFactory.RUN_STATE, loadCompletionListener);
		    loadCompletionListener.setTr(tr);
		}});
	}//end sync(transientExecutor)
	loadCompletionListener.awaitLoadCompletion();
	final BufferedImage [] result = new BufferedImage[1];
	synchronized(transientExecutor){
	    transientExecutor.execute(new Runnable(){
		@Override
		public void run() {
		    final Features features     = Features.getSingleton();
		    final TR tr                 = Features.get(features, TR.class);
		    final RootWindow rootWindow = Features.get(tr, RootWindow.class);
		    //Normalize the window size
		    try{SwingUtilities.invokeAndWait(new Runnable(){
			@Override
			public void run() {
			    GLTestUtils.resizeChildWithParent(rootWindow, rootWindow.getCanvas(), new Dimension(504,464));
			}});}
		    catch(Exception e){throw new RuntimeException(e);}
		    
		}}); 
	}//end sync(transientExecutor)
	Thread.sleep(10 * SECONDS); //XXX Padding to let things settle.
	
	synchronized(transientExecutor){
	    transientExecutor.execute(new Runnable(){
		@Override
		public void run() {
		  //Get a screenshot
		    final Features features     = Features.getSingleton();
		    final TR tr = Features.get(features, TR.class);
		    final RootWindow rootWindow = Features.get(tr, RootWindow.class);
		    final Renderer renderer = tr.mainRenderer;

		    final Future<BufferedImage> screenshotTask = renderer.getGpu().getGlExecutor().submitToGL(new GLExecutable<BufferedImage, GL3>(){
			@Override
			public BufferedImage execute(GL3 gl) {
			    return GLTestUtils.screenshot(renderer.getGpu().getGl(), rootWindow.getCanvas());
			}});
		    try {result[0] = screenshotTask.get();}
		    catch(Exception e) {e.printStackTrace();}
		    synchronized(result) {
			result.notifyAll();}
		}//end run()
	    });//end Runnable
	}//end sync(transientExecutor)
	
	synchronized(result) {
	    while( result[0] == null )
		result.wait();
	}
	final BufferedImage resultScreenshot = result[0];
	//final int width = resultScreenshot.getWidth(), height = resultScreenshot.getHeight();
	//final BufferedImage reference = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	final BufferedImage reference = ImageIO.read(TRIT.class.getResourceAsStream(SCREENSHOT_REF));
	final boolean matchesReference = GLTestUtils.compareImage(reference, resultScreenshot, 40) < .01;
	if( !matchesReference ){
	    final File screenshotFile = new File(SCREENSHOT_PATH);
	    ImageIO.write(resultScreenshot, "png", screenshotFile);
	}
	Assert.assertTrue("Resulting screenshot mismatched reference too much. Rendering problem?\nSaved to "+SCREENSHOT_PATH,matchesReference);
	System.exit(0);
    }//end testTRCL
 
 private class LoadCompletionListener implements PropertyChangeListener {
     private final boolean [] loadCompleted = new boolean[1];
     private TR tr;
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	final TRRunState runState = (TRRunState)evt.getNewValue();
	if(runState instanceof GameShellFactory.GameShellConstructed) {
	    synchronized( loadCompleted ) {
		loadCompleted[0] = true;
		loadCompleted.notifyAll();
	    }//end sync( loadCompleted )
	}//end if(constructed)
    }//end propertyChange(...)
    
    protected boolean isComplete(){
	return tr.getRunState() instanceof GameShellFactory.GameShellConstructed;
    }

    public void setTr(TR tr) {
	this.tr = tr;
	if( isComplete() ){
	    synchronized( loadCompleted ) {
	    loadCompleted[0] = true;//XXX hack for when it is ready before listener is installed.
	    loadCompleted.notifyAll();
	    }//end sync
	}//end if(isComplete)
    }//end setTr(...)

    public void awaitLoadCompletion() {
	synchronized( loadCompleted ) {
	    while( !loadCompleted[0] )
		try{loadCompleted.wait();}
	    catch(InterruptedException e){e.printStackTrace();}//Shouldn't happen.
	}//end sync(loadCompleted)
    }//end awaitLoadCompletion()
 }//end LoadCompletionListener

}//end TRIT
