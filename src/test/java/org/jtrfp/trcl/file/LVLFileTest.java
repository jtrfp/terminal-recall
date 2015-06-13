package org.jtrfp.trcl.file;

import java.io.InputStream;

import junit.framework.TestCase;

import org.jtrfp.trcl.file.LVLFile.LevelType;

public class LVLFileTest extends TestCase {
    private LVLFile subject;

    protected void setUp() throws Exception {
	super.setUp();
	//Load the test file
	InputStream is = null;
	try{is = LVLFileTest.class.getResourceAsStream("/test.LVL");
	 assertNotNull(is);
	 subject = new LVLFile(is);
	 assertNotNull(subject);}
	finally{if(is!=null)is.close();}
    }//end setUp()
    
    protected void tearDown() throws Exception {
	/*Supposedly some testing suites keep a test object instantiated, 
	 * meaning this stale ref could hang around for a while if not nulled.
	 */
	super.tearDown();
	subject=null;
    }

    public void testGetLevelEndVideoFile() {
	assertEquals("leavingvideo.tvi", subject.getLevelEndVideoFile());
    }

    public void testGetLevelType() {
	assertEquals(LevelType.Overworld, subject.getLevelType());
    }

    public void testGetBriefingTextFile() {
	assertEquals("briefing.txt", subject.getBriefingTextFile());
    }

    public void testGetHeightMapOrTunnelFile() {
	assertEquals("heightmap.raw", subject.getHeightMapOrTunnelFile());
    }

    public void testGetTexturePlacementFile() {
	assertEquals("groundtiles.clr", subject.getTexturePlacementFile());
    }

    public void testGetGlobalPaletteFile() {
	assertEquals("globalpalette.act", subject.getGlobalPaletteFile());
    }

    public void testGetLevelTextureListFile() {
	assertEquals("texturelist.tex", subject.getLevelTextureListFile());
    }

    public void testGetQkeFile() {
	assertEquals("qkefile.qke", subject.getQkeFile());
    }

    public void testGetPowerupPlacementFile() {
	assertEquals("pupfile.pup", subject.getPowerupPlacementFile());
    }

    public void testGetTextureAnimationFile() {
	assertEquals("anifile.ani", subject.getTextureAnimationFile());
    }

    public void testGetTunnelDefinitionFile() {
	assertEquals("tunneldef.tdf", subject.getTunnelDefinitionFile());
    }

    public void testGetCloudTextureFile() {
	assertEquals("skytexture.raw", subject.getCloudTextureFile());
    }

    public void testGetBackgroundGradientPaletteFile() {
	assertEquals("skypalette.act", subject.getBackgroundGradientPaletteFile());
    }

    public void testGetEnemyDefinitionAndPlacementFile() {
	assertEquals("deffile.def", subject.getEnemyDefinitionAndPlacementFile());
    }

    public void testGetNavigationFile() {
	assertEquals("navfile.nav", subject.getNavigationFile());
    }

    public void testGetBackgroundMusicFile() {
	assertEquals("musicfile.mod", subject.getBackgroundMusicFile());
    }

    public void testGetPrecalculatedFogFile() {
	assertEquals("fogfile.fog", subject.getPrecalculatedFogFile());
    }

    public void testGetLuminanceMapFile() {
	assertEquals("ltefile.lte", subject.getLuminanceMapFile());
    }

    public void testGetSunlightDirectionVector() {
	final AbstractTriplet dir = subject.getSunlightDirectionVector();
	assertEquals(1, dir.getX());
	assertEquals(2, dir.getY());
	assertEquals(3, dir.getZ());
    }

    public void testGetAmbientLight() {
	assertEquals(1000, subject.getAmbientLight());
    }

    public void testGetChamberLightDirectionVector() {
	final AbstractTriplet dir = subject.getChamberLightDirectionVector();
	assertEquals(-4, dir.getX());
	assertEquals(-5, dir.getY());
	assertEquals(-6, dir.getZ());
    }

    public void testGetChamberAmbientLight() {
	assertEquals(2000, subject.getChamberAmbientLight());
    }

    public void testGetUnknownInt1() {
	assertEquals(100, subject.getUnknownInt1());
    }

    public void testGetIntroVideoFile() {
	assertEquals("introvideo.tvi", subject.getIntroVideoFile());
    }

    public void testGetTransitionVideoFile() {
	assertEquals("transitionvideo.tvi", subject.getTransitionVideoFile());
    }

    public void testGetMissionStartTextFile() {
	assertEquals("missionstarttext.mic", subject.getMissionStartTextFile());
    }

    public void testGetMissionEndTextFile() {
	assertEquals("missionendtext.mic", subject.getMissionEndTextFile());
    }

}//end LVLFileTest
