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

import java.util.ArrayList;
import java.util.BitSet;

import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;
import org.apache.commons.collections.primitives.IntList;
import org.jtrfp.trcl.core.TriangleVertexWindow;
import org.jtrfp.trcl.gpu.VQTexture;

public class GL33TriangleModifierContextSupport {
    private TriangleVertexWindow triangleVertexWindow;
    private double maxX, maxY, maxZ;
    private static final int STRIDE = 8;
    private final ArrayDoubleList      xyzuvNxNyNz  = new ArrayDoubleList();
    private final ArrayList<VQTexture> textureIDs = new ArrayList<VQTexture>();
    private final BitSet texturesAndUVsToUpdate = new BitSet(1024);
    private final BitSet XYZToUpdate            = new BitSet(1024);
    private Double maxScalar                    = null;

    TriangleVertexWindow getTriangleVertexWindow() {
	return triangleVertexWindow;
    }

    void setTriangleVertexWindow(TriangleVertexWindow triangleVertexWindow) {
	this.triangleVertexWindow = triangleVertexWindow;
    }
    
    public void flushModifierContext(
	    DoubleList vertexModifications, 
	    DoubleList normalModifications, 
	    DoubleList uvModifications,
	    IntList    textureModificationsIndices,
	    ArrayList<VQTexture> textureModificationsTextures,
	    double maxX, double maxY, double maxZ,
	    int maxVertexID){
	
	final boolean scaleChanged;
	final TriangleVertexWindow vw        = getTriangleVertexWindow();
	final BitSet texturesAndUVsToUpdate  = getTexturesAndUVsToUpdate();
	final BitSet XYZToUpdate             = getXYZToUpdate();
	final ArrayDoubleList xyzuvNxNyNz    = getXyzuvNxNyNz();
	final ArrayList<VQTexture> textureIDs=getTextureIDs();
	
	texturesAndUVsToUpdate.clear();
	XYZToUpdate           .clear();
	
	if(maxX > this.maxX || maxY > this.maxY || maxZ > this.maxZ){
	    this.maxX = Math.max(maxX, this.maxX);
	    this.maxY = Math.max(maxY, this.maxY);
	    this.maxZ = Math.max(maxZ, this.maxZ);
	    scaleChanged = true;
	}//end if (max changed)
	else
	    scaleChanged = false;
	
	//Resize buffer
	final int oldBufferVertexCount = xyzuvNxNyNz.size() / STRIDE;
	final int newBufferVertexCount = maxVertexID;
	final int bufferVerticesToCreate = newBufferVertexCount - oldBufferVertexCount;
	final int bufferComponentsToCreate = Math.max(0, bufferVerticesToCreate * STRIDE);
	for(int i = 0; i < bufferComponentsToCreate; i++){
	    xyzuvNxNyNz.add(0);
	    textureIDs .add(null);
	}

	//Grow out the memory window
	final int oldNumVertices         = vw.getNumObjects();
	final int numNewVerticesToCreate = maxVertexID - oldNumVertices;
	//TODO: should be createConsecutive
	vw.create(new ArrayList<Integer>(), numNewVerticesToCreate);

	//Apply to buffer
	//Vertex Modifications
	{
	    final int numVertexModifications = vertexModifications.size() / 4; // id, x, y, z
	    for(int vertexModIndex = 0; vertexModIndex < numVertexModifications; vertexModIndex++){
		final int idx = vertexModIndex * 4;
		final int id  = (int)vertexModifications.get(idx);
		final double x  = vertexModifications.get(idx+1);
		final double y  = vertexModifications.get(idx+2);
		final double z  = vertexModifications.get(idx+3);

		final int xyzuvNxNyNzIdx = id * STRIDE; 
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx,   x);
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+1, y);
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+2, z);
		
