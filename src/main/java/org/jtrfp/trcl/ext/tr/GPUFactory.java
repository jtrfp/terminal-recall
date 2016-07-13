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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;

import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.World;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gpu.GLExecutor;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class GPUFactory implements FeatureFactory<TR> {
 public static class GPUFeature extends GPU implements Feature<TR> {

    public GPUFeature(ExecutorService executorService, GLExecutor glExecutor,
	    ThreadManager threadManager,
	    UncaughtExceptionHandler exceptionHandler, GLCanvas glCanvas,
	    World world) {
	super(executorService, glExecutor, threadManager, exceptionHandler, glCanvas,
		world);
    }

    @Override
    public void apply(TR target) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void destruct(TR target) {
	// TODO Auto-generated method stub
	
    }
     
 }//end GPUFeature

@Override
public Feature<TR> newInstance(TR target) {
    final RootWindow rootWindow = Features.get(target, RootWindow.class);
    return new GPUFeature(
	    target.getThreadManager().threadPool, 
	    target.getThreadManager(), 
	    target.getThreadManager(), 
	    target, rootWindow.getCanvas(),
	    target.getWorld());
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
