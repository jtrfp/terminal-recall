/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.jtrfp.trcl.WeakPropertyChangeListener;
import org.jtrfp.trcl.core.TRFactory.FailureShutdown;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.springframework.stereotype.Component;

@Component
public class NotifyAndExitShowstopperHandlerFactory
implements FeatureFactory<TR>, LoadOrderAware {
    //Should load after configurator, but still early
    public static final int LOAD_PRIORITY = (int) (LoadOrderAware.FIRST*.9 + LoadOrderAware.LAST*.1);

    @Override
    public Feature<TR> newInstance(TR target)
	    throws FeatureNotApplicableException {
	return new NotifyAndExitShowstopperHandlerFeature();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return NotifyAndExitShowstopperHandlerFeature.class;
    }

    public static class NotifyAndExitShowstopperHandlerFeature implements Feature<TR> {
	private final RunStateListener runStateListener = new RunStateListener();
	private PropertyChangeListener weakRunStateListener;
	private int exitReturnValue = 1;

	@Override
	public void apply(TR target) {
	    weakRunStateListener = new WeakPropertyChangeListener(runStateListener, target);
	    target.addPropertyChangeListener(TRFactory.RUN_STATE, weakRunStateListener);
	}

	@Override
	public void destruct(TR target) {}

	private class RunStateListener implements PropertyChangeListener {
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		final Object newVal = evt.getNewValue();
		if( newVal instanceof TRFactory.FailureShutdown ){
		    final FailureShutdown newState = (FailureShutdown)newVal;
		    System.err.println("==== SHOWSTOPPER ====");
		    final Throwable cause = newState.getCause();
		    if(cause != null)
			cause.printStackTrace();
		    System.err.println("======================");
		    System.err.println("\nIrrecoverable. Exiting...\n\n");
		    System.exit(getExitReturnValue());
		}//end if(FailureShutdown)
	    }//end propertyChange(...)
	}//end RunStateListener

	/**
	 * The current return value which will be passed upon program exit.
	 * @return
	 * @since Dec 18, 2017
	 */
	public int getExitReturnValue() {
	    return exitReturnValue;
	}

	/**
	 * Sets the return value which will be passed upon program exit.
	 * @param exitReturnValue integer representing the System.exit(int) value upon failure exit. (default 1)
	 * @since Dec 18, 2017
	 */
	public void setExitReturnValue(int exitReturnValue) {
	    this.exitReturnValue = exitReturnValue;
	}
    }//end NotifyAndExitShowstopperHandlerFeature

    @Override
    public int getFeatureLoadPriority() {
	return LOAD_PRIORITY;
    }

}//end NotifyAndExitShowstopperHandlerFactory
