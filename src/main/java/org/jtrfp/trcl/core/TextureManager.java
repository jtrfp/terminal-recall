package org.jtrfp.trcl.core;

import java.awt.Dimension;

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
	this.tr=tr;
	codebookWindow	= new TextureCodebookWindow(tr);
	tileWindow 	= new TextureTileWindow(tr);
	tocWindow 	= new TextureTOCWindow(tr);
	mipmapTOCWindow = new TextureMipmapTOCWindow(tr);
	textureTileManager = new TextureTileManager(tr.getGPU());
    }//end constructor
    
    public Gen2Texture newTexture(int width, int height){
	final Gen2Texture texture = new Gen2Texture(this);
	texture.setSize(new Dimension(width,height));
	return texture;
    }//end newTexture

    /**
     * @return the textureTileManager
     */
    public TextureTileManager getTextureTileManager() {
        return textureTileManager;
    }
}//end TextureSystem
