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

import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLRunnable;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.gpu.CanvasBoundGLExecutor;
import org.jtrfp.trcl.gpu.CanvasProvider;
import org.springframework.stereotype.Component;

@Component
public class CanvasBoundGL33ExecutorFactory implements FeatureFactory<CanvasProvider>{
    
    public static class CanvasBoundGL33Executor extends CanvasBoundGLExecutor<GL3> implements GL33Executor, Feature<CanvasProvider> {

	@Override
	public void apply(CanvasProvider target) {
	    setCanvasProvider(target);
	}

	@Override
	public void destruct(CanvasProvider target) {
	    setCanvasProvider(null);
	}

	@Override
	public Class<? extends GL> getGLClass() {
	    return GL3.class;
	}
	
    }//end CanvasBoundGL33Executor

    @Override
    public Feature<CanvasProvider> newInstance(CanvasProvider target)
	    throws FeatureNotApplicableException {
	final String [] versionString = new String[1];
	target.getCanvas().invoke(true, new GLRunnable(){

	    @Override
	    public boolean run(GLAutoDrawable drawable) {
		versionString[0] = drawable.getGL().glGetString(GL.GL_VERSION);
		return true;
	    }});
	final String [] parts      = versionString[0].split("\\.");
	final String [] minorParts = parts[1].split("\\s+");
	final int major            = Integer.parseInt(parts[0]);
	final int minor            = minorParts.length > 0?Integer.parseInt(minorParts[0]):0;
	final int compositeVersion = major * 100 + minor;
	if(compositeVersion < 303)
	    throw new FeatureNotApplicableException("Must be GL 3.3. Got "+versionString);
	return new CanvasBoundGL33Executor();
    }//end newInstance

    @Override
    public Class<CanvasProvider> getTargetClass() {
	return CanvasProvider.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return CanvasBoundGL33Executor.class;
    }
}//end CanvasBoundGL33ExecutorFactory
