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
package org.jtrfp.trcl.obj;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.GPUTriangleVertex;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ObjectDefinition;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.NullBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;

public class WorldObject implements PositionedRenderable
	{
	private double []heading = new double []{0,0,1}; //Facing direction
	private double []top = new double []{0,1,0};		//Normal describing the top of the object (for tilt)
	//protected Vector3D position = Vector3D.ZERO;
	protected double [] position = new double[3];
	private final TR tr;
	private boolean visible=true;
	private Model model;
	private ArrayList<PositionListener> positionListeners = new ArrayList<PositionListener>();
	private ObjectDefinition []triangleObjectDefinitions;
	private ObjectDefinition []lineSegmentObjectDefinitions;
	private ObjectDefinition []transparentTriangleObjectDefinitions;
	private ByteBuffer opaqueObjectDefinitionAddressesInVec4 = ByteBuffer.allocate(0);//defaults to empty
	private ByteBuffer transparentObjectDefinitionAddressesInVec4 = ByteBuffer.allocate(0);//defaults to empty
	protected final int matrixID;
	private static final List<WorldObject> allWorldObjects = Collections.synchronizedList(new ArrayList<WorldObject>());
	public static final int GPU_VERTICES_PER_BLOCK=96;
	public static final boolean LOOP = true;
	private SpacePartitioningGrid containingGrid;
	private Behavior behavior=new NullBehavior(this);
	private boolean active=true;
	
	protected final double[] aX = new double[3];
	protected final double[] aY = new double[3];
	protected final double[] aZ = new double[3];
	protected final double[] rotTransM = new double[16];
	protected final double[] camM = new double[16];
	protected final double[] rMd = new double[16];
	protected final double[] tMd = new double[16];
	protected double [] cMd = new double[16];
	
	public WorldObject(TR tr){
		this.tr=tr;
		addWorldObject(this);
		matrixID=tr.getMatrixWindow().create4x4();
		//Matrix constants setup
		rMd[15]=1;
		
		tMd[0]=1;
		tMd[5]=1;
		tMd[10]=1;
		tMd[15]=1;
		}
	
	public WorldObject(TR tr,Model m)
		{this(tr);
		setModel(m);
		}//end constructor
	
	void proposeCollision(WorldObject other){if(behavior!=null)behavior.proposeCollision(other);}
	public <T extends Behavior>T addBehavior(T ob)
		{ob.setDelegate(behavior);
		ob.setParent(this);
		behavior=ob;
		return ob;
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
		else	{
			sizeInVerts=m.getLineSegmentList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			lineSegmentObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){lineSegmentObjectDefinitions[i]=ObjectDefinition.create();}
			}
		if(m.getTriangleList()==null)triangleObjectDefinitions=new ObjectDefinition[0];
		else	{
			sizeInVerts=m.getTriangleList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			triangleObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){triangleObjectDefinitions[i]=ObjectDefinition.create();}
			}
		if(m.getTransparentTriangleList()==null)transparentTriangleObjectDefinitions=new ObjectDefinition[0];
		else	{
			sizeInVerts=m.getTransparentTriangleList().getTotalSizeInGPUVertices();
			numObjDefs=sizeInVerts/GPU_VERTICES_PER_BLOCK;
			if(sizeInVerts%GPU_VERTICES_PER_BLOCK != 0)numObjDefs++;
			transparentTriangleObjectDefinitions=new ObjectDefinition[numObjDefs];
			for(int i=0; i<numObjDefs; i++){transparentTriangleObjectDefinitions[i]=ObjectDefinition.create();}
			}
		}//end setModel(...)
	
	public void setDirection(ObjectDirection dir){
	    	if(dir.getHeading().getNorm()==0||dir.getTop().getNorm()==0){
	    	    System.err.println("Warning: Rejecting zero-norm for object direction. "+dir);
	    	    new Exception().printStackTrace();
	    	    return;}
		setHeading(dir.getHeading());
		setTop(dir.getTop());
		}
	
	@Override
	public String toString()
		{return "WorldObject Model="+model.getDebugName()+" pos="+this.getPosition()+" class="+getClass().getName();}
	
	public final void initializeObjectDefinitions()
		{if(model==null)throw new NullPointerException("Model is null. Did you forget to set it?");
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
	
	private void processPrimitiveList(PrimitiveList<?,?> primitiveList, ObjectDefinition [] objectDefinitions, ArrayList<Integer> indicesList){
		if(primitiveList==null)return; //Nothing to do, no primitives here
		int vec4Counter = primitiveList.getTotalSizeInVec4s();
		int primitiveListByteAddress = primitiveList.getStartAddressInBytes();
		final int vec4sPerBlock = primitiveList.getPrimitiveSizeInVec4s()*(GPU_VERTICES_PER_BLOCK/primitiveList.getGPUVerticesPerPrimitive());
		final int verticesPerVec4 = (int)((double)primitiveList.getGPUVerticesPerPrimitive()/(double)primitiveList.getPrimitiveSizeInVec4s());
		//For each of the allocated-but-not-yet-initialized object definitions.
		for(final ObjectDefinition ob:objectDefinitions)
			{
			ob.matrixOffset.set(tr.getMatrixWindow().getAddressInBytes(matrixID)/GLTextureBuffer.BYTES_PER_VEC4);
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
	
	protected void recalculateTransRotMBuffer(){
		if(LOOP){
			double delta = position[0]-tr.getRenderer().getCamera().getCameraPosition().getX();
			if(delta>TR.mapWidth/2.)
				{position[0]-=TR.mapWidth;}
			else if(delta<-TR.mapWidth/2.)
			{position[0]+=TR.mapWidth;}
			delta = position[1]-tr.getRenderer().getCamera().getCameraPosition().getY();
			if(delta>TR.mapWidth/2.)
				{position[1]-=TR.mapWidth;}
			else if(delta<-TR.mapWidth/2.)
				{position[1]+=TR.mapWidth;}
			delta = position[2]-tr.getRenderer().getCamera().getCameraPosition().getZ();
			if(delta>TR.mapWidth/2.)
				{position[2]-=TR.mapWidth;}
			else if(delta<-TR.mapWidth/2.)
				{position[2]+=TR.mapWidth;}
			}
		try{
		Vect3D.normalize(heading, aZ);
		Vect3D.cross(top,aZ,aX);
		Vect3D.cross(aZ,aX,aY);
		
		rMd[0]=aX[0];
		rMd[1]=aY[0];
		rMd[2]=aZ[0];
		
		rMd[4]=aX[1];
		rMd[5]=aY[1];
		rMd[6]=aZ[1];
		
		rMd[8]=aX[2];
		rMd[9]=aY[2];
		rMd[10]=aZ[2];
		
		tMd[3]=position[0];
		tMd[7]=position[1];
		tMd[11]=position[2];
		
		final RealMatrix cm = tr.getRenderer().getCamera().getMatrix();
		for(int i=0; i<16; i++){
		    cMd[i]=cm.getEntry(i/4, i%4);
		}
		if(translate()){Mat4x4.mul(tMd, rMd, rotTransM);}
		else	{System.arraycopy(rMd, 0, rotTransM, 0, 16);}
		
		Mat4x4.mul(cMd, rotTransM, camM);
		
		tr.getMatrixWindow().setTransposed(camM,matrixID);
		}catch(MathArithmeticException e){}//Don't crash.
		}//end recalculateTransRotMBuffer()
	
	protected boolean translate(){return true;}
	/**
	 * @return the visible
	 */
	public boolean isVisible(){
		return visible;
		}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible){
		this.visible = visible;
		}
	/**
	 * @return the position
	 */
	public final double [] getPosition()
		{return position;}

	/**
	 * @param position the position to set
	 */
	public WorldObject setPosition(double [] position){
		synchronized(position){
			this.position = position;
			notifyPositionListeners();
			}
		return this;
		}//end setPosition()
	public WorldObject notifyPositionChange(){
	    notifyPositionListeners();
	    return this;
	}
	
	/**
	 * @return the heading
	 */
	public final Vector3D getLookAt()
		{return new Vector3D(heading);}

	/**
	 * @param heading the heading to set
	 */
	public void setHeading(Vector3D nHeading){
	    	heading[0]=nHeading.getX();
	    	heading[1]=nHeading.getY();
	    	heading[2]=nHeading.getZ();
		}
	public Vector3D getHeading(){return new Vector3D(heading);}

	/**
	 * @return the top
	 */
	public final Vector3D getTop()
		{return new Vector3D(top);}

	/**
	 * @param top the top to set
	 */
	public void setTop(Vector3D nTop)
		{top[0]=nTop.getX();
	    	top[1]=nTop.getY();
	    	top[2]=nTop.getZ();
	    	}
	
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

	public static void uploadAllObjectDefinitionsToGPU(){
		for(WorldObject wo:allWorldObjects)
			{wo.initializeObjectDefinitions();}
		}//end uploadAllObjectDefinitionsToGPU()

	/**
	 * @return the tr
	 */
	public TR getTr()
		{return tr;}
	
	public void destroy()
		{//tr.getCollisionManager().remove(this);
	    	//setVisible(false);
	    	setActive(false);
		if(containingGrid!=null)containingGrid.remove(this);
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

	public Model getModel() {
	    return model;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
	    return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(boolean active) {
	    this.active = active;
	}

	public void movePositionBy(Vector3D delta) {
	    position[0]+=delta.getX();
	    position[1]+=delta.getY();
	    position[2]+=delta.getZ();
	    notifyPositionChange();
	}

	public void setPosition(double x, double y, double z) {
	    position[0]=x;
	    position[1]=y;
	    position[2]=z;
	    notifyPositionChange();
	}
	
	public double [] getHeadingArray(){return heading;}
	public double [] getTopArray(){return top;}
}//end WorldObject
