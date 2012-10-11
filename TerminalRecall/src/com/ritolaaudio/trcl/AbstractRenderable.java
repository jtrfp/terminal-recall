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

public abstract class AbstractRenderable implements Renderable
	{
	private double xPos, yPos, zPos;
	private RenderMode renderMode;

	/**
	 * @return the renderMode
	 */
	@Override
	public RenderMode getRenderMode()
		{
		return renderMode;
		}

	/**
	 * @param renderMode the renderMode to set
	 */
	public void setRenderMode(RenderMode renderMode)
		{
		this.renderMode = renderMode;
		}

	/**
	 * @return the xPos
	 */
	@Override
	public double getxPos()
		{
		return xPos;
		}

	/**
	 * @param xPos the xPos to set
	 */
	@Override
	public void setxPos(double xPos)
		{
		this.xPos = xPos;
		}

	/**
	 * @return the yPos
	 */
	public double getyPos()
		{
		return yPos;
		}

	/**
	 * @param yPos the yPos to set
	 */
	public void setyPos(double yPos)
		{
		this.yPos = yPos;
		}

	/**
	 * @return the zPos
	 */
	public double getzPos()
		{
		return zPos;
		}

	/**
	 * @param zPos the zPos to set
	 */
	public void setzPos(double zPos)
		{
		this.zPos = zPos;
		}
	}
