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
     * @param index
     * @param target X,Y,Z,nX,nY,nZ
     * @since Jul 26, 2015
     */
    public void addVertex(int index, double [] target);
    
    public void addPrimitive(TriangleData data);
    
    public static class TriangleData{
	private final int []       vertices = new int[3];
	private TextureDescription texture;
	private PrimitiveType      primitiveType;

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
    }//end TriangleData
    
    public interface PrimitiveType{}
}//end IModel
