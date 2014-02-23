package org.jtrfp.trcl.mem;

import java.lang.reflect.Field;

import org.jtrfp.trcl.core.IndexPool;

public abstract class MemoryWindow {
    private IByteBuffer buffer;
    private final int objectSizeInBytes;
    private final IndexPool indexPool = new IndexPool();
    //TODO: GrowthBehavior to resize the buffer.
    
    protected MemoryWindow(int objectSizeInBytes){
	this.objectSizeInBytes=objectSizeInBytes;
    }//end constructor
    
    protected final void init(){
	final Class thisClass = getClass();
	for(Field f:getClass().getFields()){
	    if(Variable.class.isAssignableFrom(f.getType())){
		System.out.println("FIELD: "+f.getName());
		try{final Variable<?,?> var = (Variable<?,?>)f.get(this);
		    var.initialize(this);}
		catch(IllegalAccessException e){e.printStackTrace();}
	    }//end if(Variable)
	}//end for(fields)
    }//end init()
    
    public final int create(){
	return indexPool.pop();
    }
    public final int getNumObjects(){
	return indexPool.getMaxCapacity();
    }
    
    public static abstract class Variable<TYPE, THIS_CLASS extends Variable>{
	private MemoryWindow parent;
	private int byteOffset;
	
	void initialize(MemoryWindow parent){
	    this.parent=parent;
	}
	public abstract THIS_CLASS set(int objectIndex, TYPE value);
	public abstract TYPE get(int objectIndex);
	/**
	 * @return the parent
	 */
	protected final MemoryWindow getParent() {
	    return parent;
	}
	public final THIS_CLASS byteOffset(int off){
	    this.byteOffset=off;
	    return (THIS_CLASS)this;
	}
	protected final int byteOffset(){
	    return byteOffset;}
    }//end Property
    
    public static final class IntVariable extends Variable<Integer,IntVariable>{
	@Override
	public IntVariable set(int objectIndex, Integer value) {
	    getParent().getBuffer().putInt(byteOffset()+objectIndex*getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Integer get(int objectIndex) {
	    return getParent().getBuffer().getInt(byteOffset()+objectIndex*getParent().getObjectSizeInBytes());
	}
	
    }//end IntVariable
    
    public static final class ByteVariable extends Variable<Byte, ByteVariable>{

	@Override
	public ByteVariable set(int objectIndex, Byte value) {
	    getParent().getBuffer().put(byteOffset()+objectIndex*getParent().getObjectSizeInBytes(), value);
	    return this;
	}

	@Override
	public Byte get(int objectIndex) {
	    return getParent().getBuffer().get(byteOffset()+objectIndex*getParent().getObjectSizeInBytes());
	}
    }//end ByteVariable
    
    public static final class Double2FloatArrayVariable extends Variable<double [],Double2FloatArrayVariable>{
	private int arrayLen=0;
	public Double2FloatArrayVariable(int arrayLen){this.arrayLen=arrayLen;}
	@Override
	public Double2FloatArrayVariable set(int objectIndex, double[] value) {
	    for(int i=0; i<arrayLen; i++){
		getParent().getBuffer().putFloat(i*4+byteOffset()+objectIndex*getParent().getObjectSizeInBytes(),(float)value[i]);
	    }
	    return this;
	}
	@Override
	public double[] get(int objectIndex) {
	    final double [] result = new double[arrayLen];
	    for(int i=0; i<arrayLen; i++){
		result[i]=getParent().getBuffer().getFloat(i*4+byteOffset()+objectIndex*getParent().getObjectSizeInBytes());
	    }
	    return result;
	}
    }//end Double2FloatArrayVariable

    /**
     * @return the buffer
     */
    public final IByteBuffer getBuffer() {
        return buffer;
    }

    /**
     * @param buffer the buffer to set
     */
    public final void setBuffer(IByteBuffer buffer) {
        this.buffer = buffer;
    }
    public final int getObjectSizeInBytes(){return objectSizeInBytes;}
    public final int getPhysicalAddressInBytes(int objectIndex){return buffer.logical2PhysicalAddressBytes(objectIndex*objectSizeInBytes);}
}//end ObjectWindow
