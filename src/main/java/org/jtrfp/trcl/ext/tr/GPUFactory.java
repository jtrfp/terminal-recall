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

package org.jtrfp.trcl.ext.tr;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.ext.tr.ThreadManagerFactory.ThreadManagerFeature;
import org.jtrfp.trcl.gpu.GLExecutor;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

import com.jogamp.opengl.GLAutoDrawable;

@Component
public class GPUFactory implements FeatureFactory<TR> {
 public static class GPUFeature extends GPU implements Feature<TR> {

    public GPUFeature() {
	super();
    }

    @Override
    public void apply(TR target) {
	final RootWindow rootWindow = Features.get(target, RootWindow.class);
	final GLExecutor<?> glExecutor = Features.get(rootWindow, GLExecutor.class);
	if(glExecutor == null)
	    throw new RuntimeException("GLExecutor feature is null. This may mean a suitable GL driver could not be found. Cannot continue.\n"
	    	+ "Is your OpenGL driver working properly and GPU up to spec?");
	final GLAutoDrawable autoDrawable = rootWindow.getAutoDrawable();
	assert autoDrawable != null;
	setAutoDrawable(autoDrawable);
	final ThreadManager threadManager = Features.get(target, ThreadManagerFeature.class);
	setThreadManager(threadManager);
	setGlExecutor(glExecutor);
	setUncaughtExceptionHandler(target);
	setWorld(target.getWorld());
	setExecutorService(threadManager.threadPool);
	setReporter(Features.get(target, Reporter.class));
	initialize();
    }//end apply(...)

    @Override
    public void destruct(TR target) {
	// TODO Auto-generated method stub
	
    }
 }//end GPUFeature

@Override
public Feature<TR> newInstance(TR target) {
    return new GPUFeature();
}//end newInstance(target)

@Override
public Class<TR> getTargetClass() {
    return TR.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return GPUFeature.class;
}
}//end GPUFactory
