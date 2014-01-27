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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.Location3D;
import org.jtrfp.trcl.file.NAVFile.NAVSubObject;
import org.jtrfp.trcl.file.NAVFile.TUN;
import org.jtrfp.trcl.file.NAVFile.XIT;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.flow.Mission;
import org.jtrfp.trcl.obj.Checkpoint;

public final class TerrainSystem extends RenderableSpacePartitioningGrid{
	final double gridSquareSize;
	final double heightScalar;
	final ArrayList<TerrainChunk> renderingCubes = new ArrayList<TerrainChunk>();
	private final TR tr;
	
	public TerrainSystem(final AltitudeMap altitude, final TextureMesh textureMesh, final double gridSquareSize, final SpacePartitioningGrid parent, final RenderableSpacePartitioningGrid terrainMirror, final TR tr, final TDFFile tdf){
		super(parent);
		this.tr=tr;
		final int width=(int)altitude.getWidth(); int height=(int)altitude.getHeight();
		this.gridSquareSize=gridSquareSize;
		this.heightScalar=tr.getWorld().sizeY/2;
		final int chunkSideLength=TR.terrainChunkSideLengthInSquares;
		final double u[] = {0,1,1,0};
		final double v[] = {1,1,0,0};
		//Come up with a point list for tunnel entrances and exits
		TDFFile.Tunnel [] tunnels = tdf.getTunnels();
		final HashMap<Integer,TunnelPoint> points = new HashMap<Integer,TunnelPoint>();
		final HashMap<String,TDFFile.Tunnel> tunnelsByName = new HashMap<String,TDFFile.Tunnel>();
		for(int i=0; i<tunnels.length; i++){
		    final TDFFile.Tunnel tun = tunnels[i];
		    TunnelPoint tp = new TunnelPoint(tun,true);
		    points.put(tp.hashCode(),tp);
		    tp = new TunnelPoint(tun,false);
		    points.put(tp.hashCode(),tp);
		    tunnelsByName.put(tun.getTunnelLVLFile(), tunnels[i]);
		}
		Future [] futures = new Future[height/chunkSideLength];
		int futureIndex=0;
		//For each chunk
		for(int gZ=0; gZ<height; gZ+=chunkSideLength){
		    	final int _gZ=gZ;
			futures[futureIndex++]=TR.threadPool.submit(new Runnable(){
				public void run(){
					for(int gX=0; gX<width; gX+=chunkSideLength){
					    //GROUND
					    {//Start scope
					    final double objectX=Math.round(((double)gX+((double)chunkSideLength/2.))*gridSquareSize);
					    final double objectZ=Math.round(((double)_gZ+((double)chunkSideLength/2.))*gridSquareSize);
					    final double objectY=Math.round(altitude.heightAt(gX, _gZ)*heightScalar);
					    final Model m = new Model(false,tr);
					    //for each square
					    for(int cZ=_gZ; cZ<_gZ+chunkSideLength; cZ++){
						for(int cX=gX; cX<gX+chunkSideLength; cX++){
						    final double hTL=altitude.heightAt(cX, cZ)*heightScalar;
						    final double hTR=altitude.heightAt((cX+1),cZ)*heightScalar;
						    final double hBR=altitude.heightAt((cX+1),(cZ+1))*heightScalar;
						    final double hBL=altitude.heightAt(cX,(cZ+1))*heightScalar;
						    final double xPos=cX*gridSquareSize;
						    final double zPos=cZ*gridSquareSize;
						    
						    final Integer tpi = cX+cZ*256;
						    Future<TextureDescription> td=(Future<TextureDescription>)(points.containsKey(tpi)?points.get(tpi).getTexture():textureMesh.textureAt(cX, cZ));
						    Triangle [] tris = Triangle.quad2Triangles(// CLOCKWISE
							new double [] {xPos-objectX,xPos+gridSquareSize-objectX,xPos+gridSquareSize-objectX,xPos-objectX}, //x
							new double [] {hTL-objectY,hTR-objectY,hBR-objectY,hBL-objectY}, 
							new double [] {zPos-objectZ,zPos-objectZ,zPos+gridSquareSize-objectZ,zPos+gridSquareSize-objectZ}, 
							u,
							v,
							td, RenderMode.STATIC);
							
							m.addTriangle(tris[0]);
							m.addTriangle(tris[1]);
							}//end for(cX)
						    }//end for(cZ)
						//Add to grid
						if(m.finalizeModel().getTriangleList()!=null){
							final TerrainChunk chunkToAdd = new TerrainChunk(tr,m,altitude);
							final double [] chunkPos = chunkToAdd.getPosition();
							chunkPos[0]=objectX;
							chunkPos[1]=objectY;
							chunkPos[2]=objectZ;
							chunkToAdd.notifyPositionChange();
							add(chunkToAdd);
							}
						else {System.out.println("Rejected chunk: "+m.getDebugName());}
					    	}//end scope
					    
					    	{//start scope ///// CEILING
					    	final double Y_NUDGE = -10000;
					    	/*
					    	 * Y_NUDGE is a kludge. There is a tiny sliver of space between the ceiling and ground, 
					    	 * likely caused by model vertex quantization in the rendering engine.
					    	 * I would rather put up with this quirk than re-design the engine, as
					    	 * the quantization exists as a side-effect of a memory-space optimization
					    	 * in the GPU and accommodating the fix of this bug could cause bigger problems further down the road.
					    	 */
					    	final double objectX=Math.round(((double)gX+((double)chunkSideLength/2.))*gridSquareSize);
						    final double objectZ=Math.round(((double)_gZ+((double)chunkSideLength/2.))*gridSquareSize);
						    final double objectY=Math.round((2.-altitude.heightAt(gX, _gZ))*heightScalar+Y_NUDGE);
						    final Model m = new Model(false,tr);
						    //for each square
						    for(int cZ=_gZ; cZ<_gZ+chunkSideLength; cZ++){
							for(int cX=gX; cX<gX+chunkSideLength; cX++){
							    final double hTL=(2.-altitude.heightAt(cX, cZ))*heightScalar+Y_NUDGE;
							    final double hTR=(2.-altitude.heightAt((cX+1),cZ))*heightScalar+Y_NUDGE;
							    final double hBR=(2.-altitude.heightAt((cX+1),(cZ+1)))*heightScalar+Y_NUDGE;
							    final double hBL=(2.-altitude.heightAt(cX,(cZ+1)))*heightScalar+Y_NUDGE;
							    final double xPos=cX*gridSquareSize;
							    final double zPos=cZ*gridSquareSize;
							    
							    //Ceiling texture cell X (Z in this engine) value is offset by 10.
							    //No tunnelpoints on ceiling
							    Future<TextureDescription> td=(Future<TextureDescription>)(textureMesh.textureAt(cX, cZ+10));
							    Triangle [] tris = Triangle.quad2Triangles(// CLOCKWISE
								new double [] {xPos-objectX,xPos+gridSquareSize-objectX,xPos+gridSquareSize-objectX,xPos-objectX}, //x
								new double [] {hTL-objectY,hTR-objectY,hBR-objectY,hBL-objectY}, 
								new double [] {zPos-objectZ,zPos-objectZ,zPos+gridSquareSize-objectZ,zPos+gridSquareSize-objectZ}, 
								u,
								v,
								td, RenderMode.STATIC);
								
								m.addTriangle(tris[0]);
								m.addTriangle(tris[1]);
								}//end for(cX)
							    }//end for(cZ)
							//Add to grid
							if(m.finalizeModel().getTriangleList()!=null){
								final TerrainChunk chunkToAdd = new TerrainChunk(tr,m,altitude);
								final double [] chunkPos = chunkToAdd.getPosition();
								chunkPos[0]=objectX;
								chunkPos[1]=objectY;
								chunkPos[2]=objectZ;
								chunkToAdd.notifyPositionChange();
								chunkToAdd.setCeiling(true);
								terrainMirror.add(chunkToAdd);
								}
							else {System.out.println("Rejected chunk: "+m.getDebugName());}
					    	}//end scope(CEILING)
						}//end for(gX)
					}//end run(){}
				});//end submit()
			}//end for(gZ)
		//Wait to finish
		for(Future f:futures){try{f.get();}catch(Exception e){e.printStackTrace();}}
		terrainMirror.deactivate();
		}//end constructor
	
