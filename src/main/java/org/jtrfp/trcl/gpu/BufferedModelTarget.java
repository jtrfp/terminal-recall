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

import java.util.ArrayList;
import java.util.Arrays;

public class BufferedModelTarget implements BasicModelTarget, BasicModelSource {
    private final ArrayList<double[]>            vertices= new ArrayList<double[]>();
    private final ArrayList<int[]>              vertexIDs= new ArrayList<int[]>();
    private final ArrayList<PrimitiveType> primitiveTypes= new ArrayList<PrimitiveType>();
    private int vIndex=-1, pIndex=-1;

    @Override
    public void addVertex(double[] target) {
	vertices.add(Arrays.copyOf(target, target.length));
	vIndex++;
    }

    @Override
    public void addPrimitive(WriterState data) {
	final int [] verts = data.getVertices();
	vertexIDs.add(Arrays.copyOf(verts, verts.length));
	primitiveTypes.add(data.getPrimitiveType());
	pIndex++;
    }

    @Override
    public int getLastVertexIndex() {
	return vIndex;
    }

    @Override
    public int getLastPrimitiveIndex() {
	return pIndex;
    }
    
    @Override
    public double [] getVertex(int index){
	return vertices.get(index);
    }
    
    @Override
    public PrimitiveType getPrimitiveType(int index){
	return primitiveTypes.get(index);
    }

    @Override
    public int[] getPrimitiveVertexIDs(int index) {
	return vertexIDs.get(index);
    }

}//end BufferedModelTarget
