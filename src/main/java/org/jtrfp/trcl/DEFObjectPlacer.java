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
package org.jtrfp.trcl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.beh.HasDescription;
import org.jtrfp.trcl.beh.RequestsMentionOnBriefing;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.gpu.GPU;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.EnemyIntro;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectPlacer;

public class DEFObjectPlacer implements ObjectPlacer{
	private DEFFile defData;
	private List<DEFObject> defList;
	private Vector3D headingOverride=null;
	private LoadingProgressReporter rootReporter;
	private final ArrayList<EnemyIntro> enemyIntros = new ArrayList<EnemyIntro>();
	//private final HashMap<EnemyDefinition,WorldObject> enemyPlacementMap = new HashMap<EnemyDefinition,WorldObject>();
	private Set<EnemyDefinition> definitionsToBeIntroduced;
	private TR tr;
	private Vector3D positionOffset = Vector3D.ZERO;
	private RenderableSpacePartitioningGrid targetGrid;
	private double firingRateScalar=1, shieldScalar=1, thrustScalar=1;
	
	@Override
	public void placeObjects(){
	    final RenderableSpacePartitioningGrid target = getTargetGrid();
	    final Vector3D positionOffset = getPositionOffset();
	    final DEFFile defData = getDefData();
	    final List<EnemyDefinition> defs = defData.getEnemyDefinitions();
	    final List<EnemyPlacement> places = defData.getEnemyPlacements();
	    //Apply scalars
	    final double shieldScalar     = getShieldScalar();
	    final double thrustScalar     = getThrustScalar();
	    final double firingRateScalar = getFiringRateScalar();
	    final Model [] models = new Model[defs.size()];
	    //final TR tr = world.getTr();
	    final LoadingProgressReporter[] defReporters = rootReporter
		    .generateSubReporters(defs.size());
	    final LoadingProgressReporter[] placementReporters = rootReporter
		    .generateSubReporters(places.size());
	    final GPU      gpu      = Features.get(tr, GPUFeature.class); 
	    final Reporter reporter = Features.get(tr, Reporter.class);
	    for(int i=0; i<defs.size(); i++){
		defReporters[i].complete();
		final int index = i;//???
		final EnemyDefinition enemyDef = defs.get(index);
		try{models[index]=tr.getResourceManager().getBINModel(enemyDef.getComplexModelFile(),tr.getGlobalPaletteVL(),null,gpu.getGl());}
		catch(Exception e){e.printStackTrace();}
		if(models[index]==null)System.out.println("Failed to get a model from BIN "+enemyDef.getComplexModelFile()+" at index "+index);
		reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".complexModelFile", defs.get(i).getComplexModelFile());
		reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".logic", defs.get(i).getLogic().toString());
		reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".simpleModelFile", defs.get(i).getSimpleModel());
	    }//end for(i:defs)
	    int placementReporterIndex=0;
	    final Set<EnemyDefinition> definitionsToBeIntroduced = getDefinitionsToBeIntroduced();
	    final List<DEFObject> defList = getDefList();
	    for(EnemyPlacement pl:places){
		placementReporters[placementReporterIndex++].complete();
		pl.setStrength((int)(pl.getStrength() * shieldScalar));
		Model model =models[pl.getDefIndex()];
		if(model!=null){
		    final EnemyDefinition def = defs.get(pl.getDefIndex());
		    def.setFireSpeed(  (int)(def.getFireSpeed()   * firingRateScalar));
		    def.setThrustSpeed((int)(def.getThrustSpeed() * thrustScalar));
		    try{
			final DEFObject obj =new DEFObject();
			obj.setEnemyDefinition(def);
			obj.setEnemyPlacement(pl);
			defList.add(obj);
			obj.addBehavior(new HasDescription().setHumanReadableDescription(def.getDescription()));
			if(def.isShowOnBriefing())
			    if(definitionsToBeIntroduced.add(def)){
				obj.addBehavior(new RequestsMentionOnBriefing());
			    }
			//USING  z,x coords
			final double [] objPos = obj.getPosition();
			objPos[0]= TRFactory.legacy2Modern	(pl.getLocationOnMap().getZ())+positionOffset.getX();
			objPos[1]=(TRFactory.legacy2Modern	(pl.getLocationOnMap().getY())/TRFactory.mapWidth)*16.*tr.getWorld().sizeY+positionOffset.getY();
			objPos[2]= TRFactory.legacy2Modern	(pl.getLocationOnMap().getX())+positionOffset.getZ();
			obj.notifyPositionChange();

			if(pl.getRoll()!=0||pl.getPitch()!=0||pl.getYaw()!=0)//Only set if not 0,0,0
			    try{obj.setDirection(new ObjectDirection(pl.getRoll(),pl.getPitch(),pl.getYaw()+65536));}
			catch(MathArithmeticException e){e.printStackTrace();}
			if(headingOverride!=null){
			    final double [] headingArray = obj.getHeadingArray();
			    headingArray[0]=headingOverride.getX();
			    headingArray[1]=headingOverride.getY();
			    headingArray[2]=headingOverride.getZ();
			}//end if(headingOverride)
			//target.add(obj);
		    }catch(Exception e){e.printStackTrace();}
		}//end if(model!=null)
		else{System.out.println("Skipping triangle list at index "+pl.getDefIndex());}
	    }//end for(places)
	    //for(EnemyDefinition ed: enemyPlacementMap.keySet())
		
	}//end placeObjects
	
	/**
	 * @return the headingOverride
	 */
	public Vector3D getHeadingOverride() {
	    return headingOverride;
	}
	/**
	 * @param headingOverride the headingOverride to set
	 */
	public DEFObjectPlacer setHeadingOverride(Vector3D headingOverride) {
	    this.headingOverride = headingOverride;
	    return this;
	}
	public List<EnemyIntro> getEnemyIntros() {
	    return enemyIntros;
	}
	public DEFFile getDefData() {
	    return defData;
	}
	public void setDefData(DEFFile defData) {
	    this.defData = defData;
	}

	public TR getTr() {
	    return tr;
	}

	public void setTr(TR tr) {
	    this.tr = tr;
	}

	public List<DEFObject> getDefList() {
	    if(defList == null)
		defList = new ArrayList<DEFObject>();
	    return defList;
	}

	public void setDefList(List<DEFObject> defList) {
	    this.defList = defList;
	}

	public LoadingProgressReporter getRootReporter() {
	    return rootReporter;
	}

	public void setRootReporter(LoadingProgressReporter rootReporter) {
	    this.rootReporter = rootReporter;
	}

	public Vector3D getPositionOffset() {
	    return positionOffset;
	}

	public void setPositionOffset(Vector3D positionOffset) {
	    this.positionOffset = positionOffset;
	}

	public RenderableSpacePartitioningGrid getTargetGrid() {
	    return targetGrid;
	}

	public void setTargetGrid(RenderableSpacePartitioningGrid targetGrid) {
	    this.targetGrid = targetGrid;
	}

	public Set<EnemyDefinition> getDefinitionsToBeIntroduced() {
	    if(definitionsToBeIntroduced == null)
		definitionsToBeIntroduced = new HashSet<EnemyDefinition>();
	    return definitionsToBeIntroduced;
	}

	public void setDefinitionsToBeIntroduced(
		Set<EnemyDefinition> definitionsToBeIntroduced) {
	    this.definitionsToBeIntroduced = definitionsToBeIntroduced;
	}

	public double getFiringRateScalar() {
	    return firingRateScalar;
	}

	public void setFiringRateScalar(double firingRateSclar) {
	    this.firingRateScalar = firingRateSclar;
	}

	public double getShieldScalar() {
	    return shieldScalar;
	}

	public void setShieldScalar(double shieldScalar) {
	    this.shieldScalar = shieldScalar;
	}

	public double getThrustScalar() {
	    return thrustScalar;
	}

	public void setThrustScalar(double thrustScalar) {
	    this.thrustScalar = thrustScalar;
	}
	}//end DEFObjectPlacer
