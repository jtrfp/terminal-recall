/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.NonNull;

public class InPlaceCollectionPropertyEditBinding<ELEMENT_TYPE> extends PropertyEditBinding<Collection<ELEMENT_TYPE>> {
    
    public InPlaceCollectionPropertyEditBinding(
	    @NonNull Supplier<Collection<ELEMENT_TYPE>> propertyGetter,
	    @NonNull Consumer<Collection<ELEMENT_TYPE>> propertySetter,
	    @NonNull Supplier<Collection<ELEMENT_TYPE>> uiGetter,
	    @NonNull Consumer<Collection<ELEMENT_TYPE>> uiSetter) {
	super(propertyGetter, propertySetter, uiGetter, uiSetter);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void proposeRevert(long newStateTimestampMillis) {
	if (newStateTimestampMillis > stateTimestampMillis) {
	    uiSetter.accept(new ArrayList<>(Objects.requireNonNullElse(propertyGetter.get(),(Collection<? extends ELEMENT_TYPE>)Collections.EMPTY_LIST)));
	    stateTimestampMillis = newStateTimestampMillis;
	}
    }//end proposeRevert()

    @Override
    public void proposeApply() {
	final Collection<ELEMENT_TYPE> newValue = uiGetter.get();
	final Collection<ELEMENT_TYPE> currentValue = propertyGetter.get();
	
	if(stateTimestampMillis == 0) //Was never set. Ignore
	    return;
	
	if( isChangedByUI(currentValue, newValue) ) {
	    apply(currentValue, newValue);
	    stateTimestampMillis = System.currentTimeMillis();
	}
    }//end proposeApply()
    
    protected void apply(Collection<ELEMENT_TYPE> propertyValue, Collection<ELEMENT_TYPE> uiValue) {
	propertyValue.clear();
	propertyValue.addAll(uiValue);
    }//end apply()
    
    @Override
    public boolean isChangedByUI() {
	final Collection<ELEMENT_TYPE> newValue = uiGetter.get();
	final Collection<ELEMENT_TYPE> currentValue = propertyGetter.get();
	return isChangedByUI(currentValue, newValue);
    }//end isChangedByUI()
    
    protected boolean isChangedByUI(Collection<ELEMENT_TYPE> currentValue, Collection<ELEMENT_TYPE> newValue) {
	if(currentValue == null && newValue != null || (newValue == null && currentValue != null))
	    return true;
	if(Objects.equals(currentValue, newValue))
	    return false;
	if(newValue.size() != currentValue.size())
	    return true;
	else if(!newValue.containsAll(currentValue) || !currentValue.containsAll(newValue))
	    return true;
	return false;
    }//end isChangedByUI(...)
}//end InPlaceCollectionPropertyEditBinding
