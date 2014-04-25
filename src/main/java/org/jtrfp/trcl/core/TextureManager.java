package org.jtrfp.trcl.core;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;

import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.TextureDescription;


/**
 * INCOMPLETE - DO NOT USE
 * @author Chuck Ritola
 *
 */

public class TextureManager {
    private final TR tr;
    private final TextureCodebookWindow 	codebookWindow;
    private final TextureTileWindow 		tileWindow;
    private final TextureTOCWindow 		tocWindow;
    private final TextureMipmapTOCWindow 	mipmapTOCWindow;
    private final TextureTileManager		textureTileManager;
    public TextureManager(TR tr){
	this.tr		= tr;
	codebookWindow	= new TextureCodebookWindow(tr);
	tileWindow 	= new TextureTileWindow(tr);
	tocWindow 	= new TextureTOCWindow(tr);
	mipmapTOCWindow = new TextureMipmapTOCWindow(tr);
	textureTileManager = new TextureTileManager(tr.getGPU());
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
    
    private static Future<TextureDescription> defaultTriPipeTexture;
    
    public Future<TextureDescription> getDefaultTriPipeTexture(){
	if(defaultTriPipeTexture==null){
	 defaultTriPipeTexture
	    	= new DummyFuture<TextureDescription>(
	    		new Texture(Texture.RGBA8FromPNG(LineSegment.class.getResourceAsStream("/grayNoise32x32.png")),
	    			"Default TriPipe Texture (grayNoise)",tr));
	}
	return defaultTriPipeTexture;
    }//end getDefaultTriPipeTexture()
}//end TextureSystem
