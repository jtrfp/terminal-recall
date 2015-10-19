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
package org.jtrfp.trcl.file;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.apache.commons.beanutils.BeanUtils;
import org.jtrfp.jfdt.ClassInclusion;
import org.jtrfp.jfdt.FailureBehavior;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.DataKey;
import org.jtrfp.jtrfp.lvl.ILvlData;

public class LVLFile extends SelfParsingFile implements ILvlData{
    private static DataKey [] usedKeys; //jTRFP stuff.
    
    LevelType levelType;

    String briefingTextFile;
    String heightMapOrTunnelFile;
    String texturePlacementFile;
    String globalPaletteFile;
    String levelTextureListFile;
    String qkeFile; // unknown
    String powerupPlacementFile;
    String textureAnimationFile;
    String tunnelDefinitionFile;
    String cloudTextureFile;
    String backgroundGradientPaletteFile;
    String enemyDefinitionAndPlacementFile;
    String navigationFile;
    String backgroundMusicFile;
    String precalculatedFogFile;
    String luminanceMapFile;

    AbstractTriplet sunlightDirectionVector;
    int ambientLight;
    // Chamber light found by WDLMaster
    AbstractTriplet chamberLightDirectionVector;
    int chamberAmbientLight, unknownInt1;
    // New Story stuff
    String introVideoFile;
    String levelEndVideoFile;
    String transitionVideoFile;
    String missionStartTextFile;
    String missionEndTextFile;

    enum LevelType {
	UNKNOWN0, Tunnel, UNKNOWN2, UNKNOWN3, Overworld
    }

