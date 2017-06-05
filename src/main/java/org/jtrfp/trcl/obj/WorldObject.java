/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2016 Chuck Ritola
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
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.MatrixWindow;
import org.jtrfp.trcl.ObjectDefinitionWindow;
import org.jtrfp.trcl.PrimitiveList;
import org.jtrfp.trcl.SpacePartitioningGrid;
import org.jtrfp.trcl.Submitter;
import org.jtrfp.trcl.WeakPropertyChangeSupport;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.beh.BehaviorNotFoundException;
import org.jtrfp.trcl.beh.CollisionBehavior;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.PropertyListenable;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.NotReadyException;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.TRFuture;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.math.Mat4x4;
import org.jtrfp.trcl.math.Vect3D;
import org.jtrfp.trcl.math.Vect3D.ZeroNormException;
import org.jtrfp.trcl.mem.VEC4Address;

public class WorldObject implements PositionedRenderable, PropertyListenable, Rotatable {
    public static final String HEADING       = "heading";
    public static final String HEADING_ARRAY = "headingArray";
    public static final String TOP           = "top";
    public static final String TOP_ARRAY     = "topArray";
    public static final String ACTIVE        = "active";
    public static final String POSITION      = "position";
    public static final String VISIBLE       = "visible";
    public static final String IN_GRID       = "inGrid";
    
