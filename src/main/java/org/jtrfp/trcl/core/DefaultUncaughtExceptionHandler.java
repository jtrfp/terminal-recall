/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.lang.Thread.UncaughtExceptionHandler;

import org.springframework.stereotype.Component;

@Component
public class DefaultUncaughtExceptionHandler implements
	UncaughtExceptionHandler {
    
    public DefaultUncaughtExceptionHandler(){}

    @Override
    public void uncaughtException(Thread t, Throwable e) {
	e.printStackTrace();
    }

}//end DefaultUncaughtExceptionHandler
