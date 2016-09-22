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


package org.jtrfp.trcl.gpu;

import java.util.ArrayList;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.IntList;

public abstract class DefaultTriangleModifierContext implements TriangleModifierContext,
	VertexNormalModifierContext, TexturedModifierContext, UVModifierContext {
    private static final int INITIAL_SIZE = 128;
    /**
     * Order: id, x, y, z
     */
    DoubleList         vertexModifications;
    DoubleList         normalModifications; //id, nX,nY,nZ
    DoubleList         uvModifications; // id, u, v
    //Saves from creating/GC'ing many holder objects
    IntList            textureModificationsIDs;
    IntList            uvModificationIDs;
    ArrayList<Texture> textureModificationsTextures;
    
    @Override
    public void setUV(int vertexID, double u, double v){
	uvModificationIDs.add(vertexID);
	uvModifications.add(u);
	uvModifications.add(v);
    }

    @Override
    public void setVertex(int id, double x, double y, double z) {
	vertexModifications.add(Math.rint(id));
	vertexModifications.add(x);
	vertexModifications.add(y);
	vertexModifications.add(z);
    }

    @Override
    public void setTexture(int primitiveID, Texture newTexture) {
	textureModificationsIDs     .add(primitiveID);
	textureModificationsTextures.add(newTexture);	
	
    }

    @Override
    public void setNormal(int id, double x, double y, double z) {
	normalModifications.add(Math.rint(id));
	normalModifications.add(x);
	normalModifications.add(y);
	normalModifications.add(z);
    }

    public DoubleList getVertexModifications() {
	if(vertexModifications == null)
	    setVertexModifications(new ArrayDoubleList(INITIAL_SIZE));
        return vertexModifications;
    }

    public void setVertexModifications(DoubleList vertexModifications) {
        this.vertexModifications = vertexModifications;
    }

    public DoubleList getNormalModifications() {
	if(normalModifications == null)
	    setNormalModifications(new ArrayDoubleList(INITIAL_SIZE));
        return normalModifications;
    }

    public void setNormalModifications(DoubleList normalModifications) {
        this.normalModifications = normalModifications;
    }

    public IntList getTextureModificationsIDs() {
	if(textureModificationsIDs == null)
	    setTextureModificationsIDs(new ArrayIntList(INITIAL_SIZE));
        return textureModificationsIDs;
    }

    public void setTextureModificationsIDs(IntList textureModificationsIDs) {
        this.textureModificationsIDs = textureModificationsIDs;
    }

    public ArrayList<Texture> getTextureModificationsTextures() {
	if(textureModificationsTextures == null)
	    setTextureModificationsTextures(new ArrayList<Texture>(INITIAL_SIZE));
        return textureModificationsTextures;
    }

    public void setTextureModificationsTextures(
    	ArrayList<Texture> textureModificationsTextures) {
        this.textureModificationsTextures = textureModificationsTextures;
    }

}//end DefaultTriangleModifierKit
