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

import org.jtrfp.trcl.coll.Decorator;
import org.jtrfp.trcl.gpu.BasicModelTarget.PrimitiveType;
import org.jtrfp.trcl.obj.Positionable;

public final class TranslatedModelSource implements BasicModelSource, Decorator<BasicModelSource> {
    private final BasicModelSource delegate;
    private Positionable           positionSource;
    
    public TranslatedModelSource(BasicModelSource delegate){
	this.delegate=delegate;
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getVertex(int)
     */
    public double[] getVertex(int index) {
	final double [] result  = new double[8];
	final double [] dR      = delegate.getVertex(index);
	final double [] position= positionSource.getPosition();
	result[0]=dR[0]+position[0]; result[1]=dR[1]+position[1]; result[2]=dR[2]+position[2];
	result[3]=dR[3]; result[4]=dR[4];//UV
	result[5]=dR[5]; result[6]=dR[6]; result[7]=dR[7];//nXYZ
	return result;
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getPrimitiveVertexIDs(int)
     */
    public int[] getPrimitiveVertexIDs(int index) {
	return delegate.getPrimitiveVertexIDs(index);
    }

    /**
     * @param index
     * @return
     * @see org.jtrfp.trcl.gpu.BasicModelSource#getPrimitiveType(int)
     */
    public PrimitiveType getPrimitiveType(int index) {
	return delegate.getPrimitiveType(index);
    }

    /**
     * @return the delegate
     */
    public BasicModelSource getDelegate() {
        return delegate;
    }

    /**
     * @return the positionSource
     */
    public Positionable getPositionSource() {
        return positionSource;
    }

    /**
     * @param positionSource the positionSource to set
     */
    public void setPositionSource(Positionable positionSource) {
        this.positionSource = positionSource;
    }

}//end TranslatedModelSource