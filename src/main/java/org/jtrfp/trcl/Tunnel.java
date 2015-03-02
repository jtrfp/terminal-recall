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

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import javax.media.opengl.GL3;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.trcl.beh.CubeCollisionBehavior;
import org.jtrfp.trcl.beh.DamageableBehavior;
import org.jtrfp.trcl.beh.DeathBehavior;
import org.jtrfp.trcl.beh.DebrisOnDeathBehavior;
import org.jtrfp.trcl.beh.ExplodesOnDeath;
import org.jtrfp.trcl.beh.phy.RotatingObjectBehavior;
import org.jtrfp.trcl.beh.phy.ShiftingObjectBehavior;
import org.jtrfp.trcl.beh.tun.DestructibleWallBehavior;
import org.jtrfp.trcl.beh.tun.IrisBehavior;
import org.jtrfp.trcl.beh.tun.TunnelEntryListener;
import org.jtrfp.trcl.core.ResourceManager;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.file.TDFFile.ExitMode;
import org.jtrfp.trcl.file.TNLFile;
import org.jtrfp.trcl.file.TNLFile.Segment;
import org.jtrfp.trcl.file.TNLFile.Segment.Obstacle;
import org.jtrfp.trcl.flow.LoadingProgressReporter;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.img.vq.ColorPaletteVectorList;
import org.jtrfp.trcl.obj.BarrierCube;
import org.jtrfp.trcl.obj.Explosion.ExplosionType;
import org.jtrfp.trcl.obj.ForceField;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.TunnelExitObject;
import org.jtrfp.trcl.obj.TunnelSegment;
import org.jtrfp.trcl.obj.WorldObject;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

public class Tunnel extends RenderableSpacePartitioningGrid {
    private LVLFile 	lvl;
    private final TR 	tr;
    private final GL3 	gl;
    final double 	tunnelDia = 150000;
    final double 	wallThickness = 5000;
    private final World world;
    private 	  Color []		   palette;
    private 	  ColorPaletteVectorList   paletteVL;
    private	  ColorPaletteVectorList   ESTuTvPalette;
    private final TDFFile.Tunnel 	   sourceTunnel;
    private final TunnelExitObject 	   exitObject;
    private final LoadingProgressReporter[]reporters;
    private final LoadingProgressReporter  tunnelAssemblyReporter;
    private ObjectSystem		   objectSystem;//DO NOT REMOVE. Strong reference to avoid GC
    private final HashSet<TunnelEntryListener>
    					   tunnelEntryListeners = new HashSet<TunnelEntryListener>();
    public static final SkyCubeGen	   TUNNEL_SKYCUBE_GEN = new HorizGradientCubeGen
		(Color.darkGray,Color.black);

    public static final Vector3D TUNNEL_START_POS = new Vector3D(0,
	    TR.mapSquareSize * 5, TR.mapSquareSize*15);
    public static final ObjectDirection TUNNEL_START_DIRECTION = new ObjectDirection(
	    new Vector3D(1, 0, 0), new Vector3D(0, 1, 0));
    public static final Vector3D TUNNEL_OBJECT_POS_OFFSET = new Vector3D(0, 0,
	    -2 * TR.mapSquareSize);

