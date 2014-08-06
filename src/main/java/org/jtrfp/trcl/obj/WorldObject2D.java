/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;


/**
 * A WorldObject which exists flat on the screen, immune to camera position or perspective effects.
 * Typically used for GUIs, messages, and HUDs.
 * @author Chuck Ritola
 *
 */
public class WorldObject2D extends WorldObject{
    
	public WorldObject2D(TR tr){
	    super(tr);
	    setTop(Vector3D.PLUS_J);
	    setHeading(Vector3D.PLUS_K);
	    //Setup matrices
	    rMd[15]=1;

	    tMd[0]=1;
	    tMd[5]=1;
	    tMd[10]=1;
	    tMd[15]=1;
	    
	    setRenderFlags((byte)1);
	    }
	public WorldObject2D(TR tr, Model m){
		super(tr, m);
		setRenderFlags((byte)1);
		}//end WorldObject2D
	@Override
	protected void recalculateTransRotMBuffer(){
		final double [] tV = position;
		
		Vect3D.normalize(getHeadingArray(), aZ);
		Vect3D.cross(getTopArray(),aZ,aX);
		Vect3D.cross(aZ,aX,aY);
		
		rMd[0]=aX[0];
		rMd[1]=aY[0];
		rMd[2]=aZ[0];
		
		rMd[4]=aX[1];
		rMd[5]=aY[1];
		rMd[6]=aZ[1];
		
		rMd[8]=aX[2];
		rMd[9]=aY[2];
		rMd[10]=aZ[2];
		
		tMd[3]=tV[0];
		tMd[7]=tV[1];
		tMd[11]=tV[2];
		Mat4x4.mul(tMd, rMd, rotTransM);
		
		getTr().matrixWindow.get().setTransposed(rotTransM,matrixID, scratchMatrixArray);
		}//end recalculateTransRotMBuffer()
	
	@Override
	public void setModel(Model m){
	    super.setModel(m);
	}
}//end WorldObject2D