    @Override
    public void describeFormat(Parser prs) throws UnrecognizedFormatException {
	// REMEMBER: use \r\n because TR files use carriage-return-line-feed and
	// not just the line-feed \n that java uses.
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("levelType", LevelType.class), false);

	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("briefingTextFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("heightMapOrTunnelFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("texturePlacementFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("globalPaletteFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("levelTextureListFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("qkeFile", String.class),
		false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("powerupPlacementFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("textureAnimationFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("tunnelDefinitionFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("cloudTextureFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("backgroundGradientPaletteFile", String.class),
		false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("enemyDefinitionAndPlacementFile", String.class),
		false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("navigationFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("backgroundMusicFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("precalculatedFogFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("luminanceMapFile", String.class), false);

	prs.subParseProposedClasses(
		prs.property("sunlightDirectionVector", AbstractTriplet.class),
		ClassInclusion.classOf(AbstractTriplet.class));
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("ambientLight", Integer.class), false);
	prs.subParseProposedClasses(prs.property("chamberLightDirectionVector",
		AbstractTriplet.class), ClassInclusion
		.classOf(AbstractTriplet.class));
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("chamberAmbientLight", Integer.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("unknownInt1", Integer.class), false);

	prs.ignoreEOF(true);
	prs.expectString(";New story stuff\r\n", FailureBehavior.IGNORE);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("introVideoFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("levelEndVideoFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("transitionVideoFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("missionStartTextFile", String.class), false);
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("missionEndTextFile", String.class), false);
	// EOF
    }

    public LVLFile(InputStream is) throws IllegalAccessException, IOException {
	super(is);
    }

    /**
     * @return the levelEndVideoFile
     */
    public String getLevelEndVideoFile() {
	return levelEndVideoFile;
    }

    /**
     * @param levelEndVideoFile
     *            the levelEndVideoFile to set
     */
    public void setLevelEndVideoFile(String levelEndVideoFile) {
	this.levelEndVideoFile = levelEndVideoFile;
    }

    /**
     * @return the levelType
     */
    public LevelType getLevelType() {
	return levelType;
    }

    /**
     * @param levelType
     *            the levelType to set
     */
    public void setLevelType(LevelType levelType) {
	this.levelType = levelType;
    }

    /**
     * @return the briefingTextFile
     */
    public String getBriefingTextFile() {
	return briefingTextFile;
    }

    /**
     * @param briefingTextFile
     *            the briefingTextFile to set
     */
    public void setBriefingTextFile(String briefingTextFile) {
	this.briefingTextFile = briefingTextFile;
    }

    /**
     * @return the heightMapOrTunnelFile
     */
    public String getHeightMapOrTunnelFile() {
	return heightMapOrTunnelFile;
    }

    /**
     * @param heightMapOrTunnelFile
     *            the heightMapOrTunnelFile to set
     */
    public void setHeightMapOrTunnelFile(String heightMapOrTunnelFile) {
	this.heightMapOrTunnelFile = heightMapOrTunnelFile;
    }

    /**
     * CLR file stored in \DATA containing terrain texture indices to use.
     * 
     * @return path to CLR file
     */
    public String getTexturePlacementFile() {
	return texturePlacementFile;
    }

    /**
     * @param texturePlacementFile
     *            the texturePlacementFile to set
     */
    public void setTexturePlacementFile(String texturePlacementFile) {
	this.texturePlacementFile = texturePlacementFile;
    }

    /**
     * @return the globalPaletteFile
     */
    public String getGlobalPaletteFile() {
	return globalPaletteFile;
    }

    /**
     * @param globalPaletteFile
     *            the globalPaletteFile to set
     */
    public void setGlobalPaletteFile(String globalPaletteFile) {
	this.globalPaletteFile = globalPaletteFile;
    }

    /**
     * @return the levelTextureListFile
     */
    public String getLevelTextureListFile() {
	return levelTextureListFile;
    }

    /**
     * @param levelTextureListFile
     *            the levelTextureListFile to set
     */
    public void setLevelTextureListFile(String levelTextureListFile) {
	this.levelTextureListFile = levelTextureListFile;
    }

    /**
     * @return the qkeFile
     */
    public String getQkeFile() {
	return qkeFile;
    }

    /**
     * @param qkeFile
     *            the qkeFile to set
     */
    public void setQkeFile(String qkeFile) {
	this.qkeFile = qkeFile;
    }

    /**
     * @return the powerupPlacementFile
     */
    public String getPowerupPlacementFile() {
	return powerupPlacementFile;
    }

    /**
     * @param powerupPlacementFile
     *            the powerupPlacementFile to set
     */
    public void setPowerupPlacementFile(String powerupPlacementFile) {
	this.powerupPlacementFile = powerupPlacementFile;
    }

    /**
     * @return the textureAnimationFile
     */
    public String getTextureAnimationFile() {
	return textureAnimationFile;
    }

    /**
     * @param textureAnimationFile
     *            the textureAnimationFile to set
     */
    public void setTextureAnimationFile(String textureAnimationFile) {
	this.textureAnimationFile = textureAnimationFile;
    }

    /**
     * @return the tunnelDefinitionFile
     */
    public String getTunnelDefinitionFile() {
	return tunnelDefinitionFile;
    }

    /**
     * @param tunnelDefinitionFile
     *            the tunnelDefinitionFile to set
     */
    public void setTunnelDefinitionFile(String tunnelDefinitionFile) {
	this.tunnelDefinitionFile = tunnelDefinitionFile;
    }

    /**
     * @return the cloudTextureFile
     */
    public String getCloudTextureFile() {
	return cloudTextureFile;
    }

    /**
     * @param cloudTextureFile
     *            the cloudTextureFile to set
     */
    public void setCloudTextureFile(String cloudTextureFile) {
	this.cloudTextureFile = cloudTextureFile;
    }

    /**
     * @return the backgroundGradientPaletteFile
     */
    public String getBackgroundGradientPaletteFile() {
	return backgroundGradientPaletteFile;
    }

    /**
     * @param backgroundGradientPaletteFile
     *            the backgroundGradientPaletteFile to set
     */
    public void setBackgroundGradientPaletteFile(
	    String backgroundGradientPaletteFile) {
	this.backgroundGradientPaletteFile = backgroundGradientPaletteFile;
    }

    /**
     * @return the enemyDefinitionAndPlacementFile
     */
    public String getEnemyDefinitionAndPlacementFile() {
	return enemyDefinitionAndPlacementFile;
    }

    /**
     * @param enemyDefinitionAndPlacementFile
     *            the enemyDefinitionAndPlacementFile to set
     */
    public void setEnemyDefinitionAndPlacementFile(
	    String enemyDefinitionAndPlacementFile) {
	this.enemyDefinitionAndPlacementFile = enemyDefinitionAndPlacementFile;
    }

    /**
     * @return the navigationFile
     */
    public String getNavigationFile() {
	return navigationFile;
    }

    /**
     * @param navigationFile
     *            the navigationFile to set
     */
    public void setNavigationFile(String navigationFile) {
	this.navigationFile = navigationFile;
    }

    /**
     * @return the backgroundMusicFile
     */
    public String getBackgroundMusicFile() {
	return backgroundMusicFile;
    }

    /**
     * @param backgroundMusicFile
     *            the backgroundMusicFile to set
     */
    public void setBackgroundMusicFile(String backgroundMusicFile) {
	this.backgroundMusicFile = backgroundMusicFile;
    }

    /**
     * @return the precalculatedFogFile
     */
    public String getPrecalculatedFogFile() {
	return precalculatedFogFile;
    }

    /**
     * @param precalculatedFogFile
     *            the precalculatedFogFile to set
     */
    public void setPrecalculatedFogFile(String precalculatedFogFile) {
	this.precalculatedFogFile = precalculatedFogFile;
    }

    /**
     * @return the luminanceMapFile
     */
    public String getLuminanceMapFile() {
	return luminanceMapFile;
    }

    /**
     * @param luminanceMapFile
     *            the luminanceMapFile to set
     */
    public void setLuminanceMapFile(String luminanceMapFile) {
	this.luminanceMapFile = luminanceMapFile;
    }

    /**
     * @return the sunlightDirectionVector
     */
    public AbstractTriplet getSunlightDirectionVector() {
	return sunlightDirectionVector;
    }

    /**
     * @param sunlightDirectionVector
     *            the sunlightDirectionVector to set
     */
    public void setSunlightDirectionVector(
	    AbstractTriplet sunlightDirectionVector) {
	this.sunlightDirectionVector = sunlightDirectionVector;
    }

    /**
     * @return the ambientLight
     */
    public int getAmbientLight() {
	return ambientLight;
    }

    /**
     * @param ambientLight
     *            the ambientLight to set
     */
    public void setAmbientLight(int ambientLight) {
	this.ambientLight = ambientLight;
    }

    /**
     * Found by WDLMaster
     * 
     * @return the chamberLightDirectionVector
     */
    public AbstractTriplet getChamberLightDirectionVector() {
	return chamberLightDirectionVector;
    }

    /**
     * Found by WDLMaster
     * 
     * @return the chamberAmbientLight
     */
    public int getChamberAmbientLight() {
	return chamberAmbientLight;
    }

    /**
     * Found by WDLMaster
     * 
     * @param chamberAmbientLight
     *            the chamberAmbientLight to set
     */
    public void setChamberAmbientLight(int chamberAmbientLight) {
	this.chamberAmbientLight = chamberAmbientLight;
    }

    /**
     * @return the unknownInt1
     */
    public int getUnknownInt1() {
	return unknownInt1;
    }

    /**
     * @param unknownInt1
     *            the unknownInt1 to set
     */
    public void setUnknownInt1(int unknownInt1) {
	this.unknownInt1 = unknownInt1;
    }

    /**
     * Found by WDLMaster
     * 
     * @param chamberLightDirectionVector
     *            the chamberLightDirectionVector to set
     */
    public void setChamberLightDirectionVector(
	    AbstractTriplet chamberLightDirectionVector) {
	this.chamberLightDirectionVector = chamberLightDirectionVector;
    }

    /**
     * @return the introVideoFile
     */
    public String getIntroVideoFile() {
	return introVideoFile;
    }

    /**
     * @param introVideoFile
     *            the introVideoFile to set
     */
    public void setIntroVideoFile(String introVideoFile) {
	this.introVideoFile = introVideoFile;
    }

    /**
     * @return the transitionVideoFile
     */
    public String getTransitionVideoFile() {
	return transitionVideoFile;
    }

    /**
     * @param transitionVideoFile
     *            the transitionVideoFile to set
     */
    public void setTransitionVideoFile(String transitionVideoFile) {
	this.transitionVideoFile = transitionVideoFile;
    }

    /**
     * @return the missionStartTextFile
     */
    public String getMissionStartTextFile() {
	return missionStartTextFile;
    }

    /**
     * @param missionStartTextFile
     *            the missionStartTextFile to set
     */
    public void setMissionStartTextFile(String missionStartTextFile) {
	this.missionStartTextFile = missionStartTextFile;
    }

    /**
     * @return the missionEndTextFile
     */
    public String getMissionEndTextFile() {
	return missionEndTextFile;
    }

    /**
     * @param missionEndTextFile
     *            the missionEndTextFile to set
     */
    public void setMissionEndTextFile(String missionEndTextFile) {
	this.missionEndTextFile = missionEndTextFile;
    }
    
    //////// E X P E R I M E N T A L ///////////////////////////////////
    @Override
    public String getValue(DataKey key) {
	try{return BeanUtils.getProperty(this, key.getIdentifier());}
	catch(NoSuchMethodException e){return null;}
	catch(InvocationTargetException e){return null;}
	catch(IllegalAccessException e){return null;}
    }//end getValue(...)
    
    private static DataKey [] getUsedKeysSingleton(){
	if(usedKeys==null)
	    usedKeys = generateUsedKeys();
	return usedKeys;
    }//end getUsedKeysSingleton()
    
    private static DataKey [] generateUsedKeys(){
	ArrayList<DataKey> result = new ArrayList<DataKey>();
	try{for(PropertyDescriptor pd:Introspector.getBeanInfo(LVLFile.class).getPropertyDescriptors())
	    result.add(new DataKey(pd.getName(), pd.getDisplayName()));
	}catch(Exception e){e.printStackTrace();}
	return result.toArray(new DataKey[result.size()]);
    }//end generateUsedKeys()

    @Override
    public DataKey[] getUsedKeys() {
	return getUsedKeysSingleton();
    }//end getUsedKeys()

}// end LVLFile
