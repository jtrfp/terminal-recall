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

import org.jtrfp.trcl.flow.GameVersion;

public class TRConfiguration{
    	private GameVersion gameVersion=GameVersion.F3;
    	private Boolean usingTextureBufferUnmap,
    			usingNewTexturing,
    			debugMode;
    	private int targetFPS =60;
    	private String skipToLevel;
    	private String voxFile;
	public TRConfiguration(){}

	public GameVersion getGameVersion() {
	    return GameVersion.F3;
	}

	public boolean isUsingNewTexturing() {
	    if(usingNewTexturing!=null)return usingNewTexturing;
	    boolean result=true;//Default
	    if(System.getProperties().containsKey("org.jtrfp.trcl.core.useNewTexturing")){
		if(System.getProperty("org.jtrfp.trcl.core.useNewTexturing").toUpperCase().contains("TRUE"))
		    result=true;
		else result=false;
	    }//end if(contains key)
	    usingNewTexturing=result;
	    return result;
	}//end isUsingTextureBufferUnmap()
	
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
		 skipToLevel = System.getProperty("org.jtrfp.flow.Game.skipToLevel");
	    }//end if(skipToLevel==null)
	    return skipToLevel;
	}//end skipToLevel

	/**
	 * @return the voxFile
	 */
	public String getVoxFile() {
	    if(voxFile==null){
		voxFile=System.getProperty("org.jtrfp.trcl.flow.voxFile");
	    }//end if(null)
	    if(voxFile==null)voxFile="Fury3";
	    return voxFile;
	}

	/**
	 * @param voxFile the voxFile to set
	 */
	public void setVoxFile(String voxFile) {
	    this.voxFile = voxFile;
	}
}//end TRConfiguration
