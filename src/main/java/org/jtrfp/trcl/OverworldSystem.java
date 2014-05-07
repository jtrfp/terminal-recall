package org.jtrfp.trcl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TextureDescription;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.TDFFile;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.ObjectSystem;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class OverworldSystem extends RenderableSpacePartitioningGrid {
    private CloudSystem cloudSystem;
    private TextureMesh textureMesh;
    private InterpolatingAltitudeMap altitudeMap;
    private Color fogColor = Color.black;
    private final ArrayList<DEFObject> defList = new ArrayList<DEFObject>();
    private RenderableSpacePartitioningGrid terrainMirror = new RenderableSpacePartitioningGrid(
	    this) {
    };
    private boolean chamberMode = false;
    private boolean tunnelMode = false;
    private final TR tr;

    public OverworldSystem(World w) {
	super(w);
	this.tr = w.getTr();
    }
    public void loadLevel(final LVLFile lvl, final TDFFile tdf){
	try { // Active by default
	    final World w = tr.getWorld();
	    Color[] globalPalette = tr.getGlobalPalette();
	    Future<TextureDescription>[] texturePalette = tr
		    .getResourceManager().getTextures(
			    lvl.getLevelTextureListFile(), globalPalette,
			    GammaCorrectingColorProcessor.singleton,
			    tr.gpu.get().getGl());
	    System.out.println("Loading height map...");
	    altitudeMap = new InterpolatingAltitudeMap(tr.getResourceManager()
		    .getRAWAltitude(lvl.getHeightMapOrTunnelFile()));
	    tr.setAltitudeMap(altitudeMap);
	    System.out.println("... Done");
	    textureMesh = tr.getResourceManager().getTerrainTextureMesh(
		    lvl.getTexturePlacementFile(), texturePalette);
	    // Terrain
	    System.out.println("Building terrain...");
	    boolean flatShadedTerrain = lvl.getHeightMapOrTunnelFile()
		    .toUpperCase().contains("BORG");
	    TerrainSystem terrain = new TerrainSystem(altitudeMap, textureMesh,
		    TR.mapSquareSize, this, terrainMirror, tr, tdf,
		    flatShadedTerrain);
	    System.out.println("...Done.");
	    // Clouds
	    System.out.println("Setting up sky...");
	    if (!lvl.getCloudTextureFile().contentEquals("stars.vox")) {
		cloudSystem = new CloudSystem(this, tr, this, lvl,
			TR.mapSquareSize * 8,
			(int) (TR.mapWidth / (TR.mapSquareSize * 8)),
			w.sizeY / 3.5);
	    }
	    System.out.println("...Done.");
	    // Objects
	    System.out.println("Setting up objects...");
	    ObjectSystem objectSystem = new ObjectSystem(this, w, lvl, defList,
		    null, Vector3D.ZERO);
	    objectSystem.activate();
	    System.out.println("...Done.");
	    // Tunnel activators
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// terrainMirror.deactivate();//TODO: Uncomment
    }// end constructor

    public Color getFogColor() {
	return fogColor;
    }

    public void setFogColor(Color c) {
	fogColor = c;
	if (fogColor == null)
	    throw new NullPointerException("Passed color is intolerably null.");
    }

    public List<DEFObject> getDefList() {
	return defList;
    }

    public SpacePartitioningGrid<PositionedRenderable> getTerrainMirror() {
	return terrainMirror;
    }

    public void setChamberMode(boolean mirrorTerrain) {
	tr.getReporter().report("org.jtrfp.OverworldSystem.isInChamber?",
		"" + mirrorTerrain);
	chamberMode = mirrorTerrain;
	final CloudSystem cs = getCloudSystem();
	if (mirrorTerrain) {
	    this.getTerrainMirror().activate();
	    if (cs != null)
		cs.deactivate();
	} else {
	    this.getTerrainMirror().deactivate();
	    if (cs != null)
		cs.activate();
	}
    }// end chamberMode

    private CloudSystem getCloudSystem() {
	return cloudSystem;
    }

    /**
     * @return the chamberMode
     */
    public boolean isChamberMode() {
	return chamberMode;
    }

    /**
     * @return the tunnelMode
     */
    public boolean isTunnelMode() {
	return tunnelMode;
    }

    /**
     * @param tunnelMode
     *            the tunnelMode to set
     */
    public void setTunnelMode(boolean tunnelMode) {
	this.tunnelMode = tunnelMode;
    }
}// end OverworldSystem
