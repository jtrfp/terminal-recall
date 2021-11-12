/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2021 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ext.lvl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gpu.Renderer;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class LVLFileEnhancementsFactory implements FeatureFactory<TR> {

    @Override
    public Feature<TR> newInstance(TR target)
	    throws FeatureNotApplicableException {
	return new LVLFileEnhancements();
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return LVLFileEnhancements.class;
    }
    
    @Getter @Setter
    public static class LVLFileEnhancements implements Feature<TR> {
	private ArrayList<LVLFileEnhancement> lvlFileEnhancements = null;

	@Override
	public void apply(TR target) {
	    //TODO
	}//end apply(...)

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub
	    
	}
	
	public ArrayList<LVLFileEnhancement> getLvlFileEnhancements() {
	    if( this.lvlFileEnhancements == null )
		this.lvlFileEnhancements = generateDefaultEnhancements();
	    return lvlFileEnhancements;
	}
	
	private ArrayList<LVLFileEnhancement> generateDefaultEnhancements() {
	    final ArrayList<LVLFileEnhancement> result = new ArrayList<>();
	    final Color atmosOrange = new Color(90,53,0);
	    final Color atmosAmb = new Color(10,5,1);
	    final Color atmosLights = new Color(200,80,130);
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(5);
		enh.setHook("ATMOS-T1.LVL");
		enh.setAmbientColor(atmosAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(atmosLights);
		enh.setDescription("ATMOS Dusty Tunnel 1");
		enh.setEnabled(true);
		enh.setSkycubeBottom(atmosOrange);
		enh.setSkycubeTop(atmosOrange);
		
		result.add(enh);
	    }
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(10);
		enh.setHook("ATMOS-T2.LVL");
		enh.setAmbientColor(atmosAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(atmosLights);
		enh.setDescription("ATMOS Dusty Tunnel 2");
		enh.setEnabled(true);
		enh.setSkycubeBottom(atmosOrange);
		enh.setSkycubeTop(atmosOrange);
		
		result.add(enh);
	    }
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(10);
		enh.setHook("ATMOS-T3.LVL");
		enh.setAmbientColor(atmosAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(atmosLights);
		enh.setDescription("ATMOS Dusty Tunnel 3");
		enh.setEnabled(true);
		enh.setSkycubeBottom(atmosOrange);
		enh.setSkycubeTop(atmosOrange);
		
		result.add(enh);
	    }
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(15);
		enh.setHook("ATMOS-T4.LVL");
		enh.setAmbientColor(atmosAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(atmosLights);
		enh.setDescription("ATMOS Dusty Tunnel 4");
		enh.setEnabled(true);
		enh.setSkycubeBottom(atmosOrange);
		enh.setSkycubeTop(atmosOrange);
		
		result.add(enh);
	    }
	    //// BORG
	    final Color borgAmb = new Color(25,25,25);
	    for(int i = 1 ; i < 7 ; i++) {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(1);
		enh.setHook("BORG-T"+i+".LVL");
		enh.setAmbientColor(borgAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(Color.black);
		enh.setDescription("BORG Neon Tunnel "+i);
		enh.setEnabled(true);
		enh.setSkycubeBottom(Color.black);
		enh.setSkycubeTop(Color.black);
		
		result.add(enh);
	    }
	    //// MCORE
	    final Color mcoreAmb = new Color(2,2,2);
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(1);
		enh.setHook("MCORE1.LVL");
		enh.setAmbientColor(mcoreAmb);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(new Color(0,20,0));
		enh.setDescription("MCORE boss entrance tunnel");
		enh.setEnabled(true);
		enh.setSkycubeBottom(Color.black);
		enh.setSkycubeTop(Color.black);
		
		result.add(enh);
	    }
	    {
		final LVLFileEnhancement enh = new LVLFileEnhancement();
		enh.setFogScalar(1);
		enh.setHook("MCORE2.LVL");
		enh.setAmbientColor(Color.black);
		enh.setSunVector(Vector3D.MINUS_J);
		enh.setSunColor(Color.black);
		enh.setDescription("MCORE boss escape tunnel");
		enh.setEnabled(true);
		enh.setSkycubeBottom(Color.black);
		enh.setSkycubeTop(Color.black);
		
		result.add(enh);
	    }
	    return result;
	}

	public LVLFileEnhancement findByHook(String levelName) {
	    final String levelNameUC = levelName.toUpperCase();
	    Optional<LVLFileEnhancement> result = getLvlFileEnhancements().stream().filter(
		    x->x.getHook().toUpperCase().contentEquals(levelNameUC) && x.isEnabled()
		    ).findAny();
	    if( result.isPresent())
		return result.get();
	    else return null;
	}

	public boolean applyToRenderer(String levelName, Renderer renderer) {
	    System.out.println("Looking for enhancement to `"+levelName+"`");
	    LVLFileEnhancement enh = findByHook(levelName);
	    if(enh != null) {
		System.out.println("... found.");
		enh.applyToRenderer(renderer);
		return true;
	    }//end if(present)
	    System.out.println("... not found.");
	    return false;
	}//end generateDefaultEnhancements()
	
    }//end LVLFileEnhancementRegistry
 
}//end LVLFileEnhancementRegistryFactory
