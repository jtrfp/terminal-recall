package org.jtrfp.trcl.core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.LineSegment;


/**
 * INCOMPLETE - DO NOT USE
 * @author Chuck Ritola
 *
 */

public class TextureManager {
    private final TR 				tr;
    private final TextureTileWindow 		tileWindow;
    private final TextureTOCWindow 		tocWindow;
    private final TextureMipmapTOCWindow 	mipmapTOCWindow;
    private final TextureTileManager		textureTileManager;
    private final Future<TextureDescription>	fallbackTexture;
    public TextureManager(TR tr){
	this.tr		= tr;
	tileWindow 	= new TextureTileWindow(tr);
	tocWindow 	= new TextureTOCWindow(tr);
	mipmapTOCWindow = new TextureMipmapTOCWindow(tr);
	textureTileManager = new TextureTileManager(tr.getGPU());
	Texture t;
	t = new Texture(
		Texture.RGBA8FromPNG(Texture.class
			.getResourceAsStream("/fallbackTexture.png")),
		"Fallback",tr);
	fallbackTexture = new DummyFuture<TextureDescription>(t);
    }//end constructor
    
    public Gen2Texture newGen2Texture(int width, int height){
	final Gen2Texture texture = new Gen2Texture(this);
	texture.setSize(new Dimension(width,height));
	return texture;
    }//end newTexture
    
    public Texture newTexture(ByteBuffer imageRGB8, String debugName){
	return new Texture(imageRGB8,debugName,tr);
    }
    public Texture newTexture(BufferedImage img, String debugName){
	return new Texture(img,debugName,tr);
    }

    /**
     * @return the textureTileManager
     */
    public TextureTileManager getTextureTileManager() {
        return textureTileManager;
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
	return fallbackTexture;
    }//end getFallbackTexture()
}//end TextureSystem
