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

import java.util.concurrent.Callable;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;
import javax.media.opengl.awt.GLCanvas;

public class GLFutureTask<V> extends TRFutureTask<V> implements GLRunnable {
    private final GLCanvas      canvas;

    public GLFutureTask(GLCanvas canvas, Callable<V> callable) {
	super(callable);
	this.canvas=canvas;
    }
    @Override
    public void run(){
	super.run();}
    
    public void enqueue(){
	canvas.invoke(false, this);
    }

    @Override
    public boolean run(GLAutoDrawable drawable) {
	super.run();
	return true;
    }//end run(GL)
    
    @Override
    public V get(){
	if(!super.isDone())
	    if(canvas.getContext().isCurrent())
	     run();
	return super.get();
    }
}//end GLFutureTask
