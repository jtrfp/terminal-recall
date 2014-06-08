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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.opengl.GL3;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
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
import org.jtrfp.trcl.GammaCorrectingColorProcessor;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.Model;
import org.jtrfp.trcl.NonPowerOfTwoException;
import org.jtrfp.trcl.RawAltitudeMapWrapper;
import org.jtrfp.trcl.RawTextureMeshWrapper;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.TextureMesh;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.file.BINFile;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.AnimatedTextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.EOFBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertex;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock05;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock19;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.LineSegmentBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.TextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.Unknown12;
import org.jtrfp.trcl.file.CLRFile;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.NAVFile;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.file.NotSquareException;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.RAWFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TNLFile;
import org.jtrfp.trcl.obj.DebrisFactory;
import org.jtrfp.trcl.obj.ExplosionFactory;
import org.jtrfp.trcl.obj.PluralizedPowerupFactory;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeFactory;

public class ResourceManager{
	LinkedList<IPodData> pods = new LinkedList<IPodData>();
	private HashMap<String, TRFutureTask<TextureDescription>> 
	/*						*/	textureNameMap = new HashMap<String,TRFutureTask<TextureDescription>>();
    	private HashMap<String, TRFutureTask<TextureDescription>[]> 
    								specialTextureNameMap 	= new HashMap<String,TRFutureTask<TextureDescription>[]>();
	private HashMap<String, BINFile.AnimationControl> 	aniBinNameMap 		= new HashMap<String,BINFile.AnimationControl>();
	private HashMap<String, BINFile.Model> 			modBinNameMap 		= new HashMap<String,BINFile.Model>();
	private HashMap<String, Model> 				modelCache 		= new HashMap<String,Model>();
	private ExplosionFactory 				explosionFactory;
	private SmokeFactory 					smokeFactory;
	private PluralizedPowerupFactory 			pluralizedPowerupFactory;
	private DebrisFactory 					debrisFactory;
	private ProjectileFactory [] 				projectileFactories;
	private final TR 					tr;
	
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
	
	public LVLFile getLVL(String name) throws IOException, FileLoadException, IllegalAccessException{
		System.out.println("Getting level "+name);
		return new LVLFile(getInputStreamFromResource("LEVELS\\"+name));
		}//end getLVL
	
	private InputStream getInputStreamFromResource(String name) throws FileNotFoundException, FileLoadException, IOException{
		System.out.println("Getting resource: "+name);
		IPodFileEntry ent;
		for(IPodData p:pods){
			if((ent=p.findEntry(name))!=null)
				{return new BufferedInputStream(ent.getInputStreamFromPod());}
			}//end for(podFiles)
		throw new FileNotFoundException(name);
		}//end getInputStreamFromResource(...)
	
	public void registerPOD(File f) throws FileLoadException{
		if(f==null)throw new NullPointerException("fileToUse should not be null.");
		pods.add(new PodFile(f).getData());
		}
	
	public TRFutureTask<TextureDescription> [] getTextures(String texFileName, Color [] palette, ColorProcessor proc, GL3 gl3) throws IOException, FileLoadException, IllegalAccessException{
		String [] files = getTEXListFile(texFileName);
		TRFutureTask<TextureDescription> [] result = new TRFutureTask[files.length];
		//Color [] palette = getPalette(actFileName);
		for(int i=0; i<files.length;i++)
			{result[i]=getRAWAsTexture(files[i],palette,proc,gl3);}
		return result;
		}//end loadTextures(...)
	
	public TRFutureTask<TextureDescription>[] getSpecialRAWAsTextures(String name, Color [] palette, ColorProcessor proc, GL3 gl, int upScalePowerOfTwo) throws IOException, FileLoadException, IllegalAccessException{
		TRFutureTask<TextureDescription> [] result = specialTextureNameMap.get(name);
		if(result==null){
		    BufferedImage [] segs = getSpecialRAWImage(name, palette, proc, upScalePowerOfTwo);
			result=new TRFutureTask[segs.length];
			for(int si=0; si<segs.length; si++)
				{result[si] = new DummyTRFutureTask<TextureDescription>(new Texture(segs[si],"name",tr));}
			specialTextureNameMap.put(name,result);
			}//end if(result=null)
		return result;
		}//end getSpecialRAWAsTextures
	
