/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.gpu;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.coll.Decorator;
import org.jtrfp.trcl.gpu.BasicModelTarget.PrimitiveType;
import org.jtrfp.trcl.obj.Rotatable;

public final class RotatedModelSource implements BasicModelSource, Decorator<BasicModelSource>{
    private BasicModelSource delegate;
    private Rotatable        rotatableSource;
    public RotatedModelSource(BasicModelSource delegate){
	this.delegate=delegate;
    }
    @Override
    public double[] getVertex(int index) {
	final double [] result = new double[8];
	final double [] dR = delegate.getVertex(index);
	final Rotation rotation = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J,rotatableSource.getHeading(),rotatableSource.getTop());
	final Vector3D xyz = rotation.applyTo(new Vector3D(dR[0],dR[1],dR[2]));
	final Vector3D xyzN= rotation.applyTo(new Vector3D(dR[5],dR[6],dR[7]));
	result[3]=dR[3];result[4]=dR[4];//UV
	result[0]=xyz.getX(); result[1]=xyz.getY(); result[2]=xyz.getZ();
	result[5]=xyzN.getX();result[6]=xyzN.getY();result[7]=xyzN.getZ();
	return result;
    }

    @Override
    public int[] getPrimitiveVertexIDs(int index) {
	return delegate.getPrimitiveVertexIDs(index);
    }

    @Override
    public PrimitiveType getPrimitiveType(int index) {
	return delegate.getPrimitiveType(index);
    }
    @Override
    public BasicModelSource getDelegate() {
	return delegate;
    }
    /**
     * @return the rotatableSource
     */
    public Rotatable getRotatableSource() {
        return rotatableSource;
    }
    /**
     * @param rotatableSource the rotatableSource to set
     */
    public void setRotatableSource(Rotatable rotatableSource) {
        this.rotatableSource = rotatableSource;
    }

}//end RotatedModelSource
