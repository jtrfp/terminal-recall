package org.jtrfp.trcl.file;

import java.io.InputStream;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.jtrfp.trcl.file.LVLFile.LevelType;

public class LVLFileTest extends TestCase {
    private LVLFile subject;

    protected void setUp() throws Exception {
	super.setUp();
	//Load the test file
	final InputStream is = LVLFileTest.class.getResourceAsStream("/test.LVL");
	Assert.assertNotNull(is);
	subject = new LVLFile(is);
	Assert.assertNotNull(subject);
	is.close();
    }//end setUp()
    
    protected void tearDown() throws Exception {
	/*Supposedly some testing suites keep a test object instantiated, 
	 * meaning this stale ref could hang around for a while if not nulled.
	 */
	subject=null;
    }

    public void testGetLevelEndVideoFile() {
	Assert.assertEquals("leavingvideo.tvi", subject.getLevelEndVideoFile());
    }

    public void testGetLevelType() {
	Assert.assertEquals(LevelType.Overworld, subject.getLevelType());
    }

    public void testGetBriefingTextFile() {
	Assert.assertEquals("briefing.txt", subject.getBriefingTextFile());
    }

    public void testGetHeightMapOrTunnelFile() {
	Assert.assertEquals("heightmap.raw", subject.getHeightMapOrTunnelFile());
    }

    public void testGetTexturePlacementFile() {
	Assert.assertEquals("groundtiles.clr", subject.getTexturePlacementFile());
    }

    public void testGetGlobalPaletteFile() {
	Assert.assertEquals("globalpalette.act", subject.getGlobalPaletteFile());
    }

    public void testGetLevelTextureListFile() {
	Assert.assertEquals("texturelist.tex", subject.getLevelTextureListFile());
    }

    public void testGetQkeFile() {
	Assert.assertEquals("qkefile.qke", subject.getQkeFile());
    }

    public void testGetPowerupPlacementFile() {
	Assert.assertEquals("pupfile.pup", subject.getPowerupPlacementFile());
    }

    public void testGetTextureAnimationFile() {
	Assert.assertEquals("anifile.ani", subject.getTextureAnimationFile());
    }

    public void testGetTunnelDefinitionFile() {
	Assert.assertEquals("tunneldef.tdf", subject.getTunnelDefinitionFile());
    }

    public void testGetCloudTextureFile() {
	Assert.assertEquals("skytexture.raw", subject.getCloudTextureFile());
    }

    public void testGetBackgroundGradientPaletteFile() {
	Assert.assertEquals("skypalette.act", subject.getBackgroundGradientPaletteFile());
    }

    public void testGetEnemyDefinitionAndPlacementFile() {
	Assert.assertEquals("deffile.def", subject.getEnemyDefinitionAndPlacementFile());
    }

    public void testGetNavigationFile() {
	Assert.assertEquals("navfile.nav", subject.getNavigationFile());
    }

    public void testGetBackgroundMusicFile() {
	Assert.assertEquals("musicfile.mod", subject.getBackgroundMusicFile());
    }

    public void testGetPrecalculatedFogFile() {
	Assert.assertEquals("fogfile.fog", subject.getPrecalculatedFogFile());
    }

    public void testGetLuminanceMapFile() {
	Assert.assertEquals("ltefile.lte", subject.getLuminanceMapFile());
    }

    public void testGetSunlightDirectionVector() {
	final AbstractTriplet dir = subject.getSunlightDirectionVector();
	Assert.assertEquals(1, dir.getX());
	Assert.assertEquals(2, dir.getY());
	Assert.assertEquals(3, dir.getZ());
    }

    public void testGetAmbientLight() {
	Assert.assertEquals(1000, subject.getAmbientLight());
    }

    public void testGetChamberLightDirectionVector() {
	final AbstractTriplet dir = subject.getChamberLightDirectionVector();
	Assert.assertEquals(-4, dir.getX());
	Assert.assertEquals(-5, dir.getY());
	Assert.assertEquals(-6, dir.getZ());
    }

    public void testGetChamberAmbientLight() {
	Assert.assertEquals(2000, subject.getChamberAmbientLight());
    }

    public void testGetUnknownInt1() {
	Assert.assertEquals(100, subject.getUnknownInt1());
    }

    public void testGetIntroVideoFile() {
	Assert.assertEquals("introvideo.tvi", subject.getIntroVideoFile());
    }

    public void testGetTransitionVideoFile() {
	Assert.assertEquals("transitionvideo.tvi", subject.getTransitionVideoFile());
    }

    public void testGetMissionStartTextFile() {
	Assert.assertEquals("missionstarttext.mic", subject.getMissionStartTextFile());
    }

    public void testGetMissionEndTextFile() {
	Assert.assertEquals("missionendtext.mic", subject.getMissionEndTextFile());
    }

}//end LVLFileTest
