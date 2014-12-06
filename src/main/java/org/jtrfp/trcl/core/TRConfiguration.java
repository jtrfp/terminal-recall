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
package org.jtrfp.trcl.core;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Statement;
import java.beans.Transient;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.swing.DefaultListModel;

import org.jtrfp.trcl.flow.GameVersion;

public class TRConfiguration{
    	private GameVersion gameVersion=GameVersion.F3;
    	private Boolean usingTextureBufferUnmap,
    			debugMode,
    			waitForProfiler;
    	private int targetFPS =60;
    	private String skipToLevel;
    	private String voxFile;
    	private boolean audioLinearFiltering=false;
    	private HashSet<String> missionList = new HashSet<String>();
    	//private HashSet<String> podList     = new HashSet<String>();
    	private DefaultListModel<String> podList=new DefaultListModel<String>();
    	private double modStereoWidth=.3;
    	public static final String AUTO_DETECT = "Auto-detect";
    	private String fileDialogStartDir;
    	
	public TRConfiguration(){//DEFAULTS
	    missionList.add(AUTO_DETECT);
	    missionList.add("Fury3");
	    missionList.add("TV");
	    missionList.add("FurySE");
	}

	@Transient
	public GameVersion getGameVersion() {
	    return gameVersion;
	}
	
	@Transient
	public void setGameVersion(GameVersion gameVersion){
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
	 * @return the audioLinearFiltering
	 */
	public boolean isAudioLinearFiltering() {
	    return audioLinearFiltering;
	}

	/**
	 * @param audioLinearFiltering the audioLinearFiltering to set
	 */
	public void setAudioLinearFiltering(boolean audioLinearFiltering) {
	    this.audioLinearFiltering = audioLinearFiltering;
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

	/**
	 * @return the modStereoWidth
	 */
	public double getModStereoWidth() {
	    return modStereoWidth;
	}

	/**
	 * @param modStereoWidth the modStereoWidth to set
	 */
	public void setModStereoWidth(double modStereoWidth) {
	    this.modStereoWidth = modStereoWidth;
	}
	
	public static File getConfigFilePath(){
	     String homeProperty = System.getProperty("user.home");
	     if(homeProperty==null)homeProperty="";
	     return new File(homeProperty+File.separator+"settings.config.trcl.xml");
	 }
	
	public static TRConfiguration getConfig(){
	     TRConfiguration result=null;
	     File fp = TRConfiguration.getConfigFilePath();
	     if(fp.exists()){
		 try{FileInputStream is = new FileInputStream(fp);
		    XMLDecoder xmlDec = new XMLDecoder(is);
		    result=(TRConfiguration)xmlDec.readObject();
		    xmlDec.close();
		    is.close();
		}catch(Exception e){e.printStackTrace();}
	     }//end if(exists)
	     if(result==null)
		result = new TRConfiguration();
	     return result;
	 }//end getConfig()
	
	public void saveConfig() throws IOException{
	    saveConfig(TRConfiguration.getConfigFilePath());
	}
	
	public void saveConfig(File f) throws IOException{
	    if(!f.getName().toLowerCase().endsWith(".config.trcl.xml"))
		    f = new File(f.getAbsolutePath()+".config.trcl.xml");
		   if(!f.exists())
		     f.createNewFile();
		    FileOutputStream os = new FileOutputStream(f);
		    XMLEncoder xmlEnc = new XMLEncoder(os);
		    xmlEnc.setExceptionListener(new ExceptionListener(){
			@Override
			public void exceptionThrown(Exception e) {
			    e.printStackTrace();
			}});
		    xmlEnc.setPersistenceDelegate(DefaultListModel.class,
			    new DefaultPersistenceDelegate() {
				protected void initialize(Class clazz,
					Object oldInst, Object newInst,
					Encoder out) {
				    super.initialize(clazz, oldInst, newInst,
					    out);
				    DefaultListModel oldLM = (DefaultListModel) oldInst;
				    DefaultListModel newLM = (DefaultListModel) newInst;
				    for (int i = 0; i < oldLM.getSize(); i++){
					final Object value = oldLM.getElementAt(i);
				    	if(value!=null)//When a DLM is initialized it contains a single null element. )X
					 out.writeStatement(new Statement(oldInst,"addElement",
						new Object[] { value }));
				    }//end for(elements)
				}//end DefaultPersistenceDelegate()
			    });
		    xmlEnc.writeObject(this);
		    xmlEnc.close();
	}

	/**
	 * @return the podList
	 */
	public DefaultListModel getPodList() {
	    return podList;
	}

	/**
	 * @param podList the podList to set
	 */
	public void setPodList(DefaultListModel podList) {
	    this.podList = podList;
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
}//end TRConfiguration
