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

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.RenderMode;
import org.jtrfp.trcl.TextureMesh;
import org.jtrfp.trcl.Triangle;
import org.jtrfp.trcl.beh.Behavior;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.FeaturesImpl.FeatureNotFoundException;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.game.Game;
import org.jtrfp.trcl.gpu.GL33Model;
import org.jtrfp.trcl.gpu.SettableTexture;
import org.jtrfp.trcl.gpu.Texture;
import org.jtrfp.trcl.gpu.VQTexture;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;

public class MiniMap extends WorldObject implements RelevantEverywhere {
    private TextureMesh textureMesh;
    private double []   modelSize;
    private int diameterInTiles = 32;
    private SettableTexture [][] grid;
    private Integer prevTileX, prevTileY;
    private Integer tileX=0,tileY=0;
    private boolean supportsLoop = false;
    private double [] uCoords, vCoords;
    private double tileTextureUVScalar = .00001;
    private Vector3D topOrigin = Vector3D.PLUS_J;
    private static final double [] ZERO = new double [] {0,0,0,0};
    private Rotation mapHack = Rotation.IDENTITY;
    private GameShell gameShell;
    
    public MiniMap() {
	super();
	setTop(Vector3D.PLUS_J);
	setHeading(Vector3D.PLUS_K);
	configureCircle();
	addBehavior(new MiniMapBehavior());
    }//end constructor
    
    private class MiniMapBehavior extends Behavior {
	@Override
	public void tick(long tickTimeMillis){
	    final Game game = getGameShell().getGame();
	    if(game == null)
		return;
	    final Player player = game.getPlayer();
	    final double [] pos = player.getPosition();
	    setMapPositionFromModern(pos[0],pos[2]);
	    updateHeading(player);
	}//end _tick(...)
	
	private void updateHeading(Player player){
	    final Vector3D playerHdg = player.getHeading();
	    final Vector3D topDir = new Vector3D(playerHdg.getX(),playerHdg.getZ(),0).normalize();
	    final Rotation rot = new Rotation(Vector3D.PLUS_K,Vector3D.PLUS_J, getHeading(), getTopOrigin());
	    final Vector3D turnDir = topDir.negate();
	    //setTop(rot.applyTo(getMapHack().applyTo(topDir)));
	    setTop(rot.applyTo(getMapHack().applyTo(turnDir)));
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
    
    protected GL33Model buildModel(){
	TR tr;
	
	try {tr= getTr();}
	catch(FeatureNotFoundException e){
	    return null;}
	
	final GL33Model result = new GL33Model(false, tr, "MiniMap "+hashCode());
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
    
    protected void setupTileAt(int x, int y, double percentY, double [] tileSize, double [] modelSize, GL33Model model){
	final Texture texture = getGrid()[x][y];
	if(texture!=null){
	    final double percentX = 2*(x-getHalfwayPoint())/getDiameterInTiles();
	    final double cX = percentX * modelSize[0];
	    final double cY = percentY * modelSize[1];
	    final double tileWidth = tileSize[0];
	    final double tileHeight= tileSize[1];
	    final Triangle [] tris = Triangle.quad2Triangles(
		    new double[]{cX, cX + tileWidth, cX + tileWidth, cX}, 
		    new double[]{cY - tileHeight, cY - tileHeight, cY, cY}, 
		    ZERO, //cZ 
		    getuCoords(), 
		    getvCoords(), 
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
	final Rotation mapHack = getMapHack();
	final double hwp = getHalfwayPoint();
	if(mesh == null)
	    throw new IllegalStateException("Cannot refresh tile textures while mesh is null.");
	for(int y=0; y < getDiameterInTiles(); y++)
	    for(int x=0; x < getDiameterInTiles(); x++){
		final SettableTexture tex = getGrid()[x][y];
		if(tex != null){
		    final Vector3D meshXYz = mapHack.applyTo(new Vector3D(x-hwp,hwp-y,0));
		    final Texture meshTex = mesh.textureAt(tileX+meshXYz.getX(), tileY+meshXYz.getY());
		    if(meshTex instanceof VQTexture)
			tex.setCurrentTexture(((VQTexture)meshTex));
		}//end if(!null)
	    }//end for(x)
    }
    
    public void setMapPositionFromModern(double modernX, double modernY){
	setMapPositionFromTile(TRFactory.modernToMapSquare(modernX),TRFactory.modernToMapSquare(modernY));
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

    protected double[] getuCoords() {
	if(uCoords == null){
	    final double scalar = getTileTextureUVScalar();
	    setuCoords(new double[]{0,scalar,scalar,0});
	    }
        return uCoords;
    }

    protected void setuCoords(double[] uCoords) {
        this.uCoords = uCoords;
    }

    protected double[] getvCoords() {
	if(vCoords == null){
	    final double scalar = getTileTextureUVScalar();
	    setvCoords(new double[]{scalar,scalar,0,0});
	    }
        return vCoords;
    }

    protected void setvCoords(double[] vCoords) {
        this.vCoords = vCoords;
    }

    public double getTileTextureUVScalar() {
        return tileTextureUVScalar;
    }

    public void setTileTextureUVScalar(double tileTextureUVScalar) {
        this.tileTextureUVScalar = tileTextureUVScalar;
    }

    public Vector3D getTopOrigin() {
        return topOrigin;
    }

    public void setTopOrigin(Vector3D topOrigin) {
        this.topOrigin = topOrigin;
    }

    public Rotation getMapHack() {
        return mapHack;
    }

    /**
     * This is to get around discrepancies between world and GUI coords
     * @param mapHack
     * @since Apr 7, 2016
     */
    public void setMapHack(Rotation mapHack) {
        this.mapHack = mapHack;
    }
    
    public GameShell getGameShell() {
	if(gameShell == null){
	    gameShell = Features.get(getTr(), GameShell.class);}
	return gameShell;
    }
    public void setGameShell(GameShell gameShell) {
	this.gameShell = gameShell;
    }
}//end MiniMap
