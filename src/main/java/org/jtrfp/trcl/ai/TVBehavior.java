/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.ai;

import org.jtrfp.trcl.TerrainSystem;
import org.jtrfp.trcl.file.DEFFile.EnemyDefinition;
import org.jtrfp.trcl.objects.Damageable;
import org.jtrfp.trcl.objects.MobileObject;

public class TVBehavior <T extends MobileObject & Damageable> extends ObjectBehavior<T>{
	TerrainSystem terrainSystem;
	private final EnemyDefinition def;
	
	public TVBehavior(ObjectBehavior wrapped, EnemyDefinition def, TerrainSystem terrainSystem, int strength)
		{super(new DamagedByCollisionWithGameplayObject(wrapped));
		this.terrainSystem=terrainSystem;
		this.def=def;
		}
	}//end TVBehavior
