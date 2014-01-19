package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;

public interface ReallocatableGLMemory extends GLMemory
	{
	public void reallocate(ByteBuffer newData);
	public void reallocate(int sizeInBytes);
	public void putShort(int byteOffset, short val);
	}