		if(!scaleChanged)
		    XYZToUpdate.set(id);
	    }//end for(vertexModIndex)
	}
	//UV Modifications
	{
	    final int uvStride = 3;
	    final int numUVModifications = uvModifications.size() / uvStride; // id, u, v
	    for(int uvModIndex = 0; uvModIndex < numUVModifications; uvModIndex++){
		final int   idx = uvModIndex * uvStride;
		final int   id  = (int)uvModifications.get(idx);
		final double u  = uvModifications     .get(idx+1);
		final double v  = vertexModifications .get(idx+2);

		final int xyzuvNxNyNzIdx = id * STRIDE; 
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+3, u);
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+4, v);
		if(!scaleChanged)
		    texturesAndUVsToUpdate.set(id);
	    }//end for(uvModIndex)
	}
	//Normal Modifications
	{
	    final int normalStride = 4;
	    final int numNormalModifications = normalModifications.size() / normalStride; // id, nx, ny, nz
	    for(int normalModIndex = 0; normalModIndex < numNormalModifications; normalModIndex++){
		final int   idx = normalModIndex * normalStride;
		final int   id  = (int)normalModifications.get(idx);
		final double nx  = uvModifications     .get(idx+1);
		final double ny  = vertexModifications .get(idx+2);
		final double nz  = vertexModifications .get(idx+3);

		final int xyzuvNxNyNzIdx = id * STRIDE;
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+5, nx);
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+6, ny);
		xyzuvNxNyNz.set(xyzuvNxNyNzIdx+7, nz);
		
		vw.normX.set(id, (byte)(nx * 127));
		vw.normY.set(id, (byte)(ny * 127));
		vw.normZ.set(id, (byte)(nz * 127));
	    }//end for(uvModIndex)
	}
	//Texture Modifications
	{
	    final int textureStride = 1;
	    final int numTextureModifications = textureModificationsIndices.size() / textureStride; // id
	    for(int textureModIndex = 0; textureModIndex < numTextureModifications; textureModIndex++){
		final int   idx  = textureModIndex * textureStride;
		final int   id   = (int)  textureModificationsIndices .get(idx);
		final VQTexture texture = textureModificationsTextures.get(id);
		textureIDs.set(id, texture);
		
		if(!scaleChanged)
		    texturesAndUVsToUpdate.set(id);
	    }//end for(textures)
	}
	//Textures/UV
	int startIndex = 0;
	int endIndex   = 0;
	while( (startIndex = texturesAndUVsToUpdate.nextSetBit(endIndex)) != -1) {
	    endIndex     = texturesAndUVsToUpdate.nextClearBit(startIndex);
	    updateTextureRange(startIndex, endIndex);
	}//end while(rangesToFlush)
	
	//XYZ
	startIndex = 0;
	endIndex   = 0;
	while( (startIndex = XYZToUpdate.nextSetBit(endIndex)) != -1) {
	    endIndex     = XYZToUpdate.nextClearBit(startIndex);
	    updateXYZRange(startIndex, endIndex);
	}//end while(rangesToFlush)
	
	//If the scale has changed, refresh everything.
	if(scaleChanged)
	    updateVertexRange(0,maxVertexID);
    }//end flushModifierContext(...)
    
    protected void updateTextureRange(final int start, final int end){
	final TriangleVertexWindow vw = getTriangleVertexWindow();
	//final double scalar = 1; //TODO
	int bufferIndex = start * STRIDE;
	final ArrayList<VQTexture> textureIDs=getTextureIDs();
	final ArrayDoubleList xyzuvNxNyNz    = getXyzuvNxNyNz();
	for(int vIndex = start; vIndex < end; vIndex++){
	    final VQTexture texture = textureIDs.get(vIndex / 3);
	    /*
	    vw.x.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.y.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.z.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    */
	    final double sideScalar = texture.getSideLength() - 1;
	    vw.u.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    vw.v.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    /*
	    vw.normX.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normY.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normZ.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    */
	    final int textureID = texture.getTexturePage();
	    vw.textureIDLo .set(vIndex, (byte)( textureID & 0xFF));
	    vw.textureIDMid.set(vIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi .set(vIndex, (byte)((textureID >> 16) & 0xFF));
	}//end for(vIndex)
    }//end updateVertexRange(...)
    
    protected void updateXYZRange(final int start, final int end){
	final TriangleVertexWindow vw = getTriangleVertexWindow();
	final double scalar = 1; //TODO
	int bufferIndex = start * STRIDE;
	final ArrayDoubleList xyzuvNxNyNz    = getXyzuvNxNyNz();
	for(int vIndex = start; vIndex < end; vIndex++){
	    //final VQTexture texture = textures.get(vIndex / 3);
	    
	    vw.x.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.y.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.z.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    /*
	    final double sideScalar = texture.getSideLength() - 1;
	    vw.u.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    vw.v.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    
	    vw.normX.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normY.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normZ.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    
	    final int textureID = texture.getTexturePage();
	    vw.textureIDLo .set(vIndex, (byte)( textureID & 0xFF));
	    vw.textureIDMid.set(vIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi .set(vIndex, (byte)((textureID >> 16) & 0xFF));
	    */
	}//end for(vIndex)
    }//end updateVertexRange(...)
    
    protected void updateVertexRange(final int start, final int end){
	final TriangleVertexWindow vw = getTriangleVertexWindow();
	final double scalar = 1; //TODO
	int bufferIndex = start * STRIDE;
	final ArrayDoubleList xyzuvNxNyNz    = getXyzuvNxNyNz();
	for(int vIndex = start; vIndex < end; vIndex++){
	    final VQTexture texture = textureIDs.get(vIndex / 3);
	    
	    vw.x.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.y.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    vw.z.set(vIndex, (short)(xyzuvNxNyNz.get(bufferIndex++) * scalar));
	    
	    final double sideScalar = texture.getSideLength() - 1;
	    vw.u.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    vw.v.set(vIndex, (short)(sideScalar * xyzuvNxNyNz.get(bufferIndex++)));
	    
	    vw.normX.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normY.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    vw.normZ.set(vIndex, (byte)(xyzuvNxNyNz.get(bufferIndex++) * 127));
	    
	    final int textureID = texture.getTexturePage();
	    vw.textureIDLo .set(vIndex, (byte)( textureID & 0xFF));
	    vw.textureIDMid.set(vIndex, (byte)((textureID >> 8) & 0xFF));
	    vw.textureIDHi .set(vIndex, (byte)((textureID >> 16) & 0xFF));
	}//end for(vIndex)
    }//end updateVertexRange(...)
    
    protected void addVertex(double x, double y, double z, 
	    double u, double v, 
	    double nx, double ny, double nz, VQTexture texture){
	final ArrayDoubleList xyzuvNxNyNz    = getXyzuvNxNyNz();
	xyzuvNxNyNz .add(x);
	xyzuvNxNyNz .add(y);
	xyzuvNxNyNz .add(z);
	xyzuvNxNyNz .add(u);
	xyzuvNxNyNz .add(v);
	xyzuvNxNyNz .add(nx);
	xyzuvNxNyNz .add(ny);
	xyzuvNxNyNz .add(nz);
	if((xyzuvNxNyNz.size() / STRIDE) % 3 == 0)
	 textureIDs   .add(texture);
    }

    protected BitSet getTexturesAndUVsToUpdate() {
        return texturesAndUVsToUpdate;
    }

    protected BitSet getXYZToUpdate() {
        return XYZToUpdate;
    }

    protected ArrayDoubleList getXyzuvNxNyNz() {
        return xyzuvNxNyNz;
    }

    protected ArrayList<VQTexture> getTextureIDs() {
        return textureIDs;
    }

    protected Double getMaxScalar() {
	if(maxScalar == null){ 
	    double newMax = Math.max(getMaxX() , getMaxY());
	           newMax = Math.max(newMax    , getMaxZ());
	    maxScalar = newMax;
	    }
        return maxScalar;
    }
    
    protected void invalidateMaxScalar(){
	maxScalar = null;
    }

    protected double getMaxX() {
        return maxX;
    }

    protected void setMaxX(double maxX) {
	if(maxX != this.maxX)
	    invalidateMaxScalar();
        this.maxX = maxX;
    }

    protected double getMaxY() {
        return maxY;
    }

    protected void setMaxY(double maxY) {
	if(maxY != this.maxY)
	    invalidateMaxScalar();
        this.maxY = maxY;
    }

    protected double getMaxZ() {
        return maxZ;
    }

    protected void setMaxZ(double maxZ) {
	if(maxZ != this.maxZ)
	    invalidateMaxScalar();
        this.maxZ = maxZ;
    }
    
    protected static int packScale(double scaleToPack) {//Base-2 log
	int result = (int) Math.round(Math.log(scaleToPack) / Math.log(2)) 
		+ 16;
	assert result >= 0:"result="+result+" scaleToPack="+scaleToPack+" debugName=";
	return result;
    }
}//end TriangleModifierContextSupport
