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
import org.apache.commons.math3.linear.RealMatrix;
import org.jtrfp.trcl.GPUTriangleVertex;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.beh.NullBehavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLTextureBuffer;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;

public class WorldObject implements PositionedRenderable {
    public static final int GPU_VERTICES_PER_BLOCK = 96;
    public static final boolean LOOP = true;

    private double[] heading = new double[] { 0, 0, 1 }; // Facing direction
    private double[] top = new double[] { 0, 1, 0 }; // Normal describing the
						     // top of the object (for
						     // tilt)
    protected double[] position = new double[3];
    private final TR tr;
    private boolean visible = true;
    private Model model;
    private ArrayList<PositionListener> positionListeners = new ArrayList<PositionListener>();
    private int[] triangleObjectDefinitions;
    private int[] lineSegmentObjectDefinitions;
    private int[] transparentTriangleObjectDefinitions;
    protected final int matrixID;
    private ByteBuffer opaqueObjectDefinitionAddressesInVec4 = ByteBuffer
	    .allocate(0);// defaults to empty
    private ByteBuffer transparentObjectDefinitionAddressesInVec4 = ByteBuffer
	    .allocate(0);// defaults to empty

    private static final List<WorldObject> allWorldObjects = Collections
	    .synchronizedList(new ArrayList<WorldObject>());

    private SpacePartitioningGrid containingGrid;
    private ArrayList<Behavior> inactiveBehaviors = new ArrayList<Behavior>();
    private ArrayList<CollisionBehavior> collisionBehaviors = new ArrayList<CollisionBehavior>();
    private ArrayList<Behavior> tickBehaviors = new ArrayList<Behavior>();
    private final NullBehavior nullBehavior;
    private boolean active = true;
    private byte renderFlags=0;

    protected final double[] aX = new double[3];
    protected final double[] aY = new double[3];
    protected final double[] aZ = new double[3];
    protected final double[] rotTransM = new double[16];
    protected final double[] camM = new double[16];
    protected final double[] rMd = new double[16];
    protected final double[] tMd = new double[16];
    protected double[] cMd = new double[16];

