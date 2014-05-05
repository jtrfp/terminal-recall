package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.LineSegment;


/**
 * 
 * @author Chuck Ritola
 *
 */

public class TextureManager {
    private final TR 				tr;
    private final SubTextureWindow 		subTextureWindow;
    private final TextureTOCWindow 		tocWindow;
    public final TRFuture<VQCodebookManager>	vqCodebookManager;
    private Future<TextureDescription>		fallbackTexture;
    public TextureManager(final TR tr){
	this.tr			= tr;
	subTextureWindow 	= new SubTextureWindow(tr);
	tocWindow 		= new TextureTOCWindow(tr);
	vqCodebookManager=tr.getThreadManager().enqueueGLOperation(new Callable<VQCodebookManager>(){
	    @Override
	    public VQCodebookManager call() throws Exception {
		return new VQCodebookManager(tr);
	    }});
	
    }//end constructor
    
    public Texture newTexture(ByteBuffer imageRGB8, String debugName){
	return new Texture(imageRGB8,debugName,tr);
    }
    public Texture newTexture(BufferedImage img, String debugName){
	return new Texture(img,debugName,tr);
    }
    public SubTextureWindow getSubTextureWindow(){
	return subTextureWindow;
    }
    
    private Future<TextureDescription> defaultTriPipeTexture;
    
    public Future<TextureDescription> getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	 defaultTriPipeTexture
	    	= new DummyFuture<TextureDescription>(
	    		new Texture(Texture.RGBA8FromPNG(LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),
	    			"Default TriPipe Texture (grayNoise)",tr));
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
    
    public Future<TextureDescription> getFallbackTexture(){
	if(fallbackTexture!=null)return fallbackTexture;
	Texture t;
	t = new Texture(
		Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/fallbackTexture.png")),
		"Fallback",tr);
	fallbackTexture = new DummyFuture<TextureDescription>(t);
	return fallbackTexture;
    }//end getFallbackTexture()
    
    public Future<TextureDescription> solidColor(Color color) {
	BufferedImage img = new BufferedImage(64, 64,
		BufferedImage.TYPE_INT_RGB);
	Graphics g = img.getGraphics();
	g.setColor(color);
	g.fillRect(0, 0, 64, 64);
	g.dispose();
	final DummyFuture<TextureDescription> result = new DummyFuture<TextureDescription>(new Texture(img,
		"Solid color " + color,tr));
	return result;
    }//end solidColor(...)
    
    public TextureTOCWindow getTOCWindow(){
	return tocWindow;
    }
}//end TextureSystem
