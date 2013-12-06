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

import java.awt.Color;

public class GammaCorrectingColorProcessor implements ColorProcessor
	{
	double GAMMA=1.2;
	public static final GammaCorrectingColorProcessor singleton = new GammaCorrectingColorProcessor();
	@Override
	public Color process(Color c)
		{
		return new Color(
				(int)(Math.pow((double)c.getRed()/256.,GAMMA)*256.),
				(int)(Math.pow((double)c.getGreen()/256.,GAMMA)*256.),
				(int)(Math.pow((double)c.getBlue()/256.,GAMMA)*256.));
		}//end process()

	}//end GammaCorrectingColorProcessor
