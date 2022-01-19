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
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.border.TitledBorder;

import lombok.Getter;

public abstract class MutableListUI<ELEMENT_TYPE> implements ObjectEditorUI<Collection<ELEMENT_TYPE>> {
    @Getter
    private final MutableListPanel<ELEMENT_TYPE> ui = new MutableListPanel<>();
    private InPlaceCollectionPropertyEditBinding<ELEMENT_TYPE> binding;
    
    public MutableListUI() {
	super();
    }

    @Override
    public JComponent getUIComponent() {
	return ui;
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
    public void configure(Consumer<Collection<ELEMENT_TYPE>> setter,
	    Supplier<Collection<ELEMENT_TYPE>> getter, Set<Annotation> annotations, String humanReadablePropertyName) {
	binding = new InPlaceCollectionPropertyEditBinding<ELEMENT_TYPE>(getter, setter, ()->ui.getElements(), (x)->ui.setElements(x));
	ui.setBorder(new TitledBorder(humanReadablePropertyName));
	ui.invalidate();
    }//end constructor

}//end MutableListUI
