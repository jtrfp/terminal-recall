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
package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.opengl.GL3;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.act.ActColor;
import org.jtrfp.jtrfp.internal.act.ActDataLoader;
import org.jtrfp.jtrfp.internal.tex.TexDataLoader;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.IPodFileEntry;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.AltitudeMap;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.ColorProcessor;
import org.jtrfp.trcl.DummyFuture;
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.NonPowerOfTwoException;
import org.jtrfp.trcl.RawAltitudeMapWrapper;
import org.jtrfp.trcl.RawTextureMeshWrapper;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Texture;
import org.jtrfp.trcl.TextureDescription;
import org.jtrfp.trcl.TextureMesh;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.file.BINFile;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.AnimatedTextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.EOFBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertex;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock19;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.LineSegmentBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.TextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.Vertex;
import org.jtrfp.trcl.file.CLRFile;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.NAVFile;
import org.jtrfp.trcl.file.NotSquareException;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.RAWFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TNLFile;
import org.jtrfp.trcl.obj.DebrisFactory;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;

public class ResourceManager{
	LinkedList<IPodData> pods = new LinkedList<IPodData>();
	private HashMap<String, Future<TextureDescription>> textureNameMap = new HashMap<String,Future<TextureDescription>>();
	private HashMap<String, Future<TextureDescription>[]> specialTextureNameMap = new HashMap<String,Future<TextureDescription>[]>();
	private HashMap<String, BINFile.AnimationControl> aniBinNameMap = new HashMap<String,BINFile.AnimationControl>();
	private HashMap<String, BINFile.Model> modBinNameMap = new HashMap<String,BINFile.Model>();
	private HashMap<String, Model> modelCache = new HashMap<String,Model>();
	private ExplosionFactory explosionFactory;
	private PluralizedPowerupFactory pluralizedPowerupFactory;
	private DebrisFactory debrisFactory;
	private ProjectileFactory [] projectileFactories;
	
	private final TR tr;
	
	public ResourceManager(TR tr){
		this.tr=tr;
	}//end ResourceManager
	
	/**
	 * @return the explosionFactory
	 */
	public ExplosionFactory getExplosionFactory() {
	    return explosionFactory;
	}
	public void setExplosionFactory(ExplosionFactory ef){
	    explosionFactory=ef;
	}
	
	public LVLFile getLVL(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		System.out.println("Getting level "+name);
		return new LVLFile(getInputStreamFromResource("LEVELS\\"+name));
		}//end getLVL
	
	private InputStream getInputStreamFromResource(String name) throws FileNotFoundException, FileLoadException, IOException
		{
		System.out.println("Getting resource: "+name);
		IPodFileEntry ent;
		for(IPodData p:pods)
			{
			if((ent=p.findEntry(name))!=null)
				{return new BufferedInputStream(ent.getInputStreamFromPod());}
			}//end for(podFiles)
		throw new FileNotFoundException(name);
		}//end getInputStreamFromResource(...)
	
	public void registerPOD(File f) throws FileLoadException
		{
		if(f==null)throw new NullPointerException("fileToUse should not be null.");
		pods.add(new PodFile(f).getData());
		}
	
	public Future<TextureDescription> [] getTextures(String texFileName, Color [] palette, ColorProcessor proc, GL3 gl3) throws IOException, FileLoadException, IllegalAccessException
		{
		String [] files = getTEXListFile(texFileName);
		Future<TextureDescription> [] result = new Future[files.length];
		//Color [] palette = getPalette(actFileName);
		for(int i=0; i<files.length;i++)
			{result[i]=getRAWAsTexture(files[i],palette,proc,gl3);}
		return result;
		}//end loadTextures(...)
	
