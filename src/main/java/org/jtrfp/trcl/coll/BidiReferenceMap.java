/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.coll;

import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.collections4.map.AbstractReferenceMap;
import org.apache.commons.collections4.map.ReferenceMap;


/**
 * Bi-Directional Map backed by a pair of ReferenceMaps.
 * @author Chuck Ritola
 *
 * @param <K>
 * @param <V>
 */
public class BidiReferenceMap<K, V> extends DualHashBidiMap<K,V> {
    private static final long serialVersionUID = 3865398484501725063L;

    public BidiReferenceMap(AbstractReferenceMap.ReferenceStrength keyType, AbstractReferenceMap.ReferenceStrength valueType, int capacity, float loadFactor, boolean purgeValues){
	super(new ReferenceMap<K,V>(keyType,valueType,capacity,loadFactor,purgeValues), 
		new ReferenceMap<V,K>(keyType,valueType,capacity,loadFactor,purgeValues),null);
    }//end constructor()
    
    public BidiReferenceMap(AbstractReferenceMap.ReferenceStrength keyValueType){
	this(keyValueType,keyValueType,16,0.75f,true);
    }

}//end BidiReferenceMap