	private class TunnelPoint{
	    final int x,z;
	    Future<TextureDescription> textureToInsert;
	    
	    public TunnelPoint(TDFFile.Tunnel tun, boolean entrance){
		try{final String texFile = entrance?tun.getEntranceTerrainTextureFile():tun.getExitTerrainTextureFile();
		textureToInsert = tr.getResourceManager().getRAWAsTexture(texFile, tr.getGlobalPalette(), GammaCorrectingColorProcessor.singleton, tr.getGPU().getGl());}
		catch(Exception e){e.printStackTrace();}
		DirectionVector v = entrance?tun.getEntrance():tun.getExit();
		x=(byte)Math.round(TR.legacy2MapSquare(v.getZ()))&0xFF;//Reversed on purpose
		//KLUDGE: I don't know for sure what exactly is going on, but the tunnel entrance in the first chamber of DESERT.LVL is off by 1 cell.
		//Yet everything else is fine. This is a nudge to fix that. May break other things. No sure yet.
		//This is probably related to the fact that absolute coordinates are being tacked onto cell coordinates.
		final double signed=TR.legacy2MapSquare(v.getX());
		z=(byte)Math.round(signed>0?signed:signed-.5)&0xFF;
		//z=(byte)Math.round(TR.legacy2MapSquare(v.getX()))&0xFF;
	    }
	    public Future<TextureDescription> getTexture(){return textureToInsert;}
	    
	    @Override
	    public boolean equals(Object other){
		return other.hashCode()==this.hashCode();
	    }
	    @Override
	    public int hashCode(){
		return (int)(x+z*256);
	    }
	}
	/**
	 * @return the gridSquareSize
	 */
	public double getGridSquareSize(){
		return gridSquareSize;
		}
	/**
	 * @return the heightScalar
	 */
	public double getHeightScalar(){
		return heightScalar;
		}
	/**
	 * @return the renderingCubes
	 */
	public ArrayList<TerrainChunk> getRenderingCubes(){
		return renderingCubes;
		}
	}//end TerrainSystem
