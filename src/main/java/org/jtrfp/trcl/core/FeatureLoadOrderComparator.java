/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.util.Comparator;

public class FeatureLoadOrderComparator implements Comparator<Object> {

    @Override
    public int compare(Object left, Object right) {
	if( left.equals(right) )
	    return 0;
	int leftOrder = LoadOrderAware.DEFAULT, rightOrder = LoadOrderAware.DEFAULT;
	if(left instanceof LoadOrderAware)
	    leftOrder  = ((LoadOrderAware)left).getFeatureLoadPriority();
	if(right instanceof LoadOrderAware)
	    rightOrder = ((LoadOrderAware)right).getFeatureLoadPriority();
	
	final int result = leftOrder - rightOrder;
	if(result == 0)//Sets will mistake this as equivalence!
	    return 1;//Arbitrary fallback value
	else return result;
    }//end compare(...)
 
}//end FeatureLoadOrderComparator
