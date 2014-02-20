package org.jtrfp.trcl.mem;

/**
 * ByteBuffer cannot (legitimately) be extended so a new interface must be constructed to create what's effectively
 * a crude ByteBuffer. This is used for utilities like the PagingByteBuffer, however other buffers may implement this 
 * interface for interoperability.
 * @author Chuck Ritola
 *
 */

public interface IByteBuffer {
    public IByteBuffer putShort(int indexInBytes,short val);
    public IByteBuffer put(int indexInBytes,byte val);
    public byte get(int indexInBytes);
    public short getShort(int indexInBytes);
}//end IByteBuffer