    public Tunnel(TR tr, TDFFile.Tunnel sourceTunnel,
	    LoadingProgressReporter rootReporter) {
	super(tr.getDefaultGrid());
	this.world	  = tr.getWorld();
	reporters	  = rootReporter.generateSubReporters(2);
	this.sourceTunnel = sourceTunnel;
	this.tr           = tr;
	gl 		  = tr.gpu.get().getGl();
	tunnelAssemblyReporter 
	  		  = reporters[0];
	Vector3D tunnelEnd = null;
	deactivate();// Sleep until activated by tunnel entrance
	try {
	    lvl = world.getTr().getResourceManager()
		    .getLVL(sourceTunnel.getTunnelLVLFile());
	    final Vector3D entranceVector = TUNNEL_START_DIRECTION.getHeading();
	    palette = tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile());
	    palette[0] = new Color(0,0,0,0);//KLUDGE: Color zero must be transparent.
	    paletteVL = new ColorPaletteVectorList(palette);
	    ESTuTvPalette = new ColorPaletteVectorList(tr.getResourceManager().getLTE("FOG\\"+lvl.getLuminanceMapFile()).toColors(palette));
	    tunnelEnd = buildTunnel(sourceTunnel, entranceVector, false);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	exitObject = new TunnelExitObject(tr, this);
	exitObject
		.setMirrorTerrain(sourceTunnel.getExitMode() == ExitMode.exitToChamber);
	exitObject.setPosition(tunnelEnd.add(new Vector3D(50000, 0, 0))
		.toArray());
	exitObject.notifyPositionChange();
	add(exitObject);
	// X is tunnel depth, Z is left-right
	try {
	    objectSystem = new ObjectSystem(this, world, lvl, null, Vector3D.MINUS_I,
		    TUNNEL_START_POS.add(TUNNEL_OBJECT_POS_OFFSET),
		    reporters[1]);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	/*tr.getGame().getCurrentMission().getOverworldSystem().add(
		entranceObject = new TunnelEntranceObject(tr, this));*/
    }// end constructor

    private Vector3D buildTunnel(TDFFile.Tunnel _tun, Vector3D groundVector,
	    boolean entrance) throws IllegalAccessException,
	    UnrecognizedFormatException, FileNotFoundException,
	    FileLoadException, IOException {
	// Entrance uses only a stub. Player is warped to TUNNEL_POS facing
	// TUNNEL_START_DIRECTION
	ResourceManager rm = tr.getResourceManager();
	LVLFile tlvl = rm.getLVL(_tun.getTunnelLVLFile());
	final ColorPaletteVectorList tunnelColorPalette = new ColorPaletteVectorList(tr.getResourceManager().getPalette(lvl.getGlobalPaletteFile()));
	TextureDescription[] tunnelTexturePalette = rm.getTextures(
		tlvl.getLevelTextureListFile(), paletteVL, ESTuTvPalette, true);
	TNLFile tun = tr.getResourceManager().getTNLData(
		tlvl.getHeightMapOrTunnelFile());

	final double segLen = 65536;
	final double bendiness = 18;
	List<Segment> segs = tun.getSegments();
	final LoadingProgressReporter[] reporters = tunnelAssemblyReporter
		.generateSubReporters(segs.size());
	// Vector3D tunnelEnd = TUNNEL_START_POS;
	Rotation rotation = entrance ? new Rotation(new Vector3D(0, 0, 1),
		groundVector) : new Rotation(new Vector3D(0, 0, 1),
		new Vector3D(1, 0, 0));
	Vector3D startPoint = TUNNEL_START_POS;

	Vector3D segPos = Vector3D.ZERO;
	
	final Vector3D top = rotation.applyTo(new Vector3D(0, 1, 0));
	/*
	if (entrance) {
	    // Entrance is just a stub so we only need a few of the segments
	    List<Segment> newSegs = new ArrayList<Segment>();
	    for (int i = 0; i < 10; i++) {
		newSegs.add(segs.get(i));
	    }
	    segs = newSegs;
	}*/
	// CONSTRUCT AND INSTALL SEGMENTS
	int segIndex = 0;
	Vector3D finalPos = TUNNEL_START_POS;
	for (Segment s : segs) {
	    reporters[segIndex].complete();
	    tr.getReporter().report(
		    "org.jtrfp.trcl.Tunnel." + _tun.getTunnelLVLFile()
			    + ".segment" + (segIndex++) + "",
		    s.getObstacle().name());
	    // Figure out the space the segment will take
	    Vector3D positionDelta = new Vector3D(
		    (double) (s.getEndX() - s.getStartX()) * bendiness * -1,
		    (double) (s.getEndY() - s.getStartY()) * bendiness, segLen);
	    // Create the segment
	    Vector3D position = startPoint.add(rotation.applyTo(segPos));
	    TunnelSegment ts = new TunnelSegment(tr, s, tunnelTexturePalette,
		    segLen, positionDelta.getX(), positionDelta.getY());
	    ts.setPosition(position.toArray());
	    ts.setHeading(entrance ? groundVector : Vector3D.PLUS_I);
	    ts.setTop(entrance ? top : Vector3D.PLUS_J);
	    // Install the segment
	    add(ts);
	    installObstacles(s, tunnelColorPalette, ESTuTvPalette, tunnelTexturePalette, entrance ? groundVector
		    : Vector3D.PLUS_I, entrance ? top : Vector3D.PLUS_J,
		    position, TR.legacy2Modern(s.getStartWidth()
			    * TunnelSegment.TUNNEL_DIA_SCALAR),
		    TR.legacy2Modern(s.getStartWidth()
			    * TunnelSegment.TUNNEL_DIA_SCALAR), tr);
	    // Move origin to next segment
	    segPos = segPos.add(positionDelta);
	    finalPos = position;
	}// end for(segments)
	return finalPos;
    }// end buildTunnel(...)

    /**
     * Tunnel items: FANBODY.BIN - fan IRIS.BIN - animated iris BEAM.BIN /
     * PIPE.BIN JAW1.BIN (right) JAW2.BIN (left) - jaws ELECTRI[0-3].RAW - force
     * field TP1.RAW - good enough for blastable door?
     * 
     * @throws IOException
     * @throws FileLoadException
     * 
     * 
     */

    private void installObstacles(Segment s, ColorPaletteVectorList tunnelColorPalette, ColorPaletteVectorList ESTuTvPalette,
	    TextureDescription[] tunnelTexturePalette, Vector3D heading,
	    Vector3D top, Vector3D wPos, double width, double height, TR tr)
	    throws IllegalAccessException, FileLoadException, IOException {
	final ColorPaletteVectorList palette = tr.getGlobalPaletteVL();
	Obstacle    obs = s.getObstacle();
	WorldObject wo;
	Model       m;
	switch (obs) {
	case none0:
	    break;
	case doorway: {
	    m = Model.buildCube(tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, .5, .5,
		    1, 1, tr);
	    wo = new WorldObject(world.getTr(), m);
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    add(wo);
	    break;
	}
	case closedDoor: {
	    BarrierCube bc = new BarrierCube(tr, tunnelDia, tunnelDia,
		    wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, .5, .5,
		    0, 1, false);
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.addBehavior(new DamageableBehavior().setHealth(4096));
	    bc.addBehavior(new ExplodesOnDeath(ExplosionType.Blast));
	    bc.addBehavior(new DeathBehavior());
	    bc.addBehavior(new DebrisOnDeathBehavior());
	    bc.addBehavior(new DestructibleWallBehavior());
	    bc.setTop(top);
	    add(bc);
	    break;
	}
	case blownOpenDoor://TODO: This is not displaying alpha
	    BarrierCube bc = new BarrierCube(tr, tunnelDia, tunnelDia,
		    wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, .5, .5,
		    1, 1, true);
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    add(bc);
	    break;
	case movingWallLeft: {
	    Vector3D endPos = wPos.add(heading.crossProduct(top)
		    .scalarMultiply(tunnelDia));
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.addBehavior(new ShiftingObjectBehavior(3000, wPos, endPos));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	}
	case movingWallRight: {
	    Vector3D endPos = wPos.subtract(heading.crossProduct(top)
		    .scalarMultiply(tunnelDia));
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.addBehavior(new ShiftingObjectBehavior(3000, wPos, endPos));
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	}
	case movingWallDown: {
	    Vector3D endPos = wPos.subtract(top.scalarMultiply(tunnelDia));
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.addBehavior(new ShiftingObjectBehavior(3000, wPos, endPos));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	}
	case movingWallUp: {
	    Vector3D endPos = wPos.add(top.scalarMultiply(tunnelDia));
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.addBehavior(new ShiftingObjectBehavior(3000, wPos, endPos));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	}
	case wallLeftSTUB:
	case wallLeft:
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { 0., tunnelDia / 2., 0 }, false);
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	case wallRightSTUB:
	case wallRight:
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia, tunnelDia / 2., 0 }, false);
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	case wallDownSTUB:
	case wallDown:
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.setPosition((wPos.subtract(top.scalarMultiply(tunnelDia / 2)))
		    .toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	case wallUpSTUB:
	case wallUp:
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { tunnelDia / 2., tunnelDia / 2., 0 }, false);
	    bc.setPosition((wPos.add(top.scalarMultiply(tunnelDia / 2)))
		    .toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;
	case rotatingHalfWall:
	    {final double rotPeriod = 32768./(double)s.getRotationSpeed();
	    final boolean rotate = !Double.isInfinite(rotPeriod);
	    
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { 0, tunnelDia / 2., 0 }, false);
	    if(rotate){
		bc.addBehavior(new RotatingObjectBehavior(heading, heading, top,
		  (int)(rotPeriod*1000.), 0));
		bc.setTop(top);
	    }else
		bc.setTop(new Rotation(heading,Math.PI+Math.PI / 2).applyTo(top));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.setTop(top);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;}
	case rotating34Wall:
	    {final double rotPeriod = 32768./(double)s.getRotationSpeed();
	    final boolean rotate = !Double.isInfinite(rotPeriod);
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { 0, tunnelDia / 2., 10 }, false);
	    if(rotate){
		bc.addBehavior(new RotatingObjectBehavior(heading, heading, top,
		    (int)(rotPeriod*1000.), Math.PI));
		bc.setTop(top);
	    }else
		bc.setTop(new Rotation(heading,Math.PI+Math.PI / 2).applyTo(top));
	    bc.setPosition(wPos.toArray());
	    bc.setHeading(heading);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    
	    bc = new BarrierCube(tr, tunnelDia, tunnelDia, wallThickness,
		    tunnelTexturePalette[s.getObstacleTextureIndex()],
		    new double[] { 0, tunnelDia / 2., 0 }, false);
	    if(rotate){
		bc.addBehavior(new RotatingObjectBehavior(heading, heading, top,
			    (int)(rotPeriod*1000.), Math.PI+Math.PI / 2));
		bc.setTop(top);
	    }else
		bc.setTop(new Rotation(heading,Math.PI * 2).applyTo(top));
	    
