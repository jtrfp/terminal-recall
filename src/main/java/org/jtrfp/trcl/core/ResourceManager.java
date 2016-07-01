/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.core;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.media.opengl.GL3;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

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
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.NonPowerOfTwoException;
import org.jtrfp.trcl.RawAltitudeMapWrapper;
import org.jtrfp.trcl.RawTextureMeshWrapper;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.SoftValueHashMap;
import org.jtrfp.trcl.TextureMesh;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.TRConfigRootFactory.TRConfigRoot;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.BINFile;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.AnimatedTextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.BillboardTexCoords0x04;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.ColorBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.EOFBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertex;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertexWithUV;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock05;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.LineSegmentBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.TextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.Unknown12;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.VertexColorBlock;
import org.jtrfp.trcl.file.CLRFile;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LTEFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.NAVFile;
import org.jtrfp.trcl.file.NDXFile;
import org.jtrfp.trcl.file.NotSquareException;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.file.RAWFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TNLFile;
import org.jtrfp.trcl.file.TXTMissionBriefFile;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.flow.FZone;
import org.jtrfp.trcl.flow.Fury3;
import org.jtrfp.trcl.flow.TV;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.UncompressedVQTextureFactory;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.img.vq.PalettedVectorList;
import org.jtrfp.trcl.img.vq.RAWVectorList;
import org.jtrfp.trcl.img.vq.VectorList;
import org.jtrfp.trcl.obj.DebrisSystem;
import org.jtrfp.trcl.obj.ExplosionSystem;
import org.jtrfp.trcl.obj.PowerupSystem;
import org.jtrfp.trcl.obj.ProjectileFactory;
import org.jtrfp.trcl.obj.SmokeSystem;
import org.jtrfp.trcl.pool.ObjectFactory;
import org.jtrfp.trcl.snd.GPUResidentMOD;
import org.jtrfp.trcl.snd.SoundTexture;

import com.ochafik.util.Adapter;

import de.quippy.javamod.multimedia.mod.loader.Module;
import de.quippy.javamod.multimedia.mod.loader.ModuleFactory;

public class ResourceManager{
	private final Map<String,IPodData> pods = new HashMap<String,IPodData>();
	private SoftValueHashMap<Integer, Texture> 
	/*						*/	 rawCache 
		= new SoftValueHashMap<Integer,Texture>();
    	private SoftValueHashMap<String, Texture[]> 
    								specialTextureNameMap 	
    		= new SoftValueHashMap<String,Texture[]>();
	private SoftValueHashMap<String, BINFile.AnimationControl>aniBinNameMap 	
		= new SoftValueHashMap<String,BINFile.AnimationControl>();
	private SoftValueHashMap<String, BINFile.Model> 	modBinNameMap 		
		= new SoftValueHashMap<String,BINFile.Model>();
	private SoftValueHashMap<String, Model> 		modelCache 		
		= new SoftValueHashMap<String,Model>();
	private SoftValueHashMap<String, Module> 		modCache 		
		= new SoftValueHashMap<String,Module>();
	private ExplosionSystem 				explosionFactory;
	private SmokeSystem 					smokeSystem;
	private PowerupSystem 					powerupSystem;
	private DebrisSystem 					debrisSystem;
	private ProjectileFactory [] 				projectileFactories;
	private final TR 					tr;
	private Texture				testTexture;
	
	public final ObjectFactory<String,GPUResidentMOD>	gpuResidentMODs;
	public final ObjectFactory<String,SoundTexture>	soundTextures;
	private TRConfigRoot configManager;
	private UncompressedVQTextureFactory uncompressedVQTextureFactory;
	
