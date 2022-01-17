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

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import lombok.Getter;

public abstract class AbstractObjectEditorUI<T> implements ObjectEditorUI<T> {
    private PropertyEditBinding<T> binding;
    @Getter
    private Set<Annotation> annotations;
    
    @Override
    public boolean isNeedingRestart() {
	return false;
    }

    @Override
    public void proposeApplySettings() {
	binding.proposeApply();
    }

    @Override
    public void proposeRevertSettings(long revertTimeMillis) {
	binding.proposeRevert(revertTimeMillis);
    }

    @Override
    public void configure(Consumer<T> propertySetter, Supplier<T> propertyGetter, Set<Annotation> annotations, String humanReadablePropertyName) {
	binding = new PropertyEditBinding<T>(
		propertyGetter, propertySetter, 
		getPropertyUISupplier(), 
		getPropertyUIConsumer());
	this.annotations = annotations;
	setName(humanReadablePropertyName);
    }//end configure(...)
    
    protected abstract void setName(String humanReadableName);

    protected abstract Supplier<T> getPropertyUISupplier();

    protected abstract Consumer<T> getPropertyUIConsumer();

}//end BasicPropertyEditorUI
