package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

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
    private TRFutureTask<TextureDescription>		fallbackTexture;
    public TextureManager(final TR tr){
	this.tr			= tr;
	subTextureWindow 	= new SubTextureWindow(tr);
	tocWindow 		= new TextureTOCWindow(tr);
	vqCodebookManager=tr.getThreadManager().submitToGL(new Callable<VQCodebookManager>(){
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
    
    private TRFutureTask<TextureDescription> defaultTriPipeTexture;
    
    public TRFutureTask<TextureDescription> getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	 defaultTriPipeTexture
	    	= new DummyTRFutureTask<TextureDescription>(
	    		new Texture(Texture.RGBA8FromPNG(LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),
	    			"Default TriPipe Texture (grayNoise)",tr));
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
    
    public TRFutureTask<TextureDescription> getFallbackTexture(){
	if(fallbackTexture!=null)return fallbackTexture;
	Texture t;
	t = new Texture(
		Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/fallbackTexture.png")),
		"Fallback",tr);
	fallbackTexture = new DummyTRFutureTask<TextureDescription>(t);
	return fallbackTexture;
    }//end getFallbackTexture()
    
    public TRFutureTask<TextureDescription> solidColor(Color color) {
	BufferedImage img = new BufferedImage(64, 64,
		BufferedImage.TYPE_INT_RGB);
	Graphics g = img.getGraphics();
	g.setColor(color);
	g.fillRect(0, 0, 64, 64);
	g.dispose();
	final DummyTRFutureTask<TextureDescription> result = new DummyTRFutureTask<TextureDescription>(new Texture(img,
		"Solid color " + color,tr));
	return result;
    }//end solidColor(...)
    
    public TextureTOCWindow getTOCWindow(){
	return tocWindow;
    }
}//end TextureSystem
