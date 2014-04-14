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
package org.jtrfp.trcl.core;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

public class RenderList {
    public static final int 	NUM_SUBPASSES 		= 4;
    public static final int	NUM_BLOCKS_PER_SUBPASS 	= 1024 * 4;
    public static final int	NUM_BLOCKS_PER_PASS 	= NUM_BLOCKS_PER_SUBPASS
	    						* NUM_SUBPASSES;
    public static final int	NUM_RENDER_PASSES 	= 2;// Opaque + transparent
    private static final int	OPAQUE_PASS 		= 0;
    private static final int	BLEND_PASS 		= 1;

    private final 	TR 			tr;
    private final 	PositionedRenderable[] 	renderables = new PositionedRenderable[NUM_BLOCKS_PER_SUBPASS];
    private 		int[] 			hostRenderListPageTable;
    private 		int 			renderablesIndex = 0;
    private final 	int 			dummyBufferID;
    private 		int 			numOpaqueBlocks;
    private 		int 			numTransparentBlocks;
    private 		int 			modulusUintOffset;
    private 		int 			opaqueIndex = 0, blendIndex = 0;
    private final 	GLUniform 		renderListOffsetUniform,
    /*    */	    				renderListPageTable,
    /*    */					useTextureMap,
    /*	  */					cameraMatrixUniform,
    /*    */					rootBuffer;
    private final	GLFrameBuffer		intermediateFrameBuffer;
    private final	GLTexture		intermediateDepthTexture,
    /*    	*/				intermediateColorTexture,
    /*    	*/				intermediateNormTexture,
    /*		*/				intermediateTextureIDTexture;
    private final	ArrayList<WorldObject>	visibleWorldObjects = new ArrayList<WorldObject>();
    private final 	Submitter<PositionedRenderable> 
    						submitter = new Submitter<PositionedRenderable>() {
	@Override
	public void submit(PositionedRenderable item) {
	    if (item instanceof WorldObject) {
		final WorldObject wo = (WorldObject)item;
		if (!((WorldObject) item).isVisible()
			|| !((WorldObject) item).isActive()) {
		    return;
		}
		visibleWorldObjects.add(wo);
	    }//end if(WorldObject)
	    final ByteBuffer opOD = item.getOpaqueObjectDefinitionAddresses();
	    final ByteBuffer trOD = item
		    .getTransparentObjectDefinitionAddresses();
	    numOpaqueBlocks += opOD.capacity() / 4;
	    numTransparentBlocks += trOD.capacity() / 4;
	    renderables[renderablesIndex++] = item;
	    tr.getObjectListWindow().opaqueIDs.set(0, opaqueIndex, opOD);
	    opaqueIndex += opOD.capacity();
	    tr.getObjectListWindow().blendIDs.set(0, blendIndex, trOD);
	    blendIndex += trOD.capacity();
	}// end submit(...)

	@Override
	public void submit(Collection<PositionedRenderable> items) {
	    for (PositionedRenderable r : items) {
		submit(r);
	    }//end for(items)
	}//end submit(...)
    };

    public RenderList(GL3 gl, GLProgram primaryProgram,
	    GLProgram deferredProgram, GLFrameBuffer intermediateFrameBuffer, 
	    GLTexture intermediateColorTexture, GLTexture intermediateDepthTexture,
	    GLTexture intermediateNormTexture, GLTexture intermediateTextureIDTexture,
	    final TR tr) {
	// Build VAO
	IntBuffer ib = IntBuffer.allocate(1);
	final GPU gpu = tr.getGPU();
	this.tr = tr;
	this.intermediateColorTexture=intermediateColorTexture;
	this.intermediateDepthTexture=intermediateDepthTexture;
	this.intermediateFrameBuffer=intermediateFrameBuffer;
	this.intermediateNormTexture=intermediateNormTexture;
	this.intermediateTextureIDTexture=intermediateTextureIDTexture;
	gl.glGenBuffers(1, ib);
	ib.clear();
	dummyBufferID = ib.get();
	gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
	gl.glBufferData(GL3.GL_ARRAY_BUFFER, 1, null, GL3.GL_DYNAMIC_DRAW);
	gl.glEnableVertexAttribArray(0);
	gl.glVertexAttribPointer(0, 1, GL3.GL_BYTE, false, 0, 0);
	renderListOffsetUniform = primaryProgram.getUniform("renderListOffset");
	renderListPageTable = primaryProgram.getUniform("renderListPageTable");
	useTextureMap = primaryProgram.getUniform("useTextureMap");
	cameraMatrixUniform = primaryProgram.getUniform("cameraMatrix");
	
	rootBuffer = deferredProgram.getUniform("rootBuffer");
	
	hostRenderListPageTable = new int[ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS
		* RenderList.NUM_RENDER_PASSES
		/ PagedByteBuffer.PAGE_SIZE_BYTES];
	

	tr.getThreadManager().addRunnableWhenFirstStarted(new Runnable() {
	    @Override
	    public void run() {
		for (int i = 0; i < hostRenderListPageTable.length; i++) {
		    hostRenderListPageTable[i] = RenderList.this.tr
			    .getObjectListWindow().logicalPage2PhysicalPage(i);
		}// end for(hostRenderListPageTable.length)
		tr.getRenderer().getPrimaryProgram().use();
		renderListPageTable.setArrayui(hostRenderListPageTable);
		modulusUintOffset = (tr.getObjectListWindow()
			.getPhysicalAddressInBytes(0) % PagedByteBuffer.PAGE_SIZE_BYTES) / 4;
	    }
	});
    }// end constructor

