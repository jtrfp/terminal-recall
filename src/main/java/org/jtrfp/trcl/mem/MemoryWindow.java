package org.jtrfp.trcl.mem;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.jtrfp.trcl.core.IndexPool;
import org.jtrfp.trcl.core.IndexPool.GrowthBehavior;
import org.jtrfp.trcl.core.TR;

public abstract class MemoryWindow {
    private PagedByteBuffer buffer;
    private int objectSizeInBytes;
    private final IndexPool indexPool = new IndexPool();
    private String debugName;
    private TR tr;

    protected final void init(TR tr, String debugName) {
	this.debugName=debugName;
	this.tr=tr;
	int byteOffset = 0;
	for (Field f : getClass().getFields()) {
	    if (Variable.class.isAssignableFrom(f.getType())) {
		try {
		    final Variable<?, ?> var = (Variable<?, ?>) f.get(this);
		    var.initialize(this);
		    var.byteOffset(byteOffset);
		    byteOffset += var.getSizeInBytes();
		} catch (IllegalAccessException e) {
		    e.printStackTrace();
		}
	    }// end if(Variable)
	}// end for(fields)
	objectSizeInBytes = byteOffset;
	setBuffer(tr.getGPU().getMemoryManager().createPagedByteBuffer(PagedByteBuffer.PAGE_SIZE_BYTES, "MemoryWindow "+this.getClass().getName()));
	indexPool.setGrowthBehavior(new GrowthBehavior(){
	    @Override
	    public int grow(int previousMaxCapacity) {
		//Grow by one page
		final int newSizeInObjects=previousMaxCapacity+PagedByteBuffer.PAGE_SIZE_BYTES/getObjectSizeInBytes();
		getBuffer().resize(newSizeInObjects*getObjectSizeInBytes());
		MemoryWindow.this.tr.getReporter().report("org.jtrfp.trcl.mem.MemoryWindow."+
			MemoryWindow.this.debugName+".sizeInObjects", newSizeInObjects);
		
		for(int p=0; p<MemoryWindow.this.numPages(); p++){
		    MemoryWindow.this.tr.getReporter().report("org.jtrfp.trcl.mem.MemoryWindow."+
				MemoryWindow.this.debugName+".page"+p, String.format("%08x", MemoryWindow.this.logicalPage2PhysicalPage(p)*PagedByteBuffer.PAGE_SIZE_BYTES));
		}
		MemoryWindow.this.tr.getReporter().report("org.jtrfp.trcl.mem.MemoryWindow."+
			MemoryWindow.this.debugName+".sizeInObjects", newSizeInObjects);
		return newSizeInObjects;
	    }});
	buffer.resize(getObjectSizeInBytes()*getNumObjects());
	for(int p=0; p<numPages(); p++){
	    MemoryWindow.this.tr.getReporter().report("org.jtrfp.trcl.mem.MemoryWindow."+
			MemoryWindow.this.debugName+".page"+p, String.format("%08x", MemoryWindow.this.logicalPage2PhysicalPage(p)*PagedByteBuffer.PAGE_SIZE_BYTES));}
    }// end init()
    
    public final int create() {
	return indexPool.pop();
    }

    public final int getNumObjects() {
	return indexPool.getMaxCapacity();
    }

    public static abstract class Variable<TYPE, THIS_CLASS extends Variable> {
	private MemoryWindow parent;
	private int byteOffset;

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

	private final THIS_CLASS byteOffset(int off) {
	    this.byteOffset = off;
	    return (THIS_CLASS) this;
	}

	protected final int byteOffset() {
	    return byteOffset;
	}

	protected abstract int getSizeInBytes();
    }// end Property

    public static final class IntVariable extends
	    Variable<Integer, IntVariable> {
	@Override
	public IntVariable set(int objectIndex, Integer value) {
	    getParent().getBuffer().putInt(
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Integer get(int objectIndex) {
	    return getParent().getBuffer().getInt(
		    byteOffset() + objectIndex
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
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}
	
	public ByteVariable set(int objectIndex, byte value) {
	    getParent().getBuffer().put(
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Byte get(int objectIndex) {
	    return getParent().getBuffer().get(
		    byteOffset() + objectIndex
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
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}
	
	public ShortVariable set(int objectIndex, short value) {
	    getParent().getBuffer().putShort(
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Short get(int objectIndex) {
	    return getParent().getBuffer().getShort(
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes());
	}

	@Override
	protected int getSizeInBytes() {
	    return 2;
	}
    }// end ShortVariable

    public static final class ByteArrayVariable extends
	    Variable<ByteBuffer, ByteArrayVariable> {
	private int arrayLen = 0;// Keep for automatic size calculation

	public ByteArrayVariable(int arrayLen) {
	    this.arrayLen = arrayLen;
	}

	@Override
	public ByteArrayVariable set(int objectIndex, ByteBuffer value) {
	    getParent().getBuffer().put(
		    byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	public ByteArrayVariable set(int objectIndex, int offsetInBytes,
		ByteBuffer value) {
	    getParent().getBuffer().put(
		    offsetInBytes + byteOffset() + objectIndex
			    * getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public ByteBuffer get(int objectIndex) {
	    return null;// unimplemented
	}

	@Override
	protected int getSizeInBytes() {
	    return arrayLen;
	}
    }// end Double2FloatArrayVariable

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
			i * 4 + byteOffset() + objectIndex
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
			i * 4 + byteOffset() + objectIndex
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

    public final int getPhysicalAddressInBytes(int objectIndex) {
	return buffer.logical2PhysicalAddressBytes(objectIndex
		* objectSizeInBytes);
    }

    public final double numObjectsPerPage() {
	return (double)PagedByteBuffer.PAGE_SIZE_BYTES / (double)getObjectSizeInBytes();
    }

    public final int numPages() {
	return buffer.sizeInPages();
    }

    public final int logicalPage2PhysicalPage(int logicalPage) {
	return buffer.logicalPage2PhysicalPage(logicalPage);
    }
}// end ObjectWindow