	public Future<TextureDescription>[] getSpecialRAWAsTextures(String name, Color [] palette, ColorProcessor proc, GL3 gl, int upScalePowerOfTwo) throws IOException, FileLoadException, IllegalAccessException{
		Future<TextureDescription> [] result = specialTextureNameMap.get(name);
		if(result==null)
			{BufferedImage [] segs = getSpecialRAWImage(name, palette, proc, upScalePowerOfTwo);
			result=new Future[segs.length];
			for(int si=0; si<segs.length; si++)
				{result[si] = new DummyFuture<TextureDescription>(new Texture(segs[si]));}
			specialTextureNameMap.put(name,result);
			}
		return result;
		}//end getSpecialRAWAsTextures
	
	public Future<TextureDescription> getRAWAsTexture(String name, final Color [] palette, ColorProcessor proc, GL3 gl3) throws IOException, FileLoadException, IllegalAccessException{
	    return getRAWAsTexture(name,palette,proc,gl3,true);
	}
	
	public Future<TextureDescription> getRAWAsTexture(final String name, final Color [] palette, final ColorProcessor proc, GL3 gl3,
			final boolean useCache) throws IOException, FileLoadException, IllegalAccessException{
	    	Future<TextureDescription> result=textureNameMap.get(name);
	    	if(result!=null&&useCache)return result;
		result= TR.threadPool.submit(new Callable<TextureDescription>(){

		    @Override
		    public TextureDescription call() throws Exception {
			TextureDescription result=null;
			try {
				if(name.substring(name.length()-5, name.length()-4).contentEquals("0") && TR.ANIMATED_TERRAIN)
					{//ends in number
					System.out.println("RAW "+name+" ends in a zero. Testing if it is animated...");
					ArrayList<String> frames = new ArrayList<String>();
					//frames.add(name);
					int frameNumber=0;
					String newName=name.substring(0,name.length()-5)+""+frameNumber+".RAW";
					System.out.println("Testing against "+newName);
					while(rawExists(newName))
						{
						frameNumber++;
						frames.add(newName);
						newName=name.substring(0,name.length()-5)+""+frameNumber+".RAW";
						//System.out.println("Testing against "+newName);
						}
					if(frames.size()>1)
						{
						Future<Texture> [] tFrames = new Future[frames.size()];
						for(int i=0; i<tFrames.length;i++)
							{tFrames[i]=new DummyFuture<Texture>(new Texture(getRAWImage(frames.get(i),palette,proc)));/*textureNameMap.put(frames.get(i), tFrames[i]);*/}
						AnimatedTexture aTex = new AnimatedTexture(new Sequencer(500,tFrames.length,false), tFrames);
						return aTex;
						}//end if(multi-frame)
					}//end if(may be animated)
				result = new Texture(getRAWImage(name,palette,proc));
				}
			catch(NotSquareException e)
				{
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=Texture.getFallbackTexture().get();
				}
			catch(NonPowerOfTwoException e)
				{
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=Texture.getFallbackTexture().get();
				}
			return result;
		    }
		    
		});
		if(useCache)textureNameMap.put(name, result);
		return result;
		}//end getRAWAsTexture(...)
	
	public boolean rawExists(String name)
		{
		for(IPodData p:pods)
			{
			/*for(IPodFileEntry ent: p.getEntries())
				{System.out.println("POD PATH: "+ent.getPath());}*/
			if((p.findEntry("ART\\"+name))!=null)
				{
				System.out.println(name+" found to exist. Returning true...");
				return true;
				}
			}//end for(podFiles)
		System.out.println(name+" found to not exist. Returning false...");
		return false;
		}//end rawExists
	
	public Model getBINModel(String name, Color [] palette, GL3 gl) throws FileLoadException, IOException, IllegalAccessException
		{
		return getBINModel(name,Texture.getFallbackTexture(),1,true,palette,gl);
		}
	
