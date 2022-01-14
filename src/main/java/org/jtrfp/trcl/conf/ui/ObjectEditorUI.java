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

public interface ObjectEditorUI<T> extends UIProvider {
    public boolean isNeedingRestart();
    public void proposeApplySettings();
    public void proposeRevertSettings(long revertTimeMillis);
    public void configure(Consumer<T> propertySetter, Supplier<T> propertyGetter, Set<Annotation> annotations, String humanReadablePropertyName);
}//end ObjectEditor<T>
