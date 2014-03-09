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
import java.util.Collection;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.GPUTriangleVertex;
import org.jtrfp.trcl.ObjectListWindow;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.gpu.GLFrameBuffer;
import org.jtrfp.trcl.gpu.GLProgram;
import org.jtrfp.trcl.gpu.GLRenderBuffer;
import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GLUniform;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.mem.PagedByteBuffer;
import org.jtrfp.trcl.obj.PositionedRenderable;
import org.jtrfp.trcl.obj.WorldObject;

public class RenderList{
	public static final int NUM_SUBPASSES=4;
	public static final int NUM_BLOCKS_PER_SUBPASS=1024*4;
	public static final int NUM_BLOCKS_PER_PASS=NUM_BLOCKS_PER_SUBPASS*NUM_SUBPASSES;
	public static final int NUM_RENDER_PASSES=2;// Opaque + transparent
	
	private static final int OPAQUE_PASS=0;
	private static final int BLEND_PASS=1;
	
	private final PositionedRenderable [] renderables = new PositionedRenderable[NUM_BLOCKS_PER_SUBPASS];
	private int renderablesIndex=0;
	private final TR tr;
	
	private final int dummyBufferID;
	private int numOpaqueBlocks;
	private int numTransparentBlocks;
	private final GLUniform renderListOffsetUniform,renderModeUniform,renderListPageTable;
	private int [] hostRenderListPageTable;
	private int modulusUintOffset;
	private GLTexture intermediateColorTexture;
	//private final int intermediateFrameBuffer;
	private GLFrameBuffer intermediateFrameBuffer;
	private GLRenderBuffer intermediateDepthRenderBuffer;
	
	private int opaqueIndex=0,blendIndex=0;
	private final Submitter<PositionedRenderable> submitter = new Submitter<PositionedRenderable>(){
	    	@Override
		public void submit(PositionedRenderable item)
			{if(item instanceof WorldObject){if(!((WorldObject)item).isVisible()||!((WorldObject)item).isActive()){return;}}
			final ByteBuffer opOD=item.getOpaqueObjectDefinitionAddresses();
			final ByteBuffer trOD=item.getTransparentObjectDefinitionAddresses();
			numOpaqueBlocks+=opOD.capacity()/4;
			numTransparentBlocks+=trOD.capacity()/4;
			renderables[renderablesIndex++]=item;
			tr.getObjectListWindow().opaqueIDs.set(0, opaqueIndex, opOD);
			opaqueIndex+=opOD.capacity();
			tr.getObjectListWindow().blendIDs.set(0, blendIndex, trOD);
			blendIndex+=trOD.capacity();
			}//end submit(...)

		@Override
		public void submit(Collection<PositionedRenderable> items)
			{for(PositionedRenderable r:items){submit(r);}}
		};
	
	public RenderList(GL3 gl, GLProgram prg, TR tr){
	    	//Build VAO
		IntBuffer ib = IntBuffer.allocate(1);
		final GPU gpu = tr.getGPU();
		this.tr=tr;
		gl.glGenBuffers(1, ib);
		ib.clear();
		dummyBufferID=ib.get();
		gl.glBindBuffer(GL3.GL_ARRAY_BUFFER,dummyBufferID);
		gl.glBufferData(GL3.GL_ARRAY_BUFFER, 1, null, GL3.GL_DYNAMIC_DRAW);
		gl.glEnableVertexAttribArray(0);
		gl.glVertexAttribPointer(0, 1, GL3.GL_BYTE, false, 0, 0 );
		renderListOffsetUniform=prg.getUniform("renderListOffset");
		renderModeUniform=prg.getUniform("renderFlags");
		renderListPageTable=prg.getUniform("renderListPageTable");
		hostRenderListPageTable=new int[ObjectListWindow.OBJECT_LIST_SIZE_BYTES_PER_PASS*RenderList.NUM_RENDER_PASSES/PagedByteBuffer.PAGE_SIZE_BYTES];
		
		intermediateColorTexture=gpu.
			newTexture().
			bind().
			setImage(GL3.GL_RGB, 1024, 768, GL3.GL_RGB, GL3.GL_UNSIGNED_BYTE,null).
			setMagFilter(GL3.GL_NEAREST).
			setMinFilter(GL3.GL_NEAREST);
		intermediateDepthRenderBuffer=gpu.
			newRenderBuffer().
			bind().
			setStorage(GL3.GL_DEPTH_COMPONENT, 1024, 768);
		
		intermediateFrameBuffer=gpu.
			newFrameBuffer().
			bindToDraw().
			attachDrawTexture(intermediateColorTexture,GL3.GL_COLOR_ATTACHMENT0).
			attachDepthRenderBuffer(intermediateDepthRenderBuffer);
		
		tr.getThreadManager().addRunnableWhenFirstStarted(new Runnable(){
		    @Override
		    public void run() {
			System.out.println("hostRenderListPageTable length="+hostRenderListPageTable.length);
			for(int i=0; i<hostRenderListPageTable.length;i++){
			    hostRenderListPageTable[i]=RenderList.this.tr.getObjectListWindow().logicalPage2PhysicalPage(i);
			}//end for(hostRenderListPageTable.length)
			renderListPageTable.setArrayui(hostRenderListPageTable);
			modulusUintOffset = (RenderList.this.tr.getObjectListWindow().getPhysicalAddressInBytes(0)%PagedByteBuffer.PAGE_SIZE_BYTES)/4;
		    }});
	    }
	private static int frameCounter=0;
	
