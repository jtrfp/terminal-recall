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

package org.jtrfp.trcl.core;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@EqualsAndHashCode
public class CubeCoordinate {
    public static final CubeCoordinate RELEVANT_EVERYWHERE = new RelevantEverywhere();
    public static final CubeCoordinate CENTER_CUBE = new CenterCube();
    @Getter @Setter
    private int x,y,z;

    private static class RelevantEverywhere extends CubeCoordinate {
	private static int HASH = "RelevantEverywhere".hashCode();
	public RelevantEverywhere() {
	    super(0,0,0);
	}
	
	@Override
	public int hashCode() {
	    return HASH;
	}
	
	@Override
	public boolean equals(Object other) {
	    return other instanceof RelevantEverywhere;
	}
    }//end RelevantEverywhere
    
    private static class CenterCube extends CubeCoordinate {
	private static int HASH = "CenterCube".hashCode();
	public CenterCube() {
	    super(0,0,0);
	}
	
	@Override
	public int hashCode() {
	    return HASH;
	}
	
	@Override
	public boolean equals(Object other) {
	    return other instanceof CenterCube;
	}
    }//end CenterCube
}//end CubeCoordinate
