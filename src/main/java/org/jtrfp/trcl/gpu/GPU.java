package org.jtrfp.trcl.gpu;

import java.awt.Component;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.media.opengl.DebugGL3;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.mem.MemoryManager;

public class GPU{
	static {GLProfile.initSingleton();}
	private final GLProfile glProfile = GLProfile.get(GLProfile.GL2GL3);
	private final GLCapabilities capabilities = new GLCapabilities(glProfile);
	private final GLCanvas canvas = new GLCanvas(capabilities);
	private ByteOrder byteOrder;
	private final TR tr;
	private GL3 gl;
	private MemoryManager memoryManager;
	
	public GPU(TR tr){
	    this.tr=tr;
	    addGLEventListener(new GLEventListener(){

		@Override
		public void init(GLAutoDrawable drawable) {
		    GPU.this.memoryManager=new MemoryManager(GPU.this);
		}

		@Override
		public void dispose(GLAutoDrawable drawable) {}

		@Override
		public void display(GLAutoDrawable drawable) {}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y,
			int width, int height) {}
		
	    });
	}
	/*
	public GL3 takeGL(){
		gl=getGl();
		if(!gl.getContext().isCurrent())gl.getContext().makeCurrent();
		return gl;
		}
	public void releaseGL()
		{if(gl.getContext().isCurrent())gl.getContext().release();}
	*/
	public GLCapabilities getCapabilities(){return capabilities;}
	
	public int glGet(int key){
		IntBuffer buf = IntBuffer.wrap(new int[1]);
		gl.glGetIntegerv(key, buf);
		return buf.get(0);
		}
	
	public String glGetString(int key)
		{return gl.glGetString(key);}
	
	public Component getComponent(){return canvas;}
	
	public ByteOrder getByteOrder(){
		if(byteOrder==null)
			{byteOrder = System.getProperty("sun.cpu.endian").contentEquals("little")?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN;}
		return byteOrder;
		}
	public void addGLEventListener(GLEventListener l)
		{canvas.addGLEventListener(l);}
	public GLTexture newTexture()
		{return new GLTexture(this);}
	public int newTextureID(){
		IntBuffer ib= IntBuffer.allocate(1);
		gl.glGenTextures(1, ib);
		ib.clear();
		return ib.get();
		}
	public GLFragmentShader newFragmentShader()
		{return new GLFragmentShader(this);}
	public GLVertexShader newVertexShader()
		{return new GLVertexShader(this);}
	public GLProgram newProgram()
		{return new GLProgram(this);}
	public GL3 getGl(){
		if(gl==null)
			{GL gl1;
			//In case GL is not ready, wait and try again.
			try{for(int i=0; i<10; i++){gl1=canvas.getGL();if(gl1!=null)
				{gl=gl1.getGL3();
				canvas.setGL(gl=new DebugGL3(gl));
				break;
				} Thread.sleep(2000);}}
			catch(InterruptedException e){e.printStackTrace();}
			}//end if(!null)
		return gl;
		}
	
	public TR getTr() {
	    	return tr;
		}
	/**
	 * @return the memoryManager
	 */
	public MemoryManager getMemoryManager() {
	    return memoryManager;
	}
	}//end GPU
