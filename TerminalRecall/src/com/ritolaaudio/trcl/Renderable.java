/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import javax.media.opengl.GL2;

public interface Renderable
	{
	public void render(GL2 gl);
	public double getxPos();
	public double getyPos();
	public double getzPos();
	public void setxPos(double x);
	public void setyPos(double y);
	public void setzPos(double y);
	public RenderMode getRenderMode();
	}
