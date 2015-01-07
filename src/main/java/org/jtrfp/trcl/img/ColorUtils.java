/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2015 Chuck Ritola and contributors.
 * See Github project's commit log for contribution details.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 ******************************************************************************/

package org.jtrfp.trcl.img;

import java.awt.Color;

import org.jtrfp.trcl.math.Misc;

public class ColorUtils {
    public static Color mul(Color l, Color r){
	return new Color(
		(int)Misc.clamp((l.getRed()*r.getRed())/255, 0, 255),
		(int)Misc.clamp((l.getGreen()*r.getGreen())/255, 0, 255),
		(int)Misc.clamp((l.getBlue()*r.getBlue())/255, 0, 255),
		(int)Misc.clamp((l.getAlpha()*r.getAlpha())/255, 0, 255));
    }//end mul()
    
    public static Color mul(Color l, float scalar){
	return new Color(
		(int)Misc.clamp((l.getRed()*scalar), 0, 255),
		(int)Misc.clamp((l.getGreen()*scalar), 0, 255),
		(int)Misc.clamp((l.getBlue()*scalar), 0, 255),
		(int)Misc.clamp((l.getAlpha()*scalar), 0, 255));
    }//end mul()
}//end ColorUtils
