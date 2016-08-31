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
package org.jtrfp.trcl.mem;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;

import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.pool.IndexPool;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;

public abstract class MemoryWindow {
    private PagedByteBuffer buffer;
    private int objectSizeInBytes;
    private IndexPool indexPool;
    private String debugName;
    private Reporter reporter;
    private static final MemoryWindowFreeingService freeingService = new MemoryWindowFreeingService();
    static {freeingService.start();}//TODO: Static vars are trouble around the corner.
    private ArrayList<Integer> freeLater = new ArrayList<Integer>();
    private int minFreeDelayMillis = 100, maxFreeDelayMillis = 500;
    private volatile long bulkFreeDeadline;
    
    static class MemoryWindowFreeingService extends Thread {
	private final Queue<MemoryWindow> stale = new ArrayDeque<MemoryWindow>();
	private final ArrayList<Integer>  freeLaterBuffer = new ArrayList<Integer>();
	private int loopTally = 0;
	
	MemoryWindowFreeingService(){
	    super("MemoryWindowFreeingService");
	}//end constructor
	
	@Override
	public void run(){
	    while(true){
		final MemoryWindow nextWindowToFree;
		synchronized(stale){
		    while(stale.isEmpty())
			try{stale.wait();}
		    catch(InterruptedException e){e.printStackTrace();}//Shouldn't happen.
		    nextWindowToFree = stale.poll();
		}//end sync(stale)
		assert nextWindowToFree!=null;
		final long now = System.currentTimeMillis();
		final long timeUntilNextFree = nextWindowToFree.bulkFreeDeadline - now;
		if(timeUntilNextFree > 0)
		    try{Thread.sleep(timeUntilNextFree);}
		catch(InterruptedException e){e.printStackTrace();}
		final ArrayList<Integer> origFreeAll = nextWindowToFree.freeLater;
		synchronized(origFreeAll){
		    freeLaterBuffer.addAll(origFreeAll);
		    origFreeAll.clear();
		}
		loopTally++;
		if(loopTally % 32 == 0)//Don't overload the console
		 System.out.println("MemoryWindow.MemoryWindowFreeingService freeing "+freeLaterBuffer.size()+" elements.");
		nextWindowToFree.getIndexPool().free(freeLaterBuffer);
		freeLaterBuffer.clear();
	    }//end while(true)
	}//end run()

	public void notifyStale(MemoryWindow memoryWindow) {
	    synchronized(stale){
		boolean wasEmpty = stale.isEmpty();
		stale.add(memoryWindow);
		if(wasEmpty)
		 stale.notifyAll();
	    }
	}//end notifyStale()
    }//end MemoryWindowFreeingService
    
    protected final void initFields(){
	int byteOffset = 0;
	for (Field f : getClass().getFields()) {
	    if (Variable.class.isAssignableFrom(f.getType())) {
		try {
		    final Variable<?, ?> var = (Variable<?, ?>) f.get(this);
		    var.initialize(this);
		    var.setByteOffset(new ByteAddress(byteOffset));
		    byteOffset += var.getSizeInBytes();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		}
	    }// end if(Variable)
	}// end for(fields)
	objectSizeInBytes = byteOffset;
    }//end initFields()
    