	private void updateStatesToGPU(){
	    	for(int i=0; i<renderablesIndex; i++)
			{renderables[i].updateStateToGPU();}}
	
	public void sendToGPU(GL3 gl)
		{frameCounter++; frameCounter%=100;updateStatesToGPU();}
	
	public void render(GL3 gl){
	    	gl.glBindFramebuffer(GL3.GL_FRAMEBUFFER, 0);
	    	gl.glClear(GL3.GL_COLOR_BUFFER_BIT);
		final int numOpaqueVertices = numOpaqueBlocks*GPUTriangleVertex.VERTICES_PER_BLOCK+96;
		final int numTransparentVertices = numTransparentBlocks*GPUTriangleVertex.VERTICES_PER_BLOCK;
		//OPAQUE
		//Turn on depth write, turn off transparency
		gl.glDisable(GL3.GL_BLEND);
		renderModeUniform.set(OPAQUE_PASS);
		final int verticesPerSubPass=(NUM_BLOCKS_PER_SUBPASS*GPUTriangleVertex.VERTICES_PER_BLOCK);
		final int numSubPasses=(numOpaqueVertices/verticesPerSubPass)+1;
		int remainingVerts=numOpaqueVertices;
		
		if(frameCounter==0){
		    tr.getReporter().report("org.jtrfp.trcl.core.RenderList.numOpaqueBlocks", ""+numOpaqueBlocks);
		    tr.getReporter().report("org.jtrfp.trcl.core.RenderList.numTransparentBlocks", ""+numTransparentBlocks);
		    }
		
		for(int sp=0; sp<numSubPasses; sp++){
		    	final int numVerts=remainingVerts<=verticesPerSubPass?remainingVerts:verticesPerSubPass;
			remainingVerts-=numVerts;
			final int newOffset=modulusUintOffset+sp*NUM_BLOCKS_PER_SUBPASS;// newOffset is in uints
			renderListOffsetUniform.setui(newOffset);
			gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numVerts);
			}//end for(subpasses)
		//TRANSPARENT
		//Turn off depth write, turn on transparency
		gl.glEnable(GL3.GL_BLEND);
		gl.glDepthMask(false);
		//////////
		//gl.glDepthFunc(GL3.GL_ALWAYS);
		/////////
		renderListOffsetUniform.setui(modulusUintOffset+NUM_BLOCKS_PER_PASS);
		renderModeUniform.set(BLEND_PASS);
		gl.glDrawArrays(GL3.GL_TRIANGLES, 0, numTransparentVertices);
		//////////
		//gl.glDepthFunc(GL3.GL_LESS);
		//////////
		
		gl.glDepthMask(true);
		
		//DEFERRED STAGE
		//Set output to screen
		//TODO:
		
		}//end render()
	public Submitter<PositionedRenderable> getSubmitter()
		{return submitter;}

	public void reset(){
	    	renderablesIndex=0;
		numOpaqueBlocks=0;
		numTransparentBlocks=0;
		blendIndex=0;
		opaqueIndex=0;
		}
	}//end RenderList
