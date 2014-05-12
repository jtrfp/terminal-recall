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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureDescription;

public abstract class LineSegment {
    public static Triangle[] buildTriPipe(Vector3D start, Vector3D end,
	    TRFutureTask<TextureDescription> texture, int thickness, Triangle[] dest,
	    int destOffset) {
	Rotation rot = new Rotation(Vector3D.PLUS_K, end.subtract(start)
		.normalize());
	final double len = start.distance(end);
	// Start
	Vector3D sbl = new Vector3D(-thickness, -thickness, 0);// bottom left
	Vector3D sbr = new Vector3D(thickness, -thickness, 0);// bottom right
	Vector3D stp = new Vector3D(0, thickness, 0);
	// End
	Vector3D ebl = new Vector3D(-thickness, -thickness, len);
	Vector3D ebr = new Vector3D(thickness, -thickness, len);
	Vector3D etp = new Vector3D(0, thickness, len);

	Vector3D cl = new Vector3D(-1, 1, 0).normalize();
	Vector3D cr = new Vector3D(1, 1, 0).normalize();
	Vector3D cb = new Vector3D(0, -1, 0);

	cl = rot.applyTo(cl);
	cr = rot.applyTo(cr);
	cb = rot.applyTo(cb);

	sbl = rot.applyTo(sbl).add(start);
	sbr = rot.applyTo(sbr).add(start);
	stp = rot.applyTo(stp).add(start);

	ebl = rot.applyTo(ebl).add(start);
	ebr = rot.applyTo(ebr).add(start);
	etp = rot.applyTo(etp).add(start);

	final double u[] = { 0, 1, 1, 0 };
	final double v[] = { 1, 1, 0, 0 };
	// TOP LEFT
	Triangle.quad2Triangles(
		new double[] { sbl.getX(), stp.getX(), etp.getX(), ebl.getX() },
		new double[] { sbl.getY(), stp.getY(), etp.getY(), ebl.getY() },
		new double[] { sbl.getZ(), stp.getZ(), etp.getZ(), ebl.getZ() },
		u, v, texture, RenderMode.STATIC, false, cl, dest, destOffset,"LineSegment.topLeft");
	// TOP RIGHT
	Triangle.quad2Triangles(
		new double[] { sbr.getX(), stp.getX(), etp.getX(), ebr.getX() },
		new double[] { sbr.getY(), stp.getY(), etp.getY(), ebr.getY() },
		new double[] { sbr.getZ(), stp.getZ(), etp.getZ(), ebr.getZ() },
		u, v, texture, RenderMode.STATIC, false, cr, dest,
		destOffset + 2,"LineSegment.topRight");
	// BOTTOM
	Triangle.quad2Triangles(
		new double[] { sbl.getX(), sbr.getX(), ebr.getX(), ebl.getX() },
		new double[] { sbl.getY(), sbr.getY(), ebr.getY(), ebl.getY() },
		new double[] { sbr.getZ(), sbr.getZ(), ebr.getZ(), ebl.getZ() },
		u, v, texture, RenderMode.STATIC, false, cb, dest,
		destOffset + 4,"LineSegment.bottom");
	return dest;
    }// end buildTriPipe
}// end LineSegment