	public Model getBINModel(String name,Future<TextureDescription> defaultTexture,double scale,boolean cache, Color [] palette, GL3 gl) throws FileLoadException, IOException, IllegalAccessException
		{if(name==null)throw new NullPointerException("Name cannot be null");
		if(palette==null)throw new NullPointerException("Palette cannot be null");
		if(gl==null)throw new NullPointerException("GL cannot be null");
		if(modelCache.containsKey(name)&& cache)return modelCache.get(name);
		if(gl.getContext().isCurrent())gl.getContext().release();//Feed the dog
		try {
			BINFile.AnimationControl ac=null;
			Model result;
			ac = aniBinNameMap.get(name);
			if(ac==null)
				{
				InputStream is = getInputStreamFromResource("MODELS\\"+name);
				//TODO: InputStream not guaranteed to close when exception is thrown. Wrap in try{}, close it, and re-throw.
				ac = new BINFile.AnimationControl(is);//This will throw an exception on and escape to the static model block
				is.close();
				aniBinNameMap.put(name, ac);
				}
			System.out.println("Recognized as animation control file.");
			//Build the Model from the BINFile.Model
			Model [] frames = new Model[ac.getNumFrames()];
			gl.getContext().makeCurrent();//feed the dog
			for(int i=0; i<frames.length;i++)
				{frames[i]=getBINModel(ac.getBinFiles().get(i),defaultTexture,scale,cache,palette,gl);}
			if(gl.getContext().isCurrent())gl.getContext().release();
			frames[0].setDebugName(name+" triangles: "+frames[0].getTriangleList().getNumPrimitives());
			//Consolidate the frames to one model
			for(int i=1; i<frames.length;i++)
				{frames[0].addFrame(frames[i]);}
			result = frames[0];
			result.setFrameDelayInMillis((int)(((double)ac.getDelay()/65535.)*1000.));
			result.finalizeModel();
			if(cache)modelCache.put(name, result);
			gl.getContext().makeCurrent();
			return result;
			}//end try{}
		catch(UnrecognizedFormatException e)
			{//ok fail. Static model
			try
				{
				BINFile.Model m=null;
				Model result = new Model(false);
				m = modBinNameMap.get(name);
				if(m==null)
					{
					InputStream is = getInputStreamFromResource("MODELS\\"+name);
					m = new BINFile.Model(is);
					modBinNameMap.put(name, m);
					}//end if(null)
				System.out.println("Recognized as model file.");
				List<BINFile.Model.Vertex> vertices = m.getVertices();
				final double cpScalar=(scale*TR.crossPlatformScalar*256.)/(double)m.getScale();
				Future<TextureDescription> currentTexture=null;
				for(ThirdPartyParseable b:m.getDataBlocks())
					{
					//Sort out types of block
					if(b instanceof TextureBlock)
						{
						TextureBlock tb = (TextureBlock)b;
						gl.getContext().makeCurrent();
						currentTexture = getRAWAsTexture(tb.getTextureFileName(), palette, GammaCorrectingColorProcessor.singleton,gl);
						gl.getContext().release();
						System.out.println("ResourceManager: TextureBlock specifies texture: "+tb.getTextureFileName());
						}//end if(TextureBlock)
					else if(b instanceof FaceBlock)
						{
						FaceBlock block = (FaceBlock)b;
						List<FaceBlockVertex>vertIndices = block.getVertices();
						if(currentTexture==null){System.out.println("Warning: Face texture not specified. Using fallback texture.");currentTexture=defaultTexture;}
						/*
						 * "The two vb_tex_coord values map the vertices of the face to the texture. 
						 * They are both in the range of 0x0 to 0xFF00, with u=0x0, v=0x0 being the upper 
						 * left corner of the texture, and u=0xFF00, v=0xFF00 being the lower right corner."
						 * - http://www.viaregio.de/pieper/mtm/bin_file_format.shtml
						 */
						//// Note: It appears that Stefan's 0xFF0000 approach works rather than the 0xFF00 value. typo?
						
						//final double cpScalar=1024./(double)m.getScale();
						
						//System.out.println("cpScalar:"+cpScalar);
						//System.out.println("Block class: "+b.getClass());
						if(vertIndices.size()==4)//Quads
							{
							Vertex [] vtx = new Vertex[4];
							for(int i=0; i<4; i++)
								{vtx[i]=vertices.get(vertIndices.get(i).getVertexIndex());}
							Triangle [] tris = Triangle.quad2Triangles(
									new double [] {vtx[0].getX()*cpScalar,vtx[1].getX()*cpScalar,vtx[2].getX()*cpScalar,vtx[3].getX()*cpScalar},//X 
									new double [] {vtx[0].getY()*cpScalar,vtx[1].getY()*cpScalar,vtx[2].getY()*cpScalar,vtx[3].getY()*cpScalar}, 
									new double [] {vtx[0].getZ()*cpScalar,vtx[1].getZ()*cpScalar,vtx[2].getZ()*cpScalar,vtx[3].getZ()*cpScalar}, 
									new double [] {(double)vertIndices.get(0).getTextureCoordinateU()/(double)0xFF0000,(double)vertIndices.get(1).getTextureCoordinateU()/(double)0xFF0000,(double)vertIndices.get(2).getTextureCoordinateU()/(double)0xFF0000,(double)vertIndices.get(3).getTextureCoordinateU()/(double)0xFF0000},//U 
									new double [] {1.-(double)vertIndices.get(0).getTextureCoordinateV()/(double)0xFF0000,1.-(double)vertIndices.get(1).getTextureCoordinateV()/(double)0xFF0000,1.-(double)vertIndices.get(2).getTextureCoordinateV()/(double)0xFF0000,1.-(double)vertIndices.get(3).getTextureCoordinateV()/(double)0xFF0000}, 
									currentTexture,
									RenderMode.DYNAMIC);
							result.addTriangle(tris[0]);
							result.addTriangle(tris[1]);
							}
						else if(vertIndices.size()==3)//Triangles
							{
							Triangle t = new Triangle();
							int vi=0;
							Vertex vtx;
							vtx=vertices.get(vertIndices.get(vi).getVertexIndex());
							t.getX()[vi]=vtx.getX()*cpScalar;
							t.getY()[vi]=vtx.getY()*cpScalar;
							t.getZ()[vi]=vtx.getZ()*cpScalar;
							t.getU()[vi]=(double)vertIndices.get(vi).getTextureCoordinateU()/(double)0xFF0000;
							t.getV()[vi]=1.-(double)vertIndices.get(vi).getTextureCoordinateV()/(double)0xFF0000;
							vi++;
							vtx=vertices.get(vertIndices.get(vi).getVertexIndex());
							t.getX()[vi]=vtx.getX()*cpScalar;
							t.getY()[vi]=vtx.getY()*cpScalar;
							t.getZ()[vi]=vtx.getZ()*cpScalar;
							t.getU()[vi]=(double)vertIndices.get(vi).getTextureCoordinateU()/(double)0xFF0000;
							t.getV()[vi]=1.-(double)vertIndices.get(vi).getTextureCoordinateV()/(double)0xFF0000;
							vi++;
							vtx=vertices.get(vertIndices.get(vi).getVertexIndex());
							t.getX()[vi]=vtx.getX()*cpScalar;
							t.getY()[vi]=vtx.getY()*cpScalar;
							t.getZ()[vi]=vtx.getZ()*cpScalar;
							t.getU()[vi]=(double)vertIndices.get(vi).getTextureCoordinateU()/(double)0xFF0000;
							t.getV()[vi]=1.-(double)vertIndices.get(vi).getTextureCoordinateV()/(double)0xFF0000;
							
							t.setRenderMode(RenderMode.DYNAMIC);
							if(currentTexture==null)
								{System.err.println("WARNING: Texture never set for "+name+". Using fallback.");currentTexture=Texture.getFallbackTexture();}
							t.setTexture(currentTexture);
							result.addTriangle(t);
							}//end if(3 vertices)
						else
							{System.err.println("ResourceManager: FaceBlock has "+vertIndices.size()+" vertices. Only 3 or 4 supported.");}
						}//end if(FaceBlock)
					else if(b instanceof FaceBlock19)
						{System.out.println("FaceBlock 0x19 (solid colored faces) not yet implemented. Skipping...");}
					else if(b instanceof LineSegmentBlock)
						{
						LineSegmentBlock block = (LineSegmentBlock)b;
						LineSegment seg = new LineSegment();
						Vertex v1 = vertices.get(block.getVertexID1());
						Vertex v2 = vertices.get(block.getVertexID2());
						seg.getX()[0]=v1.getX()*cpScalar;
						seg.getY()[0]=v1.getY()*cpScalar;
						seg.getZ()[0]=v1.getZ()*cpScalar;
						
						seg.getX()[1]=v2.getX()*cpScalar;
						seg.getY()[1]=v2.getY()*cpScalar;
						seg.getZ()[1]=v2.getZ()*cpScalar;
						Color c= palette[block.getColor()+16];
						seg.setColor(c);
						//System.out.println("ResourceManager.LineSegmentBlock(): red="+c.getRed()+" green="+c.getGreen()+" blue="+c.getBlue());
						seg.setThickness(8);//Defaulted since the file doesn't specify
						result.addLineSegment(seg);
						}
					else if(b instanceof AnimatedTextureBlock)
						{
						System.out.println("Found animated texture block.");
						AnimatedTextureBlock block = (AnimatedTextureBlock)b;
						List<String> frames = block.getFrameNames();
						double timeBetweenFramesInMillis = ((double)block.getDelay()/65535.)*1000.;
						Future<Texture> [] subTextures = new Future[frames.size()];
						for(int ti=0; ti<frames.size(); ti++){
							gl.getContext().makeCurrent();
							subTextures[ti]=(Future)getRAWAsTexture(frames.get(ti), palette, GammaCorrectingColorProcessor.singleton,gl);
							//subTextures[ti]=tex instanceof Texture?new DummyFuture<Texture>((Texture)tex):(Texture)Texture.getFallbackTexture();
							gl.getContext().release();
							}//end for(frames) //fDelay, nFrames,interp
						currentTexture = new DummyFuture<TextureDescription>(new AnimatedTexture(new Sequencer((int)timeBetweenFramesInMillis,subTextures.length,false),subTextures));
						}
					else if(b instanceof EOFBlock)
						{System.out.println("...That's all, end of BIN");}
					else
						{System.out.println("Failed to identify DataBlock: "+b.getClass().getName());}
					}//end for(dataBlocks)
				result.finalizeModel();
				result.setDebugName(name);
				if(cache)modelCache.put(name, result);
				if(gl.getContext().isCurrent())gl.getContext().release();
				return result;
				}//end try{}
			catch(UnrecognizedFormatException ee){
				//Not-good fail
				throw new UnrecognizedFormatException("Can't figure out what this is: "+name+". Giving up. Expect trouble ahead.");
				}
			catch(Exception ee){ee.printStackTrace();return null;}//TODO: This probably can be removed
			//catch(InterruptedException ee){ee.printStackTrace(); return null;}
			}//end catch(ok fail)
		//throw new NullPointerException("Experienced an unexpected failure.");
		//Bad fail.
		}//end getBINModel()
	
