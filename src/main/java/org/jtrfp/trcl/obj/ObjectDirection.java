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
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

public class ObjectDirection
	{
	private final Vector3D heading;
	private final Vector3D top;
	private final double yaw,roll,tilt;
	
	public ObjectDirection(int legacyRoll, int legacyPitch, int legacyYaw)
		{Vector3D headingAccumulator,topAccumulator;
		Rotation rot;
		yaw=((double)legacyYaw/65535.)*2*Math.PI;
		roll=((double)legacyRoll/65535.)*2*Math.PI;
		tilt=((double)legacyPitch/65535.)*2*Math.PI;
		/*
		Rotation hRot = new Rotation(//yaw only.
				new Vector3D(0,1,0),
				new Vector3D(0,0,1),
				new Vector3D(0,1,0),
				new Vector3D(Math.cos(yaw),0.,Math.sin(yaw)));
		heading = hRot.applyTo(heading);
		*/
		topAccumulator = new Vector3D(0,1,0);
		/*
		Rotation tRot = new Rotation(//Pitch and roll
				new Vector3D(0,1,0),
				new Vector3D(0,1,0),
				new Vector3D(Math.sin(roll),1,Math.cos(roll)),
				new Vector3D(0.,Math.cos(tilt),Math.cos(tilt)));
		*/
		headingAccumulator=Vector3D.PLUS_K;
		rot = new Rotation(Vector3D.PLUS_I,tilt);
		headingAccumulator=rot.applyTo(headingAccumulator);
		topAccumulator=rot.applyTo(topAccumulator);
		rot = new Rotation(Vector3D.PLUS_J,yaw+1.5*Math.PI);
		headingAccumulator=rot.applyTo(headingAccumulator);
		topAccumulator=rot.applyTo(topAccumulator);
		//Commit the values
		heading=headingAccumulator;
		top=topAccumulator;
		}
	
	public ObjectDirection(Vector3D heading, Vector3D top){
		this.heading=heading;
		this.top=top;
		yaw=Double.POSITIVE_INFINITY;
		tilt=Double.POSITIVE_INFINITY;
		roll=Double.POSITIVE_INFINITY;
		}

	public Vector3D getHeading(){return heading;}
	public Vector3D getTop(){return top;}
	
	@Override
	public String toString(){return "ObjectDirection heading="+heading+" top="+top+" hash="+hashCode();}

	/**
	 * @return the yaw
	 */
	public double getYaw() {
	    return yaw;
	}

	/**
	 * @return the roll
	 */
	public double getRoll() {
	    return roll;
	}

	/**
	 * @return the tilt
	 */
	public double getTilt() {
	    return tilt;
	}
	}//end ObjectDirection
