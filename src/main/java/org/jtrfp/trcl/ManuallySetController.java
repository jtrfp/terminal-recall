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
package org.jtrfp.trcl;

public class ManuallySetController implements Controller {
    private double frame=0;
    private boolean stale=true;
    private boolean debug=false;
    @Override
    public double getCurrentFrame() {
	if(debug)System.out.println("getCurrentFrame()");
	return frame;
    }
    
    public void setFrame(double f){frame=f;stale=true;}
/*
    @Override
    public void unstale() {
	stale=false;
    }

    @Override
    public boolean isStale() {
	return stale;
    }
*/
    @Override
    public void setDebugMode(boolean b) {
	debug=b;
    }

}//end MenaullySetController