    public WorldObject(TR tr) {
	this.nullBehavior = new NullBehavior(this);
	this.tr = tr;
	addWorldObject(this);
	matrixID = tr.getMatrixWindow().create();
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
	for (int i = 0; i < collisionBehaviors.size(); i++) {// Not using
							     // iterator to
							     // avoid excess
							     // garbage
							     // creation.
	    collisionBehaviors.get(i).proposeCollision(other);
	}// end for(collisionBehaviors)
    }// end proposeCollision(...)

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
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    tickBehaviors.get(i).tick(time);
	}// end for(size)
    }// end tick(...)

    private static final void addWorldObject(WorldObject o) {
	if (o == null) {
	    new Exception().printStackTrace();
	    System.exit(1);
	}
	allWorldObjects.add(o);
    }

    public void setModel(Model m) {
	if (m == null)
	    throw new RuntimeException("Passed model cannot be null.");
	model = m;
	int numObjDefs, sizeInVerts;
	if (m.getLineSegmentList() == null)
	    lineSegmentObjectDefinitions = new int[0];
	else {
	    sizeInVerts = m.getLineSegmentList().getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    lineSegmentObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		lineSegmentObjectDefinitions[i] = tr
			.getObjectDefinitionWindow().create();
	    }
	}
	if (m.getTriangleList() == null)
	    triangleObjectDefinitions = new int[0];
	else {
	    sizeInVerts = m.getTriangleList().getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    triangleObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		triangleObjectDefinitions[i] = tr.getObjectDefinitionWindow()
			.create();
	    }
	}
	if (m.getTransparentTriangleList() == null)
	    transparentTriangleObjectDefinitions = new int[0];
	else {
	    sizeInVerts = m.getTransparentTriangleList()
		    .getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    transparentTriangleObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		transparentTriangleObjectDefinitions[i] = tr
			.getObjectDefinitionWindow().create();
	    }
	}
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
	ArrayList<Integer> opaqueIndicesList = new ArrayList<Integer>();
	ArrayList<Integer> transparentIndicesList = new ArrayList<Integer>();

	processPrimitiveList(model.getTriangleList(),
		triangleObjectDefinitions, opaqueIndicesList);
	processPrimitiveList(model.getLineSegmentList(),
		lineSegmentObjectDefinitions, transparentIndicesList);
	processPrimitiveList(model.getTransparentTriangleList(),
		transparentTriangleObjectDefinitions, transparentIndicesList);

	ByteOrder order = getTr().getGPU().getByteOrder();
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
	final int elementsPerBlock = GPU_VERTICES_PER_BLOCK / gpuVerticesPerElement;
	final int vec4sPerBlock = elementsPerBlock * primitiveList.getElementSizeInVec4s();
	int gpuVerticesRemaining = primitiveList.getNumElements()*gpuVerticesPerElement;
	/*final int vec4sPerBlock = primitiveList.getElementSizeInVec4s()
		* (GPU_VERTICES_PER_BLOCK / primitiveList
			.getGPUVerticesPerElement());*/
	final int verticesPerVec4 =
		gpuVerticesPerElement / primitiveList
		.getElementSizeInVec4s();
	// For each of the allocated-but-not-yet-initialized object definitions.
	final ObjectDefinitionWindow odw = tr.getObjectDefinitionWindow();
	int odCounter=0;
	final int memoryWindowIndicesPerElement = primitiveList.getNumMemoryWindowIndicesPerElement();
	//final int vec4sPerElement = primitiveList.getMemoryWindow().getObjectSizeInBytes()/GLTextureBuffer.BYTES_PER_VEC4;
	
	System.out.println("primitiveList "+primitiveList.getClass().getName()+
		"\ngpuVerticesPerElement="+gpuVerticesPerElement+" elementsPerBlock="+elementsPerBlock+" vec4sPerBlock="+vec4sPerBlock);
	System.out.println("verticesPerVec4="+verticesPerVec4);
	for (final int index : objectDefinitions) {
	    final int vertexOffsetVec4s=primitiveList.getMemoryWindow().getPhysicalAddressInBytes(odCounter*elementsPerBlock*memoryWindowIndicesPerElement)
		    /GLTextureBuffer.BYTES_PER_VEC4;
	    final int matrixOffsetVec4s=tr.getMatrixWindow()
		    .getPhysicalAddressInBytes(matrixID)
		    / GLTextureBuffer.BYTES_PER_VEC4;
	    System.out.println("odIndex="+odCounter+" vertexOffsetInVEC4s="+vertexOffsetVec4s);
	    System.out.println("matrixOffset="+matrixOffsetVec4s);
	    odw.matrixOffset.set(index,matrixOffsetVec4s);
	    odw.vertexOffset.set(index,vertexOffsetVec4s);
	    odw.mode.set(index, (byte)(primitiveList.getPrimitiveRenderMode() | (renderFlags << 4)&0xF0));
	    odw.modelScale.set(index, (byte) primitiveList.getPackedScale());
	    if (gpuVerticesRemaining >= GPU_VERTICES_PER_BLOCK) {
		odw.numVertices.set(index,
			(byte) GPU_VERTICES_PER_BLOCK);
	    } else if (gpuVerticesRemaining > 0) {
		odw.numVertices.set(index,
			(byte) (gpuVerticesRemaining));
	    } else {
		throw new RuntimeException("Ran out of vec4s.");
	    }
	    gpuVerticesRemaining -= GPU_VERTICES_PER_BLOCK;
	    indicesList.add(odw.getPhysicalAddressInBytes(index)
		    / GLTextureBuffer.BYTES_PER_VEC4);
	    odCounter++;
	}// end for(ObjectDefinition)
    }// end processPrimitiveList(...)

    public final void updateStateToGPU() {
	recalculateTransRotMBuffer();
    }

    protected void recalculateTransRotMBuffer() {
	if (LOOP) {
	    double delta = position[0]
		    - tr.getRenderer().getCamera().getCameraPosition().getX();
	    if (delta > TR.mapWidth / 2.) {
		position[0] -= TR.mapWidth;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[0] += TR.mapWidth;
	    }
	    delta = position[1]
		    - tr.getRenderer().getCamera().getCameraPosition().getY();
	    if (delta > TR.mapWidth / 2.) {
		position[1] -= TR.mapWidth;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[1] += TR.mapWidth;
	    }
	    delta = position[2]
		    - tr.getRenderer().getCamera().getCameraPosition().getZ();
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

	    /*final RealMatrix cm = tr.getRenderer().getCamera().getMatrix();
	    for (int i = 0; i < 16; i++) {
		cMd[i] = cm.getEntry(i / 4, i % 4);
	    }*/
	    if (translate()) {
		Mat4x4.mul(tMd, rMd, rotTransM);
	    } else {
		System.arraycopy(rMd, 0, rotTransM, 0, 16);
	    }

	    //Mat4x4.mul(cMd, rotTransM, camM);//Camera matrix calc moved to GPU

	    //tr.getMatrixWindow().setTransposed(camM, matrixID);//Camera matrix calc moved to GPU
	    tr.getMatrixWindow().setTransposed(rotTransM, matrixID);//New version
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
	this.visible = visible;
    }

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
	for (PositionListener l : positionListeners) {
	    l.positionChanged(this);
	}
    }

    @Override
    public void addPositionListener(PositionListener listenerToAdd) {
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

    public static void uploadAllObjectDefinitionsToGPU() {
	for (WorldObject wo : allWorldObjects) {
	    wo.initializeObjectDefinitions();
	}
    }// end uploadAllObjectDefinitionsToGPU()

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
	if (containingGrid != null)
	    containingGrid.remove(this);
    }

    @Override
    public void setContainingGrid(SpacePartitioningGrid grid) {
	containingGrid = grid;
    }

    public SpacePartitioningGrid getContainingGrid() {
	return containingGrid;
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
	this.active = active;
    }

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
}// end WorldObject
