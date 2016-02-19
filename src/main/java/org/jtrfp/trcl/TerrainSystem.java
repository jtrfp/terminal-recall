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
package org.jtrfp.trcl;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.DirectionVector;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TDFFile.TunnelLogic;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.PortalTexture;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.miss.TunnelSystemFactory.TunnelSystem;
import org.jtrfp.trcl.obj.PortalEntrance;
import org.jtrfp.trcl.obj.PortalExit;
import org.jtrfp.trcl.obj.TerrainChunk;

public final class TerrainSystem extends RenderableSpacePartitioningGrid{
	final double gridSquareSize;
	//final double heightScalar;
	final ArrayList<TerrainChunk> renderingCubes = new ArrayList<TerrainChunk>();
	private final TR tr;
	private final ExecutorService executor;
	public static final double Y_NUDGE = -10000;
	
	private static class TSThreadFactory implements ThreadFactory{
	    @Override
	    public Thread newThread(final Runnable runnable) {
		final Thread result = new Thread("TerrainSystem "+hashCode()){
		    public void run(){
			runnable.run();}
		};
		return result;
	    }
	}//end TSThreadFactory
	
	
	    /*
	     * Y_NUDGE is a kludge. There is a tiny sliver of space
	     * between the ceiling and ground, likely caused by model
	     * vertex quantization in the rendering engine. I would
	     * rather put up with this quirk than re-design the engine,
	     * as the quantization exists as a side-effect of a
	     * memory-space optimization in the GPU and accommodating
	     * the fix of this bug could cause bigger problems further
	     * down the road.
	     */
	
    public TerrainSystem(final AltitudeMap altitude,
	    final TextureMesh textureMesh, final double gridSquareSize,
	    final RenderableSpacePartitioningGrid terrainMirror, final TR tr,
	    final TDFFile tdf, final boolean flatShading, 
	    final LoadingProgressReporter terrainReporter, final String debugName) {
	super();
	final int numCores = Runtime.getRuntime().availableProcessors();
	this.tr = tr;
	final int width = (int) altitude.getWidth();
	int height = (int) altitude.getHeight();
	this.gridSquareSize = gridSquareSize;
	executor = new ThreadPoolExecutor(numCores * 2, numCores * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), 
		new TSThreadFactory());
	
	final int chunkSideLength = TR.terrainChunkSideLengthInSquares;
	final double u[] = { 0, 1, 1, 0 };
	final double v[] = { 0, 0, 1, 1 };
	final double cu[] = { 0, 1, 1, 0 };
	final double cv[] = { 1, 1, 0, 0 };
	final NormalMap normalMap = new NormalMap(altitude);
	// Come up with a point list for tunnel entrances and exits
	TDFFile.Tunnel[] tunnels = tdf.getTunnels();
	final HashMap<Integer, TunnelPoint> points = new HashMap<Integer, TunnelPoint>();
	final HashMap<String, TDFFile.Tunnel> tunnelsByName = new HashMap<String, TDFFile.Tunnel>();
	final ArrayList<Future<Void>> rowTasks = new ArrayList<Future<Void>>();
	if (tunnels != null) {// Null means no tunnels
	    for (int i = 0; i < tunnels.length; i++) {
		final TDFFile.Tunnel tun = tunnels[i];
		if (tun.getEntranceLogic() != TunnelLogic.invisible) {
		    final TunnelPoint tp = new TunnelPoint(tun, true);
		    points.put(tp.hashCode(), tp);
		}
		if (tun.getExitLogic() != TunnelLogic.invisible) {
		    final TunnelPoint tp = new TunnelPoint(tun, false);
		    points.put(tp.hashCode(), tp);
		    tunnelsByName.put(tun.getTunnelLVLFile(), tunnels[i]);
		}//end if(invisible)
	    }// end for(tunnels)
	}// end if(tunnels)
	
	final LoadingProgressReporter[] reporters = terrainReporter
		.generateSubReporters(256/chunkSideLength);
	int reporterIndex=0;
	final double worldCeiling = tr.getWorld().sizeY;
	
	final int widthInChunks  = width / chunkSideLength;
	final int heightInChunks = height / chunkSideLength;
	final int numChunks = widthInChunks * heightInChunks;
	//Bulk-allocate ahead of time
	final ArrayList<Integer> matrixIDs = new ArrayList<Integer>(numChunks);
	tr.gpu.get().matrixWindow.get().create(matrixIDs,numChunks);
	
