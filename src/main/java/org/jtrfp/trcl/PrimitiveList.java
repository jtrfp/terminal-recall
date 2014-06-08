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

import javax.media.opengl.GL3;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.mem.MemoryWindow;

public abstract class PrimitiveList<PRIMITIVE_TYPE> {
    /*private static final List<PrimitiveList<?>> allLists = Collections
	    .synchronizedList(new ArrayList<PrimitiveList<?>>());*/
    
    protected static final double coordDownScaler = 512;
    protected static final double uvUpScaler = 4096;
    private final PRIMITIVE_TYPE[][] primitives;
    private final Model model;

    public static enum RenderStyle {
	OPAQUE, TRANSPARENT
    };

    //public static final ArrayList<Tickable> animators = new ArrayList<Tickable>();
    
    protected final 	String 		debugName;
    protected 		double 		scale;
    protected 		int 		packedScale;
    protected final 	TR 		tr;
    protected final 	MemoryWindow	window;

    public PrimitiveList(String debugName, PRIMITIVE_TYPE[][] primitives,
	    MemoryWindow window, TR tr, Model model) {
	this.tr = tr;
	this.window=window;
	this.debugName = debugName;
	this.primitives = primitives;
	this.model = model;
	setScale((getMaximumVertexValue() / 2048.));
	//addList(this);
    }

    protected int packScale(double scaleToPack) {
	int result = (int) Math.round(Math.log(scaleToPack) / Math.log(2));// Base-2
									   // log
	return result + 16;
    }

    protected double applyScale(double value) {
	return value / Math.pow(2, packedScale - 16);
    }
/*
    protected static void addList(PrimitiveList l) {
	if (l == null) {
	    new Exception().printStackTrace();
	    System.exit(1);
	}
	allLists.add(l);
    }
*/
    /*protected static List<PrimitiveList<?>> getAllArrayLists() {
	return allLists;
    }*/

    protected abstract double getMaximumVertexValue();

    public PRIMITIVE_TYPE[][] getPrimitives() {
	return primitives;
    }

    public int getNumElements() {
	return primitives[0].length;
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
/*
    public static void uploadAllListsToGPU(GL3 gl) {
	for (PrimitiveList<?> l : allLists) {
	    l.uploadToGPU(gl);
	}
    }// end uploadAllListsToGPU
*/
    public abstract void uploadToGPU(GL3 gl);

    //public abstract byte getPrimitiveRenderMode();
/*
    public int []getPhysicalPages() {
	final int [] result = new int[window.numPages()];
	for(int i=0; i<result.length;i++){
	    result[i]=window.logicalPage2PhysicalPage(i);
	}
	return result;
    }//end getPhysicalAddressInBytes
*/
 /*   public static void tickAnimators() {
	for (Tickable ani : animators) {
	    ani.tick();
	}
    }// end tickAnimators()
*/
    /**
     * @param scale
     *            the scale to set
     */
    public final void setScale(double scale) {
	// scale=scale>=1?scale:1;
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
    protected Model getModel() {
        return model;
    }
}// end PrimitiveList