    private static int frameCounter = 0;

    private void updateStatesToGPU() {
	for (int i = 0; i < renderablesIndex; i++) {
	    renderables[i].updateStateToGPU();
	}
    }//end updateStatesToGPU

    public void sendToGPU(GL3 gl) {
	frameCounter++;
	frameCounter %= 100;
	updateStatesToGPU();
    }//end sendToGPU
    
    public void render(GL3 gl) {
	// OPAQUE STAGE
	tr.getRenderer().getPrimaryProgram().use();
	cameraMatrixUniform.set4x4Matrix(tr.getRenderer().getCamera().getMatrixAsFlatArray(),true);
	useTextureMap.set((int)0);
	intermediateFrameBuffer.bindToDraw();
	gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, dummyBufferID);
	gl.glClear(GL3.GL_COLOR_BUFFER_BIT | GL3.GL_DEPTH_BUFFER_BIT);
	final int numOpaqueVertices = numOpaqueBlocks
		* (GPU.GPU_VERTICES_PER_BLOCK+1);
	final int numTransparentVertices = numTransparentBlocks
		* GPU.GPU_VERTICES_PER_BLOCK;
	// Turn on depth write, turn off transparency
	gl.glDisable(GL3.GL_BLEND);
	gl.glDepthFunc(GL3.GL_LESS);
	if(tr.getRenderer().isBackfaceCulling())gl.glEnable(GL3.GL_CULL_FACE);
	// renderModeUniform.set(OPAQUE_PASS);
	final int verticesPerSubPass = (NUM_BLOCKS_PER_SUBPASS * GPU.GPU_VERTICES_PER_BLOCK);
	final int numSubPasses = (numOpaqueVertices / verticesPerSubPass) + 1;
	int remainingVerts = numOpaqueVertices;

	if (frameCounter == 0) {
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numOpaqueBlocks",
		    "" + numOpaqueBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.numTransparentBlocks",
		    "" + numTransparentBlocks);
	    tr.getReporter().report(
		    "org.jtrfp.trcl.core.RenderList.approxNumSceneTriangles",
		    "" + ((numOpaqueBlocks+numTransparentBlocks)*GPU.GPU_VERTICES_PER_BLOCK)/3);
	}

	for (int sp = 0; sp < numSubPasses; sp++) {
	    final int numVerts = remainingVerts <= verticesPerSubPass ? remainingVerts
		    : verticesPerSubPass;
	    remainingVerts -= numVerts;
	    final int newOffset = modulusUintOffset + sp
		    * NUM_BLOCKS_PER_SUBPASS;// newOffset is in uints
	    renderListOffsetUniform.setui(newOffset);
	    gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVerts);
	}// end for(subpasses)
	
	// DEFERRED STAGE
	gl.glDepthMask(true);
	gl.glDepthFunc(GL3.GL_ALWAYS);
	if(tr.getRenderer().isBackfaceCulling())gl.glDisable(GL3.GL_CULL_FACE);
	final GLProgram deferredProgram = tr.getRenderer().getDeferredProgram();
	deferredProgram.use();
	gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);// Zero means
						    // "Draw to screen"
	
	GLTexture.specifyTextureUnit(gl, 1);
	intermediateColorTexture.bind(gl);
	GLTexture.specifyTextureUnit(gl, 2);
	intermediateDepthTexture.bind(gl);
	GLTexture.specifyTextureUnit(gl, 3);
	intermediateNormTexture.bind(gl);
	tr.getGPU().getMemoryManager().bindToUniform(4, deferredProgram,
		    rootBuffer);
	GLTexture.specifyTextureUnit(gl, 5);
	tr.getGPU().getTextureManager().getTextureTileManager().getRGBATexture().bind();
	GLTexture.specifyTextureUnit(gl, 6);
	intermediateTextureIDTexture.bind();
	//Execute the draw to a screen quad
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, 6);
	
	// TRANSPARENT
	// Turn off depth write, turn on transparency
	tr.getRenderer().getPrimaryProgram().use();
	useTextureMap.set((int)1);
	gl.glDepthMask(false);
	gl.glDepthFunc(GL3.GL_LESS);
	gl.glEnable(GL3.GL_BLEND);
	// ////////
	// gl.glDepthFunc(GL3.GL_ALWAYS);
	// ///////
	renderListOffsetUniform.setui(modulusUintOffset + NUM_BLOCKS_PER_PASS);
	// renderModeUniform.set(BLEND_PASS);
	gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numTransparentVertices);
	// ////////
	// gl.glDepthFunc(GL3.GL_LESS);
	// ////////
	gl.glDepthMask(true);
    }// end render()

    public Submitter<PositionedRenderable> getSubmitter() {
	return submitter;
    }

    public void reset() {
	renderablesIndex = 0;
	numOpaqueBlocks = 0;
	numTransparentBlocks = 0;
	blendIndex = 0;
	opaqueIndex = 0;
	visibleWorldObjects.clear();
    }//end reset()
    
    public List<WorldObject> getVisibleWorldObjectList(){
	return visibleWorldObjects;
    }
}// end RenderList
