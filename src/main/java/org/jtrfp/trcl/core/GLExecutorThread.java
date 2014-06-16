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
