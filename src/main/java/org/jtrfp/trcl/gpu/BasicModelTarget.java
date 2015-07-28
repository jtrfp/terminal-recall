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

import org.jtrfp.trcl.core.TextureDescription;

public interface BasicModelTarget {
    /**
     * 
     * @param target  { X Y Z U V nX nY nZ } values will be copied.
     * @return The index of the set vertex
     * @since Jul 27, 2015
     */
    public void addVertex(double [] target);
    
    public void addPrimitive(WriterState data);
    
    public static class WriterState{
	private final int []       vertices = new int[3];
	private TextureDescription texture;
	private PrimitiveType      primitiveType;
	private int                vertexIndex, primitiveIndex;

	/**
	 * @return the texture
	 */
	public TextureDescription getTexture() {
	    return texture;
	}

	/**
	 * @param texture the texture to set
	 */
	public void setTexture(TextureDescription texture) {
	    this.texture = texture;
	}

	/**
	 * @return the vertices
	 */
	public int[] getVertices() {
	    return vertices;
	}

	/**
	 * @return the primitiveType
	 */
	public PrimitiveType getPrimitiveType() {
	    return primitiveType;
	}

	/**
	 * @param primitiveType the primitiveType to set
	 */
	public void setPrimitiveType(PrimitiveType primitiveType) {
	    this.primitiveType = primitiveType;
	}

	/**
	 * @return the vertexIndex
	 */
	public int getVertexIndex() {
	    return vertexIndex;
	}

	/**
	 * @param vertexIndex the vertexIndex to set
	 */
	public void setVertexIndex(int vertexIndex) {
	    this.vertexIndex = vertexIndex;
	}

	/**
	 * @return the primitiveIndex
	 */
	public int getPrimitiveIndex() {
	    return primitiveIndex;
	}

	/**
	 * @param primitiveIndex the primitiveIndex to set
	 */
	public void setPrimitiveIndex(int primitiveIndex) {
	    this.primitiveIndex = primitiveIndex;
	}
    }//end TriangleData
    
    public interface PrimitiveType{}
    public interface TriangleType extends PrimitiveType{}
    public static final TriangleType SOLID_TRIANGLE = new TriangleType(){};
}//end IModel
