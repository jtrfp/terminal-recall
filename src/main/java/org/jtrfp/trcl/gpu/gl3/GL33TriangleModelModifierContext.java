/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gpu.gl3;

import org.jtrfp.trcl.gpu.DefaultTriangleModifierContext;
import org.jtrfp.trcl.gpu.Texture;


abstract class GL33TriangleModelModifierContext extends DefaultTriangleModifierContext {
    
    private double minX, minY, minZ, maxX, maxY, maxZ;
    private int maxVertexID;

    @Override
    public void flush() {
    }

    @Override
    public void setVertex(int id, double x, double y, double z) {
	minX = Math.min(x, minX);
	minY = Math.min(y, minY);
	minZ = Math.min(z, minZ);
	
	maxX = Math.max(x, maxX);
	maxY = Math.max(y, maxY);
	maxZ = Math.max(z, maxZ);
	
	maxVertexID = Math.max(maxVertexID, id);
	super.setVertex(id, x, y, z);
    }

    @Override
    public void setTexture(int primitiveID, Texture newTexture) {
	super.setTexture(primitiveID, newTexture);
    }

    @Override
    public void setNormal(int id, double x, double y, double z) {
	super.setNormal(id,x,y,z);
	maxVertexID = Math.max(maxVertexID, id);
    }

    double getMinX() {
        return minX;
    }

    void setMinX(double minX) {
        this.minX = minX;
    }

    double getMinY() {
        return minY;
    }

    void setMinY(double minY) {
        this.minY = minY;
    }

    double getMinZ() {
        return minZ;
    }

    void setMinZ(double minZ) {
        this.minZ = minZ;
    }

    double getMaxX() {
        return maxX;
    }

    void setMaxX(double maxX) {
        this.maxX = maxX;
    }

    double getMaxY() {
        return maxY;
    }

    void setMaxY(double maxY) {
        this.maxY = maxY;
    }

    double getMaxZ() {
        return maxZ;
    }

    void setMaxZ(double maxZ) {
        this.maxZ = maxZ;
    }

    int getMaxVertexID() {
        return maxVertexID;
    }

    void setMaxVertexID(int maxVertexID) {
        this.maxVertexID = maxVertexID;
    }

}//end GL33OpaqueTriangleModelModifierContext
