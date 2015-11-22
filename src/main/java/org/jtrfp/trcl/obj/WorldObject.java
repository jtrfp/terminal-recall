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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.WeakPropertyChangeSupport;
import org.jtrfp.trcl.World;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.PropertyListenable;
import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.ext.tr.GPUResourceFinalizer;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.mem.VEC4Address;

public class WorldObject implements PositionedRenderable, PropertyListenable, Rotatable {
    public static final String HEADING  ="heading";
    public static final String TOP      ="top";
    
    private double[] 	heading = new double[] { 0, 0, 1 }, oldHeading= new double[] {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    private double[] 	top 	= new double[] { 0, 1, 0 }, oldTop    = new double[] {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    protected volatile double[] 
	    position = new double[3], 
	    oldPosition = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    protected double[]  modelOffset= new double[3];
    private final double[]positionWithOffset 
    				= new double[3];
    private boolean	needToRecalcMatrix=true;
    private final TR 	tr;
    private boolean 	visible = true;
    private Model 	model;
    private List<PositionedRenderable>lastContainingList;
    private int[] 	triangleObjectDefinitions;
    private int[] 	transparentTriangleObjectDefinitions;
    protected Integer 	matrixID;
    private volatile WeakReference<SpacePartitioningGrid> containingGrid;
    private ArrayList<Behavior> 	inactiveBehaviors  = new ArrayList<Behavior>();
    private ArrayList<CollisionBehavior>collisionBehaviors = new ArrayList<CollisionBehavior>();
    private ArrayList<Behavior> 	tickBehaviors 	   = new ArrayList<Behavior>();
    private boolean 			active 		   = true;
    private byte 			renderFlags=0;
    private boolean			immuneToOpaqueDepthTest  = false;

    protected final double[] aX 	= new double[3];
    protected final double[] aY 	= new double[3];
    protected final double[] aZ 	= new double[3];
    protected final double[] rotTransM 	= new double[16];
    protected final double[] camM 	= new double[16];
    protected final double[] rMd 	= new double[16];
    protected final double[] tMd 	= new double[16];
    protected 	    double[] cMd 	= new double[16];
    private boolean respondToTick	= true;
    private final GPUResourceFinalizer  gpuResourceFinalizer;
    
    private CollectionActionDispatcher<VEC4Address> opaqueObjectDefinitionAddressesInVEC4      = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
    private CollectionActionDispatcher<VEC4Address> transparentObjectDefinitionAddressesInVEC4 = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
    
    protected final WeakPropertyChangeSupport pcs = new WeakPropertyChangeSupport(new PropertyChangeSupport(this));

    public WorldObject(TR tr) {
	this.tr = tr;
	if(tr!=null)
	 matrixID = tr.gpu.get().matrixWindow.get().create();
	// Matrix constants setup
	rMd[15] = 1;

	tMd[0] = 1;
	tMd[5] = 1;
	tMd[10] = 1;
	tMd[15] = 1;
	
	gpuResourceFinalizer = tr.gpu.get().getGPUResourceFinalizer();
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
    
    protected boolean recalcMatrixWithEachFrame(){
	return false;
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
		+ this.toString());
    }// end probeForBehavior

    public <T> void probeForBehaviors(Submitter<T> sub, Class<T> type) {
	synchronized(collisionBehaviors){
	if (type.isAssignableFrom(CollisionBehavior.class)) {
	    for (int i = 0; i < collisionBehaviors.size(); i++) {
		if (type.isAssignableFrom(collisionBehaviors.get(i).getClass())) {
		    sub.submit((T) collisionBehaviors.get(i));
		}
	    }// end if(instanceof)
	}// end isAssignableFrom(CollisionBehavior.class)
	}synchronized(inactiveBehaviors){
	for (int i = 0; i < inactiveBehaviors.size(); i++) {
	    if (type.isAssignableFrom(inactiveBehaviors.get(i).getClass()))
		sub.submit((T) inactiveBehaviors.get(i));
	}// end if(instanceof)
	}synchronized(tickBehaviors){
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    if (type.isAssignableFrom(tickBehaviors.get(i).getClass()))
		sub.submit((T) tickBehaviors.get(i));
	}// end for (tickBehaviors)
     }//end sync(tickBehaviors)
    }// end probeForBehaviors(...)

    public void tick(long time) {
	if(!respondToTick)return;
	synchronized(tickBehaviors){
	for (int i = 0; i < tickBehaviors.size() && isActive(); i++)
	    tickBehaviors.get(i).proposeTick(time);
	}//end sync(tickBehaviors)
    }// end tick(...)
    
    private final int [] emptyIntArray = new int[0];
    
    public void setModel(Model m) {
	if (m == null)
	    throw new RuntimeException("Passed model cannot be null.");
	model = m;
	try{model.finalizeModel().get();}catch(Exception e){throw new RuntimeException(e);}
	int numObjDefs, sizeInVerts;
	if (m.getTriangleList() == null)
	    triangleObjectDefinitions = emptyIntArray;
	else {
	    sizeInVerts = m.getTriangleList().getTotalSizeInGPUVertices();
	    numObjDefs = sizeInVerts / GPU.GPU_VERTICES_PER_BLOCK;
	    if (sizeInVerts % GPU.GPU_VERTICES_PER_BLOCK != 0)
		numObjDefs++;
	    triangleObjectDefinitions = new int[numObjDefs];
	    for (int i = 0; i < numObjDefs; i++) {
		triangleObjectDefinitions[i] = tr.gpu.get().objectDefinitionWindow.get()
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
		transparentTriangleObjectDefinitions[i] = tr.gpu.get()
			.objectDefinitionWindow.get().create();
	    }
	}
	initializeObjectDefinitions();
    }// end setModel(...)

    public synchronized void setDirection(ObjectDirection dir) {
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
	final String modelDebugName;
	if(model!=null)modelDebugName=model.getDebugName();
	else modelDebugName="[null model]";
	return "WorldObject Model=" + modelDebugName + " pos="
		+ this.getPosition() + " class=" + getClass().getName()+" hash="+hashCode();
    }

    public final void initializeObjectDefinitions() {
	if (model == null)
	    throw new NullPointerException(
		    "Model is null. Did you forget to set it?");
	//final ArrayList<Integer> opaqueIndicesList = new ArrayList<Integer>();
	//final ArrayList<Integer> transparentIndicesList = new ArrayList<Integer>();
	tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
	    @Override
	    public Void call() throws Exception {
		tr.getThreadManager().submitToGPUMemAccess(new Callable<Void>(){
		    @Override
		    public Void call() throws Exception {
			processPrimitiveList(model.getTriangleList(),
				triangleObjectDefinitions, opaqueObjectDefinitionAddressesInVEC4);
			processPrimitiveList(model.getTransparentTriangleList(),
				transparentTriangleObjectDefinitions, transparentObjectDefinitionAddressesInVEC4);
			return null;
		    }}).get();
		/*for(int i = 0; i < opaqueIndicesList.size(); i++)
		    opaqueObjectDefinitionAddressesInVEC4.add(new VEC4Address(opaqueIndicesList.get(i)));
		for(int i = 0; i < transparentIndicesList.size(); i++)
		    transparentObjectDefinitionAddressesInVEC4.add(new VEC4Address(transparentIndicesList.get(i)));
		*/
		return null;
	    }});
    }// end initializeObjectDefinitions()

    private void processPrimitiveList(PrimitiveList<?> primitiveList,
	    int[] objectDefinitions, CollectionActionDispatcher<VEC4Address> objectDefinitionAddressesInVEC4) {
	if (primitiveList == null)
	    return; // Nothing to do, no primitives here
	final int gpuVerticesPerElement = primitiveList.getGPUVerticesPerElement();
	final int elementsPerBlock      = GPU.GPU_VERTICES_PER_BLOCK / gpuVerticesPerElement;
	int gpuVerticesRemaining        = primitiveList.getNumElements()*gpuVerticesPerElement;
	// For each of the allocated-but-not-yet-initialized object definitions.
	final ObjectDefinitionWindow odw = tr.gpu.get().objectDefinitionWindow.get();
	int odCounter=0;
	final int memoryWindowIndicesPerElement = primitiveList.getNumMemoryWindowIndicesPerElement();
	for (final int index : objectDefinitions) {
	    final int vertexOffsetVec4s=new VEC4Address(primitiveList.getMemoryWindow().getPhysicalAddressInBytes(odCounter*elementsPerBlock*memoryWindowIndicesPerElement)).intValue();
	    final int matrixOffsetVec4s=new VEC4Address(tr.gpu.get().matrixWindow.get()
		    .getPhysicalAddressInBytes(matrixID)).intValue();
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
	    objectDefinitionAddressesInVEC4.add(new VEC4Address(odw.getPhysicalAddressInBytes(index)));
	    odCounter++;
	}// end for(ObjectDefinition)
    }// end processPrimitiveList(...)

    public synchronized final void updateStateToGPU(Renderer renderer) {
	attemptLoop(renderer);
	if(needToRecalcMatrix){
	    recalculateTransRotMBuffer();
	    needToRecalcMatrix=recalcMatrixWithEachFrame();
	}
	if(model!=null)model.proposeAnimationUpdate();
    }//end updateStateToGPU()
    
    public boolean supportsLoop(){
	return true;
    }
    
    protected void attemptLoop(Renderer renderer){
	if (supportsLoop()) {
	    final Vector3D camPos = renderer.getCamera().getCameraPosition();
	    double delta = position[0]
		    - camPos.getX();
	    if (delta > TR.mapWidth / 2.) {
		position[0] -= TR.mapWidth;
		needToRecalcMatrix=true;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[0] += TR.mapWidth;
		needToRecalcMatrix=true;
	    }
	    delta = position[1]
		    - camPos.getY();
	    if (delta > TR.mapWidth / 2.) {
		position[1] -= TR.mapWidth;
		needToRecalcMatrix=true;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[1] += TR.mapWidth;
		needToRecalcMatrix=true;
	    }
	    delta = position[2]
		    - camPos.getZ();
	    if (delta > TR.mapWidth / 2.) {
		position[2] -= TR.mapWidth;
		needToRecalcMatrix=true;
	    } else if (delta < -TR.mapWidth / 2.) {
		position[2] += TR.mapWidth;
		needToRecalcMatrix=true;
	    }
	}//end if(LOOP)
    }//end attemptLoop()

    protected void recalculateTransRotMBuffer() {
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
	    if(isVisible() && isActive()){
		tMd[3] = position[0]+modelOffset[0];
		tMd[7] = position[1]+modelOffset[1];
		tMd[11]= position[2]+modelOffset[2];
	    }else{
		tMd[3] = Double.POSITIVE_INFINITY;
		tMd[7] = Double.POSITIVE_INFINITY;
		tMd[11]= Double.POSITIVE_INFINITY;
	    }//end (!visible)
	    if (translate()) {
		Mat4x4.mul(tMd, rMd, rotTransM);
	    } else {
		System.arraycopy(rMd, 0, rotTransM, 0, 16);
	    }
	    tr.gpu.get().matrixWindow.get().setTransposed(rotTransM, matrixID, scratchMatrixArray);//New version
	} catch (MathArithmeticException e) {
	}// Don't crash.
    }// end recalculateTransRotMBuffer()
    
    protected final double [] scratchMatrixArray = new double[16];

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
	if(this.visible==visible)
	    return;
	needToRecalcMatrix=true;
	if(!this.visible && visible){
	    this.visible = true;
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
	this.position[0]=position[0];
	this.position[1]=position[1];
	this.position[2]=position[2];
	notifyPositionChange();
	return this;
    }// end setPosition()
    
    public synchronized WorldObject notifyPositionChange(){
	if(position[0]==Double.NaN)
	    throw new RuntimeException("Invalid position.");
	//pcs.firePropertyChange(POSITIONV3D, null, new Vector3D(position));
	pcs.firePropertyChange(POSITION, null, position);
	needToRecalcMatrix=true;
	updateOldPosition();
	return this;
    }//end notifyPositionChange()
    
    private void updateOldPosition(){
	System.arraycopy(position, 0, oldPosition, 0, 3);
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
    public synchronized void setHeading(Vector3D nHeading) {
	System.arraycopy(heading, 0, oldHeading, 0, 3);
	pcs.firePropertyChange(HEADING, oldHeading, nHeading);
	heading[0] = nHeading.getX();
	heading[1] = nHeading.getY();
	heading[2] = nHeading.getZ();
	needToRecalcMatrix=true;
    }

    public Vector3D getHeading() {
	assert !(top[0]==0 && top[1]==0 && top[2]==0);
	return new Vector3D(heading);
    }

    /**
     * @return the top
     */
    public final Vector3D getTop() {
	assert !(top[0]==0 && top[1]==0 && top[2]==0);
	return new Vector3D(top);
    }

    /**
     * @param top
     *            the top to set
     */
    public synchronized void setTop(Vector3D nTop) {
	System.arraycopy(top, 0, oldTop, 0, 3);
	pcs.firePropertyChange(TOP, oldTop, nTop);
	top[0] = nTop.getX();
	top[1] = nTop.getY();
	top[2] = nTop.getZ();
	needToRecalcMatrix=true;
    }
    
    public final CollectionActionDispatcher<VEC4Address> getOpaqueObjectDefinitionAddresses(){
	return opaqueObjectDefinitionAddressesInVEC4;
    }
    
    public final CollectionActionDispatcher<VEC4Address> getTransparentObjectDefinitionAddresses(){
	return transparentObjectDefinitionAddressesInVEC4;
    }

    /**
     * @return the tr
     */
    public TR getTr() {
	return tr;
    }

    public synchronized void destroy() {
	if(containingGrid !=null){
	    try{World.relevanceExecutor.submit(new Runnable(){
		@Override
		public void run() {
		    getContainingGrid().remove(WorldObject.this);
		}}).get();}catch(Exception e){throw new RuntimeException(e);}
	}//end if(NEW MODE and have grid)
	containingGrid=null;
	// Send it to the land of wind and ghosts.
	setActive(false);
	notifyPositionChange();
    }//end destroy()

    @Override
    public void setContainingGrid(SpacePartitioningGrid grid) {
	containingGrid = new WeakReference<SpacePartitioningGrid>(grid);
	notifyPositionChange();
    }

    public SpacePartitioningGrid<PositionedRenderable> getContainingGrid() {
	try{return containingGrid.get();}
	catch(NullPointerException e){return null;}
    }

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
     * @param active
     *            the active to set
     */
    public void setActive(boolean active) {
	if(this.active!=active)
	    needToRecalcMatrix=true;
	if(!this.active && active && isVisible()){
	    this.active=true;
	}
	this.active = active;
    }//end setActive(...)

    public synchronized void movePositionBy(Vector3D delta) {
	position[0] += delta.getX();
	position[1] += delta.getY();
	position[2] += delta.getZ();
	notifyPositionChange();
    }

    public synchronized void setPosition(double x, double y, double z) {
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
	    synchronized(inactiveBehaviors){
		inactiveBehaviors.add(behavior);
	    }
	if (behavior instanceof CollisionBehavior)
	    synchronized(collisionBehaviors){
		collisionBehaviors.remove(behavior);
	    }
	synchronized(tickBehaviors){
	    tickBehaviors.remove(behavior);
	}
    }//end disableBehavior(...)

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
	gpuResourceFinalizer.
	 submitFinalizationAction(
	  new WorldObjectFinalizerTask(tr,matrixID,triangleObjectDefinitions,transparentTriangleObjectDefinitions));
	
	
	super.finalize();
    }//end finalize()
    
    private static final class WorldObjectFinalizerTask implements Callable<Void>{
	private final Integer matrixID;
	private final int[] triangleObjectDefinitions, transparentTriangleObjectDefinitions;
	private final TR tr;
	
	public WorldObjectFinalizerTask(TR tr, Integer matrixID, int [] triangleObjectDefinitions, int [] transparentTriangleObjectDefinitions){
	    this.matrixID                             = matrixID;
	    this.triangleObjectDefinitions            = triangleObjectDefinitions;
	    this.transparentTriangleObjectDefinitions = transparentTriangleObjectDefinitions;
	    this.tr                                   = tr;
	}
	@Override
	public Void call() throws Exception {
	    if(matrixID!=null)
		 tr.gpu.get().matrixWindow.get().free(matrixID);
		if(transparentTriangleObjectDefinitions!=null)
		 for(int def:transparentTriangleObjectDefinitions)
		    tr.gpu.get().objectDefinitionWindow.get().free(def);
		if(triangleObjectDefinitions!=null)
		 for(int def:triangleObjectDefinitions)
		    tr.gpu.get().objectDefinitionWindow.get().free(def);
	    return null;
	}
	
    }//end WorldObjectFinalizerTask

    /**
     * @param modelOffset the modelOffset to set
     */
    public void setModelOffset(double x, double y, double z) {
        modelOffset[0]=x;
        modelOffset[1]=y;
        modelOffset[2]=z;
    }

    public double[] getPositionWithOffset() {
	positionWithOffset[0]=position[0]+modelOffset[0];
	positionWithOffset[1]=position[1]+modelOffset[1];
	positionWithOffset[2]=position[2]+modelOffset[2];
	return positionWithOffset;
    }

    public boolean isImmuneToOpaqueDepthTest() {
	return immuneToOpaqueDepthTest;
    }

    /**
     * @param immuneToDepthTest the immuneToDepthTest to set
     */
    public WorldObject setImmuneToOpaqueDepthTest(boolean immuneToDepthTest) {
        this.immuneToOpaqueDepthTest = immuneToDepthTest;
        return this;
    }

    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
	pcs.addPropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }

    /**
     * @param arg0
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
	pcs.removePropertyChangeListener(arg0);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }

    /*public void checkPositionSanity() {
	if(position[0]==Double.NaN||position[1]==Double.NaN||position[2]==Double.NaN)
	    throw new RuntimeException("Invalid position");
    }*/
}// end WorldObject
