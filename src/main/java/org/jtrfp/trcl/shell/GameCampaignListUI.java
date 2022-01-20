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

package org.jtrfp.trcl.shell;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import org.jtrfp.trcl.conf.ui.BeanEditorDialog;
import org.jtrfp.trcl.conf.ui.MutableListPanel;
import org.jtrfp.trcl.conf.ui.MutableListUI;

public class GameCampaignListUI extends MutableListUI<GameCampaignData> {
    
    public GameCampaignListUI() {
	super();
	final MutableListPanel<GameCampaignData> ui = super.getUi();
	ui.setBorder(new TitledBorder("Game Campaign Editor"));
	ui.setElementFactory(elementFactory);
	ui.setEditor(elementEditor);
    }//end constructor
    
    private final UnaryOperator<GameCampaignData> elementEditor = new UnaryOperator<>() {

	@Override
	public GameCampaignData apply(GameCampaignData t) {
	    @SuppressWarnings("unchecked")
	    final BeanEditorDialog dlg = new BeanEditorDialog((JFrame)SwingUtilities.getRoot(getUi()), t, (Set<Annotation>)Collections.EMPTY_SET);
	    dlg.setVisible(true);
	    return t;
	}
	
    };//end elementEditor
    
    private final Supplier<Optional<GameCampaignData>> elementFactory = new Supplier<>() {
	
	@Override
	public Optional<GameCampaignData> get() {
	    return Optional.of(new GameCampaignData());
	}//end get()
    };

    @Override
    public boolean isNeedingRestart() {
	return true;
    }

}//end GameResourceListUI
