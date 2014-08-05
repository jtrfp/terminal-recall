/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.obj;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.NullBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;

public class WorldObject implements PositionedRenderable {
    
    public static final boolean LOOP = true;

    private double[] 	heading = new double[] { 0, 0, 1 };
    private double[] 	top 	= new double[] { 0, 1, 0 };
    protected double[] position = new double[3];
    private final TR 	tr;
    private boolean 	visible = true;
    private Model 	model;
    private List<PositionListener> 
    			positionListeners 
    				= Collections.synchronizedList(new ArrayList<PositionListener>());
    private int[] 	triangleObjectDefinitions;
    private int[] 	transparentTriangleObjectDefinitions;
    protected final int matrixID;
    private ByteBuffer 	opaqueObjectDefinitionAddressesInVec4 = ByteBuffer
	    .allocate(0);// defaults to empty
    private ByteBuffer 	transparentObjectDefinitionAddressesInVec4 = ByteBuffer
	    .allocate(0);// defaults to empty

    private WeakReference<SpacePartitioningGrid> containingGrid;
    private ArrayList<Behavior> 	inactiveBehaviors  = new ArrayList<Behavior>();
    private ArrayList<CollisionBehavior>collisionBehaviors = new ArrayList<CollisionBehavior>();
    private ArrayList<Behavior> 	tickBehaviors 	   = new ArrayList<Behavior>();
    private final NullBehavior 		nullBehavior;
    private boolean 			active 		   = true;
    private byte 			renderFlags=0;

    protected final double[] aX 	= new double[3];
    protected final double[] aY 	= new double[3];
    protected final double[] aZ 	= new double[3];
    protected final double[] rotTransM 	= new double[16];
    protected final double[] camM 	= new double[16];
    protected final double[] rMd 	= new double[16];
    protected final double[] tMd 	= new double[16];
    protected 	    double[] cMd 	= new double[16];
    private boolean respondToTick	= true;

    public WorldObject(TR tr) {
	this.nullBehavior = new NullBehavior(this);
	this.tr = tr;
	matrixID = tr.matrixWindow.get().create();
	// Matrix constants setup
	rMd[15] = 1;

	tMd[0] = 1;
	tMd[5] = 1;
	tMd[10] = 1;
	tMd[15] = 1;
    }

    public WorldObject(TR tr, Model m) {
	this(tr);
	setModel(m);
    }// end constructor

    void proposeCollision(WorldObject other) {
	for (int i = 0; i < collisionBehaviors.size(); i++) {
	    collisionBehaviors.get(i).proposeCollision(other);
	}// end for(collisionBehaviors)
    }// end proposeCollision(...)

    
    public boolean isCollideable(){
	return !collisionBehaviors.isEmpty();
    }
    
    public <T extends Behavior> T addBehavior(T ob) {
	if (ob.isEnabled()) {
	    if (ob instanceof CollisionBehavior)
		collisionBehaviors.add((CollisionBehavior) ob);
	    tickBehaviors.add(ob);
	} else {
	    inactiveBehaviors.add(ob);
	}
	ob.setParent(this);
	return ob;
    }