	    bc.setPosition((wPos.add(new Vector3D(100, 0, 0))).toArray());
	    bc.setHeading(heading);
	    bc.addBehavior(new CubeCollisionBehavior(bc));
	    add(bc);
	    break;}
	case fan:
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "BLADE.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 28,
		    false, palette,ESTuTvPalette));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    wo.addBehavior(new RotatingObjectBehavior(heading, heading, top,
		    6000, Math.random()*2*Math.PI));
	    add(wo);
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "FANBODY.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 28,
		    false, palette,null));//No ESTuTv for fan for now.
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case jawsVertical:
	    // Up jaw
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "JAW2.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette,ESTuTvPalette));
	    wo.addBehavior(new ShiftingObjectBehavior(3000, wPos, wPos.add(top
		    .scalarMultiply(tunnelDia / 2))));
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(heading.crossProduct(top).negate());
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    // Down jaw
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "JAW1.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette,ESTuTvPalette));
	    wo.addBehavior(new ShiftingObjectBehavior(3000, wPos, wPos
		    .subtract(top.scalarMultiply(tunnelDia / 2))));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(heading.crossProduct(top).negate());
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case jawsHorizontal:
	    // Left jaw
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "JAW2.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.addBehavior(new ShiftingObjectBehavior(3000, wPos, wPos
		    .add(heading.crossProduct(top)
			    .scalarMultiply(tunnelDia / 2))));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    // Right jaw
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "JAW1.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.addBehavior(new ShiftingObjectBehavior(3000, wPos, wPos
		    .subtract(heading.crossProduct(top).scalarMultiply(
			    tunnelDia / 2))));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case metalBeamUp:
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "BEAM.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.setPosition(wPos.add(new Vector3D(0, tunnelDia / 6, 0))
		    .toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case metalBeamDown:
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "BEAM.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.setPosition(wPos.add(new Vector3D(0, -tunnelDia / 6, 0))
		    .toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case metalBeamLeft:
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "BEAM.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.setPosition(wPos.add(new Vector3D(-tunnelDia / 6, 0, 0))
		    .toArray());
	    wo.setHeading(heading);
	    wo.setTop(top.crossProduct(heading));
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case metalBeamRight:
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "BEAM.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 8,
		    false, palette, ESTuTvPalette));
	    wo.setPosition(wPos.add(new Vector3D(tunnelDia / 6, 0, 0))
		    .toArray());
	    wo.setHeading(heading);
	    wo.setTop(top.crossProduct(heading));
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	case forceField: {
	    //ELECTRI[0-3].RAW 
	    final ForceField ff = new ForceField(tr,(int)tunnelDia,(int)wallThickness);
	    ff.setPosition(wPos.toArray());
	    ff.setHeading(heading);
	    ff.setTop(top);
	    add(ff);
	    break;
	}
	// Invisible walls, as far as I know, are never used.
	// This makes sense: There is nothing fun about trying to get through a
	// tunnel and crashing into invisible walls.
	case invisibleWallUp:// TODO
	    break;
	case invisibleWallDown:// TODO
	    break;
	case invisibleWallLeft:// TODO
	    break;
	case invisibleWallRight:// TODO
	    break;
	case iris: {
	    wo = new WorldObject(tr, tr.getResourceManager().getBINModel(
		    "IRIS.BIN",
		    tunnelTexturePalette[s.getObstacleTextureIndex()], 4 * 256,
		    false, palette, ESTuTvPalette));
	    final Model mod = wo.getModel();
	    wo.addBehavior(new IrisBehavior(new Sequencer(mod
		    .getFrameDelayInMillis(), 2, true), width));
	    wo.setPosition(wPos.toArray());
	    wo.setHeading(heading);
	    wo.setTop(top);
	    wo.addBehavior(new CubeCollisionBehavior(wo));
	    add(wo);
	    break;
	}
	}// end switch(obstruction)
    }// end installObstacles()

    public WorldObject getFallbackModel() throws IllegalAccessException,
	    FileLoadException, IOException {
	return new WorldObject(tr, tr.getResourceManager().getBINModel(
		"NAVTARG.BIN", null, 8, false, paletteVL,null));
    }

    /**
     * @return the sourceTunnel
     */
    public TDFFile.Tunnel getSourceTunnel() {
	return sourceTunnel;
    }

    /**
     * @return the exitObject
     */
    public TunnelExitObject getExitObject() {
	return exitObject;
    }
    
    public void addTunnelEntryListener(TunnelEntryListener l){
	tunnelEntryListeners.add(l);
    }
    
    public void removeTunnelEntryListener(TunnelEntryListener l){
	tunnelEntryListeners.remove(l);
    }

    public void dispatchTunnelEntryNotifications() {
	for(TunnelEntryListener l:tunnelEntryListeners)
	    l.notifyTunnelEntered(this);
    }//end dispatchTunnelEntryNotifications()
}// end Tunnel
