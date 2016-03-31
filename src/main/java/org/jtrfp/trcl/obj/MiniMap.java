/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.obj;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureMesh;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gpu.SettableTexture;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;

public class MiniMap extends WorldObject implements RelevantEverywhere {
    private TextureMesh textureMesh;
    private double []   modelSize;
    private int diameterInTiles = 32;
    private SettableTexture [][] grid;
    private Integer prevTileX, prevTileY;
    private Integer tileX=0,tileY=0;
    private boolean supportsLoop = false;
    
    public MiniMap(TR tr) {
	super(tr);
	setTop(Vector3D.PLUS_J);
	setHeading(Vector3D.MINUS_K);
	configureCircle();
	addBehavior(new MiniMapBehavior());
    }//end constructor
    
    private class MiniMapBehavior extends Behavior {
	@Override
	public void tick(long tickTimeMillis){
	    final Player player = getTr().getGame().getPlayer();
	    final double [] pos = player.getPosition();
	    setMapPositionFromModern(pos[0],pos[2]);
	    updateHeading(player);
	}//end _tick(...)
	
	private void updateHeading(Player player){
	    final Vector3D playerHdg = player.getHeading();
	    final Vector2D topDir = new Vector2D(playerHdg.getX(),playerHdg.getZ()).normalize();
	    setTop(new Vector3D(topDir.getX(),topDir.getY(),0));
	}
    }//end MiniMapBehavior
    
    public void setSupportsLoop(boolean supportsLoop){
	this.supportsLoop = supportsLoop;
    }
    
    @Override
    public boolean supportsLoop(){
	return supportsLoop;
    }
    
    protected void configureCircle(){
	final int diameterInTiles = getDiameterInTiles();
	for(int y=0; y<diameterInTiles; y++){
	    for(int x=0; x<diameterInTiles; x++){
		if(isInCircleRange(x,y))
		    getGrid()[x][y] = new SettableTexture();
	    }//end for(x)
	}//end for(y)
    }//end configureCircle()
    
    protected boolean isInCircleRange(int x, int y){
	return (distanceFromCenter(x,y) <= getHalfwayPoint());
    }
    
    protected double distanceFromCenter(double x, double z){
	final double hwp = getHalfwayPoint();
	final double dx = x-hwp;
	final double dz = z-hwp;
	return Math.sqrt(dx*dx+dz*dz);
    }//end distanceFromCenter
    
    protected double getHalfwayPoint(){
	return getDiameterInTiles()/2.;
    }
    
    protected void configureGridAt(int x, int y, int rowDia){
	getGrid()[x+( (int)getHalfwayPoint() - rowDia/2 )][y] = new SettableTexture();
    }//end configureGridAt(...)
    
    protected int diameterAtPctY(double percentY){
	return (int)(Math.sin(Math.PI*percentY)*getDiameterInTiles());
    }//end diameterAtPctY()
    
    protected Model buildModel(){
	final Model result = new Model(false, getTr(), "MiniMap "+hashCode());
	final int diameterInTiles = getDiameterInTiles();
	final double halfwayPoint = getHalfwayPoint();
	final double [] modelSize= getModelSize();
	final double [] diaTiles = new double[]{diameterInTiles,diameterInTiles};
	final double [] tileSize = new double[]{2*modelSize[0] / diaTiles[0],2*modelSize[1] / diaTiles[1]};
	for(int y=0; y<diameterInTiles; y++){
	    final double percentY = 2*(y-halfwayPoint)/diameterInTiles;
	    for(int x=0; x<diameterInTiles; x++){
		setupTileAt(x,y,percentY, tileSize, modelSize, result);
	    }//end for(x)
	}//end for(y)
	return result;
    }//end buildModel(...)
    
