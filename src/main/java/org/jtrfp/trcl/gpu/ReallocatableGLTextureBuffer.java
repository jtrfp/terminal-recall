package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;

import javax.media.opengl.GL2;

public class ReallocatableGLTextureBuffer implements ReallocatableGLMemory
	{
	private GLTextureBuffer buffer;
	private GPU gpu;
	private MemoryUsageHint usageHint;
	
	ReallocatableGLTextureBuffer(GPU gpu)
		{this.gpu=gpu;buffer=new GLTextureBuffer(1,gpu);}
	
	@Override
	public ByteBuffer map()
		{
		buffer.map(gpu.getGl());
		return buffer.getDuplicateReferenceOfUnderlyingBuffer();
		}

	@Override
	public int getSizeInBytes()
		{return buffer.getUnderlyingBuffer().capacity();}

	@Override
	public void setUsageHint(MemoryUsageHint hint)
		{buffer.setUsageHint((usageHint=hint).getGLEnumInt());}

	@Override
	public MemoryUsageHint getUsageHint()
		{return usageHint;}

	@Override
	public void reallocate(ByteBuffer newData)
		{buffer.free(gpu.getGl());
		buffer = new GLTextureBuffer(newData.capacity(),gpu);
		ByteBuffer bb = map();
		bb.rewind();
		bb.put(newData);
		unmap();
		}

	@Override
	public void unmap()
		{buffer.unmap(gpu.getGl());}

	@Override
	public void bind()
		{buffer.bind((GL2)gpu.getGl());}

	@Override
	public void unbind()
		{buffer.unbind((GL2)gpu.getGl());}

	@Override
	public ByteBuffer getDuplicateReferenceOfUnderlyingBuffer()
		{
		return buffer.getDuplicateReferenceOfUnderlyingBuffer();
		}

	@Override
	public void bindToUniform(int textureUnit, GLProgram program,
			GLUniform uniform)
		{
		buffer.bindToUniform(gpu.getGl(), textureUnit, program, uniform);
		}

	@Override
	public void reallocate(int sizeInBytes)
		{
		buffer.free(gpu.getGl());
		buffer = new GLTextureBuffer(sizeInBytes,gpu);
		}

	}//end ReallocatableGLTextureBuffer
