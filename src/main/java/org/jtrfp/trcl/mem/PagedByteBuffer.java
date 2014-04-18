package org.jtrfp.trcl.mem;

import java.nio.ByteBuffer;

import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.pool.IndexPool;

public final class PagedByteBuffer  implements IByteBuffer, Resizeable{
    private final 	ByteBuffer [] 	intrinsic;//Should be size=1. Serves as an indirect reference.
    public static final int 		PAGE_SIZE_BYTES=GPU.BYTES_PER_VEC4*GPU.GPU_VERTICES_PER_BLOCK*1;//Anchored to number of triangle vertices per block
    private 		int [] 		pageTable;//Using array since performance is crucial
    private final 	IndexPool 	pageIndexPool;
    private final 	String		debugName;
    
    PagedByteBuffer(ByteBuffer [] intrinsic, IndexPool pageIndexPool, int initialSizeInBytes, String debugName){
	this.intrinsic=intrinsic;
	this.pageIndexPool=pageIndexPool;
	this.debugName=debugName;
	final int sizeInPages = sizeInPages(initialSizeInBytes);
	pageTable = new int[sizeInPages];
	for(int i=0; i<sizeInPages; i++){
	    pageTable[i]=pageIndexPool.pop();
	}//end for(sizeInPages)
    }//end constructor
    
    public int sizeInPages(){
	return pageTable.length;
    }
    public int logicalPage2PhysicalPage(int logicalPage){
	return pageTable[logicalPage];
    }
    
    private static int sizeInPages(int sizeInBytes){
	return index2Page(sizeInBytes)+1;
    }
    private static int index2Page(int indexInBytes){
	return indexInBytes/PAGE_SIZE_BYTES;
    }
    private static int pageModulus(int indexInBytes){
	return indexInBytes%PAGE_SIZE_BYTES;
    }
    private int logicalIndex2PhysicalIndex(int logicalIndexInBytes){
	return (PAGE_SIZE_BYTES*pageTable[index2Page(logicalIndexInBytes)])+pageModulus(logicalIndexInBytes);
    }

    @Override
    public void resize(int newSizeInBytes) {
	final int newNumPages=sizeInPages(newSizeInBytes);
	final int pageNumDelta=newNumPages-pageTable.length;
	if(pageNumDelta==0)return;
	int [] newTable;
	if(pageNumDelta>0){	//GROW
	    newTable = new int[newNumPages];
	    System.arraycopy(pageTable, 0, newTable, 0, pageTable.length);
	    for(int i=pageTable.length; i<newNumPages; i++){
		newTable[i]=pageIndexPool.pop();
	    }//end for(new pages)
	}else{			//SHRINK
	    newTable = new int[newNumPages];
	    System.arraycopy(pageTable, 0, newTable, 0, newTable.length);
	    for(int i=newTable.length; i<pageTable.length; i++){
		pageIndexPool.free(pageTable[i]);
	    }//end for(new pages)
	}//end if(pageNumDelta...)
	pageTable = newTable;
    }//end resize()
    
    private void deallocate(){
	for(int i:pageTable){
	    pageIndexPool.free(i);
	}//end for(pageTable)
    }//end deallocate()
    
    /**
     * Must notify the page IndexPool that this buffer is being forgotten such that its pages may be freed.
     */
    @Override
    public void finalize() throws Throwable{
	deallocate();
	super.finalize();
    }//end finalize()

    @Override
    public IByteBuffer putShort(int indexInBytes, short val) {
	int index=logicalIndex2PhysicalIndex(indexInBytes);
	intrinsic[0].putShort(index, val);
	return this;
    }

    @Override
    public IByteBuffer put(int indexInBytes, byte val) {
	intrinsic[0].put(logicalIndex2PhysicalIndex(indexInBytes), val);
	return this;
    }

    @Override
    public byte get(int indexInBytes) {
	return intrinsic[0].get(logicalIndex2PhysicalIndex(indexInBytes));
    }

    @Override
    public short getShort(int indexInBytes) {
	return intrinsic[0].getShort(logicalIndex2PhysicalIndex(indexInBytes));
    }

    @Override
    public IByteBuffer putFloat(int indexInBytes, float val) {
	intrinsic[0].putFloat(logicalIndex2PhysicalIndex(indexInBytes), val);
	return this;
    }

    @Override
    public IByteBuffer put(int startIndexInBytes, ByteBuffer src) {
	if(logicalIndex2PhysicalIndex(startIndexInBytes)==0)throw new RuntimeException("ZERO!");
	intrinsic[0].position(logicalIndex2PhysicalIndex(startIndexInBytes));
	intrinsic[0].put(src);
	return this;
    }

    @Override
    public IByteBuffer putInt(int indexInBytes, int val) {
	intrinsic[0].putInt(logicalIndex2PhysicalIndex(indexInBytes),val);
	return this;
    }

    @Override
    public int logical2PhysicalAddressBytes(int logicalAddressInBytes) {
	return logicalIndex2PhysicalIndex(logicalAddressInBytes);
    }

    @Override
    public double getFloat(int posInBytes) {
	return intrinsic[0].getFloat(logicalIndex2PhysicalIndex(posInBytes));
    }

    @Override
    public Integer getInt(int posInBytes) {
	return intrinsic[0].getInt(logicalIndex2PhysicalIndex(posInBytes));
    }
    
    @Override
    public String toString(){
	return "PagedByteBuffer '"+debugName+"' "+" hash="+hashCode();
    }
}//end PageByteBuffer