    protected void setupTileAt(int x, int y, double percentY, double [] tileSize, double [] modelSize, Model model){
	final Texture texture = getGrid()[x][y];
	if(texture!=null){
	    final double percentX = 2*(x-getHalfwayPoint())/getDiameterInTiles();
	    final double cX = percentX * modelSize[0];
	    final double cY = percentY * modelSize[1];
	    final double cZ = 0;
	    final double tileWidth = tileSize[0];
	    final double tileHeight= tileSize[1];
	    final Triangle [] tris = Triangle.quad2Triangles(
		    new double[]{cX, cX + tileWidth, cX + tileWidth, cX}, 
		    new double[]{cY - tileHeight, cY - tileHeight, cY, cY}, 
		    new double[]{cZ,cZ,cZ,cZ}, 
		    new double[]{0,1,1,0}, 
		    new double[]{1,1,0,0}, 
		    texture, 
		    RenderMode.DYNAMIC, 
		    false, //hasAlpha
		    Vector3D.ZERO, //centroidNormal
		    "MiniMap tile x="+x+" y="+y);
	    model.addTriangles(tris);
	}//end if(!texture)
    }//end setupTileAt(...)
    
    private void reEvaluateModelState(){
	if(getModelSize() == null)
	    return;
	setModel(buildModel());
    }//end reEvaluateModelState()

    public TextureMesh getTextureMesh() {
        return textureMesh;
    }

    public void setTextureMesh(TextureMesh textureMesh) {
	if(this.textureMesh == textureMesh)
	    return;
        this.textureMesh = textureMesh;
        if(textureMesh != null)
         refreshTileTextures();
    }

    public double[] getModelSize() {
        return modelSize;
    }

    public void setModelSize(double [] modelSize) {
	if(this.modelSize == modelSize)
	    return;
        this.modelSize = modelSize;
        reEvaluateModelState();
    }//end setModelSize(...)
    
    public void setMapPositionFromTile(int tileX, int tileY){
	if(     prevTileX != null && 
		prevTileY != null && 
		tileX == prevTileX && 
		tileY == prevTileY || 
		getTextureMesh() == null)
	    return;
	setTileX(tileX);
	setTileY(tileY);
	prevTileX = tileX; prevTileY = tileY;
	refreshTileTextures();
    }//end setPositionFromTile(...)
    
    protected void refreshTileTextures(){
	final TextureMesh mesh = getTextureMesh();
	if(mesh == null)
	    throw new IllegalStateException("Cannot refresh tile textures while mesh is null.");
	for(int y=0; y < getDiameterInTiles(); y++)
	    for(int x=0; x < getDiameterInTiles(); x++){
		final SettableTexture tex = getGrid()[x][y];
		if(tex != null){
		    final Texture meshTex = mesh.textureAt(tileX+x-getHalfwayPoint(), tileY+y-getHalfwayPoint());
		    if(meshTex instanceof VQTexture)
			tex.setCurrentTexture(((VQTexture)meshTex));
		}//end if(!null)
	    }//end for(x)
    }
    
    public void setMapPositionFromModern(double modernX, double modernY){
	setMapPositionFromTile(TR.modernToMapSquare(modernX),TR.modernToMapSquare(modernY));
    }

    public SettableTexture[][] getGrid() {
	if(grid == null){
	    final int diameterInTiles = getDiameterInTiles();
	    grid = new SettableTexture[diameterInTiles][diameterInTiles];
	    }
        return grid;
    }

    public void setGrid(SettableTexture[][] grid) {
        this.grid = grid;
    }

    public int getDiameterInTiles() {
        return diameterInTiles;
    }

    public void setDiameterInTiles(int diameterInTiles) {
	if(this.diameterInTiles == diameterInTiles)
	    return;//No change.
	setGrid(null);//clear the grid
        this.diameterInTiles = diameterInTiles;
    }

    public Integer getTileX() {
        return tileX;
    }

    public void setTileX(Integer tileX) {
        this.tileX = tileX;
    }

    public Integer getTileY() {
        return tileY;
    }

    public void setTileY(Integer tileY) {
        this.tileY = tileY;
    }
}//end MiniMap
