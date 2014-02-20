package org.jtrfp.trcl.mem;

import java.nio.ByteBuffer;

import org.jtrfp.trcl.core.IndexPool;

public final class PagedByteBuffer  implements IByteBuffer, Resizeable{
    private final 	ByteBuffer [] 	intrinsic;//Should be size=1. Serves as an indirect reference.
    static final 	int 		PAGE_SIZE_BYTES=1536;//Should be enforced since a GPU Vertex Block is 1536 bytes
    private 		int [] 		pageTable;//Using array since performance is crucial
    private final IndexPool 		pageIndexPool;
    
    PagedByteBuffer(ByteBuffer [] intrinsic, IndexPool pageIndexPool, int initialSizeInBytes){
	this.intrinsic=intrinsic;
	this.pageIndexPool=pageIndexPool;
	final int sizeInPages = sizeInPages(initialSizeInBytes);
	pageTable = new int[sizeInPages];
	for(int i=0; i<sizeInPages; i++){
	    pageTable[i]=pageIndexPool.pop();
	}//end for(sizeInPages)
    }//end constructor
    
    private static int sizeInPages(int sizeInBytes){
	return index2Page(sizeInBytes)+1;
    }
    private static int index2Page(int indexInBytes){
	return indexInBytes/PAGE_SIZE_BYTES;
    }
    private static int pageModulus(int indexInBytes){
	return indexInBytes%PAGE_SIZE_BYTES;
    }
    private final int logicalIndex2PhysicalIndex(int logicalIndexInBytes){
	return pageTable[index2Page(logicalIndexInBytes)]+pageModulus(logicalIndexInBytes);
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
	intrinsic[0].putShort(logicalIndex2PhysicalIndex(indexInBytes), val);
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
}//end PageByteBuffer
