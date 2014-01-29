/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.media.opengl.GL3;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jtrfp.trcl.gpu.GLTexture;
import org.jtrfp.trcl.gpu.GPU;

public class Texture implements TextureDescription
	{
	TextureTreeNode nodeForThisTexture;
	private Color averageColor;
	private static double pixelSize=.7/4096.; //TODO: This is a kludge; doesn't scale with megatexture
	private static TextureTreeNode rootNode=null;
	private static GLTexture globalTexture;
	public static final List<Future<TextureDescription>> texturesToBeAccounted = Collections.synchronizedList(new LinkedList<Future<TextureDescription>>());
	
	private static void waitUntilTextureProcessingEnds(){
	    while(!texturesToBeAccounted.isEmpty()){
		try{texturesToBeAccounted.remove(0).get();}catch(Exception e){e.printStackTrace();}
	    }
	}//end waitUntilTextureProcessingEnds()
	
	private static final Future<TextureDescription> fallbackTexture;
	static  {
		Texture t;
		t=new Texture(RGBA8FromPNG(Texture.class.getResourceAsStream("/fallbackTexture.png")),"Fallback");
		fallbackTexture=new DummyFuture<TextureDescription>(t);
		}
	private static ByteBuffer emptyRow=null;
	
	public Texture(ByteBuffer imageRGB8, String debugName){
	    	if(imageRGB8.capacity()==0){throw new IllegalArgumentException("Cannot create texture of zero size.");}
		final int sideLength=(int)Math.sqrt((imageRGB8.capacity()/4));
		TextureTreeNode newNode = new TextureTreeNode(sideLength,null,debugName);
		nodeForThisTexture=newNode;
		newNode.setImage(imageRGB8);
		registerNode(newNode);
		}//end constructor
	
	public Texture(BufferedImage img, String debugName){
		final int sideLength=img.getWidth();
		if(sideLength==0){throw new IllegalArgumentException("Cannot create texture of zero size.");}
		TextureTreeNode newNode = new TextureTreeNode(sideLength,null,debugName);
		nodeForThisTexture=newNode;
		//BufferedImage scaledDown=new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_ARGB);
		//Graphics g = scaledDown.getGraphics();
		//g.drawImage(img,0, 0, img.getWidth(), img.getHeight(), 0, 0, img.getWidth(), img.getHeight(), null);
		//g.drawImage(img, 4, 4, img.getWidth()-2, img.getHeight()-2, 0, 0, img.getWidth(), img.getHeight(), null);
		//g.dispose();
		long redA=0,greenA=0,blueA=0;
		ByteBuffer data = ByteBuffer.allocateDirect(img.getWidth()*img.getHeight()*4);
		for(int y=0; y<img.getHeight(); y++)
			{for(int x=0; x<img.getWidth(); x++)
				{Color c= new Color(img.getRGB(x, y),true);
				data.put((byte)c.getRed());
				data.put((byte)c.getGreen());
				data.put((byte)c.getBlue());
				data.put((byte)c.getAlpha());
				redA+=c.getRed();
				greenA+=c.getGreen();
				blueA+=c.getBlue();
				}//end for(x)
			}//end for(y)
		final int div=data.capacity()/4;
		averageColor=new Color((redA/div)/255f,(greenA/div)/255f,(blueA/div)/255f);
		newNode.setImage(data);
		registerNode(newNode);
		}
	
	public static ByteBuffer RGBA8FromPNG(File f){
	    try{return RGBA8FromPNG(new FileInputStream(f));}
	    catch(FileNotFoundException e){e.printStackTrace();return null;}
	}
	
	public static ByteBuffer RGBA8FromPNG(InputStream f){
		try{
			BufferedImage image = ImageIO.read(f);
			int color;
			ByteBuffer buf = ByteBuffer.allocateDirect(image.getWidth()*image.getHeight()*4);
			for(int y=0; y<image.getHeight(); y++){
				for(int x=0; x<image.getWidth(); x++){
				    	color=image.getRGB(x, y);
					buf.put((byte)((color&0x00FF0000)>>16));
					buf.put((byte)((color&0x0000FF00)>>8));
					buf.put((byte)(color&0x000000FF));
					buf.put((byte)((color&0xFF000000)>>24));
					}//end for(x)
				}//end for(y)
			buf.clear();//Rewind
			return buf;
			}
		catch(Exception e){e.printStackTrace();}
		return null;
		}//end RGB8FromPNG(...)
	
	static double getPixelSize(){return pixelSize;}
	
	public static GLTexture getGlobalTexture(){return globalTexture;}
	
	private static synchronized void registerNode(TextureTreeNode newNode)
		{
		if(rootNode==null)
			{
			//System.out.println("Creating initial rootNode with sideLength of "+newNode.getSideLength()*2);
			rootNode=new TextureTreeNode(newNode.getSideLength()*2,null,"Root or former root as branch");//Assuming square
			rootNode.setSizeU(1);rootNode.setSizeV(1);
			//System.out.println("Adding first subnode to this new root..");
			rootNode.addNode(newNode);
			}
		else
			{
			if(newNode.getSideLength()>=rootNode.getSideLength())//Too big to fit
				{//New, bigger root
				//System.out.println("NewNode>=rootNode sideLen");
				TextureTreeNode oldRoot = rootNode;
				rootNode=new TextureTreeNode(newNode.getSideLength()*2,null,"Root or former root as branch");
				rootNode.addNode(newNode);
				//Try again recursively until we fit the old root into the new root
				registerNode(oldRoot);
				}
			else
				{//Small enough to fit but might be out of space
				try
					{
					//System.out.println("registerNode() small enough to fit but may be out of space...");
					rootNode.addNode(newNode);
					}
				catch(OutOfTextureSpaceException e)
					{//New, bigger root
					TextureTreeNode oldRoot = rootNode;
					//System.out.println("===>Need a bigger root. Creating one of size "+oldRoot.getSideLength()*2);
					//System.out.println("This resize is upon receiving texture #"+textureCount);
					rootNode=new TextureTreeNode(oldRoot.getSideLength()*2,null,"Root or former root as branch");
					//System.out.println("Adding oldRoot to new rootNode...");
					rootNode.addNode(oldRoot);
					//Try again recursively until we fit the new node into the tree
					//System.out.println("Re-attempting to add newNode to new Root");
					registerNode(newNode);
					}
				}//end else(small enough to fit)
			}//end else{rootNode!=null}
		//System.out.println("...done registering.\n");
		}//end registerNode(...)
	
	public static Future<TextureDescription> getFallbackTexture(){return fallbackTexture;}
	
	//public static int getGlobalTextureID(){return globalTexID;}
	
	public Color getAverageColor()
		{
		if(averageColor==null)
			{//Compute a new one
			double sRed=0,sGreen=0,sBlue=0;
			final int sLen=nodeForThisTexture.getSideLength();
			final ByteBuffer img=nodeForThisTexture.getImage();
			img.clear();//Doesn't erase it. Clears the marks, limits, positions.
			for(int i=0; i<sLen; i++)
				{sRed+=(int)img.get() & 0xFF; sGreen+=(int)img.get() & 0xFF; sBlue+=(int)img.get() & 0xFF;}
			sRed/=sLen; sGreen/=sLen; sBlue/=sLen;
			averageColor=new Color((int)sRed,(int)sGreen,(int)sBlue);
			}
		return averageColor;
		}//end getAverageColor()
	
	public static void finalize(GPU gpu)
		{
		//gl.getContext().release();
		final int gSideLen=rootNode.getSideLength();
		
		//Setup the empty rows
		emptyRow = ByteBuffer.allocate(gSideLen*4);
		for(int i=0; i<gSideLen; i++)
			{emptyRow.put((byte)(Math.random()*256));}
		emptyRow.rewind();
		System.out.println("Finalizing global U/V coordinates...");
		waitUntilTextureProcessingEnds();
		rootNode.finalizeUV(0, 0, 1, 1);
		System.out.println("\t...Done.");
		System.out.println("Allocating "+gSideLen+"x"+gSideLen+" shared texture in client RAM...");
		ByteBuffer buf = ByteBuffer.allocateDirect(gSideLen*gSideLen*4);
		System.out.println("\t...Done.");
		System.out.println("Assembling the megatexture...");
		//Fill the buffer, raster row by raster row
		buf.rewind();
		for(int row=0; row<gSideLen*gSideLen*4; row++)
			{buf.put((byte)(Math.random()*255.));}
		buf.rewind();
		
		for(int row=gSideLen-1; row>=0; row--)
			{rootNode.dumpRowToBuffer(buf,row);}
		
		System.out.println("\t...Done.");
		buf.rewind();
		System.out.println("Creating a new OpenGL texture for megatexture...");
		
		GLTexture tex = gpu.newTexture(/*(int)(int) (Math.log(gSideLen) / Math.log(2))*/);
		tex.setTextureImageRGBA(buf);
		globalTexture=tex;
		}//end finalize()
	
	public static final int createTextureID(GL3 gl)
		{
		IntBuffer ib= IntBuffer.allocate(1);
		gl.glGenTextures(1, ib);
		ib.clear();
		return ib.get();
		}
	
	public static final Color [] GREYSCALE;
	static
		{
		GREYSCALE=new Color[256];
		for(int i=0; i<256; i++)
			{
			GREYSCALE[i]=new Color(i,i,i);
			}
		}//end static{}
	
	static class TextureTreeNode
		{
		private TextureTreeNode parent;
		private double offsetU,offsetV;//In OpenGL orientation: (0,0) is bottom left.
		private double sizeU,sizeV;
		private TextureTreeNode topLeft,topRight,bottomLeft,bottomRight;
		private ByteBuffer image;
		private int sideLength;
		private String debugName="[unset]";
		
		public TextureTreeNode(int sideLength, TextureTreeNode parent, String debugName)
			{
			this.sideLength=sideLength;
			this.parent=parent;
			this.debugName=debugName;
			}
		
		public boolean isFull()
			{
			if(image!=null)return true;
			if(topLeft!=null && topRight!=null && bottomLeft!=null && bottomRight!=null)
				{
				return topLeft.isFull()&&topRight.isFull()&&bottomLeft.isFull()&&bottomRight.isFull();
				}
			return false;
			}//end isFull()
		
		public void finalizeUV(double offU, double offV, double sU, double sV){
			this.setOffsetU(offU+Texture.getPixelSize());
			this.setOffsetV(offV+Texture.getPixelSize());
			this.setSizeU(sU-Texture.getPixelSize()*2.);
			this.setSizeV(sV-Texture.getPixelSize()*2.);
			
			if(topLeft!=null){topLeft.finalizeUV(offU, offV+sV/2., sU/2., sV/2.);}
			if(topRight!=null){topRight.finalizeUV(offU+sU/2., offV+sV/2., sU/2., sV/2.);}
			if(bottomLeft!=null){bottomLeft.finalizeUV(offU, offV, sU/2., sV/2.);}
			if(bottomRight!=null){bottomRight.finalizeUV(offU+sU/2., offV, sU/2., sV/2.);}
			}
		
		public void dumpRowToBuffer(ByteBuffer buf, int row)
			{//Rows start at top, not OpenGL-bottom.
			if(this.getSizeU()<=0){System.out.println("Usize is "+this.getSizeU()+" name is "+debugName);System.exit(1);}
			if(this.getSizeV()<=0){System.out.println("Vsize is "+this.getSizeV());System.exit(1);}
			if(image==null)
				{
				if(row>=getSideLength()/2)
					{
					//Bottom two
					if(bottomLeft!=null)bottomLeft.dumpRowToBuffer(buf, row-getSideLength()/2);
					else {emptyRow.clear();emptyRow.limit(4*getSideLength()/2);buf.put(emptyRow);}
					if(bottomRight!=null)bottomRight.dumpRowToBuffer(buf, row-getSideLength()/2);
					else {emptyRow.clear();emptyRow.limit(4*getSideLength()/2);buf.put(emptyRow);}
					}
				else
					{
					//Top two
					if(topLeft!=null)topLeft.dumpRowToBuffer(buf, row);
					else {emptyRow.clear();emptyRow.limit(4*getSideLength()/2);buf.put(emptyRow);}
					if(topRight!=null)topRight.dumpRowToBuffer(buf, row);
					else {emptyRow.clear();emptyRow.limit(4*getSideLength()/2);buf.put(emptyRow);}
					}
				}//end image==null
			else
				{
				image.clear();
				image.limit( (row*getSideLength()*4) + (getSideLength()*4) );
				image.position(row*getSideLength()*4);
				buf.put(image);
				}//end image!=null
			}//end dumpRowToBuffer()

		public boolean isLeaf(){
			return image!=null;
			}
		
		
		public void addNode(TextureTreeNode newNode){
			if(isFull())throw new OutOfTextureSpaceException();
			
			final int sideLength=newNode.getSideLength();
			//System.out.println("addNode, newNode.sideLength="+newNode.getSideLength()+" to thisNode.sideLength="+this.getSideLength());
			//System.out.println("\t");
			//System.out.println("Attempting to match to empty leaf...");
			if(sideLength==this.sideLength/2)//Perfectly matches branches/leaves
				{
				if(topLeft==null)
					{
					//TextureTreeNode newNode =new TextureTreeNode(sideLength,this);
					newNode.setParent(this);
					//newNode.setColorGrid(colorGrid);
					//Set offsets
					//System.out.println("added to TopLeft");
					topLeft=newNode;
					return;
					}
				else if(topRight==null)
					{
					//TextureTreeNode newNode =new TextureTreeNode(sideLength,this);
					newNode.setParent(this);
					//newNode.setColorGrid(colorGrid);
					//Set offsets
					//System.out.println("added to TopRight");
					topRight=newNode;
					return;
					}
				else if(bottomLeft==null)
					{
					//TextureTreeNode newNode =new TextureTreeNode(sideLength,this);
					newNode.setParent(this);
					bottomLeft=newNode;
					//System.out.println("added to BottomLeft");
					return;
					}
				else if(bottomRight==null)
					{
					//TextureTreeNode newNode =new TextureTreeNode(sideLength,this);
					newNode.setParent(this);
					bottomRight=newNode;
					//System.out.println("added to BottomRight");
					return;
					}
				//System.out.println("None of the corners is null. Means that there are branches but no single fragment big enough. Throwing OutOfSpace exception...");
				throw new OutOfTextureSpaceException();
				}//end if(sideLength==this.sideLength/2)
			else if(sideLength<=this.sideLength/2)//Smaller than branches/leaves
				{//Find a non-leaf, if none, try to create a leaf. If none can be created, throw exception
				//System.out.println("Attempt to match to empty leaf failed.");
				//System.out.println("Trying to find an existing branch which can accept this texture...");
				//Find non-leaf and try to push it there.
				if(topLeft!=null && !topLeft.isLeaf())
					{
					try 
						{
						topLeft.addNode(newNode);
						//System.out.println("pushed to TopLeft");
						return;
						}
					catch(OutOfTextureSpaceException e)
						{}
					}//end if(topLeft)
				if(topRight!=null && !topRight.isLeaf())
					{
					try 
						{
						topRight.addNode(newNode);
						//System.out.println("pushed to TopRight");
						return;
						}
					catch(OutOfTextureSpaceException e)
						{}
					}//end if(topRight)
				if(bottomLeft!=null && !bottomLeft.isLeaf())
					{
					try 
						{
						bottomLeft.addNode(newNode);
						//System.out.println("pushed to BottomLeft");
						return;
						}
					catch(OutOfTextureSpaceException e)
						{}
					}//end if(bottomLeft)
				if(bottomRight!=null && !bottomRight.isLeaf())
					{
					try 
						{
						bottomRight.addNode(newNode);
						//System.out.println("pushed to BottomRight");
						return;
						}
					catch(OutOfTextureSpaceException e)
						{}
					}//end if(bottomRight)
				//No leaf found. Try to create one
				//System.out.println("Attempt to find an existing branch which can accept this as a leaf has failed.");
				//System.out.println("Attempting to create a child branch which is larger than this node which can contain the newNode.");
				try
					{
					if(topLeft==null)
						{
						topLeft=new TextureTreeNode(this.sideLength/2,this,"Branch");
						topLeft.addNode(newNode);
						//System.out.println("pushed to new TopLeft");
						return;
						}
					if(topRight==null)
						{
						topRight=new TextureTreeNode(this.sideLength/2,this,"Branch");
						topRight.addNode(newNode);
						//System.out.println("pushed to new TopRight");
						return;
						}
					if(bottomLeft==null)
						{
						bottomLeft=new TextureTreeNode(this.sideLength/2,this,"Branch");
						bottomLeft.addNode(newNode);
						//System.out.println("pushed to new BottomLeft");
						return;
						}
					if(bottomRight==null)
						{
						bottomRight=new TextureTreeNode(this.sideLength/2,this,"Branch");
						bottomRight.addNode(newNode);
						//System.out.println("pushed to new BottomRight");
						return;
						}
					}//end try{}
				catch(OutOfTextureSpaceException e)
					{e.printStackTrace();System.out.println("ASSERT: Impossible situation!");}
				//Could not create leaf. Throw exception
				throw new OutOfTextureSpaceException();
				}//end else if(sideLength<=this.sideLength/2)
			else if(sideLength>this.sideLength/2)//Bigger than these leaves
				{
				throw new RuntimeException("Added Node's sideLength is bigger than half this Node's sideLength. Can't integrate. Proposed sideLength="+sideLength+" this.sideLength="+this.sideLength);
				}
			}//end addTexture(...)
		
		public double getGlobalUFromLocal(double localU)
			{
			double sizeOfPixel=.5*this.getSizeU()/(double)this.getSideLength();
			double borderingScalar = (double)this.getSideLength()/((double)this.getSideLength()+1.);
			return this.getOffsetU()+sizeOfPixel+localU*this.getSizeU()*borderingScalar;
			}
		public double getGlobalVFromLocal(double localV)
			{
			double sizeOfPixel=.5*this.getSizeU()/(double)this.getSideLength();
			double borderingScalar = (double)this.getSideLength()/((double)this.getSideLength()+1.);
			return this.getOffsetV()+sizeOfPixel+localV*this.getSizeV()*borderingScalar;
			}
		
		/*
		public double getGlobalUFromLocal(double localU)
			{return localU;}
		public double getGlobalVFromLocal(double localV)
			{return localV;}
		*/
		/**
		 * @return the offsetU
		 */
		public double getOffsetU()
			{
			return offsetU;
			}

		/**
		 * @param offsetU the offsetU to set
		 */
		public void setOffsetU(double offsetU)
			{
			this.offsetU = offsetU;
			}

		/**
		 * @return the offsetV
		 */
		public double getOffsetV()
			{
			return offsetV;
			}

		/**
		 * @param offsetV the offsetV to set
		 */
		public void setOffsetV(double offsetV)
			{
			this.offsetV = offsetV;
			}

		/**
		 * @return the topLeft
		 */
		public TextureTreeNode getTopLeft()
			{
			return topLeft;
			}

		/**
		 * @param topLeft the topLeft to set
		 */
		public void setTopLeft(TextureTreeNode topLeft)
			{
			this.topLeft = topLeft;
			}

		/**
		 * @return the topRight
		 */
		public TextureTreeNode getTopRight()
			{
			return topRight;
			}

		/**
		 * @param topRight the topRight to set
		 */
		public void setTopRight(TextureTreeNode topRight)
			{
			this.topRight = topRight;
			}

		/**
		 * @return the bottomLeft
		 */
		public TextureTreeNode getBottomLeft()
			{
			return bottomLeft;
			}

		/**
		 * @param bottomLeft the bottomLeft to set
		 */
		public void setBottomLeft(TextureTreeNode bottomLeft)
			{
			this.bottomLeft = bottomLeft;
			}

		/**
		 * @return the bottomRight
		 */
		public TextureTreeNode getBottomRight()
			{
			return bottomRight;
			}

		/**
		 * @param bottomRight the bottomRight to set
		 */
		public void setBottomRight(TextureTreeNode bottomRight)
			{
			this.bottomRight = bottomRight;
			}

		/**
		 * @return the sideLength
		 */
		public int getSideLength()
			{
			return sideLength;
			}

		/**
		 * @param sideLength the sideLength to set
		 */
		public void setSideLength(int sideLength)
			{
			this.sideLength = sideLength;
			}

		/**
		 * @return the parent
		 */
		public TextureTreeNode getParent()
			{
			return parent;
			}

		/**
		 * @param parent the parent to set
		 */
		public void setParent(TextureTreeNode parent)
			{
			this.parent = parent;
			}

		/**
		 * @return the sizeU
		 */
		public double getSizeU()
			{
			return sizeU;
			}

		/**
		 * @param sizeU the sizeU to set
		 */
		public void setSizeU(double sizeU)
			{this.sizeU = sizeU;}

		/**
		 * @return the sizeV
		 */
		public double getSizeV()
			{
			return sizeV;
			}

		/**
		 * @param sizeV the sizeV to set
		 */
		public void setSizeV(double sizeV)
			{
			this.sizeV = sizeV;
			}

		/**
		 * @return the image
		 */
		public ByteBuffer getImage()
			{
			return image;
			}

		/**
		 * @param image the image to set
		 */
		public void setImage(ByteBuffer image)
			{
			this.image = image;
			}
		}//end TextureTreeNode

	/**
	 * @return the nodeForThisTexture
	 */
	public TextureTreeNode getNodeForThisTexture()
		{
		return nodeForThisTexture;
		}

	/**
	 * @param nodeForThisTexture the nodeForThisTexture to set
	 */
	public void setNodeForThisTexture(TextureTreeNode nodeForThisTexture)
		{
		this.nodeForThisTexture = nodeForThisTexture;
		}

	public static Future<TextureDescription> solidColor(Color color)
		{
		BufferedImage img = new BufferedImage(64,64,BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(color);
		g.fillRect(0, 0, 64, 64);
		g.dispose();
		
		return new DummyFuture<TextureDescription>(new Texture(img,"Solid color "+color));
		}
	}//end Texture
