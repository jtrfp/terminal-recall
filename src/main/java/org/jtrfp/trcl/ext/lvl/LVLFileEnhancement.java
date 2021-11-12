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

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.jtrfp.trcl.gpu.Renderer;
import org.jtrfp.trcl.prop.HorizGradientCubeGen;
import org.jtrfp.trcl.prop.SkyCubeGen;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LVLFileEnhancement implements Enhancement {
    private Color /*fogColor = Color.white,*/ sunColor = new Color(255,255,200), ambientColor = new Color(10,10,20), 
	    skycubeTop = Color.blue, skycubeBottom = Color.white;
    private Vector3D sunVector = new Vector3D(1,1,1).normalize();
    private boolean enabled = false;
    private String description = "New LVL Enhancement", hook = "NULL.LVL";
    private float fogScalar = 1;
    
    private SkyCubeGen skyCubeGen;
    
    public void applyToRenderer(Renderer r) {
	r.setSunColor(sunColor);
	r.setSunVector(sunVector);
	r.setAmbientLight(ambientColor);
	r.setFogScalar(fogScalar);
	r.getSkyCube().setSkyCubeGen(getSkyCubeGen());
    }//end applyToRenderer(...)
    
    private SkyCubeGen getSkyCubeGen() {
	if(skyCubeGen == null)
	    skyCubeGen = new HorizGradientCubeGen
			(skycubeBottom, skycubeTop);
	return skyCubeGen;
    }
}//end LVLFileEhancement
