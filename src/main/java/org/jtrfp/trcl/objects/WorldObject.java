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
package org.jtrfp.trcl.objects;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.GPUTriangleVertex;
import org.jtrfp.trcl.Matrix;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ObjectDefinition;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.ai.Behavior;
import org.jtrfp.trcl.ai.NullBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLTextureBuffer;

public class WorldObject implements PositionedRenderable
	{
	private Vector3D heading = new Vector3D(new double []{0,0,1}); //Facing direction
	private Vector3D top = new Vector3D(new double []{0,1,0});		//Normal describing the top of the object (for tilt)
	protected Vector3D position = Vector3D.ZERO;
	private final TR tr;
	//private World world;
	private boolean visible=true;
	private Model model;
	private ArrayList<PositionListener> positionListeners = new ArrayList<PositionListener>();
	private ObjectDefinition []triangleObjectDefinitions;
	private ObjectDefinition []lineSegmentObjectDefinitions;
	private ObjectDefinition []transparentTriangleObjectDefinitions;
	private ByteBuffer opaqueObjectDefinitionAddressesInVec4 = ByteBuffer.allocate(0);//defaults to empty
	private ByteBuffer transparentObjectDefinitionAddressesInVec4 = ByteBuffer.allocate(0);//defaults to empty
	protected final Matrix matrix;
	private static final List<WorldObject> allWorldObjects = Collections.synchronizedList(new ArrayList<WorldObject>());
	public static final int GPU_VERTICES_PER_BLOCK=96;
	public static final boolean LOOP = true;
	private SpacePartitioningGrid containingGrid;
	private Behavior behavior=new NullBehavior(this);
	
	public WorldObject(TR tr)
		{
		this.tr=tr;
		addWorldObject(this);
		matrix=Matrix.create4x4();
		}
	
	public WorldObject(TR tr,Model m)
		{this(tr);
		setModel(m);
		}//end constructor
	
	void proposeCollision(WorldObject other){if(behavior!=null)behavior.proposeCollision(other);}
	public void addBehavior(Behavior ob)
		{ob.setDelegate(behavior);
		ob.setParent(this);
		behavior=ob;
		}
	
	public void tick(long time)
		{getBehavior().tick(time);}
	
	private static final void addWorldObject(WorldObject o)
		{if(o==null){new Exception().printStackTrace();System.exit(1);}
		allWorldObjects.add(o);}
	
	public void setModel(Model m)
		{
		if(m==null)throw new RuntimeException("Passed model cannot be null.");
		model=m;
		int numObjDefs,sizeInVerts;
		if(m.getLineSegmentList()==null)lineSegmentObjectDefinitions=new ObjectDefinition[0];
		else
			{
			sizeInVerts=m.getLineSegmentList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			lineSegmentObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){lineSegmentObjectDefinitions[i]=ObjectDefinition.create();}
			}
		if(m.getTriangleList()==null)triangleObjectDefinitions=new ObjectDefinition[0];
		else
			{
			sizeInVerts=m.getTriangleList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			triangleObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){triangleObjectDefinitions[i]=ObjectDefinition.create();}
			}
		if(m.getTransparentTriangleList()==null)transparentTriangleObjectDefinitions=new ObjectDefinition[0];
		else
			{
			sizeInVerts=m.getTransparentTriangleList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			transparentTriangleObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){transparentTriangleObjectDefinitions[i]=ObjectDefinition.create();}
			}
		}//end setModel(...)
	
	public void setDirection(ObjectDirection dir)
		{
		heading=dir.getHeading();
		top=dir.getTop();
		}
	
	@Override
	public String toString()
		{return "WorldObject Model="+model.getDebugName()+" pos="+this.getPosition()+" class="+getClass().getName();}
	
	public final void initializeObjectDefinitions()
		{
		//if(world==null)throw new RuntimeException("World cannot be null. (did you forget to set it?)");
		ArrayList<Integer> opaqueIndicesList = new ArrayList<Integer>();
		ArrayList<Integer> transparentIndicesList = new ArrayList<Integer>();
		
		processPrimitiveList(model.getTriangleList(),triangleObjectDefinitions,opaqueIndicesList);
		processPrimitiveList(model.getLineSegmentList(),lineSegmentObjectDefinitions,transparentIndicesList);
		processPrimitiveList(model.getTransparentTriangleList(),transparentTriangleObjectDefinitions,transparentIndicesList);
		
		ByteOrder order=getTr().
				getGPU().
				getByteOrder();
		opaqueObjectDefinitionAddressesInVec4 = ByteBuffer.allocateDirect(opaqueIndicesList.size()*4).order(order);//4 bytes per int
		transparentObjectDefinitionAddressesInVec4 = ByteBuffer.allocateDirect(transparentIndicesList.size()*4).order(order);
		
		IntBuffer trans=transparentObjectDefinitionAddressesInVec4.asIntBuffer(),opaque=opaqueObjectDefinitionAddressesInVec4.asIntBuffer();
		
		for(Integer elm:transparentIndicesList)
			{trans.put(elm);}
		for(Integer elm:opaqueIndicesList)
			{opaque.put(elm);}
		}//end initializeObjectDefinitions()
	
	private void processPrimitiveList(PrimitiveList<?,?> primitiveList, ObjectDefinition [] objectDefinitions, ArrayList<Integer> indicesList)
		{
		if(primitiveList==null)return; //Nothing to do, no primitives here
		int vec4Counter = primitiveList.getTotalSizeInVec4s();
		int primitiveListByteAddress = primitiveList.getStartAddressInBytes();
		final int vec4sPerBlock = primitiveList.getPrimitiveSizeInVec4s()*(GPU_VERTICES_PER_BLOCK/primitiveList.getGPUVerticesPerPrimitive());
		final int verticesPerVec4 = (int)((double)primitiveList.getGPUVerticesPerPrimitive()/(double)primitiveList.getPrimitiveSizeInVec4s());
		//For each of the allocated-but-not-yet-initialized object definitions.
		for(final ObjectDefinition ob:objectDefinitions)
			{
			ob.matrixOffset.set(matrix.getAddressInBytes()/GLTextureBuffer.BYTES_PER_VEC4);
			ob.vertexOffset.set(primitiveListByteAddress/GLTextureBuffer.BYTES_PER_VEC4);
			ob.mode.set(primitiveList.getPrimitiveRenderMode());
			ob.modelScale.set(primitiveList.getPackedScale());
			if(vec4Counter>=vec4sPerBlock)
				{ob.numVertices.set((byte)GPUTriangleVertex.VERTICES_PER_BLOCK);}
			else if(vec4Counter>0)
				{ob.numVertices.set((byte)(vec4Counter*verticesPerVec4));}
			else{throw new RuntimeException("Ran out of vec4s.");}
			vec4Counter-=vec4sPerBlock;
			primitiveListByteAddress+=vec4sPerBlock*GLTextureBuffer.BYTES_PER_VEC4;
			indicesList.add(ob.getAddressInBytes()/GLTextureBuffer.BYTES_PER_VEC4);
			}//end for(ObjectDefinition)
		}//end processPrimitiveList(...)
	
	public final void updateStateToGPU()
		{recalculateTransRotMBuffer();}
	
	protected void recalculateTransRotMBuffer()
		{
		Vector3D tV=position;
		
		if(LOOP)
			{
			double delta = position.getX()-tr.getRenderer().getCamera().getCameraPosition().getX();
			if(delta>TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX()-TR.mapWidth,tV.getY(),tV.getZ());}
			else if(delta<-TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX()+TR.mapWidth,tV.getY(),tV.getZ());}
			delta = position.getY()-tr.getRenderer().getCamera().getCameraPosition().getY();
			if(delta>TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX(),tV.getY()-TR.mapWidth,tV.getZ());}
			else if(delta<-TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX(),tV.getY()+TR.mapWidth,tV.getZ());}
			delta = position.getZ()-tr.getRenderer().getCamera().getCameraPosition().getZ();
			if(delta>TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX(),tV.getY(),tV.getZ()-TR.mapWidth);}
			else if(delta<-TR.mapWidth/2.)
				{tV=new Vector3D(tV.getX(),tV.getY(),tV.getZ()+TR.mapWidth);}
			}
		
		Vector3D aZ=heading.normalize();
		Vector3D aX=top.crossProduct(aZ).normalize();
		Vector3D aY=aZ.crossProduct(aX);
		
		RealMatrix rM = new Array2DRowRealMatrix(new double [][] 
					{
					new double[]{aX.getX(),aY.getX(),	aZ.getX(),	0},
					new double[]{aX.getY(),aY.getY(),	aZ.getY(),	0},
					new double[]{aX.getZ(),aY.getZ(),	aZ.getZ(),	0},
					new double[]{0,		0,			0,			1}
					});
		
		RealMatrix tM = new Array2DRowRealMatrix(new double [][] 
					{
					new double[]{1,0,	0,	tV.getX()},
					new double[]{0,1,	0,	tV.getY()},
					new double[]{0,0,	1,	tV.getZ()},
					new double[]{0,0,	0,	1}
					});
		
		RealMatrix rotTransM;
		if(translate())		{rotTransM = tM.multiply(rM);}
		else 				{rotTransM = rM;}
		
		matrix.set(tr.getRenderer().getCamera().getMatrix().multiply(rotTransM).transpose());
		}//end recalculateTransRotMBuffer()
	
	protected boolean translate(){return true;}
	/**
	 * @return the visible
	 */
	public boolean isVisible()
		{
		return visible;
		}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible)
		{
		this.visible = visible;
		}
	/**
	 * @return the position
	 */
	public final Vector3D getPosition()
		{return position!=null?position:Vector3D.ZERO;}

	/**
	 * @param position the position to set
	 */
	public void setPosition(Vector3D position)
		{
		synchronized(position)
			{
			this.position = position;
			notifyPositionListeners();
			}
		}//end setPosition()
	
	/**
	 * @return the heading
	 */
	public final Vector3D getLookAt()
		{return heading;}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(Vector3D heading)
		{this.heading = heading;}
	public Vector3D getHeading(){return heading;}

	/**
	 * @return the top
	 */
	public final Vector3D getTop()
		{return top;}

	/**
	 * @param top the top to set
	 */
	public void setTop(Vector3D top)
		{this.top = top;}
	
	private void notifyPositionListeners()
		{for(PositionListener l:positionListeners){l.positionChanged(this);}}
	
	@Override
	public void addPositionListener(PositionListener listenerToAdd)
		{positionListeners.add(listenerToAdd);}

	@Override
	public void removePositionListener(
			PositionListener listenerToRemove)
		{positionListeners.remove(listenerToRemove);}
	
	public final ByteBuffer getOpaqueObjectDefinitionAddresses()
		{opaqueObjectDefinitionAddressesInVec4.clear();return opaqueObjectDefinitionAddressesInVec4;}
	public final ByteBuffer getTransparentObjectDefinitionAddresses()
		{transparentObjectDefinitionAddressesInVec4.clear();return transparentObjectDefinitionAddressesInVec4;}

	public static void uploadAllObjectDefinitionsToGPU()
		{
		for(WorldObject wo:allWorldObjects)
			{wo.initializeObjectDefinitions();}
		}//end uploadAllObjectDefinitionsToGPU()

	/**
	 * @return the tr
	 */
	public TR getTr()
		{return tr;}
	
	public void destroy()
		{tr.getCollisionManager().remove(this);
		containingGrid.remove(this);
		//this.setVisible(false);
		}
	
	@Override
	public void setContainingGrid(SpacePartitioningGrid grid)
		{containingGrid=grid;}
	public SpacePartitioningGrid getContainingGrid(){return containingGrid;}

	/**
	 * @return the behavior
	 */
	public Behavior getBehavior()
		{return behavior;}
	}//end WorldObject