	public TRFutureTask<TextureDescription> getRAWAsTexture(String name, final Color [] palette, ColorProcessor proc, GL3 gl3) throws IOException, FileLoadException, IllegalAccessException{
	    return getRAWAsTexture(name,palette,proc,gl3,true);
	}
	
	public TRFutureTask<TextureDescription> getRAWAsTexture(final String name, final Color [] palette, final ColorProcessor proc, GL3 gl3,
			final boolean useCache) throws IOException, FileLoadException, IllegalAccessException{
	    	TRFutureTask<TextureDescription> result=textureNameMap.get(name);
	    	if(result!=null&&useCache)return result;
		result= tr.getThreadManager().submitToThreadPool(new Callable<TextureDescription>(){

		    @Override
		    public TextureDescription call() throws Exception {
			TextureDescription result=null;
			try {
				if(name.substring(name.length()-5, name.length()-4).contentEquals("0") && TR.ANIMATED_TERRAIN)
					{//ends in number
					System.out.println("RAW "+name+" ends in a zero. Testing if it is animated...");
					ArrayList<String> frames = new ArrayList<String>();
					int frameNumber=0;
					String newName=name.substring(0,name.length()-5)+""+frameNumber+".RAW";
					System.out.println("Testing against "+newName);
					while(rawExists(newName)){
						frameNumber++;
						frames.add(newName);
						newName=name.substring(0,name.length()-5)+""+frameNumber+".RAW";
						}
					if(frames.size()>1){
						TRFutureTask<Texture> [] tFrames = new TRFutureTask[frames.size()];
						for(int i=0; i<tFrames.length;i++)
							{tFrames[i]=new DummyTRFutureTask<Texture>(new Texture(getRAWImage(frames.get(i),palette,proc),""+frames.get(i),null));/*textureNameMap.put(frames.get(i), tFrames[i]);*/}
						AnimatedTexture aTex = new AnimatedTexture(new Sequencer(500,tFrames.length,false), tFrames);
						return aTex;
						}//end if(multi-frame)
					}//end if(may be animated)
				result = new Texture(getRAWImage(name,palette,proc),name,tr);
				}
			catch(NotSquareException e){
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=tr.gpu.get().textureManager.get().getFallbackTexture().get();
				}
			catch(NonPowerOfTwoException e){
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=tr.gpu.get().textureManager.get().getFallbackTexture().get();
				}
			catch(Exception e){e.printStackTrace();result=null;}
			return result;
		    }
		    
		});
		if(useCache)textureNameMap.put(name, result);
		Texture.texturesToBeAccounted.add(result);
		return result;
		}//end getRAWAsTexture(...)
	
	public boolean rawExists(String name){
		for(IPodData p:pods){
			if((p.findEntry("ART\\"+name))!=null){
				System.out.println(name+" found to exist. Returning true...");
				return true;
				}
			}//end for(podFiles)
		System.out.println(name+" found to not exist. Returning false...");
		return false;
		}//end rawExists
	
	public Model getBINModel(String name, Color [] palette, GL3 gl) throws FileLoadException, IOException, IllegalAccessException{
		return getBINModel(name,tr.gpu.get().textureManager.get().getFallbackTexture(),1,true,palette,gl);
		}
	
	private static final double [] BOX_U = new double[]{0,1,1,0};
	private static final double [] BOX_V = new double[]{0,0,1,1};
	
