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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.DEFFile;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.file.DEFFile.EnemyPlacement;
import org.jtrfp.trcl.gpu.Model;
import org.jtrfp.trcl.gui.ReporterFactory.Reporter;
import org.jtrfp.trcl.miss.LoadingProgressReporter;
import org.jtrfp.trcl.obj.DEFObject;
import org.jtrfp.trcl.obj.EnemyIntro;
import org.jtrfp.trcl.obj.ObjectDirection;
import org.jtrfp.trcl.obj.ObjectPlacer;
import org.jtrfp.trcl.obj.WorldObject;

public class DEFObjectPlacer implements ObjectPlacer{
	private DEFFile def;
	private List<DEFObject> defList;
	private Vector3D headingOverride=null;
	private final LoadingProgressReporter rootReporter;
	private final ArrayList<EnemyIntro> enemyIntros = new ArrayList<EnemyIntro>();
	private final HashMap<EnemyDefinition,WorldObject> enemyPlacementMap = new HashMap<EnemyDefinition,WorldObject>();
	private final TR tr;
	
	public DEFObjectPlacer(DEFFile def, TR tr, LoadingProgressReporter reporter)
		{this.def=def;this.tr=tr;this.rootReporter=reporter;}
	public DEFObjectPlacer(DEFFile defFile, TR tr,
		List<DEFObject> defList, LoadingProgressReporter defObjectReporter) {
	    this(defFile,tr,defObjectReporter);
	    this.defList=defList;
	}//end constructor
	@Override
	public void placeObjects(RenderableSpacePartitioningGrid target, Vector3D positionOffset){
		final List<EnemyDefinition> defs = def.getEnemyDefinitions();
		final List<EnemyPlacement> places = def.getEnemyPlacements();
		final Model [] models = new Model[defs.size()];
		//final TR tr = world.getTr();
		final LoadingProgressReporter[] defReporters = rootReporter
			.generateSubReporters(defs.size());
		final LoadingProgressReporter[] placementReporters = rootReporter
			.generateSubReporters(places.size());
		for(int i=0; i<defs.size(); i++){
		    	defReporters[i].complete();
			final int index = i;//???
					final EnemyDefinition def = defs.get(index);
					try{models[index]=tr.getResourceManager().getBINModel(def.getComplexModelFile(),tr.getGlobalPaletteVL(),null,tr.gpu.get().getGl());}
					catch(Exception e){e.printStackTrace();}
					if(models[index]==null)System.out.println("Failed to get a model from BIN "+def.getComplexModelFile()+" at index "+index);
			final Reporter reporter = Features.get(tr, Reporter.class);
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".complexModelFile", defs.get(i).getComplexModelFile());
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".logic", defs.get(i).getLogic().toString());
			reporter.report("org.jtrfp.trcl.DEFObjectPlacer.def."+defs.get(i).getDescription().replace('.', ' ')+".simpleModelFile", defs.get(i).getSimpleModel());
			}//end for(i:defs)
		int placementReporterIndex=0;
		for(EnemyPlacement pl:places){
		    placementReporters[placementReporterIndex++].complete();
			Model model =models[pl.getDefIndex()];
			if(model!=null){
			    final EnemyDefinition def = defs.get(pl.getDefIndex());
			    try{
				final DEFObject obj =new DEFObject(tr,def,pl);
				if(defList!=null)defList.add(obj);
				if(def.isShowOnBriefing()&&!enemyPlacementMap.containsKey(def)){
					enemyPlacementMap.put(def, obj);
				    }//end 
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
				addWithSubObjects(obj,target);
				//target.add(obj);
			    }catch(Exception e){e.printStackTrace();}
				}//end if(model!=null)
			else{System.out.println("Skipping triangle list at index "+pl.getDefIndex());}
			}//end for(places)
		for(EnemyDefinition ed: enemyPlacementMap.keySet()){
		    enemyIntros.add(new EnemyIntro(enemyPlacementMap.get(ed),ed.getDescription()));
		}
		}//end placeObjects
	
	/**
	 * Yo dawg, we heard you like subObject objects, so we put subObject objects in your subObject objects
	 * so you can have subObjects from your subObjects after they are subObjected.
	 * @param object
	 * @since Jan 28, 2016
	 */
	private void addWithSubObjects(WorldObject object, RenderableSpacePartitioningGrid target){
	    target.add(object);
	    if(object instanceof DEFObject)
	     for(WorldObject subObject:((DEFObject)object).getSubObjects())
	      addWithSubObjects(subObject, target);
	}//end addWithRuins(...)
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
	}//end DEFObjectPlacer
