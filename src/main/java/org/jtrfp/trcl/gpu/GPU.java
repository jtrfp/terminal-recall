package org.jtrfp.trcl.gpu;

import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.concurrent.Callable;

import javax.media.opengl.DebugGL3;
import javax.media.opengl.GL;
import javax.media.opengl.GL3;
import javax.media.opengl.awt.GLCanvas;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRFutureTask;
import org.jtrfp.trcl.core.TextureManager;
import org.jtrfp.trcl.mem.MemoryManager;

public class GPU{
    	public static final int 			GPU_VERTICES_PER_BLOCK = 96;
    	public static final int 			BYTES_PER_VEC4 = 16;
	
	private ByteOrder 				byteOrder;
	private final TR 				tr;
	private GL3 					gl;
	public final TRFutureTask<MemoryManager> 	memoryManager;
	public final TRFutureTask<TextureManager> 	textureManager;
	
	public GPU(final TR tr){
	    this.tr=tr;
	    memoryManager = new TRFutureTask<MemoryManager>(tr,new Callable<MemoryManager>(){

		@Override
		public MemoryManager call() throws Exception {
		    Thread.currentThread().setName("MemoryManager constructor.");
		    return new MemoryManager(GPU.this);
		}
		
	    });
	    tr.getThreadManager().threadPool.submit(memoryManager);
	    textureManager = new TRFutureTask<TextureManager>(tr,new Callable<TextureManager>(){

		@Override
		public TextureManager call() throws Exception {
		    Thread.currentThread().setName("TextureManager constructor.");
		    return new TextureManager(tr);
		}
		
	    });tr.getThreadManager().threadPool.submit(textureManager);
	}//end constructor
	
	public int glGet(int key){
		IntBuffer buf = IntBuffer.wrap(new int[1]);
		gl.glGetIntegerv(key, buf);
		return buf.get(0);
		}
	
	public String glGetString(int key)
		{return gl.glGetString(key);}
	
	public ByteOrder getByteOrder(){
		if(byteOrder==null)
			{byteOrder = System.getProperty("sun.cpu.endian").contentEquals("little")?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN;}
		return byteOrder;
		}
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
			final GLCanvas canvas = tr.getRootWindow().getCanvas();
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
	public GLFrameBuffer newFrameBuffer() {
	    return new GLFrameBuffer(gl);
	}
	public GLRenderBuffer newRenderBuffer() {
	    return new GLRenderBuffer(gl);
	}
	}//end GPU