    private double[] 	heading = new double[] { 0, 0, 1 }, oldHeading= new double[] {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    private double[] 	top 	= new double[] { 0, 1, 0 }, oldTop    = new double[] {Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    protected volatile double[] 
	    position = new double[3], 
	    positionAfterLoop = new double[3],
	    oldPosition = new double[]{Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
    private boolean loopedBefore = false;
    protected double[]  modelOffset= new double[3];
    private final double[]positionWithOffset 
    				= new double[3];
    private boolean	needToRecalcMatrix=true;
    private TR  	tr;
    private boolean 	visible = true;
    private GL33Model   model;
    private int[] 	triangleObjectDefinitions;
    private int[] 	transparentTriangleObjectDefinitions;
    protected volatile Integer 	matrixID;
    private volatile WeakReference<SpacePartitioningGrid> containingGrid;
    private ArrayList<Behavior> 	inactiveBehaviors  = new ArrayList<Behavior>();
    private ArrayList<CollisionBehavior>collisionBehaviors = new ArrayList<CollisionBehavior>();
    private ArrayList<Behavior> 	tickBehaviors 	   = new ArrayList<Behavior>();
    private boolean 			active 		   = true;
    private volatile byte 		renderFlags=0;
    private boolean			immuneToOpaqueDepthTest  = false;
    //private boolean                     objectDefsInitialized = false;
    private boolean                     inGrid = false;

    protected final double[] aX 	= new double[3];
    protected final double[] aY 	= new double[3];
    protected final double[] aZ 	= new double[3];
    protected final double[] rotTransM 	= new double[16];
    protected final double[] camM 	= new double[16];
    protected final double[] rMd 	= new double[16];
    protected final double[] tMd 	= new double[16];
    protected 	    double[] cMd 	= new double[16];
    private boolean respondToTick	= true;
    private double scale                = 1.;
    private final ReentrantLock         lock = new ReentrantLock();
    private String debugName            = "[unnamed]";
    
    //Cache
    private       GPU                    gpu;
    private       MatrixWindow           matrixWindow;
    private       ObjectDefinitionWindow objectDefinitionWindow;
    
    private CollectionActionDispatcher<VEC4Address> opaqueObjectDefinitionAddressesInVEC4      = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
    private CollectionActionDispatcher<VEC4Address> transparentObjectDefinitionAddressesInVEC4 = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
    
    protected final WeakPropertyChangeSupport pcs = new WeakPropertyChangeSupport(new PropertyChangeSupport(this));

    private TRFutureTask<Void> objectDefinitionsFuture;
    
    public enum RenderFlags{
	IgnoreCamera((byte)0x1);
	
	private final byte mask;
	private RenderFlags(byte mask){
	    this.mask=mask;
	}
	public byte getMask() {
	    return mask;
	}
    };
    
    public WorldObject(){
	// Matrix constants setup
	rMd[15] = 1;

	tMd[0] = 1;
	tMd[5] = 1;
	tMd[10] = 1;
	tMd[15] = 1;
	
	//Keep because of race condition of multiple threads grabbing multiple IDs; 
	//object defs end up with different ID from matrix writes and things disappear from invalid matrices containing DEADBEEF
	//getMatrixID();//TODO: Some sort of refactor? matrix should init once and be left alone, no need to lazy-load.
    }

    public WorldObject(GL33Model m) {
	this();
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
    
    public <T extends Behavior> T removeBehavior(T beh) {
	if (beh.isEnabled()) {
	    if (beh instanceof CollisionBehavior)
		collisionBehaviors.remove((CollisionBehavior) beh);
	    tickBehaviors.remove(beh);
	} else 
	    inactiveBehaviors.remove(beh);
	return beh;
    }//end removeBehavior()
    
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
	final ArrayList<T> result = new ArrayList<T>();
	synchronized(collisionBehaviors){
	if (type.isAssignableFrom(CollisionBehavior.class)) {
	    for (int i = 0; i < collisionBehaviors.size(); i++) {
		if (type.isAssignableFrom(collisionBehaviors.get(i).getClass())) {
		    result.add((T) collisionBehaviors.get(i));
		}
	    }// end if(instanceof)
	}// end isAssignableFrom(CollisionBehavior.class)
	}synchronized(inactiveBehaviors){
	for (int i = 0; i < inactiveBehaviors.size(); i++) {
	    if (type.isAssignableFrom(inactiveBehaviors.get(i).getClass()))
		result.add((T) inactiveBehaviors.get(i));
	}// end if(instanceof)
	}synchronized(tickBehaviors){
	for (int i = 0; i < tickBehaviors.size(); i++) {
	    if (type.isAssignableFrom(tickBehaviors.get(i).getClass()))
		result.add((T) tickBehaviors.get(i));
	}// end for (tickBehaviors)
     }//end sync(tickBehaviors)
     sub.submit(result);
    }// end probeForBehaviors(...)

    public void tick(long time) {
	if(!respondToTick)return;
	synchronized(tickBehaviors){
	for (int i = 0; i < tickBehaviors.size() && isActive(); i++)
	    tickBehaviors.get(i).proposeTick(time);
	}//end sync(tickBehaviors)
    }// end tick(...)
    
    private final int [] emptyIntArray = new int[0];
    
    public void setModel(GL33Model m) {
	final GL33Model previousModel = this.model;
	if(previousModel != null)
	    releaseCurrentModel();
	if( m == null )
	    return;
	try{this.model = m.finalizeModel();
	    refreshObjectDefinitions();
	}catch(Exception e){throw new RuntimeException(e);}
    }// end setModel(...)
    
    private void releaseCurrentModel(){
	if(transparentTriangleObjectDefinitions!=null)
	    for(int def:transparentTriangleObjectDefinitions)
		getObjectDefinitionWindow().freeLater(def);
	if(triangleObjectDefinitions!=null)
	    for(int def:triangleObjectDefinitions)
		getObjectDefinitionWindow().freeLater(def);
	Renderer.RENDER_LIST_EXECUTOR.submit(new Runnable(){
	    @Override
	    public void run() {
		getOpaqueObjectDefinitionAddressesInVEC4()     .clear();
		getTransparentObjectDefinitionAddressesInVEC4().clear();
	    }});
	
	transparentTriangleObjectDefinitions = null;
	triangleObjectDefinitions            = null;
	this.model            = null;
	//objectDefsInitialized = false;
    }//end releaseCurrentModel()

    public void setDirection(ObjectDirection dir) {
	lock.lock();
	try{
	 if (dir.getHeading().getNorm() == 0 || dir.getTop().getNorm() == 0) {
	    System.err
		    .println("Warning: Rejecting zero-norm for object direction. "
			    + dir);
	    new Exception().printStackTrace();
	    return;
	 }
	 setHeading(dir.getHeading());
	 setTop(dir.getTop());
	}finally {lock.unlock();}
    }//end setDirection(...)

    @Override
    public String toString() {
	final String modelDebugName;
	if(model!=null)modelDebugName=getModel().getDebugName();
	else modelDebugName="[null model]";
	return "WorldObject("+getDebugName()+") Model=" + modelDebugName + " pos="
		+ this.getPosition() + " class=" + getClass().getName()+" hash="+hashCode();
    }

    public final void refreshObjectDefinitions() {
	//if(objectDefsInitialized)
	//    return;
	if (model == null)
	    throw new NullPointerException(
		    "Model is null. Did you forget to set it? Object in question is: \n"+this.toString());
	//final TR tr = getTr();
	//objectDefinitionsFuture = tr.getThreadManager().submitToThreadPool(new Callable<Void>(){
	    //@Override
	    //public Void call() throws Exception {
		try{//Thread.currentThread().setName("WorldObject.objectDefinitionsFuture "+hashCode());
		    //tr.getThreadManager().submitToGPUMemAccess(new Callable<Void>(){
		    //    @Override
		    //    public Void call() throws Exception {
		    final GL33Model model = getModel();
		    final ObjectDefinitionWindow objectDefContextWindow = (ObjectDefinitionWindow)getObjectDefinitionWindow().newContextWindow();
		    processPrimitiveList(model.getTriangleList(),
			    getTriangleObjectDefinitions(), getOpaqueObjectDefinitionAddressesInVEC4(), 
			    objectDefContextWindow);
		    processPrimitiveList(model.getTransparentTriangleList(),
			    getTransparentTriangleObjectDefinitions(), getTransparentObjectDefinitionAddressesInVEC4(),
			    objectDefContextWindow);
		    //return null;
		    //   }}).get();
		    updateAllRenderFlagStates(objectDefContextWindow);
		    objectDefContextWindow.flush();
		    //Thread.currentThread().setName("Freed by "+hashCode());
		}catch(Exception e){e.printStackTrace();}
		//return null;
	    //}});
    }// end initializeObjectDefinitions()

    private void processPrimitiveList(PrimitiveList<?> primitiveList,
	    int[] objectDefinitions, final CollectionActionDispatcher<VEC4Address> objectDefinitionAddressesInVEC4,
	    ObjectDefinitionWindow objectDefContextWindow) {
	if (primitiveList == null)
	    return; // Nothing to do, no primitives here
	final GPU gpu = getGpu();
	final int gpuVerticesPerElement = primitiveList.getGPUVerticesPerElement();
	final int elementsPerBlock      = GPU.GPU_VERTICES_PER_BLOCK / gpuVerticesPerElement;
	int gpuVerticesRemaining        = primitiveList.getNumElements()*gpuVerticesPerElement;
	
	// For each of the allocated-but-not-yet-initialized object definitions.
	int odCounter=0;
	final int memoryWindowIndicesPerElement = primitiveList.getNumMemoryWindowIndicesPerElement();
	final Integer matrixID = getMatrixID();
	//Cache to hold new addresses for submission in bulk
	final ArrayList<VEC4Address> addressesToAdd = new ArrayList<VEC4Address>(objectDefinitions.length);
	if(objectDefinitions.length == 0)
	    System.err.println("WARNING: objectDefAddresses array of zero length! "+this.getClass().getName());
	for (final int index : objectDefinitions) {
	    final int vertexOffsetVec4s=new VEC4Address(primitiveList.getMemoryWindow().getPhysicalAddressInBytes(odCounter*elementsPerBlock*memoryWindowIndicesPerElement)).intValue();
	    final int matrixOffsetVec4s=new VEC4Address(gpu.matrixWindow.get()
		    .getPhysicalAddressInBytes(matrixID)).intValue();
	    objectDefContextWindow.matrixOffset.set(index,matrixOffsetVec4s);
	    objectDefContextWindow.vertexOffset.set(index,vertexOffsetVec4s);
	    objectDefContextWindow.modelScale.set(index, (byte) primitiveList.getPackedScale());
	    if (gpuVerticesRemaining >= GPU.GPU_VERTICES_PER_BLOCK) {
		objectDefContextWindow.numVertices.set(index,
			(byte) GPU.GPU_VERTICES_PER_BLOCK);
	    } else if (gpuVerticesRemaining > 0) {
		objectDefContextWindow.numVertices.set(index,
			(byte) (gpuVerticesRemaining));
	    } else {
		throw new RuntimeException("Ran out of vec4s.");
	    }
	    gpuVerticesRemaining -= GPU.GPU_VERTICES_PER_BLOCK;
	    addressesToAdd.add(new VEC4Address(objectDefContextWindow.getPhysicalAddressInBytes(index)));
	    odCounter++;
	}// end for(ObjectDefinition)
	Renderer.RENDER_LIST_EXECUTOR.submit(new Runnable(){
	    @Override
	    public void run() {
		objectDefinitionAddressesInVEC4.addAll(addressesToAdd);
	    }});
    }// end processPrimitiveList(...)
    
    protected void updateAllRenderFlagStates(ObjectDefinitionWindow objectDefinitionContext){
	final GL33Model model = getModel();
	if(model == null)
	    return;
	updateRenderFlagStatesPL(model.getTriangleList(),getTriangleObjectDefinitions(), objectDefinitionContext);
	updateRenderFlagStatesPL(model.getTransparentTriangleList(),getTransparentTriangleObjectDefinitions(), objectDefinitionContext);
    }
    
    protected void updateRenderFlagStatesPL(PrimitiveList<?> pl, int [] objectDefinitionIndices, ObjectDefinitionWindow objectDefinitionContext){
	for(int index : objectDefinitionIndices)
	    objectDefinitionContext.mode.set(index, (byte)(pl.getPrimitiveRenderMode() | (renderFlags << 4)&0xF0));
    }//end updateRenderFlagStatesPL

    public final void updateStateToGPU(Renderer renderer, MatrixWindow mwContext) throws NotReadyException {
	if(!lock.tryLock())
	    throw new NotReadyException();
	try{
	    //initializeObjectDefinitions();
	    System.arraycopy(position, 0, positionAfterLoop, 0, 3);
	    attemptLoop(renderer);
	    if(needToRecalcMatrix){
		needToRecalcMatrix=recalcMatrixWithEachFrame();
		recalculateTransRotMBuffer(mwContext);
	    }
	    if(model!=null)getModelRealtime().proposeAnimationUpdate();
	}finally{lock.unlock();}
    }//end updateStateToGPU()
    
    public boolean supportsLoop(){
	return true;
    }
    
    protected void attemptLoop(Renderer renderer){
	if (supportsLoop()) {
	    boolean change = false;
	    final Vector3D camPos = renderer.getCamera().getCameraPosition();
	    final double [] delta = new double[]{
		    positionAfterLoop[0] - camPos.getX(), 
		    positionAfterLoop[1] - camPos.getY(), 
		    positionAfterLoop[2] - camPos.getZ()};
	    if (delta[0] > TRFactory.mapWidth / 2.) {
		positionAfterLoop[0] -= TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    } else if (delta[0] < -TRFactory.mapWidth / 2.) {
		positionAfterLoop[0] += TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    }
	    if (delta[1] > TRFactory.mapWidth / 2.) {
		positionAfterLoop[1] -= TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    } else if (delta[1] < -TRFactory.mapWidth / 2.) {
		positionAfterLoop[1] += TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    }
	    if (delta[2] > TRFactory.mapWidth / 2.) {
		positionAfterLoop[2] -= TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    } else if (delta[2] < -TRFactory.mapWidth / 2.) {
		positionAfterLoop[2] += TRFactory.mapWidth;
		change = true;
		needToRecalcMatrix=true;
	    }
	    if(change){
		needToRecalcMatrix = true;
		loopedBefore = true;
	    }else{
		if(loopedBefore)
		    needToRecalcMatrix = true;
		loopedBefore = false;
	    }
	}//end if(LOOP)
    }//end attemptLoop()

    protected void recalculateTransRotMBuffer(MatrixWindow matrixWindow) {
	try {
	    if(!isVisible() || !isActive()){
		matrixWindow.matrix.set(getMatrixID(), invisibleMatrixArray);
		return;
	    }
	    Vect3D.normalize(heading, aZ);
	    Vect3D.normalize(top,aY);
	    Vect3D.cross(top, aZ, aX);
	    
	    recalculateRotBuffer();
	    if (translate()) {
		recalculateTransBuffer();
		Mat4x4.mul(tMd, rMd, rotTransM);
	    } else {
		System.arraycopy(rMd, 0, rotTransM, 0, 16);
	    }
	    matrixWindow.
	     setTransposed(rotTransM, getMatrixID(), scratchMatrixArray);//New version
	} catch (MathArithmeticException e) {e.printStackTrace();}// Don't crash.
	  catch (ZeroNormException e) {e.printStackTrace();}
    }// end recalculateTransRotMBuffer()
    
    protected void recalculateRotBuffer(){
	//Scale
	Vect3D.scalarMultiply(aX, getScale(), aX);
	Vect3D.scalarMultiply(aY, getScale(), aY);
	Vect3D.scalarMultiply(aZ, getScale(), aZ);

	rMd[0] = aX[0];
	rMd[1] = aY[0];
	rMd[2] = aZ[0];

	rMd[4] = aX[1];
	rMd[5] = aY[1];
	rMd[6] = aZ[1];

	rMd[8] = aX[2];
	rMd[9] = aY[2];
	rMd[10] = aZ[2];
    }//end recalculateRotBuffer
    
    protected void recalculateTransBuffer(){
	if(isVisible() && isActive()){
	    tMd[3] = positionAfterLoop[0]+modelOffset[0];
	    tMd[7] = positionAfterLoop[1]+modelOffset[1];
	    tMd[11]= positionAfterLoop[2]+modelOffset[2];
	}
    }//end recalculateTransBuffer()
    
    protected final double [] scratchMatrixArray = new double[16];
    protected final double [] invisibleMatrixArray = new double[] {
	    Double.NaN,Double.NaN,Double.NaN,Double.NaN,
	    Double.NaN,Double.NaN,Double.NaN,Double.NaN,
	    Double.NaN,Double.NaN,Double.NaN,Double.NaN,
	    Double.NaN,Double.NaN,Double.NaN,Double.NaN
    };

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
	final MatrixWindow matrixWindowContext = (MatrixWindow)getMatrixWindow().newContextWindow();
	recalculateTransRotMBuffer(matrixWindowContext);
	matrixWindowContext.flush();
	this.visible = visible;
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
    public void setPosition(double[] position) {
	this.position[0]=position[0];
	this.position[1]=position[1];
	this.position[2]=position[2];
	notifyPositionChange();
	//return this;
    }// end setPosition()
    
    public WorldObject notifyPositionChange(){
	lock.lock();
	try{
	    if(position[0]==Double.NaN)
		throw new RuntimeException("Invalid position.");
	    pcs.firePropertyChange(POSITION, oldPosition, position);
	    needToRecalcMatrix=true;
	    updateOldPosition();
	}finally{lock.unlock();}
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
    public void setHeading(Vector3D nHeading) {
	if(nHeading.getNorm() == 0)
	    throw new IllegalArgumentException("Cannot apply zero-norm vector to heading.");
	lock.lock();
	try{
	    System.arraycopy(heading, 0, oldHeading, 0, 3);
	    heading[0] = nHeading.getX();
	    heading[1] = nHeading.getY();
	    heading[2] = nHeading.getZ();
	    pcs.firePropertyChange(HEADING, oldHeading, nHeading);
	    needToRecalcMatrix=true;
	}finally{lock.unlock();}
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
    public void setTop(Vector3D nTop) {
	if(nTop.getNorm() == 0)
	    throw new IllegalArgumentException("Cannot apply zero-norm vector to top.");
	lock.lock();
	try{
	    System.arraycopy(top, 0, oldTop, 0, 3);
	    top[0] = nTop.getX();
	    top[1] = nTop.getY();
	    top[2] = nTop.getZ();
	    pcs.firePropertyChange(TOP, oldTop, nTop);
	    needToRecalcMatrix=true;
	}finally{lock.unlock();}
    }//end setTop(...)
    
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
	if( tr == null)
	    tr = Features.get(Features.getSingleton(), TR.class);
	return tr;
    }

    public void destroy() {
	lock.lock();
	try{
	    final SpacePartitioningGrid<PositionedRenderable> grid = getContainingGrid();
	    if(grid != null)
	     grid.remove(this);//TODO: This occasionally throws exceptions because the grid doesn't always contain it.
	    /*
	    if(grid !=null){
		try{World.relevanceExecutor.submit(new Runnable(){
		    @Override
		    public void run() {
			grid.remove(WorldObject.this);
		    }}).get();}catch(Exception e){throw new RuntimeException(e);}
	    }//end if(NEW MODE and have grid)
	    */
	    setContainingGrid(null);
	    // Send it to the land of wind and ghosts.
	    setActive(false);
	    notifyPositionChange();
	}finally{lock.unlock();}
    }//end destroy()

    @Override
    public void setContainingGrid(SpacePartitioningGrid grid) {
	containingGrid = new WeakReference<SpacePartitioningGrid>(grid);
	setInGrid(grid != null);
	notifyPositionChange();
    }

    public SpacePartitioningGrid<PositionedRenderable> getContainingGrid() {
	try{return containingGrid.get();}
	catch(NullPointerException e){return null;}
    }

    public GL33Model getModel() {
	//try{return model.get();}
	//catch(NullPointerException e){return null;}
	//catch(Exception e){throw new RuntimeException(e);}
	return model;
    }
    
    public GL33Model getModelRealtime() throws NotReadyException{
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
	final boolean oldState = this.active;
	if(this.active!=active)
	    needToRecalcMatrix=true;
	if(!this.active && active && isVisible()){
	    this.active=true;
	}
	this.active = active;
	pcs.firePropertyChange(ACTIVE,oldState,active);
    }//end setActive(...)

    public void movePositionBy(Vector3D delta) {
	lock.lock();
	try{
	    position[0] += delta.getX();
	    position[1] += delta.getY();
	    position[2] += delta.getZ();
	    notifyPositionChange();
	}finally{lock.unlock();}
    }//end movePositionBy(...)

    public void setPosition(double x, double y, double z) {
	lock.lock();
	try{
	    position[0] = x;
	    position[1] = y;
	    position[2] = z;
	    notifyPositionChange();}
	finally{lock.unlock();}
    }

    public double[] getHeadingArray() {
	return heading;
    }
    
    public void setHeadingArray(double [] newHeading){
	System.arraycopy(newHeading, 0, heading, 0, 3);
    }

    public double[] getTopArray() {
	return top;
    }
    
    public void setTopArray(double [] newTop){
	System.arraycopy(newTop, 0, top, 0, 3);
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
        final ObjectDefinitionWindow objectDefinitionContext = (ObjectDefinitionWindow)getObjectDefinitionWindow().newContextWindow();
        updateAllRenderFlagStates(objectDefinitionContext);
        objectDefinitionContext.flush();
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
	final TR tr = getTr();
	if(matrixID!=null)
	    getGpu().matrixWindow.get().freeLater(matrixID);
	if(transparentTriangleObjectDefinitions!=null)
	    for(int def:transparentTriangleObjectDefinitions)
		getObjectDefinitionWindow().freeLater(def);
	if(triangleObjectDefinitions!=null)
	    for(int def:triangleObjectDefinitions)
		getObjectDefinitionWindow().freeLater(def);

	super.finalize();
    }//end finalize()
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

    public boolean hasBehavior(Class<? extends Behavior> behaviorClass) {
	try{probeForBehavior(behaviorClass);}
	catch(BehaviorNotFoundException e){return false;}
	return true;
    }

    protected int[] getTriangleObjectDefinitions() {
        return triangleObjectDefinitions = 
        	getObjectDefinitions(triangleObjectDefinitions, getModel().getTriangleList());
    }

    protected int[] getTransparentTriangleObjectDefinitions() {
        return transparentTriangleObjectDefinitions = 
        	getObjectDefinitions(transparentTriangleObjectDefinitions, getModel().getTransparentTriangleList());
    }
    
    protected int[] getObjectDefinitions(int [] originalObjectDefs, PrimitiveList<?> pList){
	if(originalObjectDefs == null){
	    int numObjDefs, sizeInVerts;
	    if (pList == null)
		originalObjectDefs = emptyIntArray;
	    else {
		sizeInVerts = pList
			.getTotalSizeInGPUVertices();
		numObjDefs = sizeInVerts / GPU.GPU_VERTICES_PER_BLOCK;
		if (sizeInVerts % GPU.GPU_VERTICES_PER_BLOCK != 0)
		    numObjDefs++;
		originalObjectDefs = new int[numObjDefs];
		final ObjectDefinitionWindow odw = getObjectDefinitionWindow();
		for (int i = 0; i < numObjDefs; i++) {
		    originalObjectDefs[i] = odw.create();
		}//end for(numObjDefs)
	    }//end if(!null)
	}//end if(null)
	return originalObjectDefs;
    }//end getObjectDefinitions(...)

    protected CollectionActionDispatcher<VEC4Address> getOpaqueObjectDefinitionAddressesInVEC4() {
	if(opaqueObjectDefinitionAddressesInVEC4==null)
	    opaqueObjectDefinitionAddressesInVEC4 = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
        return opaqueObjectDefinitionAddressesInVEC4;
    }

    protected CollectionActionDispatcher<VEC4Address> getTransparentObjectDefinitionAddressesInVEC4() {
        if(transparentObjectDefinitionAddressesInVEC4==null)
            transparentObjectDefinitionAddressesInVEC4 = new CollectionActionDispatcher<VEC4Address>(new ArrayList<VEC4Address>());
	return transparentObjectDefinitionAddressesInVEC4;
    }

    protected Integer getMatrixID() {
	if(matrixID == null)
	    return getMatrixSafe();
        return matrixID;
    }
    
    private synchronized Integer getMatrixSafe(){
	if(matrixID == null)
	    matrixID = getGpu().matrixWindow.get().create();
        return matrixID;
    }

    public void setMatrixID(Integer matrixID) {
        this.matrixID = matrixID;
    }

    protected double getScale() {
        return scale;
    }

    protected void setScale(double scale) {
        this.scale = scale;
    }
    
    public void setRenderFlag(RenderFlags flag){
	setRenderFlags((byte)(getRenderFlags() | flag.getMask()));
    }
    
    public void unsetRenderFlag(RenderFlags flag){
	setRenderFlags((byte)(getRenderFlags() & ~flag.getMask()));
    }
    
    public boolean getRenderFlag(RenderFlags flag){
	return ((getRenderFlags()&0xFF) & flag.getMask()) != 0;
    }

    public void setTr(TR tr) {
        this.tr = tr;
    }
    
    public boolean isInGrid(){
	return inGrid;
    }
    
    public void setInGrid(boolean inGrid){
	this.inGrid = inGrid; 
    }

    GPU getGpu() {
	if(gpu == null)
	    gpu = Features.get(getTr(), GPUFeature.class);
        return gpu;
    }

    void setGpu(GPU gpu) {
        this.gpu = gpu;
    }

    public MatrixWindow getMatrixWindow() {
	if(matrixWindow == null)
	    matrixWindow = getGpu().matrixWindow.get();
        return matrixWindow;
    }

    public void setMatrixWindow(MatrixWindow matrixWindow) {
        this.matrixWindow = matrixWindow;
    }

    ObjectDefinitionWindow getObjectDefinitionWindow() {
	if(objectDefinitionWindow == null)
	    objectDefinitionWindow = getGpu().objectDefinitionWindow.get();
        return objectDefinitionWindow;
    }

    void setObjectDefinitionWindow(ObjectDefinitionWindow objectDefinitionWindow) {
        this.objectDefinitionWindow = objectDefinitionWindow;
    }

    public String getDebugName() {
        return debugName;
    }

    public void setDebugName(String debugName) {
        this.debugName = debugName;
    }
}// end WorldObject