	public Model getBINModel(String name,TRFutureTask<TextureDescription> defaultTexture,double scale,boolean cache, Color [] palette, GL3 gl) throws FileLoadException, IOException, IllegalAccessException{
	    	if(name==null)throw new NullPointerException("Name is intolerably null");
		if(palette==null)throw new NullPointerException("Palette is intolerably null");
		if(gl==null)throw new NullPointerException("GL cannot be null");
		if(modelCache.containsKey(name)&& cache)return modelCache.get(name);
		//The models like to set up two line segments where there should be one. 
		//This set is for identifying and culling redundant segs.
		final HashSet<Integer>alreadyVisitedLineSegs = new HashSet<Integer>();
		boolean hasAlpha=false;
		try {
			BINFile.AnimationControl ac=null;
			Model result;
			ac = aniBinNameMap.get(name);
			if(ac==null){
				InputStream is = getInputStreamFromResource("MODELS\\"+name);
				//TODO: InputStream not guaranteed to close when exception is thrown. Wrap in try{}, close it, and re-throw.
				ac = new BINFile.AnimationControl(is);//This will throw an exception on and escape to the static model block
				is.close();
				aniBinNameMap.put(name, ac);
				}
			System.out.println("Recognized as animation control file.");
			//Build the Model from the BINFile.Model
			Model [] frames = new Model[ac.getNumFrames()];
			for(int i=0; i<frames.length;i++)
				{frames[i]=getBINModel(ac.getBinFiles().get(i),defaultTexture,scale,cache,palette,gl);}
			frames[0].setDebugName(name+" triangles: "+frames[0].getTriangleList().getNumElements());
			//Consolidate the frames to one model
			for(int i=1; i<frames.length;i++)
				{frames[0].addFrame(frames[i]);}
			result = frames[0];
			result.setFrameDelayInMillis((int)(((double)ac.getDelay()/65535.)*1000.));
			result.finalizeModel();
			if(cache)modelCache.put(name, result);
			return result;
			}//end try{}
		catch(UnrecognizedFormatException e){//ok fail. Static model
			try	{
				BINFile.Model m=null;
				Model result = new Model(false,tr);
				result.setDebugName(name);
				m = modBinNameMap.get(name);
				if(m==null){
					InputStream is = getInputStreamFromResource("MODELS\\"+name);
					m = new BINFile.Model(is);
					modBinNameMap.put(name, m);
					}//end if(null)
				final double cpScalar=(scale*TR.crossPlatformScalar*256.)/(double)m.getScale();
				System.out.println("Recognized as model file.");
				List<org.jtrfp.trcl.gpu.Vertex> vertices = new ArrayList<org.jtrfp.trcl.gpu.Vertex>();
				for(BINFile.Model.Vertex binVtx:m.getVertices()){
				    vertices.add(new org.jtrfp.trcl.gpu.Vertex().setPosition(new Vector3D(
						    binVtx.getX()*cpScalar,
						    binVtx.getY()*cpScalar,
						    binVtx.getZ()*cpScalar)));
				}//end try{}
				
				TRFutureTask<TextureDescription> currentTexture=null;
				for(ThirdPartyParseable b:m.getDataBlocks()){
					//Sort out types of block
					if(b instanceof TextureBlock){
						TextureBlock tb = (TextureBlock)b;
						if(hasAlpha)currentTexture = getRAWAsTexture(tb.getTextureFileName(), palette, GammaCorrectingColorProcessor.singleton,gl,hasAlpha);
						else{currentTexture = getRAWAsTexture(tb.getTextureFileName(), palette, GammaCorrectingColorProcessor.singleton,gl);}
						System.out.println("ResourceManager: TextureBlock specifies texture: "+tb.getTextureFileName());
						}//end if(TextureBlock)
					else if(b instanceof FaceBlock){
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
						if(vertIndices.size()==4){//Quads
						    	org.jtrfp.trcl.gpu.Vertex [] vtx = new org.jtrfp.trcl.gpu.Vertex[4];
							for(int i=0; i<4; i++)
								{vtx[i]=vertices.get(vertIndices.get(i).getVertexIndex()%(b instanceof FaceBlock05?10:Integer.MAX_VALUE));}
							Triangle [] tris = Triangle.quad2Triangles(
									vtx,
									new Vector2D[]{
										new Vector2D(
											(double)vertIndices.get(0).getTextureCoordinateU()/(double)0xFF0000,
											1.-(double)vertIndices.get(0).getTextureCoordinateV()/(double)0xFF0000),
										new Vector2D(
											(double)vertIndices.get(1).getTextureCoordinateU()/(double)0xFF0000,
											1.-(double)vertIndices.get(1).getTextureCoordinateV()/(double)0xFF0000),
										new Vector2D(
											(double)vertIndices.get(2).getTextureCoordinateU()/(double)0xFF0000,
											1.-(double)vertIndices.get(2).getTextureCoordinateV()/(double)0xFF0000),
										new Vector2D(
											(double)vertIndices.get(3).getTextureCoordinateU()/(double)0xFF0000,
											1.-(double)vertIndices.get(3).getTextureCoordinateV()/(double)0xFF0000)
									},
									currentTexture,
									RenderMode.DYNAMIC,hasAlpha,
									new Vector3D(block.getNormalX(),block.getNormalY(),block.getNormalZ()).normalize(),"quad.BINmodel"+name);
							result.addTriangle(tris[0]);
							result.addTriangle(tris[1]);
							}
						else if(vertIndices.size()==3)//Triangles
							{Triangle t = new Triangle(currentTexture);
							try{t.setCentroidNormal(new Vector3D(block.getNormalX(),block.getNormalY(),block.getNormalZ()).normalize());}
							catch(MathArithmeticException ee){t.setCentroidNormal(Vector3D.ZERO);}
							t.setAlphaBlended(hasAlpha);
							t.setRenderMode(RenderMode.DYNAMIC);
							
							for(int vi=0; vi < 3; vi++){
							    final org.jtrfp.trcl.gpu.Vertex vtx=
								    vertices.get(vertIndices.get(vi).getVertexIndex()-(b instanceof FaceBlock05?m.getUnknown2():0));
							    t.setVertex(vtx, vi);
								if(b instanceof FaceBlock05)
								    t.setUV(new Vector2D(BOX_U[vi],BOX_V[vi]), vi);
								else{
								    t.setUV(new Vector2D(
									(double)vertIndices.get(vi).getTextureCoordinateU()/(double)0xFF0000,
									1.-(double)vertIndices.get(vi).getTextureCoordinateV()/(double)0xFF0000), vi);}
							}//end for(vi)
							if(currentTexture==null)
								{System.err.println("WARNING: Texture never set for "+name+". Using fallback.");currentTexture=tr.gpu.get().textureManager.get().getFallbackTexture();}
							result.addTriangle(t);
							}//end if(3 vertices)
						else
							{System.err.println("ResourceManager: FaceBlock has "+vertIndices.size()+" vertices. Only 3 or 4 supported.");}
						}//end if(FaceBlock)
					else if(b instanceof FaceBlock19)
						{System.out.println(b.getClass().getSimpleName()+" (solid colored faces) not yet implemented. Skipping...");}
					else if(b instanceof FaceBlock05){}//TODO
					else if(b instanceof LineSegmentBlock){
						LineSegmentBlock block = (LineSegmentBlock)b;
						org.jtrfp.trcl.gpu.Vertex v1 = vertices.get(block.getVertexID1());
						org.jtrfp.trcl.gpu.Vertex v2 = vertices.get(block.getVertexID2());
						if(!alreadyVisitedLineSegs.contains(v1.hashCode()*v2.hashCode())){
						    Triangle [] newTris = new Triangle[6];
						   
						    LineSegment.buildTriPipe(v1.getPosition(), v2.getPosition(), 
							    tr.gpu.get().textureManager.get().getDefaultTriPipeTexture(), 
							    200, newTris, 0);
						    result.addTriangles(newTris);
						    alreadyVisitedLineSegs.add(v1.hashCode()*v2.hashCode());
						}//end if(not already visited)
					}//end if(LineSegmentBlock)
					else if(b instanceof Unknown12){
					    System.out.println("Found unknown12. Assuming this is a tag for a transparent texture...");
					    hasAlpha=true;
					}
					else if(b instanceof AnimatedTextureBlock){
						System.out.println("Found animated texture block.");
						AnimatedTextureBlock block = (AnimatedTextureBlock)b;
						List<String> frames = block.getFrameNames();
						double timeBetweenFramesInMillis = ((double)block.getDelay()/65535.)*1000.;
						TRFutureTask<Texture> [] subTextures = new TRFutureTask[frames.size()];
						for(int ti=0; ti<frames.size(); ti++){
							if(!hasAlpha)subTextures[ti]=(TRFutureTask)getRAWAsTexture(frames.get(ti), palette, GammaCorrectingColorProcessor.singleton,gl);
							else subTextures[ti]=(TRFutureTask)getRAWAsTexture(frames.get(ti), palette, GammaCorrectingColorProcessor.singleton,gl,true);
							//subTextures[ti]=tex instanceof Texture?new DummyTRFutureTask<Texture>((Texture)tex):(Texture)Texture.getFallbackTexture();
							}//end for(frames) //fDelay, nFrames,interp
						currentTexture = new DummyTRFutureTask<TextureDescription>(new AnimatedTexture(new Sequencer((int)timeBetweenFramesInMillis,subTextures.length,false),subTextures));
						}
					else if(b instanceof EOFBlock)
						{System.out.println("...That's all, end of BIN");}
					else
						{System.out.println("Failed to identify DataBlock: "+b.getClass().getName());}
					}//end for(dataBlocks)
				result.finalizeModel();
				result.setDebugName(name);
				if(cache)modelCache.put(name, result);
				return result;
				}//end try{}
			catch(UnrecognizedFormatException ee){
				//Not-good fail
				throw new UnrecognizedFormatException("Can't figure out what this is: "+name+". Giving up. Expect trouble ahead.");
				}
			}//end catch(ok fail)
		//Bad fail.
		}//end getBINModel()
	
