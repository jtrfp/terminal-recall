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

public class EnemyIntro {
    private final WorldObject worldObject;
    private final String descriptionString;
    
    public EnemyIntro(WorldObject wo, String descriptionString){
	this.worldObject=wo;
	this.descriptionString=descriptionString;
    }
    
    /**
     * @return the worldObject
     */
    public WorldObject getWorldObject() {
        return worldObject;
    }
    /**
     * @return the descriptionString
     */
    public String getDescriptionString() {
        return descriptionString;
    }
}//end EnemyIntro
