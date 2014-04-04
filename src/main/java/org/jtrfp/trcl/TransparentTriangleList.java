/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import org.jtrfp.trcl.core.TR;

public class TransparentTriangleList extends TriangleList {
    public TransparentTriangleList(Triangle[][] triangles,
	    int timeBetweenFramesMsec, String debugName, boolean animateUV,
	    Controller controller, TR tr, Model m) {
	super(triangles, timeBetweenFramesMsec, debugName, animateUV,
		controller, tr, m);
    }

    @Override
    public RenderStyle getRenderStyle() {
	return RenderStyle.TRANSPARENT;
    }
}// end TransparentTriangleList
