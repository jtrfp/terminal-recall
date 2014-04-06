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
    public TextureManager(TR tr){
	this.tr=tr;
	codebookWindow	= new TextureCodebookWindow(tr);
	tileWindow 	= new TextureTileWindow(tr);
	tocWindow 	= new TextureTOCWindow(tr);
	mipmapTOCWindow = new TextureMipmapTOCWindow(tr);
    }//end constructor
    
    public Gen2Texture newTexture(int width, int height){
	final Gen2Texture texture = new Gen2Texture(this);
	texture.setSize(new Dimension(width,height));
	return texture;
    }//end newTexture
}//end TextureSystem
