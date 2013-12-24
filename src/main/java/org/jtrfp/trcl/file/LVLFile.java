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
package org.jtrfp.trcl.file;
import java.io.IOException;
import java.io.InputStream;

import org.jtrfp.jfdt.ClassInclusion;
import org.jtrfp.jfdt.FailureBehavior;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.UnrecognizedFormatException;


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
	
	@Override
	public void describeFormat(Parser prs) throws UnrecognizedFormatException
		{
		//REMEMBER: use \r\n because TR files use carriage-return-line-feed and not just the line-feed \n that java uses.
		prs.stringEndingWith("\r\n", prs.property("levelType",LevelType.class), false);
		
		prs.stringEndingWith("\r\n", prs.property("briefingTextFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("heightMapOrTunnelFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("texturePlacementFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("globalPaletteFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("levelTextureListFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("qkeFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("powerupPlacementFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("textureAnimationFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("tunnelDefinitionFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("cloudTextureFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("backgroundGradientPaletteFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("enemyDefinitionAndPlacementFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("navigationFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("backgroundMusicFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("precalculatedFogFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("luminanceMapFile", String.class), false);
		
		prs.subParseProposedClasses(prs.property("sunlightDirectionVector",AbstractVector.class),ClassInclusion.classOf(AbstractVector.class));
		prs.stringEndingWith("\r\n", prs.property("ambientLight",Integer.class), false);
		prs.subParseProposedClasses(prs.property("unknownVector",AbstractVector.class),ClassInclusion.classOf(AbstractVector.class));
		prs.stringEndingWith("\r\n",prs.property("unknownInt0",Integer.class),false);
		prs.stringEndingWith("\r\n",prs.property("unknownInt1",Integer.class),false);
		
		prs.ignoreEOF(true);
		prs.expectString(";New story stuff\r\n", FailureBehavior.IGNORE);
		prs.stringEndingWith("\r\n", prs.property("introVideoFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("levelEndVideoFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("transitionVideoFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("missionStartTextFile", String.class), false);
		prs.stringEndingWith("\r\n", prs.property("missionEndTextFile", String.class), false);
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