	private BufferedImage [] getSpecialRAWImage(String name, Color [] palette, ColorProcessor proc, int upscalePowerOfTwo) throws IllegalAccessException, FileLoadException, IOException
		{
		RAWFile dat = getRAW(name);
		dat.setPalette(palette);
		BufferedImage [] segs = dat.asSegments(upscalePowerOfTwo);
		
		for(BufferedImage seg:segs)
			{
			//stamper.setRGB(0, 0, dat.getSideLength(), dat.getSideLength(), temp, 0, 1);
			Graphics g = seg.getGraphics();
			BufferedImage scaled = new BufferedImage(seg.getColorModel(),seg.copyData(null),seg.isAlphaPremultiplied(),null);
			g.drawImage(scaled.getScaledInstance(seg.getWidth()-2, seg.getHeight()-2, Image.SCALE_AREA_AVERAGING), 1, 1, seg.getWidth()-2, seg.getHeight()-2, null);
			g.dispose();
			}
		return segs;
		}
	
	private RAWFile getRAW(String name) throws IOException,FileLoadException,IllegalAccessException
		{return new RAWFile(getInputStreamFromResource("ART\\"+name));}
	
	/**
	 * Returns RAW image as an R8-G8-B8 buffer with a side-width which is the square root of the total number of pixels
	 * @param name
	 * @param palette
	 * @param proc
	 * @return
	 * @throws IOException
	 * @throws FileLoadException
	 * @throws IllegalAccessException
	 * @throws NotSquareException 
	 * @since Oct 26, 2012
	 */
	public BufferedImage getRAWImage(String name, Color [] palette, ColorProcessor proc) throws IOException, FileLoadException, IllegalAccessException, NotSquareException, NonPowerOfTwoException
		{
		//IRawData dat = RawDataLoader.load(getInputStreamFromResource("ART\\"+name));
		RAWFile dat = getRAW(name);
		
		byte [] raw = dat.getRawBytes();
		if(raw.length!=dat.getSideLength()*dat.getSideLength()) throw new NotSquareException(name);
		if((dat.getSideLength() & (dat.getSideLength()-1))!=0) throw new NonPowerOfTwoException(name);
		//ByteBuffer result = ByteBuffer.allocateDirect(dat.getSideLength()*dat.getSideLength()*4);
		//int [] temp = new int[raw.length];
		final BufferedImage stamper = new BufferedImage(dat.getSideLength(),dat.getSideLength(),BufferedImage.TYPE_INT_ARGB);
		//final BufferedImage scaled = new BufferedImage(dat.getSideLength(),dat.getSideLength(),BufferedImage.TYPE_INT_ARGB);
		
		Graphics stG = stamper.getGraphics();
		//Graphics scG = scaled.getGraphics();
		for(int i=0; i<raw.length; i++)
			{
			Color c= palette[(int)raw[i] & 0xFF];
			stG.setColor(c);
			//scG.setColor(c);
			
			stG.fillRect(i%dat.getSideLength(), i/dat.getSideLength(), 1, 1);
			//scG.fillRect(i%dat.getSideLength(), i/dat.getSideLength(), 1, 1);
			}
		stG.dispose();
		//scG.dispose();
		//stamper.setRGB(0, 0, dat.getSideLength(), dat.getSideLength(), temp, 0, 1);
		Graphics g = stamper.getGraphics();
		//The following code stamps the filename into the texture for debugging purposes
		if(tr.isStampingTextures())
			{
			g.setFont(new Font(g.getFont().getName(),g.getFont().getStyle(),9));
			g.drawString(name, 1, 16);
			}
		//Blit a scaled-down version by 2 px on each dim, keeping borders for filter padding
		//g.drawImage(scaled, 1, 1, dat.getSideLength()-2, dat.getSideLength()-2, null);
		
		g.dispose();
		return stamper;
		/*
		for(int i=0; i<raw.length; i++)
			{
			final Color c=new Color(stamper.getRGB(i%dat.getSideLength(), i/dat.getSideLength()));
			result.put((byte)c.getRed());
			result.put((byte)c.getGreen());
			result.put((byte)c.getBlue());
			result.put((byte)c.getAlpha());
			}
		return result;
		*/
		}//end getRAWImage
	