    protected final void init(GPU gpu, String debugName) {
	this.debugName = debugName;
	initFields();
	setBuffer(gpu
		.memoryManager.get()
		.createPagedByteBuffer(PagedByteBuffer.PAGE_SIZE_BYTES,
			"MemoryWindow " + this.getDebugName()));
	getIndexPool().setGrowthBehavior(new GrowthBehavior() {
	    @Override
	    public int grow(int previousMaxCapacity) {
		// Grow by one page
		final int newSizeInObjects = previousMaxCapacity
			+ (int)Math.ceil((double)PagedByteBuffer.PAGE_SIZE_BYTES
			/ (double)getObjectSizeInBytes());
		getBuffer().resize(newSizeInObjects * getObjectSizeInBytes());
		if(MemoryWindow.this.reporter!=null)
		 MemoryWindow.this.reporter.report(
			"org.jtrfp.trcl.mem.MemoryWindow."
				+ MemoryWindow.this.debugName
				+ ".sizeInObjects", newSizeInObjects+"");

		for (int p = 0; p < MemoryWindow.this.numPages(); p++) {
		    if(MemoryWindow.this.reporter!=null)
		     MemoryWindow.this.reporter
			    .report("org.jtrfp.trcl.mem.MemoryWindow."
				    + MemoryWindow.this.debugName + ".page" + p,
				    String.format(
					    "%08x",
					    MemoryWindow.this
						    .logicalPage2PhysicalPage(p)
						    * PagedByteBuffer.PAGE_SIZE_BYTES));
		}
		if(MemoryWindow.this.reporter!=null)
		 MemoryWindow.this.reporter.report(
			"org.jtrfp.trcl.mem.MemoryWindow."
				+ MemoryWindow.this.debugName
				+ ".sizeInObjects", newSizeInObjects+"");
		return newSizeInObjects;
	    }//end grow()

	    @Override
	    public int shrink(int minDesiredCapacity) {
		//Attempt to shrink by one page
		final int previousMaxCapacity = getIndexPool().getMaxCapacity();
		final int objectsPerPage =  (int)Math.ceil((double)PagedByteBuffer.PAGE_SIZE_BYTES
			/ (double)getObjectSizeInBytes());
		final int proposedNewSizePages = (int)Math.ceil(((double)minDesiredCapacity)/((double)objectsPerPage));
		final int proposedNewSizeInObjects = proposedNewSizePages * objectsPerPage;
		if(proposedNewSizeInObjects != previousMaxCapacity){
		    getBuffer().resize(proposedNewSizeInObjects * getObjectSizeInBytes());
		    return proposedNewSizeInObjects;
		}else return previousMaxCapacity;//No change.
	    }//end shrink(...)
	});
	buffer.resize(getObjectSizeInBytes() * getNumObjects());
	for (int p = 0; p < numPages(); p++) {
	    if(MemoryWindow.this.reporter!=null)
	     MemoryWindow.this.reporter.report(
		    "org.jtrfp.trcl.mem.MemoryWindow."
			    + MemoryWindow.this.debugName + ".page" + p,
		    String.format("%08x",
			    MemoryWindow.this.logicalPage2PhysicalPage(p)
				    * PagedByteBuffer.PAGE_SIZE_BYTES));
	}//end init()
	
    }// end init()
    
    public final int create() {
	return getIndexPool().pop();
    }//end create()
    
    public final void create(Collection<Integer> dest, int count){
	getIndexPool().pop(dest, count);
    }//end create(...)
    
    public final int free(int objectIDToFree){
	return getIndexPool().free(objectIDToFree);
    }//end free(...)
    
    public final void freeLater(int objectIDToFree){
	synchronized(freeLater){
	    kickFreeLaterListForward();
	    freeLater.add(objectIDToFree);
	}//end sync(...)
    }//end freeLater(...)
    
    public final void freeLater(Collection<Integer> objectIdsToFree){
	synchronized(freeLater){
	    kickFreeLaterListForward();
	    freeLater.addAll(objectIdsToFree);
	}//end sync(...)
    }//end freeLater(...)

    private void kickFreeLaterListForward(){
	final long now = System.currentTimeMillis();
	if(freeLater.isEmpty()){
	    bulkFreeDeadline   = now + this.getMaxFreeDelayMillis();
	    getFreeingService().notifyStale(this);
	}else
	    bulkFreeDeadline = now + this.getMinFreeDelayMillis();
    }//end kickFreeLaterListForward()
    
    public final void free(Collection<Integer> objectIdsToFree){
	getIndexPool().free(objectIdsToFree);
    }

    public final int getNumObjects() {
	return getIndexPool().getMaxCapacity();
    }//end getNumObjects()

    public static abstract class Variable<TYPE, THIS_CLASS extends Variable> {
	private MemoryWindow parent;
	//private int byteOffset;
	private ByteAddress byteOffset;

	void initialize(MemoryWindow parent) {
	    this.parent = parent;
	}

	public abstract THIS_CLASS set(int objectIndex, TYPE value);

	public abstract TYPE get(int objectIndex);

	/**
	 * @return the parent
	 */
	protected final MemoryWindow getParent() {
	    return parent;
	}

	private final THIS_CLASS setByteOffset(ByteAddress off) {
	    this.byteOffset = off;
	    return (THIS_CLASS) this;
	}

	public final ByteAddress logicalByteOffsetWithinObject() {
	    return byteOffset;
	}

	protected abstract int getSizeInBytes();
    }// end Property

