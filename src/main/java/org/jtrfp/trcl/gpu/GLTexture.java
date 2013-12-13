package org.jtrfp.trcl.gpu;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.glu.gl2.GLUgl2;

public class GLTexture
	{
	private final GPU gpu;
	private final int textureID;
	private int rawSideLength;
	GLTexture(GPU gpu)
		{
		System.out.println("Creating GL Texture...");
		this.gpu=gpu;
		textureID=gpu.newTextureID();
		//Setup the empty rows
		System.out.println("...Done.");
		}//end constructor
	
	/**
	 * Takes a square texture in RGBA 8888 format. Automatically determines dimensions from buffer size.
	 * @param buf	Directly-allocated buffer containing the image data.
	 * @since Dec 11, 2013
	 */
	public void setTextureImageRGBA(ByteBuffer buf)
		{
		rawSideLength = (int)Math.sqrt(buf.capacity()/4);
		buf.rewind();
		System.out.println("Creating a new OpenGL texture for megatexture...");
		
		System.out.println("\t...Done.");
		System.out.println("Uploading megatexture to OpenGL...");
		
		GLUgl2 glu = new GLUgl2();
		GL3 gl = gpu.getGl();
		System.out.println("glu: "+glu.getCurrentGL()+" gl: "+gl);
		gl.glBindTexture(GL3.GL_TEXTURE_2D, textureID);
		FloatBuffer isoSize = FloatBuffer.wrap(new float[]{0});
		gl.glGetFloatv(GL3.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, isoSize);
		System.out.println("Isotropy limit: "+isoSize.get(0));
		gl.glTexParameterf(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAX_ANISOTROPY_EXT, isoSize.get(0));
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_REPEAT);
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_REPEAT);
		/*gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MAG_FILTER, 
		                   GL3.GL_LINEAR_MIPMAP_LINEAR);*/
		gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, 
		                   GL3.GL_LINEAR_MIPMAP_LINEAR);
		System.out.println("Uploading texture...");
		glu.gluBuild2DMipmaps( GL3.GL_TEXTURE_2D, GL3.GL_RGBA4, rawSideLength, rawSideLength, 
				GL3.GL_RGBA, GL3.GL_UNSIGNED_BYTE, buf);
		System.out.println("\t...Done.");
		}
	
	public void delete()
		{
		GL3 gl = gpu.takeGL();
		gl.glBindTexture(GL3.GL_TEXTURE_2D, textureID);
		gl.glDeleteTextures(1, IntBuffer.wrap(new int[]{textureID}));
		gpu.releaseGL();
		}

	int getTextureID()
		{return textureID;}
	
	public static void specifyTextureUnit(GL3 gl,int unitNumber)
		{gl.glActiveTexture(GL2.GL_TEXTURE0+unitNumber);}
	
	public void bind(GL3 gl)
		{gl.glBindTexture(GL2.GL_TEXTURE_2D, getTextureID());}
	
	public int getCurrentSideLength(){return rawSideLength;}
	}//GLTexture
