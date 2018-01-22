/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2018 Chuck Ritola
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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.KeyedExecutor;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.flow.TransientExecutor;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class TRRootWindowShutdownFactory implements FeatureFactory<RootWindow> {

    @Override
    public Feature<RootWindow> newInstance(RootWindow target)
	    throws FeatureNotApplicableException {
	final TRRootWindowShutdownFeature result = new TRRootWindowShutdownFeature();
	result.setTr(Features.get(Features.getSingleton(), TR.class));
	return result;
    }

    @Override
    public Class<RootWindow> getTargetClass() {
	return RootWindow.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return TRRootWindowShutdownFeature.class;
    }
    
    public static class TRRootWindowShutdownFeature implements Feature<RootWindow> {
	private final CloseListener closeListener = new CloseListener();
	private TR tr;

	@Override
	public void apply(RootWindow target) {
	    target.addWindowListener(closeListener);
	}

	@Override
	public void destruct(RootWindow target) {
	    target.removeWindowListener(closeListener);
	}
	
	private class CloseListener implements WindowListener {

	    @Override
	    public void windowOpened(WindowEvent e) {}

	    @Override
	    public void windowClosing(WindowEvent e) {
		//Isolate the EDT from the Transient thread.
		final KeyedExecutor<?> executor = TransientExecutor.getSingleton();
		synchronized(executor) {
		    TransientExecutor.getSingleton().execute(new Runnable(){
			@Override
			public void run() {
			    final TR tr = getTr();
			    tr.shutdown();
			}});
		}//end sync(executor)
	    }//end windowClosing(...)

	    @Override
	    public void windowClosed(WindowEvent e) {}

	    @Override
	    public void windowIconified(WindowEvent e) {}

	    @Override
	    public void windowDeiconified(WindowEvent e) {}

	    @Override
	    public void windowActivated(WindowEvent e) {}

	    @Override
	    public void windowDeactivated(WindowEvent e) {}
	    
	}//end CloseListener

	public TR getTr() {
	    return tr;
	}

	public void setTr(TR tr) {
	    this.tr = tr;
	}
	
    }//end TRRootWindowShutdownFeature

}//end TRRootWindowShutdownFactory