    public static final class IntVariable extends
	    Variable<Integer, IntVariable> {
	@Override
	public IntVariable set(int objectIndex, Integer value) {
	    getParent().getBuffer().putInt(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Integer get(int objectIndex) {
	    return getParent().getBuffer().getInt(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes());
	}

	@Override
	protected int getSizeInBytes() {
	    return 4;
	}
    }// end IntVariable

    public static final class ByteVariable extends Variable<Byte, ByteVariable> {

	@Override
	public ByteVariable set(int objectIndex, Byte value) {
	    getParent().getBuffer().put(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	public ByteVariable set(int objectIndex, byte value) {
	    getParent().getBuffer().put(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Byte get(int objectIndex) {
	    return getParent().getBuffer().get(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes());
	}

	@Override
	protected int getSizeInBytes() {
	    return 1;
	}
    }// end ByteVariable

    public static final class ShortVariable extends
	    Variable<Short, ShortVariable> {

	@Override
	public ShortVariable set(int objectIndex, Short value) {
	    getParent().getBuffer().putShort(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	public ShortVariable set(int objectIndex, short value) {
	    getParent().getBuffer().putShort(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Short get(int objectIndex) {
	    return getParent().getBuffer().getShort(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes());
	}

	@Override
	protected int getSizeInBytes() {
	    return 2;
	}
    }// end ShortVariable

    public static final class IntArrayVariable extends
	    Variable<int[], IntArrayVariable> {
	private final int arrayLen;// Keep for automatic size calculation

	public IntArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}
	
	@Override
	public IntArrayVariable set(int objectIndex, int[] value) {
	    getParent().getBuffer().putInts(logicalByteOffsetWithinObject().intValue() + objectIndex
			* getParent().getObjectSizeInBytes(),value);
	    return this;
	}

	public IntArrayVariable set(int objectIndex, int offsetInInts,
		int[] value) {
	    getParent().getBuffer().putInts(logicalByteOffsetWithinObject().intValue() + offsetInInts * 4 + objectIndex
			* getParent().getObjectSizeInBytes(),value);
	    return this;
	}// end set(...)

	@Override
	public int[] get(int objectIndex) {
	    throw new RuntimeException("Unimplemented.");
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen * 4;
	}

	public void setAt(int objectIndex, int arrayIndex, int value) {
	    getParent().getBuffer().putInt(
		    logicalByteOffsetWithinObject().intValue() + arrayIndex * 4 + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	}

	public int get(int objectIndex, int arrayIndex) {
	    return getParent().getBuffer().getInt(
		    logicalByteOffsetWithinObject().intValue() + arrayIndex * 4 + objectIndex
			    * getParent().getObjectSizeInBytes());
	}

	public IntArrayVariable setAt(int objectIndex, int offsetInInts,
		Collection<? extends Number> c) {
	    getParent().getBuffer().putInts(logicalByteOffsetWithinObject().intValue() + offsetInInts * 4 + objectIndex
			* getParent().getObjectSizeInBytes(),c);
	    return this;
	}
    }// end IntArrayVariable

    public static final class VEC4ArrayVariable extends
	    Variable<int[], VEC4ArrayVariable> {
	private final int arrayLen;// Keep for automatic size calculation

	public VEC4ArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}

	@Override
	public VEC4ArrayVariable set(int objectIndex, int[] value) {
	    for (int i = 0; i < value.length; i++) {
		getParent().getBuffer().putInt(
			logicalByteOffsetWithinObject().intValue() + objectIndex
				* getParent().getObjectSizeInBytes(), value[i]);
	    }// end for(i)
	    return this;
	}

	public VEC4ArrayVariable setAt(int objectIndex, int offsetInVEC4s,
		int[] value) {
	    for (int i = 0; i < value.length; i++) {
		getParent().getBuffer().putInt(
			logicalByteOffsetWithinObject().intValue() + offsetInVEC4s * 16 + objectIndex
				* getParent().getObjectSizeInBytes(), value[i]);
	    }// end for(i)
	    return this;
	}// end set(...)

	@Override
	public int[] get(int objectIndex) {
	    throw new RuntimeException("Unimplemented.");
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen * 16;
	}
    }// end VEC4ArrayVariable

    public static final class ByteArrayVariable extends
	    Variable<ByteBuffer, ByteArrayVariable> {
	private final int arrayLen;// Keep for automatic size calculation

	public ByteArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}

	@Override
	public ByteArrayVariable set(int objectIndex, ByteBuffer value) {
	    getParent().getBuffer().put(
		    logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	public ByteArrayVariable set(int objectIndex, int offsetInBytes,
		ByteBuffer value) {
	    getParent().getBuffer().put(
		    offsetInBytes + logicalByteOffsetWithinObject().intValue() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public ByteBuffer get(int objectIndex) {
	    throw new RuntimeException("Unimplemented.");
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen;
	}

	public void setAt(int objectIndex, int arrayIndex, byte value) {
	    getParent().getBuffer().put(
		    logicalByteOffsetWithinObject().intValue() + arrayIndex + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	}
    }// end ByteArrayVariable

    public static final class ShortArrayVariable extends
	    Variable<ShortBuffer, ShortArrayVariable> {
	private final int arrayLen;// Keep for automatic size calculation

	public ShortArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}

	@Override
	public ShortArrayVariable set(int objectIndex, ShortBuffer value) {
	    throw new RuntimeException("Unimplemented.");
	}

	public ByteArrayVariable set(int objectIndex, int offsetInBytes,
		ByteBuffer value) {
	    throw new RuntimeException("Unimplemented.");
	}

	@Override
	public ShortBuffer get(int objectIndex) {
	    throw new RuntimeException("Unimplemented.");
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen * 2;
	}

	public void setAt(int objectIndex, int arrayIndex, short value) {
	    getParent().getBuffer().putShort(
		    logicalByteOffsetWithinObject().intValue() + arrayIndex * 2 + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	}
    }// end ShortArrayVariable

    public static final class Double2FloatArrayVariable extends
	    Variable<double[], Double2FloatArrayVariable> {
	private int arrayLen = 0;

	public Double2FloatArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}

	@Override
	public Double2FloatArrayVariable set(int objectIndex, double[] value) {
	    for (int i = 0; i < arrayLen; i++) {
		getParent().getBuffer().putFloat(
			i * 4 + logicalByteOffsetWithinObject().intValue() + objectIndex
				* getParent().getObjectSizeInBytes(),
			(float) value[i]);
	    }
	    return this;
	}

	@Override
	public double[] get(int objectIndex) {
	    final double[] result = new double[arrayLen];
	    for (int i = 0; i < arrayLen; i++) {
		result[i] = getParent().getBuffer().getFloat(
			i * 4 + logicalByteOffsetWithinObject().intValue() + objectIndex
				* getParent().getObjectSizeInBytes());
	    }
	    return result;
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen * 4;
	}
    }// end Double2FloatArrayVariable

    /**
     * @return the buffer
     */
    public final PagedByteBuffer getBuffer() {
	return buffer;
    }

    /**
     * @param buffer
     *            the buffer to set
     */
    public final void setBuffer(PagedByteBuffer buffer) {
	this.buffer = buffer;
    }

    public final int getObjectSizeInBytes() {
	return objectSizeInBytes;
    }

    public final ByteAddress getPhysicalAddressInBytes(int objectIndex) {
	return new ByteAddress(buffer.logical2PhysicalAddressBytes(objectIndex
		* objectSizeInBytes));
    }

    public final double numObjectsPerPage() {
	return (double) PagedByteBuffer.PAGE_SIZE_BYTES
		/ (double) getObjectSizeInBytes();
    }

    public final int numPages() {
	return buffer.sizeInPages();
    }

    public final int logicalPage2PhysicalPage(int logicalPage) {
	return buffer.logicalPage2PhysicalPage(logicalPage);
    }

    /**
     * @return the reporter
     */
    public Reporter getReporter() {
        return reporter;
    }

    /**
     * @param reporter the reporter to set
     */
    public MemoryWindow setReporter(Reporter reporter) {
        this.reporter = reporter;
        return this;
    }
    
    public void compact(){
	getIndexPool().compact();
    }

    public String getDebugName() {
        return debugName;
    }

    public void setDebugName(String debugName) {
        this.debugName = debugName;
    }

    public int getMinFreeDelayMillis() {
        return minFreeDelayMillis;
    }

    public void setMinFreeDelayMillis(int minFreeDelayMillis) {
        this.minFreeDelayMillis = minFreeDelayMillis;
    }

    public int getMaxFreeDelayMillis() {
        return maxFreeDelayMillis;
    }

    public void setMaxFreeDelayMillis(int maxFreeDelayMillis) {
        this.maxFreeDelayMillis = maxFreeDelayMillis;
    }

    public MemoryWindowFreeingService getFreeingService() {
        return freeingService;
    }

    IndexPool getIndexPool() {
	if(indexPool == null)
	    indexPool = new IndexPool();
        return indexPool;
    }

    void setIndexPool(IndexPool indexPool) {
        this.indexPool = indexPool;
    }
    
    public MemoryWindow newContextWindow(){
	final Class<? extends MemoryWindow> thisClass = getClass();
	try{
	    final MemoryWindow result = (MemoryWindow)thisClass.newInstance();
	    result.initFields();
	    result.setIndexPool(getIndexPool());
	    result.setBuffer(getBuffer());
	    return result;
	}catch(Exception e){e.printStackTrace();}
	return null;
    }//end getContextWindow()
}// end ObjectWindow
