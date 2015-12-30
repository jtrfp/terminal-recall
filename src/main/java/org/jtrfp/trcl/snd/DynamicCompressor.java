/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2015 Chuck Ritola.
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

package org.jtrfp.trcl.snd;

import java.nio.FloatBuffer;

import org.jtrfp.trcl.math.Misc;

public final class DynamicCompressor implements AudioProcessor {
private FloatBuffer source;
private double release = .01f;
private double scalar = 1f;

@Override
public float get(){
  scalar += release;
  scalar = Misc.clamp(scalar, 0, 1);
  double val = source.get();
  final double aVal = Math.abs(val*scalar);
  if(aVal>1)
      scalar /=aVal;
  return (float)(val*scalar);
 }//end get()

/**
 * @param source the source to set
 */
public void setSource(FloatBuffer source) {
    this.source = source;
}

/**
 * @param release the release to set
 */
public void setRelease(double changePerSample) {
    this.release = changePerSample;
}
}//end AudioCompressor
