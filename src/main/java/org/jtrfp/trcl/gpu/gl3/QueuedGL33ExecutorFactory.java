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

package org.jtrfp.trcl.gpu.gl3;

import java.util.concurrent.Executors;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.gpu.GLAutoDrawableProvider;
import org.jtrfp.trcl.gpu.QueuedGLExecutor;
import org.springframework.stereotype.Component;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLRunnable;

@Component
public class QueuedGL33ExecutorFactory implements FeatureFactory<GLAutoDrawableProvider>{
    
    public static class QueuedGL33Executor extends QueuedGLExecutor<GL3> implements GL33Executor, Feature<GLAutoDrawableProvider> {
	
	@Override
	public void apply(GLAutoDrawableProvider target) {
	    setGLAutoDrawableProvider(target);
	}

	@Override
	public void destruct(GLAutoDrawableProvider target) {
	    setGLAutoDrawableProvider(null);
	}

	@Override
	public Class<? extends GL> getGLClass() {
	    return GL3.class;
	}
	
    }//end CanvasBoundGL33Executor

    @Override
    public Feature<GLAutoDrawableProvider> newInstance(GLAutoDrawableProvider target)
	    throws FeatureNotApplicableException {
	final String [] versionString = new String[1];
	target.getAutoDrawable().invoke(true, new GLRunnable(){

	    @Override
	    public boolean run(GLAutoDrawable drawable) {
		versionString[0] = drawable.getGL().glGetString(GL.GL_VERSION);
		return true;
	    }});
	System.out.println("QueuedGL33Executor: Evaluating GL version string `"+versionString[0]+"`");
	final String [] parts      = versionString[0].split("\\.");
	final String [] minorParts = parts[1].split("\\s+");
	try {
	final int major            = Integer.parseInt(parts[0]);
	final int minor            = minorParts.length > 0?Integer.parseInt(minorParts[0]):0;
	final int compositeVersion = major * 100 + minor;
	if(compositeVersion < 303) {
	    System.out.println("\t... parsed composite version number: "+compositeVersion);
	    throw new FeatureNotApplicableException("Must be GL (not ES) 3.3. Got "+versionString);
	}
	} catch(NumberFormatException e){
	    System.out.println("\t... failed to parse the version number.");
	    throw new FeatureNotApplicableException("Must be GL (not ES) 3.3, with major formatted as a number. Got "+versionString);
	    }
	return new QueuedGL33Executor();
    }//end newInstance

    @Override
    public Class<GLAutoDrawableProvider> getTargetClass() {
	return GLAutoDrawableProvider.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return QueuedGL33Executor.class;
    }
}//end CanvasBoundGL33ExecutorFactory