	// For each chunk
	for (int gZ = 0; gZ < height; gZ += chunkSideLength) {
	    reporters[reporterIndex++].complete();
	    final int _gZ = gZ;
	    rowTasks.add(executor.submit(new Callable<Void>(){
		@Override
		public Void call() throws Exception {
		    for (int gX = 0; gX < width; gX += chunkSideLength) {
			// GROUND
			{// Start scope
			    final double objectX = Math
				    .round(((double) gX + ((double) chunkSideLength / 2.))
					    * gridSquareSize);
			    final double objectZ = Math
				    .round(((double) _gZ + ((double) chunkSideLength / 2.))
					    * gridSquareSize);
			    final double objectY = Math.round(altitude
				    .heightAt(gX*gridSquareSize, _gZ*gridSquareSize));
			    final Model m = new Model(false, tr,"Terrain Chunk");
			    m.setDebugName(debugName);
			    // for each square
			    for (int cZ = _gZ; cZ < _gZ + chunkSideLength; cZ++) {
				for (int cX = gX; cX < gX + chunkSideLength; cX++) {
				    final double xPos = cX * gridSquareSize;
				    final double zPos = cZ * gridSquareSize;
				    final double hTL = altitude.heightAt(xPos, cZ* gridSquareSize);
				    final double hTR = altitude.heightAt((cX + 1)* gridSquareSize, cZ* gridSquareSize);
				    final double hBR = altitude.heightAt((cX + 1)* gridSquareSize,
					    (cZ + 1)* gridSquareSize);
				    final double hBL = altitude.heightAt(xPos, (cZ + 1)* gridSquareSize);
				    
				    
				    Vector3D norm0, norm1, norm2, norm3;
				    norm3 = normalMap.normalAt(xPos, cZ* gridSquareSize);
				    /*norm3 = new Vector3D(norm.getX() * 3, norm.getY(),
					    norm.getZ() * 3).normalize();*/
				    norm2 = normalMap.normalAt((cX + 1)* gridSquareSize, cZ* gridSquareSize);
				    /*norm2 = new Vector3D(norm.getX() * 3, norm.getY(),
					    norm.getZ() * 3).normalize();*/
				    norm1 = normalMap.normalAt((cX + 1)* gridSquareSize, (cZ + 1)* gridSquareSize);
				    /*norm1 = new Vector3D(norm.getX() * 3, norm.getY(),
					    norm.getZ() * 3).normalize();*/
				    norm0 = normalMap.normalAt(xPos, (cZ + 1)* gridSquareSize);
				    /*norm0 = new Vector3D(norm.getX() * 3, norm.getY(),
					    norm.getZ() * 3).normalize();*/

				    if (flatShading)
					norm0 = norm1 = norm2 = norm3 = normalMap
						.normalAt((cX + .5)* gridSquareSize, (cZ + .5)* gridSquareSize);

				    final Integer tpi = cX + cZ * 256;
				    
				    if(points.containsKey(tpi)){
					final Model portalModel = new Model(false, tr,"PortalEntrance");
					//Place a PortalEntrance
					final int Y_OFFSET = -300;
					final double portalX = xPos+gridSquareSize/2.;
					final double portalY = (hBL+hBR+hTR+hTL)/4.;
					final double portalZ = zPos+gridSquareSize/2.;
					final PortalTexture portalTexture = new PortalTexture();
					Triangle[] tris = Triangle
						.quad2Triangles(
							// COUNTER-CLOCKWISE
							// //x
							new double[] {
								gridSquareSize/2,
								-gridSquareSize/2,
								-gridSquareSize/2,
								gridSquareSize/2 },
								new double[] {-gridSquareSize/2,-gridSquareSize/2,gridSquareSize/2,gridSquareSize/2},
								new double[] {
								0,
								0,
								0,
								0 }, u, v, portalTexture,
								RenderMode.STATIC,
								new Vector3D[] { norm0, norm1,
								norm2, norm3 }, cX + cZ % 4);
					portalModel.addTriangles(tris);
					final PortalExit exit = new PortalExit(tr);
					final PortalEntrance entrance;
					entrance = new PortalEntrance(tr,portalModel,exit,tr.getGame().getPlayer());
					final TunnelSystem ts = Features.get(tr.getGame().getCurrentMission(),TunnelSystem.class);
					ts.registerTunnelEntrancePortal(new Point(cX,cZ), entrance);
					
					entrance.setPortalTexture(portalTexture);
					Vector3D heading = normalMap.normalAt(xPos, cZ* gridSquareSize).normalize().negate();
					entrance.setHeading(heading);
					if(heading.getY()>-.99&heading.getNorm()>0)//If the ground is flat this doesn't work.
					 entrance.setTop(Vector3D.PLUS_J.crossProduct(heading).crossProduct(heading).negate());
					else entrance.setTop(Vector3D.PLUS_I);// ... so we create a clause for that.
					entrance.setPosition(new Vector3D(portalX,portalY,portalZ).add(heading.scalarMultiply(2000)).toArray());
					entrance.notifyPositionChange();
					add(entrance);
				    }//end if(tunnel)
				    
				    TextureDescription td = (TextureDescription) (points
					    .containsKey(tpi) ? points.get(tpi)
					    .getTexture() : textureMesh.textureAt(cX,
					    cZ));
				    Triangle[] tris = Triangle
					    .quad2Triangles(
						    // COUNTER-CLOCKWISE
						    // //x
						    new double[] {
							    xPos - objectX,
							    xPos + gridSquareSize
								    - objectX,
							    xPos + gridSquareSize
								    - objectX,
							    xPos - objectX },
						    new double[] { hBL - objectY,
							    hBR - objectY,
							    hTR - objectY,
							    hTL - objectY },
						    new double[] {
							    zPos + gridSquareSize
								    - objectZ,
							    zPos + gridSquareSize
								    - objectZ,
							    zPos - objectZ,
							    zPos - objectZ }, u, v, td,
						    RenderMode.STATIC,
						    new Vector3D[] { norm0, norm1,
							    norm2, norm3 }, cX + cZ % 4);
				    if(points.containsKey(tpi)){
					tris[0].setAlphaBlended(true);
					tris[1].setAlphaBlended(true);
				    }
				    m.addTriangle(tris[0]);
				    m.addTriangle(tris[1]);
				}// end for(cX)
			    }// end for(cZ)
			     // Add to grid
			   // if (m.finalizeModel().get().getTriangleList() != null || m.getTransparentTriangleList() != null) {
				final TerrainChunk chunkToAdd = new TerrainChunk(tr, m,
					altitude);
				final double[] chunkPos = chunkToAdd.getPosition();
				chunkPos[0] = objectX;
				chunkPos[1] = objectY;
				chunkPos[2] = objectZ;
				chunkToAdd.notifyPositionChange();
				add(chunkToAdd);
			    /*} else {
				System.out.println("Rejected chunk: "
					+ m.getDebugName());
			    }*/
			}// end scope

			{// start scope ///// CEILING
			    final double objectX = Math
				    .round(((double) gX + ((double) chunkSideLength / 2.))
					    * gridSquareSize);
			    final double objectZ = Math
				    .round(((double) _gZ + ((double) chunkSideLength / 2.))
					    * gridSquareSize);
			    final double objectY = Math.round((2. - altitude.heightAt(
				    gX* gridSquareSize, _gZ* gridSquareSize)) + Y_NUDGE);
			    final Model m = new Model(false, tr,"CeilingChunk");
			    // for each square
			    for (int cZ = _gZ; cZ < _gZ + chunkSideLength; cZ++) {
				for (int cX = gX; cX < gX + chunkSideLength; cX++) {
				    final double xPos = cX * gridSquareSize;
				    final double zPos = cZ * gridSquareSize;
				    final double hTL = (worldCeiling - altitude.heightAt(xPos, cZ* gridSquareSize))
					    + Y_NUDGE;
				    final double hTR = (worldCeiling - altitude.heightAt(
					    (cX + 1)* gridSquareSize, cZ* gridSquareSize)) + Y_NUDGE;
				    final double hBR = (worldCeiling - altitude.heightAt(
					    (cX + 1)* gridSquareSize, (cZ + 1)* gridSquareSize))
					    + Y_NUDGE;
				    final double hBL = (worldCeiling - altitude.heightAt(xPos,
					    (cZ + 1)* gridSquareSize)) + Y_NUDGE;

				    Vector3D norm0, norm1, norm2, norm3;
				    norm3=norm2=norm1=norm0 = normalMap.normalAt(xPos, cZ* gridSquareSize);
				    /*
				    norm3 = altitude.heightAt(cX, cZ)<.9?
					    new Vector3D(norm.getX() * 3, norm.getY()*-1,
					    norm.getZ() * 3).normalize():
						new Vector3D(norm.getX() * 3, norm.getY(),
						norm.getZ() * 3).normalize();
				    norm = altitude.normalAt(cX + 1, cZ);
				    norm2 = altitude.heightAt(cX + 1, cZ)<.9?
					    new Vector3D(norm.getX() * 3, norm.getY()*-1,
					    norm.getZ() * 3).normalize():
						new Vector3D(norm.getX() * 3, norm.getY(),
						norm.getZ() * 3).normalize();
				    norm = altitude.normalAt(cX + 1, cZ + 1);
				    norm1 = altitude.heightAt(cX + 1 , cZ + 1)< .9?
					    new Vector3D(norm.getX() * 3, norm.getY()*-1,
					    norm.getZ() * 3).normalize():
					    new Vector3D(norm.getX() * 3, norm.getY(),
						norm.getZ() * 3).normalize();
				    norm = altitude.normalAt(cX, cZ + 1);
				    norm0 = altitude.heightAt(cX, cZ + 1)<.9?
					    new Vector3D(norm.getX() * 3, norm.getY()*-1,
					    norm.getZ() * 3).normalize():
						new Vector3D(norm.getX() * 3, norm.getY(),
						norm.getZ() * 3).normalize();
				    */
				    norm0 = norm1 = norm2 = norm3 = Vector3D.ZERO;
				    // Ceiling texture cell X (Z in this engine) value
				    // is offset by 10.
				    // No tunnelpoints on ceiling
				    TextureDescription td = (TextureDescription) (textureMesh
					    .textureAt(cX, cZ + 10));
									 // features.
				    Triangle[] tris = Triangle.quad2Triangles(
					    // CLOCKWISE (else backface culling will eat
					    // it)
					    new double[] { xPos - objectX,
						    xPos + gridSquareSize - objectX,
						    xPos + gridSquareSize - objectX,
						    xPos - objectX }, // x
					    new double[] { hTL - objectY,
						    hTR - objectY, hBR - objectY,
						    hBL - objectY },
					    new double[] { zPos - objectZ,
						    zPos - objectZ,
						    zPos + gridSquareSize - objectZ,
						    zPos + gridSquareSize - objectZ },
					    cu,
					    cv,
					    td,
					    RenderMode.STATIC,
					    new Vector3D[] { norm3,
						    norm2, norm1,
						    norm0 }, cX + cZ % 4);
				    m.addTriangle(tris[0]);
				    m.addTriangle(tris[1]);
				}// end for(cX)
			    }// end for(cZ)
			     // Add to grid
			    //if (m.finalizeModel().get().getTriangleList() != null || m.getTransparentTriangleList() != null) {
				final TerrainChunk chunkToAdd = new TerrainChunk(tr, m,
					altitude);
				final double[] chunkPos = chunkToAdd.getPosition();
				chunkPos[0] = objectX;
				chunkPos[1] = objectY;
				chunkPos[2] = objectZ;
				chunkToAdd.notifyPositionChange();
				chunkToAdd.setCeiling(true);
				terrainMirror.add(chunkToAdd);
			    /*} else {
				System.out.println("Rejected chunk: "
					+ m.getDebugName());
			    }*/
			}// end scope(CEILING)
		    }// end for(gX)
		    return null;
		}}));
	}// end for(gZ)
	for(Future<Void> task:rowTasks)
	    try{task.get();}catch(Exception e){throw new RuntimeException(e);}
    }// end constructor
	
	private class TunnelPoint{
	    final int x,z;
	    TextureDescription textureToInsert;
	    
	    public TunnelPoint(TDFFile.Tunnel tun, boolean entrance){
		try{final String texFile = entrance?tun.getEntranceTerrainTextureFile():tun.getExitTerrainTextureFile();
		textureToInsert = tr.getResourceManager().getRAWAsTexture(texFile, tr.getGlobalPaletteVL(),null,false);}
		catch(Exception e){e.printStackTrace();}
		DirectionVector v = entrance?tun.getEntrance():tun.getExit();
		x = (int)TR.legacy2MapSquare(v.getZ());
		z = (int)TR.legacy2MapSquare(v.getX());
	    }
	    public TextureDescription getTexture(){return textureToInsert;}
	    
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
	 * @return the renderingCubes
	 */
	public ArrayList<TerrainChunk> getRenderingCubes(){
		return renderingCubes;
		}
	

	@Override
	public void finalize() throws Throwable{
	    super.finalize();
	    executor.shutdown();
	}//end finalize()
	}//end TerrainSystem
