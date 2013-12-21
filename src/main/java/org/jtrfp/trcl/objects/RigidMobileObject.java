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
package org.jtrfp.trcl.objects;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.ai.ObjectBehavior;

public class RigidMobileObject extends MobileObject
	{
	private Vector3D velocity = Vector3D.ZERO;
	private Vector3D drag = Vector3D.ZERO;
	
	public RigidMobileObject(Model model,
			ObjectBehavior behavior, World world)
		{super(model, behavior, world);}

	/**
	 * @return the velocity
	 */
	public Vector3D getVelocity()
		{
		return velocity;
		}

	/**
	 * @param velocity the velocity to set
	 */
	public void setVelocity(Vector3D velocity)
		{
		this.velocity = velocity;
		}

	/**
	 * @return the drag
	 */
	public Vector3D getDrag()
		{
		return drag;
		}

	/**
	 * @param drag the drag to set
	 */
	public void setDrag(Vector3D drag)
		{
		this.drag = drag;
		}
	}//end RigidMobileObject
