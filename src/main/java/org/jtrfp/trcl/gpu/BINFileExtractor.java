package org.jtrfp.trcl.gpu;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.trcl.AnimatedTexture;
import org.jtrfp.trcl.LineSegment;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.Sequencer;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.file.BINFile;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.AnimatedTextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.BillboardTexCoords0x04;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.ColorBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.EOFBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertex;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock.FaceBlockVertexWithUV;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock05;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.FaceBlock19;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.LineSegmentBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.TextureBlock;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.Unknown12;
import org.jtrfp.trcl.file.BINFile.Model.DataBlock.VertexColorBlock;
import org.jtrfp.trcl.gpu.BasicModelTarget.WriterState;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;

public class BINFileExtractor {
    private static final double [] BOX_U = new double[]{0,1,1,0};
    private static final double [] BOX_V = new double[]{0,0,1,1};
    private final HashSet<Integer>alreadyVisitedLineSegs = new HashSet<Integer>();
    private ColorPaletteVectorList palette, ESTuTvPalette;
    private final ResourceManager rm;
    private Texture defaultTexture, triPipeTexture;
    private Color [] globalPalette;
    
    public BINFileExtractor(ResourceManager rm){
	this.rm=rm;
    }
    
    public void extract(BINFile.Model from, BasicModelTarget to) {
	final int SCALE     = 1;
	boolean hasAlpha    = false;
	boolean skipLighting= false;
	WriterState triangleData = new WriterState();
	try	{
		final double cpScalar=(SCALE*TRFactory.crossPlatformScalar*256.)/(double)from.getScale();
		System.out.println("Recognized as model file.");
		List<org.jtrfp.trcl.gpu.Vertex> vertices = new ArrayList<org.jtrfp.trcl.gpu.Vertex>();
		final double [] pos = new double[8];
		for(BINFile.Model.Vertex binVtx:from.getVertices()){
		    vertices.add(new org.jtrfp.trcl.gpu.Vertex().setPosition(new Vector3D(
				    binVtx.getX()*cpScalar,
				    binVtx.getY()*cpScalar,
				    binVtx.getZ()*cpScalar)));
		    pos[0]=binVtx.getX()*cpScalar;
		    pos[1]=binVtx.getY()*cpScalar;
		    pos[2]=binVtx.getZ()*cpScalar;
		    to.addVertex(pos);
		}//end try{}
		
		//TextureDescription currentTexture=null;
		final double [] u = new double[4];
		final double [] v = new double[4];
		for(ThirdPartyParseable b:from.getDataBlocks()){
			//Sort out types of block
			if(b instanceof TextureBlock){
			        if(palette != null && ESTuTvPalette != null){
			            TextureBlock tb = (TextureBlock)b;
					if(hasAlpha)triangleData.setTexture(rm.getRAWAsTexture(tb.getTextureFileName(), palette, ESTuTvPalette, hasAlpha,true));
					else{triangleData.setTexture(rm.getRAWAsTexture(tb.getTextureFileName(), palette, ESTuTvPalette, false, true));}
					System.out.println("ResourceManager: TextureBlock specifies texture: "+tb.getTextureFileName()); 
			        }else{triangleData.setTexture(defaultTexture);}
				}//end if(TextureBlock)
			else if(b instanceof FaceBlock){
			    	//System.out.println("FaceBlock found: "+b.getClass().getSimpleName());
				FaceBlock block = (FaceBlock)b;
				List<FaceBlockVertex>vertIndices = block.getVertices();
				if(triangleData.getTexture()==null){System.out.println("Warning: Face texture not specified. Using fallback texture.");triangleData.setTexture(defaultTexture);}
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
					Vector3D blockNormal = new Vector3D(block.getNormalX(),block.getNormalY(),block.getNormalZ());
					if(blockNormal.getNorm()==0)blockNormal = new Vector3D(1,0,0);//Use filler if zero norm.
					if(vertIndices.get(0) instanceof FaceBlockVertexWithUV){
					    for(int i=0; i<4; i++){
						final FaceBlockVertexWithUV fbvi = (FaceBlockVertexWithUV)vertIndices.get(i);
						u[i]=(double)(fbvi).
							getTextureCoordinateU()/(double)0xFF0000;
						v[i]=(double)(fbvi).
							getTextureCoordinateV()/(double)0xFF0000;
					    }//end for(4)
					}else{
					    u[0]=BOX_U[0];
					    v[0]=BOX_V[0];
					    u[1]=BOX_U[1];
					    v[1]=BOX_V[1];
					    u[2]=BOX_U[2];
					    v[2]=BOX_V[2];
					    u[3]=BOX_U[3];
					    v[3]=BOX_V[3];
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
							triangleData.getTexture(),
							RenderMode.DYNAMIC,hasAlpha,
							blockNormal.normalize(),"quad.BINmodel");
					if(skipLighting){
					    tris[0].setCentroidNormal(Vector3D.ZERO);
					    tris[1].setCentroidNormal(Vector3D.ZERO);
					}
					final int [] triangleIndices = triangleData.getVertices();
					for(int i=0; i<3; i++)
					    triangleIndices[i] = vertIndices.get(i).getVertexIndex();
					to.addPrimitive(triangleData);
					for(int i=0; i<3; i++)
					    triangleIndices[i] = vertIndices.get((i+2)%4).getVertexIndex();
					to.addPrimitive(triangleData);
					}
				else if(vertIndices.size()==3)//Triangles
					{Triangle t = new Triangle(triangleData.getTexture());
					try{t.setCentroidNormal(new Vector3D(block.getNormalX(),block.getNormalY(),block.getNormalZ()).normalize());}
					catch(MathArithmeticException ee){t.setCentroidNormal(Vector3D.ZERO);}
					t.setAlphaBlended(hasAlpha);
					t.setRenderMode(RenderMode.DYNAMIC);
					
					for(int vi=0; vi < 3; vi++){
					    final org.jtrfp.trcl.gpu.Vertex vtx=
						    vertices.get(vertIndices.get(vi).getVertexIndex()-(b instanceof FaceBlock05?from.getUnknown2():0));
					    t.setVertex(vtx, vi);
						if(b instanceof FaceBlock05 || !(vertIndices.get(0) instanceof FaceBlockVertexWithUV))
						    t.setUV(new Vector2D(BOX_U[vi],BOX_V[vi]), vi);
						else {
						    t.setUV(new Vector2D(
							(double)((FaceBlockVertexWithUV)vertIndices.get(vi)).getTextureCoordinateU()/(double)0xFF0000,
							1.-(double)((FaceBlockVertexWithUV)vertIndices.get(vi)).getTextureCoordinateV()/(double)0xFF0000), vi);}
					}//end for(vi)
					if(triangleData.getTexture()==null)
						{System.err.println("WARNING: Texture never set. Using fallback.");triangleData.setTexture(defaultTexture);}
					if(skipLighting)
					    t.setCentroidNormal(Vector3D.ZERO);
					final int [] triangleIndices = triangleData.getVertices();
					for(int i=0; i<3; i++)
					    triangleIndices[i] = vertIndices.get(i).getVertexIndex();
					to.addPrimitive(triangleData);
					}//end if(3 vertices)
				else
					{System.err.println("ResourceManager: FaceBlock has "+vertIndices.size()+" vertices. Only 3 or 4 supported.");}
				}//end if(FaceBlock)
			else if(b instanceof ColorBlock){
			    final ColorBlock cb = (ColorBlock)b;
			    final byte [] bytes = cb.getBytes();
			    final Color color = new Color(bytes[0]&0xFF,bytes[1]&0xFF,bytes[2]&0xFF);
			    triangleData.setTexture(rm.solidColor(color));
			}
			else if(b instanceof FaceBlock19){
			    System.out.println(b.getClass().getSimpleName()+" (solid colored faces) not yet implemented. Skipping...");}
			else if(b instanceof FaceBlock05){}//TODO
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
				triangleData.setTexture(rm.solidColor(globalPalette[indices.get(0).intValue()]));
			}else if(b instanceof LineSegmentBlock){
			        triangleData.setTexture(triPipeTexture);
				LineSegmentBlock block = (LineSegmentBlock)b;
				org.jtrfp.trcl.gpu.Vertex v1 = vertices.get(block.getVertexID1());
				org.jtrfp.trcl.gpu.Vertex v2 = vertices.get(block.getVertexID2());
				if(!alreadyVisitedLineSegs.contains(v1.hashCode()*v2.hashCode())){
				    Triangle [] newTris = new Triangle[6];
				    LineSegment.buildTriPipe(v1.getPosition(), v2.getPosition(),
					    200, 0, to,triangleData);
				    alreadyVisitedLineSegs.add(v1.hashCode()*v2.hashCode());
				}//end if(not already visited)
			}//end if(LineSegmentBlock)
			else if(b instanceof Unknown12){
			    System.out.println("Found unknown12. Assuming this is a tag for a transparent texture...");
			    hasAlpha=true;
			    skipLighting=true;
			}
			else if(b instanceof AnimatedTextureBlock && palette !=null && ESTuTvPalette != null){
				System.out.println("Found animated texture block.");
				if(palette!=null && ESTuTvPalette != null){
				    AnimatedTextureBlock block = (AnimatedTextureBlock)b;
					List<String> frames = block.getFrameNames();
					double timeBetweenFramesInMillis = ((double)block.getDelay()/65535.)*1000.;
					VQTexture [] subTextures = new VQTexture[frames.size()];
					for(int ti=0; ti<frames.size(); ti++){
						try{if(!hasAlpha)subTextures[ti]=(VQTexture)rm.getRAWAsTexture(frames.get(ti), palette,ESTuTvPalette, false, true);
						else subTextures[ti]=(VQTexture)rm.getRAWAsTexture(frames.get(ti), palette,ESTuTvPalette, true, true);}catch(Exception e){e.printStackTrace();}
						}//end for(frames) //fDelay, nFrames,interp
					triangleData.setTexture(new AnimatedTexture(new Sequencer((int)timeBetweenFramesInMillis,subTextures.length,false),subTextures));
				}else{triangleData.setTexture(defaultTexture);}
				}
			else if(b instanceof EOFBlock)
				{System.out.println("...That's all, end of BIN");}
			else
				{System.out.println("Failed to identify DataBlock: "+b.getClass().getName());}
			}//end for(dataBlocks)
		}//end try{}
	catch(UnrecognizedFormatException ee){
		//Not-good fail
		throw new UnrecognizedFormatException("Can't figure out what this is: Giving up. Expect trouble ahead.");
		}
	catch(Exception e){e.printStackTrace();}
    }//end extract(...)

    /**
     * @return the defaultTexture
     */
    public Texture getDefaultTexture() {
        return defaultTexture;
    }

    /**
     * @param defaultTexture the defaultTexture to set
     */
    public void setDefaultTexture(Texture defaultTexture) {
        this.defaultTexture = defaultTexture;
    }

    /**
     * @return the triPipeTexture
     */
    public Texture getTriPipeTexture() {
        return triPipeTexture;
    }

    /**
     * @param triPipeTexture the triPipeTexture to set
     */
    public void setTriPipeTexture(Texture triPipeTexture) {
        this.triPipeTexture = triPipeTexture;
    }
    
}//end BINWrappedModel
