/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl.file;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.ritolaaudio.jfdt1.ClassInclusion;
import com.ritolaaudio.jfdt1.FailureBehavior;
import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.SelfParsingFile;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class LVLFile extends SelfParsingFile
	{

	LevelType levelType;
	
	String briefingTextFile;
	String heightMapOrTunnelFile;
	String texturePlacementFile;
	String globalPaletteFile;
	String levelTextureListFile;
	String qkeFile; //unknown
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
	
	AbstractVector sunlightDirectionVector;
	int ambientLight;
	AbstractVector unknownVector;
	int unknownInt0,unknownInt1;
	//New Story stuff
	String introVideoFile;
	String levelEndVideoFile;
	String transitionVideoFile;
	String missionStartTextFile;
	String missionEndTextFile;
	
	enum LevelType
		{
		UNKNOWN0,
		Tunnel,
		UNKNOWN2,
		UNKNOWN3,
		Overworld
		}
	
	private File file;
	
	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		//REMEMBER: use \r\n because TR files use carriage-return-line-feed and not just the line-feed \n that java uses.
		Parser.stringEndingWith("\r\n", Parser.property("levelType",LevelType.class), false);
		
		Parser.stringEndingWith("\r\n", Parser.property("briefingTextFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("heightMapOrTunnelFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("texturePlacementFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("globalPaletteFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("levelTextureListFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("qkeFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("powerupPlacementFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("textureAnimationFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("tunnelDefinitionFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("cloudTextureFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("backgroundGradientPaletteFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("enemyDefinitionAndPlacementFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("navigationFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("backgroundMusicFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("precalculatedFogFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("luminanceMapFile", String.class), false);
		
		Parser.subParseProposedClasses(Parser.property("sunlightDirectionVector",AbstractVector.class),ClassInclusion.classOf(AbstractVector.class));
		Parser.stringEndingWith("\r\n", Parser.property("ambientLight",Integer.class), false);
		Parser.subParseProposedClasses(Parser.property("unknownVector",AbstractVector.class),ClassInclusion.classOf(AbstractVector.class));
		Parser.stringEndingWith("\r\n",Parser.property("unknownInt0",Integer.class),false);
		Parser.stringEndingWith("\r\n",Parser.property("unknownInt1",Integer.class),false);
		Parser.expectString(";New story stuff\r\n", FailureBehavior.IGNORE);
		Parser.stringEndingWith("\r\n", Parser.property("introVideoFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("levelEndVideoFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("transitionVideoFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("missionStartTextFile", String.class), false);
		Parser.stringEndingWith("\r\n", Parser.property("missionEndTextFile", String.class), false);
		//EOF
		}

	public LVLFile(InputStream is) throws IllegalAccessException, IOException
	{super(is);}
	
	/**
	 * @return the levelEndVideoFile
	 */
	public String getLevelEndVideoFile()
		{
		return levelEndVideoFile;
		}

	/**
	 * @param levelEndVideoFile the levelEndVideoFile to set
	 */
	public void setLevelEndVideoFile(String levelEndVideoFile)
		{
		this.levelEndVideoFile = levelEndVideoFile;
		}

	/**
	 * @return the levelType
	 */
	public LevelType getLevelType()
		{
		return levelType;
		}

	/**
	 * @param levelType the levelType to set
	 */
	public void setLevelType(LevelType levelType)
		{
		this.levelType = levelType;
		}

	/**
	 * @return the briefingTextFile
	 */
	public String getBriefingTextFile()
		{
		return briefingTextFile;
		}

	/**
	 * @param briefingTextFile the briefingTextFile to set
	 */
	public void setBriefingTextFile(String briefingTextFile)
		{
		this.briefingTextFile = briefingTextFile;
		}

	/**
	 * @return the heightMapOrTunnelFile
	 */
	public String getHeightMapOrTunnelFile()
		{
		return heightMapOrTunnelFile;
		}

	/**
	 * @param heightMapOrTunnelFile the heightMapOrTunnelFile to set
	 */
	public void setHeightMapOrTunnelFile(String heightMapOrTunnelFile)
		{
		this.heightMapOrTunnelFile = heightMapOrTunnelFile;
		}

	/**
	 * CLR file stored in \DATA containing terrain texture indices to use.
	 * @return path to CLR file
	 */
	public String getTexturePlacementFile()
		{
		return texturePlacementFile;
		}

	/**
	 * @param texturePlacementFile the texturePlacementFile to set
	 */
	public void setTexturePlacementFile(String texturePlacementFile)
		{
		this.texturePlacementFile = texturePlacementFile;
		}

	/**
	 * @return the globalPaletteFile
	 */
	public String getGlobalPaletteFile()
		{
		return globalPaletteFile;
		}

	/**
	 * @param globalPaletteFile the globalPaletteFile to set
	 */
	public void setGlobalPaletteFile(String globalPaletteFile)
		{
		this.globalPaletteFile = globalPaletteFile;
		}

	/**
	 * @return the levelTextureListFile
	 */
	public String getLevelTextureListFile()
		{
		return levelTextureListFile;
		}

	/**
	 * @param levelTextureListFile the levelTextureListFile to set
	 */
	public void setLevelTextureListFile(String levelTextureListFile)
		{
		this.levelTextureListFile = levelTextureListFile;
		}

	/**
	 * @return the qkeFile
	 */
	public String getQkeFile()
		{
		return qkeFile;
		}

	/**
	 * @param qkeFile the qkeFile to set
	 */
	public void setQkeFile(String qkeFile)
		{
		this.qkeFile = qkeFile;
		}

	/**
	 * @return the powerupPlacementFile
	 */
	public String getPowerupPlacementFile()
		{
		return powerupPlacementFile;
		}

	/**
	 * @param powerupPlacementFile the powerupPlacementFile to set
	 */
	public void setPowerupPlacementFile(String powerupPlacementFile)
		{
		this.powerupPlacementFile = powerupPlacementFile;
		}

	/**
	 * @return the textureAnimationFile
	 */
	public String getTextureAnimationFile()
		{
		return textureAnimationFile;
		}

	/**
	 * @param textureAnimationFile the textureAnimationFile to set
	 */
	public void setTextureAnimationFile(String textureAnimationFile)
		{
		this.textureAnimationFile = textureAnimationFile;
		}

	/**
	 * @return the tunnelDefinitionFile
	 */
	public String getTunnelDefinitionFile()
		{
		return tunnelDefinitionFile;
		}

	/**
	 * @param tunnelDefinitionFile the tunnelDefinitionFile to set
	 */
	public void setTunnelDefinitionFile(String tunnelDefinitionFile)
		{
		this.tunnelDefinitionFile = tunnelDefinitionFile;
		}

	/**
	 * @return the cloudTextureFile
	 */
	public String getCloudTextureFile()
		{
		return cloudTextureFile;
		}

	/**
	 * @param cloudTextureFile the cloudTextureFile to set
	 */
	public void setCloudTextureFile(String cloudTextureFile)
		{
		this.cloudTextureFile = cloudTextureFile;
		}

	/**
	 * @return the backgroundGradientPaletteFile
	 */
	public String getBackgroundGradientPaletteFile()
		{
		return backgroundGradientPaletteFile;
		}

	/**
	 * @param backgroundGradientPaletteFile the backgroundGradientPaletteFile to set
	 */
	public void setBackgroundGradientPaletteFile(
			String backgroundGradientPaletteFile)
		{
		this.backgroundGradientPaletteFile = backgroundGradientPaletteFile;
		}

	/**
	 * @return the enemyDefinitionAndPlacementFile
	 */
	public String getEnemyDefinitionAndPlacementFile()
		{
		return enemyDefinitionAndPlacementFile;
		}

	/**
	 * @param enemyDefinitionAndPlacementFile the enemyDefinitionAndPlacementFile to set
	 */
	public void setEnemyDefinitionAndPlacementFile(
			String enemyDefinitionAndPlacementFile)
		{
		this.enemyDefinitionAndPlacementFile = enemyDefinitionAndPlacementFile;
		}

	/**
	 * @return the navigationFile
	 */
	public String getNavigationFile()
		{
		return navigationFile;
		}

	/**
	 * @param navigationFile the navigationFile to set
	 */
	public void setNavigationFile(String navigationFile)
		{
		this.navigationFile = navigationFile;
		}

	/**
	 * @return the backgroundMusicFile
	 */
	public String getBackgroundMusicFile()
		{
		return backgroundMusicFile;
		}

	/**
	 * @param backgroundMusicFile the backgroundMusicFile to set
	 */
	public void setBackgroundMusicFile(String backgroundMusicFile)
		{
		this.backgroundMusicFile = backgroundMusicFile;
		}

	/**
	 * @return the precalculatedFogFile
	 */
	public String getPrecalculatedFogFile()
		{
		return precalculatedFogFile;
		}

	/**
	 * @param precalculatedFogFile the precalculatedFogFile to set
	 */
	public void setPrecalculatedFogFile(String precalculatedFogFile)
		{
		this.precalculatedFogFile = precalculatedFogFile;
		}

	/**
	 * @return the luminanceMapFile
	 */
	public String getLuminanceMapFile()
		{
		return luminanceMapFile;
		}

	/**
	 * @param luminanceMapFile the luminanceMapFile to set
	 */
	public void setLuminanceMapFile(String luminanceMapFile)
		{
		this.luminanceMapFile = luminanceMapFile;
		}

	/**
	 * @return the sunlightDirectionVector
	 */
	public AbstractVector getSunlightDirectionVector()
		{
		return sunlightDirectionVector;
		}

	/**
	 * @param sunlightDirectionVector the sunlightDirectionVector to set
	 */
	public void setSunlightDirectionVector(AbstractVector sunlightDirectionVector)
		{
		this.sunlightDirectionVector = sunlightDirectionVector;
		}

	/**
	 * @return the ambientLight
	 */
	public int getAmbientLight()
		{
		return ambientLight;
		}

	/**
	 * @param ambientLight the ambientLight to set
	 */
	public void setAmbientLight(int ambientLight)
		{
		this.ambientLight = ambientLight;
		}

	/**
	 * @return the unknownVector
	 */
	public AbstractVector getUnknownVector()
		{
		return unknownVector;
		}

	/**
	 * @return the unknownInt0
	 */
	public int getUnknownInt0()
		{
		return unknownInt0;
		}

	/**
	 * @param unknownInt0 the unknownInt0 to set
	 */
	public void setUnknownInt0(int unknownInt0)
		{
		this.unknownInt0 = unknownInt0;
		}

	/**
	 * @return the unknownInt1
	 */
	public int getUnknownInt1()
		{
		return unknownInt1;
		}

	/**
	 * @param unknownInt1 the unknownInt1 to set
	 */
	public void setUnknownInt1(int unknownInt1)
		{
		this.unknownInt1 = unknownInt1;
		}

	
	/**
	 * @param unknownVector the unknownVector to set
	 */
	public void setUnknownVector(AbstractVector unknownVector)
		{
		this.unknownVector = unknownVector;
		}

	/**
	 * @return the introVideoFile
	 */
	public String getIntroVideoFile()
		{
		return introVideoFile;
		}

	/**
	 * @param introVideoFile the introVideoFile to set
	 */
	public void setIntroVideoFile(String introVideoFile)
		{
		this.introVideoFile = introVideoFile;
		}

	/**
	 * @return the transitionVideoFile
	 */
	public String getTransitionVideoFile()
		{
		return transitionVideoFile;
		}

	/**
	 * @param transitionVideoFile the transitionVideoFile to set
	 */
	public void setTransitionVideoFile(String transitionVideoFile)
		{
		this.transitionVideoFile = transitionVideoFile;
		}

	/**
	 * @return the missionStartTextFile
	 */
	public String getMissionStartTextFile()
		{
		return missionStartTextFile;
		}

	/**
	 * @param missionStartTextFile the missionStartTextFile to set
	 */
	public void setMissionStartTextFile(String missionStartTextFile)
		{
		this.missionStartTextFile = missionStartTextFile;
		}

	/**
	 * @return the missionEndTextFile
	 */
	public String getMissionEndTextFile()
		{
		return missionEndTextFile;
		}

	/**
	 * @param missionEndTextFile the missionEndTextFile to set
	 */
	public void setMissionEndTextFile(String missionEndTextFile)
		{
		this.missionEndTextFile = missionEndTextFile;
		}

	}//end LVLFile
