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
package org.jtrfp.trcl.obj;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.DEFObjectPlacer;
import org.jtrfp.trcl.RenderableSpacePartitioningGrid;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.LVLFile;
import org.jtrfp.trcl.file.PUPFile;
import org.jtrfp.trcl.game.TVF3Game;
import org.jtrfp.trcl.game.TVF3Game.Difficulty;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.jtrfp.trcl.tools.Util;

public class ObjectSystem extends RenderableSpacePartitioningGrid {
    private List<DEFObject> defList, pupList;
    private DEFObjectPlacer defPlacer;
    private TR        tr;
    private Vector3D  headingOverride;
    private Vector3D  positionOffset = Vector3D.ZERO;
    private GameShell gameShell;
    private LoadingProgressReporter progressReporter, defObjectReporter, pupObjectReporter;
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    
    public ObjectSystem(){
	super();
    }// end ObjectSystem(...)
    
    public void populateFromLVL(LVLFile lvlFile){
	Util.assertPropertiesNotNull(this, "tr");
	final TR tr = getTr();
	try{
	DEFFile defFile = tr.getResourceManager().getDEFData(
		lvlFile.getEnemyDefinitionAndPlacementFile());
	PUPFile pupFile = tr.getResourceManager().getPUPData(
		lvlFile.getPowerupPlacementFile());
	final DEFObjectPlacer defPlacer = getDefPlacer();
	defPlacer.setDefData(defFile);
	defPlacer.setHeadingOverride(getHeadingOverride());
	defPlacer.setPositionOffset(getPositionOffset());
	defPlacer.setTargetGrid(this);
	defPlacer.setDefList(getDefList());
	defPlacer.placeObjects();
	final List<DEFObject> defList = defPlacer.getDefList();
	//Temporarily force these to be flagged on-grid so they don't get omitted. 
	for(DEFObject def:defList)
	    def.setInGrid(true);
	setDefList(defList);
	assert getDefList() != null;
	PUPObjectPlacer pupPlacer = new PUPObjectPlacer();
	pupPlacer.setPupData(pupFile);
	pupPlacer.setRootReporter(getPupObjectReporter());
	pupPlacer.setTr(tr);
	pupPlacer.setPositionOffset(getPositionOffset());
	pupPlacer.setTargetGrid(this);
	pupPlacer.placeObjects();
	}catch(Exception e){uncaughtException(e);}
    }//populateFromLVL(...)
    
    private void uncaughtException(Exception e){
	final UncaughtExceptionHandler h = getUncaughtExceptionHandler();
	if(h == null)
	    e.printStackTrace();
	else
	    h.uncaughtException(Thread.currentThread(), e);
    }//end uncaughtException
    /**
     * @return the defPlacer
     */
    public DEFObjectPlacer getDefPlacer() {
	if(defPlacer == null){
	    final DEFObjectPlacer newResult = new DEFObjectPlacer();
	    final TVF3Game game = (TVF3Game)getGameShell().getGame();
	    final Difficulty difficulty = game.getDifficulty();
	    newResult.setFiringIntervalScalar(difficulty.getFiringRateScalar());
	    newResult.setShieldScalar    (difficulty.getShieldScalar());
	    newResult.setThrustScalar    (difficulty.getDefSpeedScalar());
	    newResult.setRootReporter(getDefObjectReporter());
	    newResult.setTr(getTr());
	    defPlacer = newResult;
	    }
        return defPlacer;
    }//getDefPlacer()
    
    public void setDefList(List<DEFObject> defObjectList) {
	if(defObjectList == null)
	    throw new NullPointerException("defObjectList intolerably null.");
	final List<DEFObject> oldList = defList;
	if(defList != null)
	    for(DEFObject def:oldList)
		removeWithSubObjects(def,this);
	this.defList = defObjectList;
	//Familiarize these DEFObjects
	for(DEFObject defObject:defObjectList){
	    defObject.setTr(getTr());
	    defObject.setGameShell(getGameShell());
	    if(defObject.isInGrid())
		addWithSubObjects(defObject,this);
	}//end for(defObjects)
    }//setDefList(...)
    
    private void addWithSubObjects(WorldObject object, RenderableSpacePartitioningGrid target){
	    target.add(object);
	    if(object instanceof DEFObject)
	     for(WorldObject subObject:((DEFObject)object).getSubObjects())
	      addWithSubObjects(subObject, target);
	}//end addWithRuins(...)

    private void removeWithSubObjects(WorldObject object, RenderableSpacePartitioningGrid target){
	target.remove(object);
	if(object instanceof DEFObject)
	    for(WorldObject subObject:((DEFObject)object).getSubObjects())
		removeWithSubObjects(subObject, target);
    }//end addWithRuins(...)

    public List<DEFObject> getDefList() {
        return defList;
    }

    public TR getTr() {
        return tr;
    }

    public void setTr(TR tr) {
        this.tr = tr;
    }

    public GameShell getGameShell() {
	if(gameShell == null)
	    gameShell = Features.get(getTr(), GameShell.class);
        return gameShell;
    }

    public void setGameShell(GameShell gameShell) {
        this.gameShell = gameShell;
    }

    public LoadingProgressReporter getProgressReporter() {
        return progressReporter;
    }

    public void setProgressReporter(LoadingProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    public List<DEFObject> getPupList() {
        return pupList;
    }

    public void setPupList(List<DEFObject> pupList) {
        this.pupList = pupList;
    }

    public LoadingProgressReporter getDefObjectReporter() {
	if(defObjectReporter == null)
	    defObjectReporter = getProgressReporter().generateSubReporters(1)[0];
        return defObjectReporter;
    }

    public void setDefObjectReporter(LoadingProgressReporter defObjectReporter) {
        this.defObjectReporter = defObjectReporter;
    }

    public LoadingProgressReporter getPupObjectReporter() {
	if(pupObjectReporter == null)
	    pupObjectReporter = getProgressReporter().generateSubReporters(1)[0];
        return pupObjectReporter;
    }

    public void setPupObjectReporter(LoadingProgressReporter pupObjectReporter) {
        this.pupObjectReporter = pupObjectReporter;
    }

    public Vector3D getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(Vector3D positionOffset) {
        this.positionOffset = positionOffset;
    }

    public Vector3D getHeadingOverride() {
        return headingOverride;
    }

    public void setHeadingOverride(Vector3D headingOverride) {
        this.headingOverride = headingOverride;
    }

    public UncaughtExceptionHandler getUncaughtExceptionHandler() {
        return uncaughtExceptionHandler;
    }

    public void setUncaughtExceptionHandler(
    	UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }
}// end ObjectSystem