	public AltitudeMap getRAWAltitude(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		return new RawAltitudeMapWrapper(new RAWFile(getInputStreamFromResource("DATA\\"+name)));
		}//end getRAWAltitude
	
	public TextureMesh getTerrainTextureMesh(String name, Future<TextureDescription>[] texturePalette) throws IOException, FileLoadException, IllegalAccessException
		{
		final CLRFile	dat = new CLRFile(getInputStreamFromResource("DATA\\"+name));
		return new RawTextureMeshWrapper(dat,texturePalette);
		}//end getRAWAltitude
	
	public String [] getTEXListFile(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		return TexDataLoader.load(getInputStreamFromResource("DATA\\"+name)).getTextureNames();
		}//end getTEXListFile
	
	public Color [] getPalette(String name) throws IOException, FileLoadException, IllegalAccessException
		{
		ActColor [] actColors= ActDataLoader.load(getInputStreamFromResource("ART\\"+name)).getColors();
		
		Color [] result = new Color[actColors.length];
		for(int i=0; i<result.length;i++)
			{
			result[i]=new Color(actColors[i].getComponent1(),actColors[i].getComponent2(),actColors[i].getComponent3());
			}
		return result;
		}//end getTEXListFile

	public DEFFile getDEFData(String enemyDefinitionAndPlacementFile) throws FileNotFoundException, IOException, IllegalAccessException, FileLoadException
		{
		return new DEFFile(getInputStreamFromResource("DATA\\"+enemyDefinitionAndPlacementFile));
		}
	
