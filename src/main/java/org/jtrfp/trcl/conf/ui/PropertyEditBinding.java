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

package org.jtrfp.trcl.conf.ui;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor @Getter
public class PropertyEditBinding<T> {
    protected long stateTimestampMillis;
    @NonNull protected final Supplier<T> propertyGetter;
    @NonNull protected final Consumer<T> propertySetter;
    @NonNull protected final Supplier<T> uiGetter;
    @NonNull protected final Consumer<T> uiSetter;
    
    public void proposeRevert(long newStateTimestampMillis) {
	if (newStateTimestampMillis > stateTimestampMillis) {
	    uiSetter.accept(propertyGetter.get());
	    stateTimestampMillis = newStateTimestampMillis;
	}
    }//end proposeRevert()
    
    public void proposeApply() {
	if( stateTimestampMillis == 0 ) //Was never set. Ignore.
	    return;
	final T newValue = uiGetter.get();
	if( isChangedByUI(newValue) ) {
	    propertySetter.accept(newValue);
	    stateTimestampMillis = System.currentTimeMillis();
	}
    }//end proposeApply()
    
    public boolean isChangedByUI() {
	final T newValue = uiGetter.get();
	return isChangedByUI(newValue);
    }//end isChangedByUI()
    
    private boolean isChangedByUI(T newValue) {
	final T currentValue = propertyGetter.get();
	return !Objects.equals(newValue, currentValue);
    }//end isChangedByUI(...)
    
}//end PropertyEditBinding
