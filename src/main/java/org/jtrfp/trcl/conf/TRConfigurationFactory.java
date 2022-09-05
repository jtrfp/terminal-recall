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
package org.jtrfp.trcl.conf;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashSet;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.flow.GameVersion;
import org.springframework.stereotype.Component;

/**
 * This is a temporary stand-in for TRConfiguration to provide backwards compatibility with pre-Feature configs.
 * @author Chuck Ritola
 *
 */

//TODO: Migrate into TR class and/or other Features.

@Component
public class TRConfigurationFactory implements FeatureFactory<TR>{
    public static final String
	//ACTIVE_AUDIO_DRIVER = "activeAudioDriver",
	//ACTIVE_AUDIO_DEVICE = "activeAudioDevice",
	//ACTIVE_AUDIO_OUTPUT = "activeAudioOutput",
	//ACTIVE_AUDIO_FORMAT = "activeAudioFormat",
	//AUDIO_BUFFER_SIZE   = "audioBufferSize",
	CROSSHAIRS_ENABLED  = "crosshairsEnabled";
    
    public static class TRConfiguration implements Feature<TR>{
	
	private GameVersion gameVersion=GameVersion.F3;
	private Boolean usingTextureBufferUnmap,
	debugMode,
	waitForProfiler;
	private int targetFPS =60;
	private String skipToLevel;
	private String voxFile;
	private boolean /*audioLinearFiltering=false, audioBufferLag=true, */crosshairsEnabled = true;
	private HashSet<String> missionList = new HashSet<String>();
	/*private String activeAudioDriver = "org.jtrfp.trcl.snd.JavaSoundSystemAudioOutput",
		activeAudioDevice,
		activeAudioOutput,
		activeAudioFormat;*/
	//private HashSet<String> podList     = new HashSet<String>();
	//private DefaultListModel podList=new DefaultListModel();
	//private double modStereoWidth=.3;
	public static final String AUTO_DETECT = "Auto-detect";
	private String fileDialogStartDir;
	//private Map<String,Object> componentConfigs;
	private ConfigRootFeature.FeatureTreeElement featureTreeElements;

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public TRConfiguration(){//DEFAULTS
	    missionList.add(AUTO_DETECT);
	    missionList.add("Fury3");
	    missionList.add("TV");
	    missionList.add("FurySE");
	}

	//@Transient
	public GameVersion _getGameVersion() {
	    return gameVersion;
	}

	//@Transient
	public void _setGameVersion(GameVersion gameVersion){
	    this.gameVersion=gameVersion;
	}

	public boolean isUsingTextureBufferUnmap() {
	    if(usingTextureBufferUnmap!=null)return usingTextureBufferUnmap;
	    boolean result=true;
	    if(System.getProperties().containsKey("org.jtrfp.trcl.Renderer.unmapTextureBuffer")){
		if(System.getProperty("org.jtrfp.trcl.Renderer.unmapTextureBuffer").toUpperCase().contains("FALSE"))
		    result=false;
	    }//end if(contains key)
	    usingTextureBufferUnmap=result;
	    return result;
	}//end isUsingTextureBufferUnmap()

	public boolean isDebugMode() {
	    if(debugMode!=null)return debugMode;
	    boolean result=false;
	    if(System.getProperties().containsKey("org.jtrfp.trcl.debugMode")){
		if(System.getProperty("org.jtrfp.trcl.debugMode").toUpperCase().contains("TRUE"))
		    result=true;
	    }//end if(contains key)
	    debugMode=result;
	    return result;
	}//end isDebugMode()

	public int getTargetFPS() {
	    return targetFPS;
	}//end getTargetFPS()

	public String skipToLevel() {
	    if(skipToLevel==null){
		skipToLevel = System.getProperty("org.jtrfp.trcl.flow.Game.skipToLevel");
	    }//end if(skipToLevel==null)
	    return skipToLevel;
	}//end skipToLevel

	/**
	 * @return the voxFile
	 */
	public String getVoxFile() {
	    String result = voxFile;
	    if(result==null)
		result = voxFile = AUTO_DETECT;
	    return result;
	}

	/**
	 * @param voxFile the voxFile to set
	 */
	public void setVoxFile(String voxFile) {
	    this.voxFile = voxFile;
	}

	public boolean isWaitForProfiler() {
	    if(waitForProfiler!=null)return waitForProfiler;
	    boolean result=false;
	    if(System.getProperties().containsKey("org.jtrfp.trcl.dbg.waitForProfiler")){
		if(System.getProperty("org.jtrfp.trcl.dbg.waitForProfiler").toUpperCase().contains("TRUE"))
		    result=true;
	    }//end if(contains key)
	    waitForProfiler=result;
	    return result;
	}

	/**
	 * @return the missionList
	 */
	public HashSet<String> getMissionList() {
	    return missionList;
	}

	/**
	 * @param missionList the missionList to set
	 */
	public void setMissionList(HashSet<String> missionList) {
	    this.missionList = missionList;
	}
	
	public String getFileDialogStartDir() {
	    if(fileDialogStartDir!=null)
		return fileDialogStartDir;
	    return System.getProperty("user.home");
	}

	/**
	 * @param fileDialogStartDir the fileDialogStartDir to set
	 */
	public void setFileDialogStartDir(String fileDialogStartDir) {
	    this.fileDialogStartDir = fileDialogStartDir;
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
	 */
	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}

	public boolean isCrosshairsEnabled() {
	    return crosshairsEnabled;
	}

	/**
	 * @param crosshairsEnabled the crosshairsEnabled to set
	 */
	public void setCrosshairsEnabled(boolean crosshairsEnabled) {
	    final boolean oldValue = this.crosshairsEnabled;
	    this.crosshairsEnabled = crosshairsEnabled;
	    pcs.firePropertyChange(CROSSHAIRS_ENABLED,oldValue,crosshairsEnabled);
	}
	/*
	public Map<String, Object> getComponentConfigs() {
	    if(componentConfigs==null)
		componentConfigs = new HashMap<String,Object>();
	    return componentConfigs;
	}

	public void setComponentConfigs(Map<String, Object> componentConfigs) {
	    this.componentConfigs = componentConfigs;
	}*/

	public ConfigRootFeature.FeatureTreeElement getFeatureTreeElements() {
	    return featureTreeElements;
	}

	public void setFeatureTreeElements(
		ConfigRootFeature.FeatureTreeElement featureTreeElements) {
	    this.featureTreeElements = featureTreeElements;
	}

	@Override
	public void apply(TR target) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub

	}
    }//end TRConfiguration

    @Override
    public Feature<TR> newInstance(TR target) {
	return new TRConfiguration();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<TRConfiguration> getFeatureClass() {
	return TRConfiguration.class;
    }
}//end TRConfiguration