	public TDFFile getTDFData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException
		{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		TDFFile result = new Parser().readToNewBean(is, TDFFile.class);
		is.close();
		return result;
		}
	public TNLFile getTNLData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException
		{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		TNLFile result = new Parser().readToNewBean(is, TNLFile.class);
		is.close();
		return result;
		}
	public NAVFile getNAVData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException
		{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		NAVFile result = new Parser().readToNewBean(is, NAVFile.class);
		is.close();
		return result;
		}
	public Font getFont(String zipName, String fontFileName) throws IOException, FontFormatException
		{
	    	zipName="/fonts/"+zipName;
		if(zipName.toUpperCase().endsWith(".ZIP"))
			{//Search the zip
			ZipInputStream zip = new ZipInputStream(ResourceManager.class.getResourceAsStream(zipName));
			ZipEntry entry;
			
			while((entry=zip.getNextEntry())!=null){
				System.out.println("ZIP ENTRY: "+entry.getName());
				if(entry.getName().toUpperCase().endsWith(fontFileName.toUpperCase()))
					return Font.createFont(Font.TRUETYPE_FONT, zip);
				}//end while(elements)
			}//end if(zip)
		else{}//TODO: Handle non-zipped fonts?
		return null;
		}//end getFont(...)

	public PUPFile getPUPData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException
		{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		PUPFile result = new Parser().readToNewBean(is, PUPFile.class);
		is.close();
		return result;
		}

	public void setPluralizedPowerupFactory(
		PluralizedPowerupFactory pluralizedPowerupFactory) {
	    this.pluralizedPowerupFactory=pluralizedPowerupFactory;
	    
	}
	public PluralizedPowerupFactory getPluralizedPowerupFactory(){return pluralizedPowerupFactory;}

	/**
	 * @return the debrisFactory
	 */
	public DebrisFactory getDebrisFactory() {
	    return debrisFactory;
	}

	/**
	 * @param debrisFactory the debrisFactory to set
	 */
	public void setDebrisFactory(DebrisFactory debrisFactory) {
	    this.debrisFactory = debrisFactory;
	}

	public ProjectileFactory[] getProjectileFactories() {
	    return projectileFactories;
	}

	/**
	 * @param projectileFactories the projectileFactories to set
	 */
	public void setProjectileFactories(ProjectileFactory[] projectileFactories) {
	    this.projectileFactories = projectileFactories;
	}
}//end ResourceManager
