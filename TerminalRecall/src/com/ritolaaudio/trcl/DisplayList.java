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

public class DisplayList
	{
	int id;
	public DisplayList(GL2 gl)
		{
		id=gl.glGenLists(1);
		}
	
	public void begin(GL2 gl)
		{
		gl.glNewList(id, GL2.GL_COMPILE);
		}
	public void end(GL2 gl)
		{
		gl.glEndList();
		}
	public void execute(GL2 gl)
		{
		gl.glCallList(id);
		}
	}//end DisplayList
