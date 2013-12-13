package org.jtrfp.trcl.gpu;

import javax.media.opengl.GL3;

public enum MemoryUsageHint
	{
	DymamicDraw(GL3.GL_DYNAMIC_DRAW),
	StaticDraw(GL3.GL_STATIC_DRAW),
	StreamDraw(GL3.GL_STREAM_DRAW),
	StreamRead(GL3.GL_STREAM_READ),
	StaticRead(GL3.GL_STATIC_READ),
	DynamicRead(GL3.GL_DYNAMIC_READ),
	DynamicCopy(GL3.GL_DYNAMIC_COPY),
	StaticCopy(GL3.GL_STATIC_COPY),
	StreamCopy(GL3.GL_STREAM_COPY);
	
	private final int glEnum;
	private MemoryUsageHint(int glEnum)
		{this.glEnum=glEnum;}
	
	public int getGLEnumInt(){return glEnum;}
	}
