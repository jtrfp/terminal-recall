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

import org.jtrfp.trcl.gpu.OpaqueModifierContext;

public class GL33OpaqueTriangleModifierContext extends
	GL33TriangleModelModifierContext implements OpaqueModifierContext {
    
    @Override
    public void flush(){
	//TODO
	super.flush();
    }

}//end GL33OpaqueTriangleModifierContext