	public ResourceManager(final TR tr){
		this.tr=tr;
		try{Class.forName("de.quippy.javamod.multimedia.mod.loader.tracker.ProTrackerMod");
		    Class.forName("de.quippy.javamod.multimedia.mod.ModContainer"); // ModContainer uses the ModFactory!!
		    }
		catch(Exception e){tr.showStopper(e);}
		gpuResidentMODs = 
		 new ObjectFactory<String, GPUResidentMOD>(new SoftValueHashMap<String,GPUResidentMOD>(),new Adapter<String,GPUResidentMOD>(){

		    @Override
		    public GPUResidentMOD adapt(String value) {
			return new GPUResidentMOD(tr,getMOD(value));
		    }

		    @Override
		    public String reAdapt(GPUResidentMOD value) {
			// TODO Auto-generated method stub
			return null;
		    }});
	 	soundTextures =
	 	 new ObjectFactory<String,SoundTexture>(new SoftValueHashMap<String,SoundTexture>(),new Adapter<String,SoundTexture>(){
		    @Override
		    public SoundTexture adapt(String key) {
			try{
			    final AudioInputStream ais = AudioSystem.getAudioInputStream(getInputStreamFromResource("SOUND\\"+key));
			    final FloatBuffer fb       = ByteBuffer.allocateDirect((int)ais.getFrameLength()*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
			    int value;
			    while((value=ais.read())!=-1){
				fb.put(((float)(value-128))/128f);
			    }fb.clear();
			    return tr.soundSystem.get().newSoundTexture(fb, (int)ais.getFormat().getFrameRate());
			}catch(Exception e){tr.showStopper(e);return null;}
		    }//end adapt(...)

		    @Override
		    public String reAdapt(SoundTexture value) {
			// TODO Auto-generated method stub
			return null;
		    }});
		    
		setupPODListeners();
	}//end ResourceManager
	
	private void setupPODListeners(){
	    //final TRConfiguration config = tr.config;
	    final TRConfiguration trConfig = Features.get(tr, TRConfiguration.class);
	    final DefaultListModel podList = trConfig.getPodList();
	    new ListModelSetBridge<String>(podList,new SetModelListener<String>(){
		@Override
		public void added(String item) {
		    if(item!=null)
		     try{ResourceManager.this.registerPOD(new File(item));}
		     catch(FileLoadException e){
			JOptionPane.showMessageDialog(tr.getRootWindow(), 
				"Failed to parse a PODfile:"+e.getLocalizedMessage(), 
				"Failed To Load POD", JOptionPane.ERROR_MESSAGE);
		    }//end catch(e)
		}//end added()

		@Override
		public void removed(String item) {
		    if(item!=null)
		     ResourceManager.this.deregisterPOD(item);
		}});
	}//end setupPODListeners
	
	/**
	 * @return the explosionFactory
	 */
	public ExplosionSystem getExplosionFactory() {
	    return explosionFactory;
	}
	public void setExplosionFactory(ExplosionSystem ef){
	    explosionFactory=ef;
	}
	
	public LVLFile getLVL(String name) throws IllegalAccessException, FileNotFoundException, IOException, FileLoadException{
		System.out.println("Getting level "+name);
		return new LVLFile(getInputStreamFromResource("LEVELS\\"+name));
		}//end getLVL
	
	private InputStream getInputStreamFromResource(String name) throws FileNotFoundException, FileLoadException, IOException{
		System.out.println("Getting resource: "+name);
		IPodFileEntry ent;
		String localPath = name;
		if(name.startsWith("java:\\"))
		    return new BufferedInputStream(this.getClass().getResourceAsStream(name.substring(7)));
		if(File.separatorChar != '\\' )
		    localPath = localPath.replace('\\', File.separatorChar);
		final File localPathAttempt = new File(localPath.toLowerCase());
		if(localPathAttempt.exists())
		    return new BufferedInputStream(new FileInputStream(localPathAttempt));
		for(IPodData p:pods.values())
			if((ent=p.findEntry(name))!=null)
				return new BufferedInputStream(ent.getInputStreamFromPod());
		throw new FileNotFoundException(name);
		}//end getInputStreamFromResource(...)
	
	public void deregisterPOD(String podToDeregister){
	    if(podToDeregister==null)throw new NullPointerException("fileToDeregister should not be null.");
	    pods.remove(podToDeregister);
	}
	
	public void registerPOD(String key, PodFile podToRegister) throws FileLoadException{
	    if(podToRegister==null)throw new NullPointerException("fileToRegister should not be null.");
	    pods.put(key,podToRegister.getData());
	}
	
	public void registerPOD(File fileToRegister) throws FileLoadException{
		if(fileToRegister==null)throw new NullPointerException("fileToRegister should not be null.");
		System.out.println("Register pod "+fileToRegister);
		registerPOD(fileToRegister.getAbsolutePath(),new PodFile(fileToRegister));
		}
	
	public Texture [] getTextures(String texFileName, ColorPaletteVectorList paletteRGBA, ColorPaletteVectorList paletteESTuTv,  boolean uvWrapping) throws IOException, FileLoadException, IllegalAccessException{
		if(texFileName==null)
		    throw new NullPointerException("texFileName is intolerably null");
		if(paletteRGBA==null)
		    throw new NullPointerException("paletteRGBA is intolerably null");
	    	String [] files = getTEXListFile(texFileName);
		Texture [] result = new Texture[files.length];
		for(int i=0; i<files.length;i++)
			{result[i]=getRAWAsTexture(files[i],paletteRGBA,paletteESTuTv,uvWrapping);}
		return result;
		}//end loadTextures(...)
	
	public Texture[] getSpecialRAWAsTextures(String name, Color [] palette, GL3 gl, int upScalePowerOfTwo, boolean uvWrapping) {
		try{
	    	Texture [] result = specialTextureNameMap.get(name);
		if(result==null){
		    BufferedImage [] segs = getSpecialRAWImage(name, palette, upScalePowerOfTwo);
			result=new Texture[segs.length];
			for(int si=0; si<segs.length; si++)
				{result[si] = getUncompressedVQTextureFactory().newUncompressedVQTexture(segs[si],null,"name",uvWrapping);}
			specialTextureNameMap.put(name,result);
			}//end if(result=null)
		return result;
		}catch(Exception e){tr.showStopper(e);}
		return null;//never happens.
		}//end getSpecialRAWAsTextures
	
	public Texture getRAWAsTexture(String name, final ColorPaletteVectorList paletteRGBA, final ColorPaletteVectorList paletteESTuTv, boolean uvWrapping) throws IOException, FileLoadException, IllegalAccessException{
	    return getRAWAsTexture(name,paletteRGBA,paletteESTuTv,true,uvWrapping);
	}
	
	public Texture getRAWAsTexture(final String name, final ColorPaletteVectorList paletteRGBA,
			ColorPaletteVectorList paletteESTuTv, final boolean useCache, final boolean uvWrapping) throws IOException, FileLoadException, IllegalAccessException{
	    if(name==null)
		throw new NullPointerException("Name is intolerably null.");
	    if(paletteRGBA==null)
		throw new NullPointerException("paletteRGBA is intolerably null.");
	    final int hash=name.hashCode()*paletteRGBA.hashCode();
	        Texture result=rawCache.get(hash);
	    	if(result!=null&&useCache)return result;
			try {    if(name.substring(name.length()-5, name.length()-4).contentEquals("0") && TRFactory.ANIMATED_TERRAIN)
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
						VQTexture [] tFrames = new VQTexture[frames.size()];
						for(int i=0; i<tFrames.length;i++){
						    PalettedVectorList pvlRGBA  = getRAWVectorList(frames.get(i),paletteRGBA);
						    PalettedVectorList pvlESTuTv= getRAWVectorList(frames.get(i),paletteESTuTv);
						    tFrames[i]=getUncompressedVQTextureFactory().newUncompressedVQTexture(pvlRGBA,pvlESTuTv,""+frames.get(i),uvWrapping);}
						AnimatedTexture aTex = new AnimatedTexture(new Sequencer(500,tFrames.length,false), tFrames);
						return aTex;
						}//end if(multi-frame)
					}//end if(may be animated)
				result = getUncompressedVQTextureFactory().newUncompressedVQTexture(getRAWVectorList(name,paletteRGBA),paletteESTuTv!=null?getRAWVectorList(name,paletteESTuTv):null,name,uvWrapping);
				}
			catch(NotSquareException e){
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=tr.gpu.get().textureManager.get().getFallbackTexture();
				}
			catch(NonPowerOfTwoException e){
				System.err.println(e.getMessage());
				System.err.println("Using fallback texture.");
				result=tr.gpu.get().textureManager.get().getFallbackTexture();
				}
			catch(Exception e){e.printStackTrace();result=null;}
		if(useCache)rawCache.put(name.hashCode()*paletteRGBA.hashCode(), result);
		return result;
		}//end getRAWAsTexture(...)
	
	public boolean rawExists(String name){
		for(IPodData p:pods.values()){
			if((p.findEntry("ART\\"+name))!=null){
				System.out.println(name+" found to exist. Returning true...");
				return true;
				}
			}//end for(podFiles)
		System.out.println(name+" found to not exist. Returning false...");
		return false;
		}//end rawExists
	
	public Model getBINModel(String name, ColorPaletteVectorList palette,ColorPaletteVectorList paletteESTuTv, GL3 gl) throws FileLoadException, IOException, IllegalAccessException{
		return getBINModel(name,tr.gpu.get().textureManager.get().getFallbackTexture(),1,true,palette,paletteESTuTv);
		}
	
	private static final double [] BOX_U = new double[]{0,1,1,0};
	private static final double [] BOX_V = new double[]{0,0,1,1};
	
	public BINFile.AnimationControl getAnimationControlBIN(String name) throws FileNotFoundException, FileLoadException, IOException, IllegalAccessException{
	    BINFile.AnimationControl result;
	    result = aniBinNameMap.get(name);
	    if(result!=null)
		    return result;
	    InputStream is = getInputStreamFromResource("MODELS\\"+name);
	    //TODO: InputStream not guaranteed to close when exception is thrown. Wrap in try{}, close it, and re-throw.
	    result = new BINFile.AnimationControl(is);//This will throw an exception on and escape to the static model block
	    is.close();
	    aniBinNameMap.put(name, result);
	    return result;
	}
	
	public BINFile.Model getBinFileModel(String name) throws FileNotFoundException, FileLoadException, IOException, IllegalAccessException{
	    BINFile.Model result;
	    result = modBinNameMap.get(name);
	    if(result==null){
		InputStream is = getInputStreamFromResource("MODELS\\"+name);
		result = new BINFile.Model(is);
		modBinNameMap.put(name, result);
	    }//end if(null)
	    return result;
	}//end getBinFileModel()
	
	public Model getBINModel(String name,Texture defaultTexture,double scale,boolean cache, ColorPaletteVectorList palette, ColorPaletteVectorList ESTuTvPalette) throws FileLoadException, IOException, IllegalAccessException{
	    	if(name==null)throw new NullPointerException("Name is intolerably null");
		if(palette==null)throw new NullPointerException("Palette is intolerably null");
		if(modelCache.containsKey(name)&& cache)return modelCache.get(name);
		//The models like to set up two line segments where there should be one. 
		//This set is for identifying and culling redundant segs.
		final HashSet<Integer>alreadyVisitedLineSegs = new HashSet<Integer>();
		boolean hasAlpha=false;
		boolean skipLighting=false;
		try {
			BINFile.AnimationControl ac=null;
			Model result = new Model(true,tr,"BINModel."+name);
			ac = getAnimationControlBIN(name);
			System.out.println("Recognized as animation control file.");
			//Build the Model from the BINFile.Model
			Model [] frames = new Model[ac.getNumFrames()];
			for(int i=0; i<frames.length;i++)
				{frames[i]=getBINModel(ac.getBinFiles().get(i),defaultTexture,scale,cache,palette, ESTuTvPalette);}
			result.setDebugName(name+" triangles: "+frames[0].getRawTriangleLists().get(0).size());
			//Consolidate the frames to one model
			for(int i=0; i<frames.length;i++)
				{result.addFrame(frames[i]);}
			result.setFrameDelayInMillis((int)(((double)ac.getDelay()/65535.)*1000.));
			if(cache)modelCache.put(name, result);
			return result;
			}//end try{}
		catch(UnrecognizedFormatException e){//ok fail. Static model
			try	{
				BINFile.Model m=null;
				Model result = new Model(false,tr,"StaticBinModel."+name);
				result.setDebugName(name);
				m = getBinFileModel(name);
				
				final double cpScalar=(scale*TRFactory.crossPlatformScalar*256.)/(double)m.getScale();
				System.out.println("Recognized as model file.");
				List<org.jtrfp.trcl.gpu.Vertex> vertices = new ArrayList<org.jtrfp.trcl.gpu.Vertex>();
				for(BINFile.Model.Vertex binVtx:m.getVertices()){
				    vertices.add(new org.jtrfp.trcl.gpu.Vertex().setPosition(new Vector3D(
						    binVtx.getX()*cpScalar,
						    binVtx.getY()*cpScalar,
						    binVtx.getZ()*cpScalar)));
				}//end try{}
				
				Texture currentTexture=null;
				final double [] u = new double[4];
				final double [] v = new double[4];
				for(ThirdPartyParseable b:m.getDataBlocks()){
					//Sort out types of block
					if(b instanceof TextureBlock){
						TextureBlock tb = (TextureBlock)b;
						if(hasAlpha)currentTexture = getRAWAsTexture(tb.getTextureFileName(), palette, ESTuTvPalette, hasAlpha);
						else{currentTexture = getRAWAsTexture(tb.getTextureFileName(), palette, ESTuTvPalette, false);}
						System.out.println("ResourceManager: TextureBlock specifies texture: "+tb.getTextureFileName());
						}//end if(TextureBlock)
					else if(b instanceof FaceBlock){
					    	//System.out.println("FaceBlock found: "+b.getClass().getSimpleName());
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
								{vtx[i]=vertices.get(vertIndices.get(i).getVertexIndex()%(b instanceof FaceBlock05?100:Integer.MAX_VALUE));}
							Vector3D blockNormal = new Vector3D(block.getNormalX(),block.getNormalY(),block.getNormalZ());
							if(blockNormal.getNorm()==0)blockNormal = new Vector3D(1,0,0);//Use filler if zero norm.
							if(vertIndices.get(0) instanceof FaceBlockVertexWithUV){
							    for(int i=0; i<4; i++){
								final FaceBlockVertexWithUV fbvi = (FaceBlockVertexWithUV)vertIndices.get(i);
								u[i]=1.-(double)(fbvi).
									getTextureCoordinateU()/(double)0xFF0000;
								v[i]=(double)(fbvi).
									getTextureCoordinateV()/(double)0xFF0000;
							    }//end for(4)
							}else{
							    System.arraycopy(BOX_U, 0, u, 0, 4);
							    System.arraycopy(BOX_V, 0, v, 0, 4);
							}
							Triangle [] tris = Triangle.quad2Triangles(
									vtx,
									new Vector2D[]{
										new Vector2D(
											u[0],
											1.-v[0]),
										new Vector2D(
											u[1],
											1.-v[1]),
										new Vector2D(
											u[2],
											1.-v[2]),
										new Vector2D(
											u[3],
											1.-v[3])
									},
									currentTexture,
									RenderMode.DYNAMIC,hasAlpha,
									blockNormal.normalize(),"quad.BINmodel"+name);
							if(skipLighting){
							    tris[0].setCentroidNormal(Vector3D.ZERO);
							    tris[1].setCentroidNormal(Vector3D.ZERO);
							}
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
								if(b instanceof FaceBlock05 || !(vertIndices.get(0) instanceof FaceBlockVertexWithUV))
								    t.setUV(new Vector2D(BOX_U[vi],BOX_V[vi]), vi);
								else {
								    t.setUV(new Vector2D(
									(double)((FaceBlockVertexWithUV)vertIndices.get(vi)).getTextureCoordinateU()/(double)0xFF0000,
									1.-(double)((FaceBlockVertexWithUV)vertIndices.get(vi)).getTextureCoordinateV()/(double)0xFF0000), vi);}
							}//end for(vi)
							if(currentTexture==null)
								{System.err.println("WARNING: Texture never set for "+name+". Using fallback.");currentTexture=tr.gpu.get().textureManager.get().getFallbackTexture();}
							if(skipLighting)
							    t.setCentroidNormal(Vector3D.ZERO);
							result.addTriangle(t);
							}//end if(3 vertices)
						else
							{System.err.println("ResourceManager: FaceBlock has "+vertIndices.size()+" vertices. Only 3 or 4 supported.");}
						}//end if(FaceBlock)
					else if(b instanceof ColorBlock){
					    final ColorBlock cb = (ColorBlock)b;
					    final byte [] bytes = cb.getBytes();
					    final int colorID = 16+(bytes[0]&0xFF);
					    
					    final Color color = new Color((float)palette.componentAt(colorID, 0),(float)palette.componentAt(colorID, 1),(float)palette.componentAt(colorID, 2),.5f);
					    System.out.println("Color block: "+color+" colorID="+colorID);
					    currentTexture = tr.gpu.get().textureManager.get().solidColor(color);
					}
					else if(b instanceof BillboardTexCoords0x04){
					    hasAlpha=true;
					    skipLighting=true;
					    System.out.println("0x04 tag billboard/sprite.");
					}else if(b instanceof VertexColorBlock){
					    //TODO: finish implementation.
					    System.out.println("Found Vertex Color Block. Note: Implementation not complete.");
					    final VertexColorBlock vcb = (VertexColorBlock)b;
					    List<Long> indices = vcb.getPaletteIndices();
					    if(!indices.isEmpty())
						currentTexture = tr.gpu.get().textureManager.get().solidColor(tr.getGlobalPalette()[indices.get(0).intValue()]);
					}else if(b instanceof LineSegmentBlock){
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
					    skipLighting=true;
					}
					else if(b instanceof AnimatedTextureBlock){
						System.out.println("Found animated texture block.");
						AnimatedTextureBlock block = (AnimatedTextureBlock)b;
						List<String> frames = block.getFrameNames();
						double timeBetweenFramesInMillis = ((double)block.getDelay()/65535.)*1000.;
						VQTexture [] subTextures = new VQTexture[frames.size()];
						for(int ti=0; ti<frames.size(); ti++){
							if(!hasAlpha)subTextures[ti]=(VQTexture)getRAWAsTexture(frames.get(ti), palette,ESTuTvPalette, false);
							else subTextures[ti]=(VQTexture)getRAWAsTexture(frames.get(ti), palette,ESTuTvPalette, true);
							//subTextures[ti]=tex instanceof Texture?new DummyTRFutureTask<Texture>((Texture)tex):(Texture)Texture.getFallbackTexture();
							}//end for(frames) //fDelay, nFrames,interp
						currentTexture = new AnimatedTexture(new Sequencer((int)timeBetweenFramesInMillis,subTextures.length,false),subTextures);
						}
					else if(b instanceof EOFBlock)
						{System.out.println("...That's all, end of BIN");}
					else
						{System.out.println("Failed to identify DataBlock: "+b.getClass().getName());}
					}//end for(dataBlocks)
				result.setDebugName(name);
				if(cache)modelCache.put(name, result);
				return result;
				}//end try{}
			catch(UnrecognizedFormatException ee){//Bad fail
				throw new UnrecognizedFormatException("Can't figure out what this is: "+name+". Giving up. Expect trouble ahead.");
				}//end catch(bad fail)
			}//end catch(ok fail)
		}//end getBINModel()
	
	private BufferedImage [] getSpecialRAWImage(String name, Color [] palette, int upscalePowerOfTwo) throws IllegalAccessException, FileLoadException, IOException{
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
	public BufferedImage getRAWImage(String name, Color [] palette) throws IOException, FileLoadException, IllegalAccessException, NotSquareException, NonPowerOfTwoException{
		final RAWFile dat = getRAW(name);
		final byte [] raw = dat.getRawBytes();
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
	
	public PalettedVectorList getRAWVectorList(String name, VectorList palette) throws IOException, FileLoadException, IllegalAccessException, NotSquareException, NonPowerOfTwoException{
	    if(name==null)
		throw new NullPointerException("Name is intolerably null");
	    if(palette==null)
		throw new NullPointerException("Palette is intolerably null");
	    final RAWFile raw =  getRAW(name);
	    return new PalettedVectorList(new RAWVectorList(raw),palette);
	}//end getRAWVectorList()
	
	public AltitudeMap getRAWAltitude(String name) throws IOException, FileLoadException, IllegalAccessException{
	    	return new RawAltitudeMapWrapper(new RAWFile(getInputStreamFromResource("DATA\\"+name)));
		}//end getRAWAltitude
	
	public TextureMesh getTerrainTextureMesh(String name, Texture[] texturePalette) throws IOException, FileLoadException, IllegalAccessException{
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
		}//end getPalette

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

    public Font getFont(String zipName, String fontFileName) {
	    zipName = "/fonts/" + zipName;
	    if (zipName.toUpperCase().endsWith(".ZIP")) {// Search the zip
		InputStream is = ResourceManager.class.getResourceAsStream(zipName); 
		ZipInputStream zip = new ZipInputStream(
			is);
		ZipEntry entry;
		try{
		while ((entry = zip.getNextEntry()) != null) {
		    System.out.println("ZIP ENTRY: " + entry.getName());
		    if (entry.getName().toUpperCase()
			    .endsWith(fontFileName.toUpperCase())){
			Font font = Font.createFont(Font.TRUETYPE_FONT, zip);
			zip.closeEntry();
			zip.close();
			return font;
		    }
		}// end while(elements)
		}catch(Exception e){e.printStackTrace();}
		try{zip.closeEntry();}catch(Exception e){e.printStackTrace();}
		try{zip.close();}     catch(Exception e){e.printStackTrace();}
		try{is.close();}      catch(Exception e){e.printStackTrace();}
	    }// end if(zip)
	    else {
	    }// TODO: Handle non-zipped fonts?
	return null;
    }// end getFont(...)

	public PUPFile getPUPData(String fileName) throws IllegalAccessException, UnrecognizedFormatException, FileNotFoundException, FileLoadException, IOException{
		InputStream is = getInputStreamFromResource("DATA\\"+fileName);
		PUPFile result = new Parser().readToNewBean(is, PUPFile.class);
		is.close();
		return result;
		}

	public void setPowerupSystem(
		PowerupSystem powerupSystem) {
	    this.powerupSystem=powerupSystem;
	    
	}
	public PowerupSystem getPowerupSystem(){return powerupSystem;}

	public DebrisSystem getDebrisSystem() {
	    return debrisSystem;
	}
	public void setDebrisSystem(DebrisSystem debrisSystem) {
	    this.debrisSystem = debrisSystem;
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

	public NDXFile getNDXFile(String resString) throws FileNotFoundException, FileLoadException, IOException {
	    return new NDXFile().read(getInputStreamFromResource(resString));
	}

	/**
	 * @return the smokeFactory
	 */
	public SmokeSystem getSmokeSystem() {
	    return smokeSystem;
	}

	/**
	 * @param smokeFactory the smokeFactory to set
	 */
	public void setSmokeSystem(SmokeSystem smokeFactory) {
	    this.smokeSystem = smokeFactory;
	}

	public VOXFile getVOXFile(String fileName) throws IllegalAccessException, IOException, FileLoadException {
	    if(fileName==null)throw new NullPointerException("Passed VOX file name String is intolerably null.");
	    //Special cases: Fury3, TV, fZone
	    if(fileName.contentEquals("Fury3"))
	    	return Fury3.getDefaultMission();
	    if(fileName.contentEquals("TV"))
	    	return TV.getDefaultMission();
	    if(fileName.contentEquals("FurySE"))
	    	return FZone.getDefaultMission();
	    final InputStream is = new FileInputStream(new File(fileName));
		VOXFile result = new Parser().readToNewBean(is, VOXFile.class);
		is.close();
		return result;
	}//end getVOXFile(...)

	public TXTMissionBriefFile getMissionText(String missionStartTextFile) {
	    try{
	    return new Parser().readToNewBean(getInputStreamFromResource("DATA\\"+missionStartTextFile), TXTMissionBriefFile.class);
	    }catch(Exception e)
		{tr.showStopper(e);}
	    assert false;
	    return null;
	}//end getMissionText(...)

	public Module getMOD(String podPath) {
	    Module result = modCache.get(podPath);
	    if(result!=null)
		return result;
	    try{
	     final InputStream is= getInputStreamFromResource("MUSIC\\"+podPath);
	     final File tempFile = File.createTempFile("org.jtrfp.trcl.mod", podPath);
	     final FileOutputStream os = new FileOutputStream(tempFile);
	     while(is.available()>0)
		os.write(is.read());//Slow but it's a MOD so it won't matter much.
	     is.close();
	     os.close();
	     result = ModuleFactory.getInstance(tempFile);
	     modCache.put(podPath,result);
	    }catch(Exception e){tr.showStopper(e);}
	    return result;
	}//end getMOD(...)

	public Collection<IPodData> getRegisteredPODs() {
	    return pods.values();
	}
	
	public Texture getTestTexture(){
	    if(testTexture!=null)
		return testTexture;
	    InputStream is = null;
	    try{return testTexture = tr.gpu.get().textureManager.get().newTexture(
		    VQTexture.RGBA8FromPNG(is = this.getClass().getResourceAsStream("/testTexture.png")),null, "testTexture", true);}
	    finally{if(is!=null)try{is.close();}catch(Exception e){e.printStackTrace();}}
	}//end getTestTexture()
	
	public LTEFile getLTE(String resourceNameWithDirectoryPrefix) throws IOException, IllegalAccessException, UnrecognizedFormatException, FileLoadException{
	    final InputStream is = getInputStreamFromResource(resourceNameWithDirectoryPrefix);
	    final LTEFile result = new Parser().readToNewBean(is, LTEFile.class);
	    is.close();
	    return result;
	}

	public Texture solidColor(Color color) {
	    return tr.gpu.get().textureManager.get().solidColor(color);
	}

	public UncompressedVQTextureFactory getUncompressedVQTextureFactory() {
	    if(uncompressedVQTextureFactory == null)
		setUncompressedVQTextureFactory(new UncompressedVQTextureFactory(tr.gpu.get(), tr.threadManager, "ResourceManager"));
	    return uncompressedVQTextureFactory;
	}

	public void setUncompressedVQTextureFactory(
		UncompressedVQTextureFactory uncompressedVQTextureFactory) {
	    this.uncompressedVQTextureFactory = uncompressedVQTextureFactory;
	}

	public TRConfigRoot getConfigManager() {
	    if(configManager == null)
		configManager = Features.get(tr, TRConfigRoot.class);
	    return configManager;
	}

	public void setConfigManager(TRConfigRoot configManager) {
	    this.configManager = configManager;
	}
}//end ResourceManager
