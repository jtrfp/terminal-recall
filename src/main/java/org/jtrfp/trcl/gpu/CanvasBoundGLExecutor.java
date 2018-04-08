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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLRunnable;
import com.jogamp.opengl.awt.GLCanvas;

import org.jtrfp.trcl.core.GLFutureTask;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.gui.GLExecutable;

import com.ochafik.util.listenable.Pair;

public abstract class CanvasBoundGLExecutor<GL_TYPE extends GL> implements GLExecutor<GL_TYPE> {
    private CanvasProvider canvasProvider;
    private GLEventListener glEventListener;
    private final PriorityQueue<Pair<Double,GLExecutable<?,GL_TYPE>>> repeatingTasks = new PriorityQueue<Pair<Double,GLExecutable<?,GL_TYPE>>>(64, new PairKeyComparator());
    private final ArrayList<GLExecutable<?,GL_TYPE>> executeOnDispose = new ArrayList<GLExecutable<?,GL_TYPE>>();
    private final ArrayList<GLExecutable<?,GL_TYPE>> executeOnResize = new ArrayList<GLExecutable<?,GL_TYPE>>();
    private final ArrayList<GLExecutable<?,GL_TYPE>> executeBuffer = new ArrayList<GLExecutable<?,GL_TYPE>>();
    
    public abstract Class<? extends GL> getGLClass();
    
    @Override
    public <T> TRFutureTask<T> submitToGL(Callable<T> c) {
	final GLFutureTask<T> task  = new GLFutureTask<T>(getCanvas(), c);
	task.enqueue();
	return task;
    }//end submitToGL()

    @Override
    public <T> TRFutureTask<T> submitToGL(
	    GLExecutable<T,GL_TYPE> executable) {
	final GLExecutableWrapper<T> wrapper = new GLExecutableWrapper<T>(executable, getCanvas());
	final GLFutureTask<T> result = new GLFutureTask<T>(getCanvas(),wrapper);//TODO: More direct version
	return result;
    }

    @Override
    public void executeOnEachRefresh(GLExecutable<Void, GL_TYPE> executable,
	    double orderPriority) {
	synchronized(repeatingTasks){
	    repeatingTasks.add(new Pair<Double,GLExecutable<?,GL_TYPE>>(orderPriority,executable));
	}
    }

    protected CanvasProvider getCanvasProvider() {
        return canvasProvider;
    }

    protected void setCanvasProvider(CanvasProvider canvasProvider) {
	final GLCanvas              oldCanvas = getCanvas();
	final GLEventListener glEventListener = getGlEventListener();
	if(oldCanvas != null)
	    oldCanvas.removeGLEventListener(glEventListener);
        this.canvasProvider = canvasProvider;
        if(canvasProvider != null){
            final GLCanvas                  canvas = canvasProvider.getCanvas();
            final GL                            gl = canvas.getGL();
            final Class<? extends GL> desiredClass = getGLClass();
            if(! desiredClass.isAssignableFrom(gl.getClass())){
        	throw new IllegalArgumentException(
        		"CanvasProvider gives a "+gl.getClass().getName()+
        		" but expected a GL instance of type "+desiredClass.getName());}
            canvas.addGLEventListener(glEventListener);
            }
    }//end setCanvasProvider()
    
    protected GLCanvas getCanvas(){
	final CanvasProvider canvasProvider = getCanvasProvider();
	if(canvasProvider == null)
	    return null;
	return canvasProvider.getCanvas();
    }//end getCanvas()
    
    
    private class DefaultGLEventListener implements GLEventListener {

	@Override
	public void init(GLAutoDrawable drawable) {}

	@Override
	public void dispose(GLAutoDrawable drawable) {
	    synchronized(executeOnDispose){
		executeBuffer.addAll(executeOnDispose);
		executeOnDispose.clear();
	    }//end sync(executeOnDispose)
	    final GL_TYPE gl = (GL_TYPE)drawable.getGL();
	    for(GLExecutable<?, GL_TYPE> executable:executeBuffer)
		executable.execute(gl);
	    executeBuffer.clear();
	}//end dispose()

	@Override
	public void display(GLAutoDrawable drawable) {
	    synchronized(repeatingTasks){
		for(Pair<Double,GLExecutable<?,GL_TYPE>> pair:repeatingTasks)
		    executeBuffer.add(pair.getSecond());
	    }//end sync(executeOnDispose)
	    final GL_TYPE gl = (GL_TYPE)drawable.getGL();
	    for(GLExecutable<?, GL_TYPE> executable:executeBuffer)
		executable.execute(gl);
	    executeBuffer.clear();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
		int height) {
	    synchronized(executeOnResize){
		executeBuffer.addAll(executeOnResize);
	    }//end sync(executeOnReshape)
	    final GL_TYPE gl = (GL_TYPE)drawable.getGL();
	    for(GLExecutable<?, GL_TYPE> executable:executeBuffer)
		executable.execute(gl);
	    executeBuffer.clear();
	}
	
    }//end DefaultGLEventListener


    protected GLEventListener getGlEventListener() {
	if(glEventListener == null)
	    glEventListener = new DefaultGLEventListener();
        return glEventListener;
    }

    protected void setGlEventListener(GLEventListener glEventListener) {
        this.glEventListener = glEventListener;
    }

    @Override
    public void executeOnResize(GLExecutable<Void, GL_TYPE> executable) {
	synchronized(executeOnResize){
	    executeOnResize.add(executable);
	}
    }

    @Override
    public void executeOnDispose(GLExecutable<Void, GL_TYPE> executable) {
	synchronized(executeOnDispose){
	    executeOnDispose.add(executable);
	}
    }
    
    protected class DefaultExecutableRunner<RETURN_TYPE> implements GLRunnable {
	private final GLExecutable<RETURN_TYPE,GL_TYPE> executable;
	
	public DefaultExecutableRunner(GLExecutable<RETURN_TYPE, GL_TYPE> executable){
	    this.executable = executable;
	}

	@Override
	public boolean run(GLAutoDrawable drawable) {
	    executable.execute((GL_TYPE)drawable.getGL());
	    return true;
	}
	
    }//end DefaultExecutableRunner
    
    protected class GLExecutableWrapper<RETURN_TYPE> implements Callable<RETURN_TYPE> {
	private final GLExecutable<RETURN_TYPE, GL_TYPE> executable;
	private GLCanvas canvas;
	
	public GLExecutableWrapper(GLExecutable<RETURN_TYPE, GL_TYPE> executable, GLCanvas canvas){
	    this.executable = executable;
	    this.canvas     = canvas;
	}

	@Override
	public RETURN_TYPE call() throws Exception {
	    return executable.execute((GL_TYPE)canvas.getGL().getGL());
	}
    }//end GLExecutableWrapper
    
    protected class PairKeyComparator implements Comparator<Pair<Double,GLExecutable<?, GL_TYPE>>> {

	@Override
	public int compare(Pair<Double, GLExecutable<?, GL_TYPE>> left,
		Pair<Double, GLExecutable<?, GL_TYPE>> right) {
	    if(left.getFirst() > right.getFirst())
		return 1;
	    if(left.getFirst() < right.getFirst())
		return -1;
	    return 0;
	}//end compare(...)
	
    }//end PairKeyComparator
}//end CanvasBoundGL_TYPEExecutor