    public <T> T probeForBehavior(Class<T> bC) {
	if (bC.isAssignableFrom(CollisionBehavior.class)) {
	    for (int i = 0; i < collisionBehaviors.size(); i++) {
		if (bC.isAssignableFrom(collisionBehaviors.get(i).getClass())) {
		    return (T) collisionBehaviors.get(i);
		}
	    }// end if(instanceof)
	}// emd if(isAssignableFrom(CollisionBehavior.class))
	for (int i = 0; i < inactiveBehaviors.size(); i++) {
	    if (bC.isAssignableFrom(inactiveBehaviors.get(i).getClass())) {
		return (T) inactiveBehaviors.get(i);
	    }
	}// end if(instanceof)
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    if (bC.isAssignableFrom(tickBehaviors.get(i).getClass())) {
		return (T) tickBehaviors.get(i);
	    }
	}// end if(instanceof)
	throw new BehaviorNotFoundException("Cannot find behavior of type "
		+ bC.getName() + " in behavior sandwich owned by "
		+ this.getClass().getName());
    }// end probeForBehavior

    public <T> void probeForBehaviors(Submitter<T> sub, Class<T> type) {
	if (type.isAssignableFrom(CollisionBehavior.class)) {
	    for (int i = 0; i < collisionBehaviors.size(); i++) {
		if (type.isAssignableFrom(collisionBehaviors.get(i).getClass())) {
		    sub.submit((T) collisionBehaviors.get(i));
		}
	    }// end if(instanceof)
	}// end isAssignableFrom(CollisionBehavior.class)
	for (int i = 0; i < inactiveBehaviors.size(); i++) {
	    if (type.isAssignableFrom(inactiveBehaviors.get(i).getClass())) {
		sub.submit((T) inactiveBehaviors.get(i));
	    }
	}// end if(instanceof)
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    if (type.isAssignableFrom(tickBehaviors.get(i).getClass())) {
		sub.submit((T) tickBehaviors.get(i));
	    }
	}// end for (tickBehaviors)
    }// end probeForBehaviors(...)

    public void tick(long time) {
	if(!respondToTick)return;
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    tickBehaviors.get(i).tick(time);
	}// end for(size)
    }// end tick(...)
    
    private final int [] emptyIntArray = new int[0];
    
    public void setModel(Model m) {
	if (m == null)
	    throw new RuntimeException("Passed model cannot be null.");
	model = m;
	int numObjDefs, sizeInVerts;
	/*
	if (m.getLineSegmentList() == null)
	    lineSegmentObjectDefinitions = new int[0];
	else {
	    sizeInVerts = m.getLineSegmentList().getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU.GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU.GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    lineSegmentObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		lineSegmentObjectDefinitions[i] = tr
			.getObjectDefinitionWindow().create();
	    }
	    
	}*/
	if (m.getTriangleList() == null)
	    triangleObjectDefinitions = emptyIntArray;
	else {
	    sizeInVerts = m.getTriangleList().getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU.GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU.GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    triangleObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		triangleObjectDefinitions[i] = tr.objectDefinitionWindow.get()
			.create();
	    }
	}
	if (m.getTransparentTriangleList() == null)
	    transparentTriangleObjectDefinitions = emptyIntArray;
	else {
	    sizeInVerts = m.getTransparentTriangleList()
		    .getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU.GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU.GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    transparentTriangleObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		transparentTriangleObjectDefinitions[i] = tr
			.objectDefinitionWindow.get().create();
	    }
	}
	initializeObjectDefinitions();
    }// end setModel(...)

    public void setDirection(ObjectDirection dir) {
	if (dir.getHeading().getNorm() == 0 || dir.getTop().getNorm() == 0) {
	    System.err
		    .println("Warning: Rejecting zero-norm for object direction. "
			    + dir);
	    new Exception().printStackTrace();
	    return;
	}
	setHeading(dir.getHeading());
	setTop(dir.getTop());
    }

    @Override
    public String toString() {
	return "WorldObject Model=" + model.getDebugName() + " pos="
		+ this.getPosition() + " class=" + getClass().getName();
    }

    public final void initializeObjectDefinitions() {
	if (model == null)
	    throw new NullPointerException(
		    "Model is null. Did you forget to set it?");
	final ArrayList<Integer> opaqueIndicesList = new ArrayList<Integer>();
	final ArrayList<Integer> transparentIndicesList = new ArrayList<Integer>();
	
	tr.getThreadManager().submitToGPUMemAccess(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		processPrimitiveList(model.getTriangleList(),
			triangleObjectDefinitions, opaqueIndicesList);
		processPrimitiveList(model.getTransparentTriangleList(),
			transparentTriangleObjectDefinitions, transparentIndicesList);
		return null;
	    }}).get();//TODO: Make non-blocking
	ByteOrder order = getTr().gpu.get().getByteOrder();
	opaqueObjectDefinitionAddressesInVec4 = ByteBuffer.allocateDirect(
		opaqueIndicesList.size() * 4).order(order);// 4 bytes per int
	transparentObjectDefinitionAddressesInVec4 = ByteBuffer.allocateDirect(
		transparentIndicesList.size() * 4).order(order);

	IntBuffer trans = transparentObjectDefinitionAddressesInVec4
		.asIntBuffer(), opaque = opaqueObjectDefinitionAddressesInVec4
		.asIntBuffer();

	for (Integer elm : transparentIndicesList) {
	    trans.put(elm);
	}
	for (Integer elm : opaqueIndicesList) {
	    opaque.put(elm);
	}
    }// end initializeObjectDefinitions()

    private void processPrimitiveList(PrimitiveList<?> primitiveList,
	    int[] objectDefinitions, ArrayList<Integer> indicesList) {
	if (primitiveList == null)
	    return; // Nothing to do, no primitives here
	//int vec4sRemaining = primitiveList.getTotalSizeInVec4s();
	final int gpuVerticesPerElement = primitiveList.getGPUVerticesPerElement();
	final int elementsPerBlock = GPU.GPU_VERTICES_PER_BLOCK / gpuVerticesPerElement;
	int gpuVerticesRemaining = primitiveList.getNumElements()*gpuVerticesPerElement;
	// For each of the allocated-but-not-yet-initialized object definitions.
	final ObjectDefinitionWindow odw = tr.objectDefinitionWindow.get();
	int odCounter=0;
	final int memoryWindowIndicesPerElement = primitiveList.getNumMemoryWindowIndicesPerElement();
	for (final int index : objectDefinitions) {
	    final int vertexOffsetVec4s=primitiveList.getMemoryWindow().getPhysicalAddressInBytes(odCounter*elementsPerBlock*memoryWindowIndicesPerElement)
		    /GPU.BYTES_PER_VEC4;
	    final int matrixOffsetVec4s=tr.matrixWindow.get()
		    .getPhysicalAddressInBytes(matrixID)
		    / GPU.BYTES_PER_VEC4;
	    odw.matrixOffset.set(index,matrixOffsetVec4s);
	    odw.vertexOffset.set(index,vertexOffsetVec4s);
	    odw.mode.set(index, (byte)(primitiveList.getPrimitiveRenderMode() | (renderFlags << 4)&0xF0));
	    odw.modelScale.set(index, (byte) primitiveList.getPackedScale());
	    if (gpuVerticesRemaining >= GPU.GPU_VERTICES_PER_BLOCK) {
		odw.numVertices.set(index,
			(byte) GPU.GPU_VERTICES_PER_BLOCK);
	    } else if (gpuVerticesRemaining > 0) {
		odw.numVertices.set(index,
			(byte) (gpuVerticesRemaining));
	    } else {
		throw new RuntimeException("Ran out of vec4s.");
	    }
	    gpuVerticesRemaining -= GPU.GPU_VERTICES_PER_BLOCK;
	    indicesList.add(odw.getPhysicalAddressInBytes(index)
		    / GPU.BYTES_PER_VEC4);
	    odCounter++;
	}// end for(ObjectDefinition)
    }// end processPrimitiveList(...)

    public final void updateStateToGPU() {
	recalculateTransRotMBuffer();
	if(model!=null)model.proposeAnimationUpdate();
    }

    protected void recalculateTransRotMBuffer() {
	if (LOOP) {
	    double delta = position[0]
		    - tr.renderer.get().getCamera().getCameraPosition().getX();
	    if (delta > TR.mapWidth / 2.) {
		position[0] -= TR.mapWidth;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[0] += TR.mapWidth;
	    }
	    delta = position[1]
		    - tr.renderer.get().getCamera().getCameraPosition().getY();
	    if (delta > TR.mapWidth / 2.) {
		position[1] -= TR.mapWidth;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[1] += TR.mapWidth;
	    }
	    delta = position[2]
		    - tr.renderer.get().getCamera().getCameraPosition().getZ();
	    if (delta > TR.mapWidth / 2.) {
		position[2] -= TR.mapWidth;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[2] += TR.mapWidth;
	    }
	}
	try {
	    Vect3D.normalize(heading, aZ);
	    Vect3D.cross(top, aZ, aX);
	    Vect3D.cross(aZ, aX, aY);

	    rMd[0] = aX[0];
	    rMd[1] = aY[0];
	    rMd[2] = aZ[0];

	    rMd[4] = aX[1];
	    rMd[5] = aY[1];
	    rMd[6] = aZ[1];

	    rMd[8] = aX[2];
	    rMd[9] = aY[2];
	    rMd[10] = aZ[2];

	    tMd[3] = position[0];
	    tMd[7] = position[1];
	    tMd[11] = position[2];
	    
	    if (translate()) {
		Mat4x4.mul(tMd, rMd, rotTransM);
	    } else {
		System.arraycopy(rMd, 0, rotTransM, 0, 16);
	    }
	    tr.matrixWindow.get().setTransposed(rotTransM, matrixID);//New version
	} catch (MathArithmeticException e) {
	}// Don't crash.
    }// end recalculateTransRotMBuffer()

    protected boolean translate() {
	return true;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
	return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
	if(!this.visible && visible){
	    this.visible = true;
	    tr.threadManager.submitToGPUMemAccess(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    WorldObject.this.updateStateToGPU();
		    return null;
		}
	    });
	    tr.renderer.get().temporarilyMakeImmediatelyVisible(this);
	}else this.visible = visible;
    }//end setvisible()

    /**
     * @return the position
     */
    public final double[] getPosition() {
	return position;
    }

    /**
     * @param position
     *            the position to set
     */
    public WorldObject setPosition(double[] position) {
	synchronized (position) {
	    this.position = position;
	    notifyPositionListeners();
	}
	return this;
    }// end setPosition()

    public WorldObject notifyPositionChange() {
	notifyPositionListeners();
	return this;
    }

    /**
     * @return the heading
     */
    public final Vector3D getLookAt() {
	return new Vector3D(heading);
    }

    /**
     * @param heading
     *            the heading to set
     */
    public void setHeading(Vector3D nHeading) {
	heading[0] = nHeading.getX();
	heading[1] = nHeading.getY();
	heading[2] = nHeading.getZ();
    }

    public Vector3D getHeading() {
	return new Vector3D(heading);
    }

    /**
     * @return the top
     */
    public final Vector3D getTop() {
	return new Vector3D(top);
    }

    /**
     * @param top
     *            the top to set
     */
    public void setTop(Vector3D nTop) {
	top[0] = nTop.getX();
	top[1] = nTop.getY();
	top[2] = nTop.getZ();
    }

    private void notifyPositionListeners() {
	for(int i=0; i<positionListeners.size(); i++){
	    positionListeners.get(i).positionChanged(this);
	}
    }//end notifyPositionListeners()
    
    public List<PositionListener> getPositionListeners(){
	return positionListeners;
    }

    @Override
    public void addPositionListener(PositionListener listenerToAdd) {
	if(!positionListeners.contains(listenerToAdd))
	    positionListeners.add(listenerToAdd);
    }

    @Override
    public void removePositionListener(PositionListener listenerToRemove) {
	positionListeners.remove(listenerToRemove);
    }

    public final ByteBuffer getOpaqueObjectDefinitionAddresses() {
	opaqueObjectDefinitionAddressesInVec4.clear();
	return opaqueObjectDefinitionAddressesInVec4;
    }

    public final ByteBuffer getTransparentObjectDefinitionAddresses() {
	transparentObjectDefinitionAddressesInVec4.clear();
	return transparentObjectDefinitionAddressesInVec4;
    }

    /**
     * @return the tr
     */
    public TR getTr() {
	return tr;
    }

    public void destroy() {
	// Send it to the land of wind and ghosts.
	final double[] pos = getPosition();
	pos[0] = Double.NEGATIVE_INFINITY;
	pos[1] = Double.NEGATIVE_INFINITY;
	pos[2] = Double.NEGATIVE_INFINITY;
	notifyPositionChange();
	setActive(false);
	if (containingGrid != null){
	    SpacePartitioningGrid g = getContainingGrid();
	    if(g!=null)
		containingGrid.get().remove(this);
	}//end if(gird!=null)
    }

    @Override
    public void setContainingGrid(SpacePartitioningGrid grid) {
	containingGrid = new WeakReference<SpacePartitioningGrid>(grid);
    }

    public SpacePartitioningGrid getContainingGrid() {
	try{return containingGrid.get();}
	catch(NullPointerException e){return null;}
    }

    public Model getModel() {
	return model;
    }

    public Behavior getBehavior() {
	return nullBehavior;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
	return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive(boolean active) {
	if(!this.active && active && isVisible()){
	    this.active=true;
	    tr.renderer.get().temporarilyMakeImmediatelyVisible(this);
	    tr.threadManager.submitToGPUMemAccess(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    WorldObject.this.updateStateToGPU();
		    return null;
		}
	    });
	}
	this.active = active;
    }//end setActive(...)

    public void movePositionBy(Vector3D delta) {
	position[0] += delta.getX();
	position[1] += delta.getY();
	position[2] += delta.getZ();
	notifyPositionChange();
    }

    public void setPosition(double x, double y, double z) {
	position[0] = x;
	position[1] = y;
	position[2] = z;
	notifyPositionChange();
    }

    public double[] getHeadingArray() {
	return heading;
    }

    public double[] getTopArray() {
	return top;
    }

    public void enableBehavior(Behavior behavior) {
	if (!inactiveBehaviors.contains(behavior)) {
	    throw new RuntimeException(
		    "Tried to enabled an unregistered behavior.");
	}
	if (behavior instanceof CollisionBehavior) {
	    if (!collisionBehaviors.contains(behavior)
		    && behavior instanceof CollisionBehavior) {
		collisionBehaviors.add((CollisionBehavior) behavior);
	    }
	}
	if (!tickBehaviors.contains(behavior)) {
	    tickBehaviors.add(behavior);
	}
    }// end enableBehavior(...)

    public void disableBehavior(Behavior behavior) {
	if (!inactiveBehaviors.contains(behavior))
	    inactiveBehaviors.add(behavior);
	if (behavior instanceof CollisionBehavior)
	    collisionBehaviors.remove(behavior);
	tickBehaviors.remove(behavior);
    }

    /**
     * @return the renderFlags
     */
    public int getRenderFlags() {
        return renderFlags;
    }

    /**
     * @param renderFlags the renderFlags to set
     */
    public void setRenderFlags(byte renderFlags) {
        this.renderFlags = renderFlags;
    }

    /**
     * @return the respondToTick
     */
    public boolean isRespondToTick() {
        return respondToTick;
    }

    /**
     * @param respondToTick the respondToTick to set
     */
    public void setRespondToTick(boolean respondToTick) {
        this.respondToTick = respondToTick;
    }
    
    @Override
    public void finalize() throws Throwable{
	System.out.println("WorldObject.finalize()");
	tr.matrixWindow.get().free(matrixID);
	for(int def:transparentTriangleObjectDefinitions)
	    tr.objectDefinitionWindow.get().free(def);
	for(int def:triangleObjectDefinitions)
	    tr.objectDefinitionWindow.get().free(def);
	super.finalize();
    }//end finalize()
}// end WorldObject
