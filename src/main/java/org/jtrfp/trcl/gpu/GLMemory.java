package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;

public interface GLMemory
	{
	public ByteBuffer map();
	public void unmap();
	public void bind();
	public void unbind();
	public int getSizeInBytes();
	public void setUsageHint(MemoryUsageHint hint);
	public MemoryUsageHint getUsageHint();
	public ByteBuffer getDuplicateReferenceOfUnderlyingBuffer();
	public void bindToUniform(int textureUnit, GLProgram program, GLUniform uniform);
	}
