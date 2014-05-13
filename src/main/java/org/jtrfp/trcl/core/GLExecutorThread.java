package org.jtrfp.trcl.core;

import javax.media.opengl.GLContext;

public class GLExecutorThread extends Thread {
    private final GLContext context;
    public GLExecutorThread(GLContext context, Runnable runnable) {
	super(runnable);
	setName("glThreadPool");
	//setPriority(ThreadManager.RENDERING_PRIORITY);
	this.context=context;
    }
    /**
     * @return the context
     */
    public GLContext getContext() {
        return context;
    }

}//end GLExecutorThread