	private BufferedImage [] getSpecialRAWImage(String name, Color [] palette, ColorProcessor proc, int upscalePowerOfTwo) throws IllegalAccessException, FileLoadException, IOException{
		RAWFile dat = getRAW(name);
		dat.setPalette(palette);
		BufferedImage [] segs = dat.asSegments(upscalePowerOfTwo);
		for(BufferedImage seg:segs){
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
	public BufferedImage getRAWImage(String name, Color [] palette, ColorProcessor proc) throws IOException, FileLoadException, IllegalAccessException, NotSquareException, NonPowerOfTwoException{
		RAWFile dat = getRAW(name);
		
		byte [] raw = dat.getRawBytes();
		if(raw.length!=dat.getSideLength()*dat.getSideLength()) throw new NotSquareException(name);
		if((dat.getSideLength() & (dat.getSideLength()-1))!=0) throw new NonPowerOfTwoException(name);
		final BufferedImage stamper = new BufferedImage(dat.getSideLength(),dat.getSideLength(),BufferedImage.TYPE_INT_ARGB);
		Graphics stG = stamper.getGraphics();
		for(int i=0; i<raw.length; i++){
			Color c= palette[(int)raw[i] & 0xFF];
			stG.setColor(c);
			stG.fillRect(i%dat.getSideLength(), i/dat.getSideLength(), 1, 1);
			}
		stG.dispose();
		Graphics g = stamper.getGraphics();
		//The following code stamps the filename into the texture for debugging purposes
		if(tr.isStampingTextures()){
			g.setFont(new Font(g.getFont().getName(),g.getFont().getStyle(),9));
			g.drawString(name, 1, 16);
			}
		
		g.dispose();
		return stamper;
		}//end getRAWImage
	
	public AltitudeMap getRAWAltitude(String name) throws IOException, FileLoadException, IllegalAccessException{
	    	return new RawAltitudeMapWrapper(new RAWFile(getInputStreamFromResource("DATA\\"+name)));
		}//end getRAWAltitude
	
	public TextureMesh getTerrainTextureMesh(String name, TRFutureTask<TextureDescription>[] texturePalette) throws IOException, FileLoadException, IllegalAccessException{
		final CLRFile	dat = new CLRFile(getInputStreamFromResource("DATA\\"+name));
		return new RawTextureMeshWrapper(dat,texturePalette);
		}//end getRAWAltitude
	
	public String [] getTEXListFile(String name) throws IOException, FileLoadException, IllegalAccessException{
		return TexDataLoader.load(getInputStreamFromResource("DATA\\"+name)).getTextureNames();
		}//end getTEXListFile
	
	public Color [] getPalette(String name) throws IOException, FileLoadException, IllegalAccessException{
		ActColor [] actColors= ActDataLoader.load(getInputStreamFromResource("ART\\"+name)).getColors();
		
		Color [] result = new Color[actColors.length];
		for(int i=0; i<result.length;i++){
			result[i]=new Color(actColors[i].getComponent1(),actColors[i].getComponent2(),actColors[i].getComponent3());
			}
		return result;
		}//end getTEXListFile

	public DEFFile getDEFData(String enemyDefinitionAndPlacementFile) throws FileNotFoundException, IOException, IllegalAccessException, FileLoadException{
		return new DEFFile(getInputStreamFromResource("DATA\\"+enemyDefinitionAndPlacementFile));
		}
	
	public TDFFile getTDFData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		TDFFile result = new Parser().readToNewBean(is, TDFFile.class);
		is.close();
		return result;
		}
	public TNLFile getTNLData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		TNLFile result = new Parser().readToNewBean(is, TNLFile.class);
		is.close();
		return result;
		}
	public NAVFile getNAVData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		NAVFile result = new Parser().readToNewBean(is, NAVFile.class);
		is.close();
		return result;
		}
	public Font getFont(String zipName, String fontFileName) throws IOException, FontFormatException{
	    	zipName="/fonts/"+zipName;
		if(zipName.toUpperCase().endsWith(".ZIP")){//Search the zip
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

	public PUPFile getPUPData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
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

	public ByteBuffer []getFontBIN(String fontPath, NDXFile ndx) {
	    try{
	     InputStream is = getInputStreamFromResource(fontPath);
	     List<Integer>widths=ndx.getWidths();
	     ByteBuffer [] result = new ByteBuffer[widths.size()];
	     for(int c=0; c<ndx.getWidths().size(); c++){
		 final int len = 23*widths.get(c);
		 result[c]=ByteBuffer.allocate(23*widths.get(c)*4);
		 result[c].order(ByteOrder.LITTLE_ENDIAN);
		 for(int i=0; i<len; i++){
		     byte b= (byte)(is.read());
		     result[c].put(b);
		     result[c].put(b);
		     result[c].put(b);
		     result[c].put((byte)((b&0xFF)>1?255:b));//Less than 1 is alpha'd out
		 }
		 result[c].clear();
	     }//end for(chars)
	     return result;
	    }catch(Exception e){e.printStackTrace();return null;}
	}//end getFontBIN(...)

	public NDXFile getNDXFile(String resString) {
	    try{return new NDXFile().read(getInputStreamFromResource(resString));}
	    catch(Exception e){e.printStackTrace();return null;}
	}

	/**
	 * @return the smokeFactory
	 */
	public SmokeFactory getSmokeFactory() {
	    return smokeFactory;
	}

	/**
	 * @param smokeFactory the smokeFactory to set
	 */
	public void setSmokeFactory(SmokeFactory smokeFactory) {
	    this.smokeFactory = smokeFactory;
	}
}//end ResourceManager
