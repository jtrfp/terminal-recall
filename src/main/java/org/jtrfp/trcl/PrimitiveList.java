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
package org.jtrfp.trcl;

import java.util.Arrays;
import java.util.concurrent.Future;

import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.mem.MemoryWindow;

public abstract class PrimitiveList<PRIMITIVE_TYPE> {
    
    protected static final int MAX_PACKED_COORD_VALUE = 1024;
    //protected static final double coordDownScaler = 512;
    //protected static final double uvUpScaler = 4096;
    private final PRIMITIVE_TYPE[][] primitives;
    private final GL33Model model;
    private int cachedNumElements=-1;
    private boolean primitivesFinalized=false;

    public static enum RenderStyle {
	OPAQUE, TRANSPARENT
    };
    
    protected final 	String 		debugName;
    protected 		double 		scale;
    protected 		int 		packedScale;
    protected final 	TR 		tr;
    protected final 	MemoryWindow	window;

    public PrimitiveList(String debugName, PRIMITIVE_TYPE[][] primitives,
	    MemoryWindow window, TR tr, GL33Model model) {
	this.tr = tr;
	this.window=window;
	this.debugName = debugName;
	this.primitives = primitives;
	this.model = model;
	calculateDims();
	setScale((getMaximumVertexValue() / (double)MAX_PACKED_COORD_VALUE));
	for(int i=0; i<primitives.length; i++)
	    if(primitives[i]==null) throw new NullPointerException("Frame intolerably null at index "+i+".");
    }

    protected int packScale(double scaleToPack) {//Base-2 log
	int result = (int) Math.round(Math.log(scaleToPack) / Math.log(2)) 
		+ 16;
	assert result >= 0:"result="+result+" scaleToPack="+scaleToPack+" debugName="+debugName+" maxVertexValue="+getMaximumVertexValue();
	return result;
    }

    protected double applyScale(double value) {
	return value / Math.pow(2, packedScale - 16);
    }
    
    protected abstract void calculateDims();

    protected abstract double getMaximumVertexValue();

    public PRIMITIVE_TYPE[][] getPrimitives() {
	return primitives;
    }

    public int getNumElements() {
	PRIMITIVE_TYPE [] prims = primitives[0];
	if(prims==null)
	    return cachedNumElements;
	return prims.length;
    }

    public abstract int getElementSizeInVec4s();

    public abstract int getGPUVerticesPerElement();

    public abstract RenderStyle getRenderStyle();

    public int getTotalSizeInVec4s() {
	return getElementSizeInVec4s() * getNumElements();
    }

    public int getTotalSizeInGPUVertices() {
	return (getTotalSizeInVec4s() / getElementSizeInVec4s())
		* getGPUVerticesPerElement();
    }

    public String getDebugName() {
	return debugName;
    }
    public abstract Future<Void> uploadToGPU();
    /**
     * @param scale
     *            the scale to set
     */
    public final void setScale(double scale) {
	this.scale = scale;
	packedScale = packScale(scale);
    }

    public final double getScale() {
	return scale;
    }

    public final int getPackedScale() {
	return packedScale;
    }
    
    public final MemoryWindow getMemoryWindow(){
	return window;
    }

    /**
     * @return the primitiveRenderMode
     */
    public abstract byte getPrimitiveRenderMode();

    public abstract int getNumMemoryWindowIndicesPerElement();

    /**
     * @return the model
     */
    protected GL33Model getModel() {
        return model;
    }
    
    protected void finalizePrimitives(){//TODO: Factor this out if switching completely to MemoryWindow from Triangle[][]
	cachedNumElements = getNumElements();
	primitivesFinalized=true;
	Arrays.fill(primitives, null);
    }

    /**
     * @return the primitivesFinalized
     */
    protected boolean isPrimitivesFinalized() {
        return primitivesFinalized;
    }
}// end PrimitiveList
