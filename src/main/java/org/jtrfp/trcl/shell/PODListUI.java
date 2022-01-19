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

import java.io.File;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JFileChooser;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.jtrfp.trcl.conf.ui.MutableListPanel;
import org.jtrfp.trcl.conf.ui.MutableListUI;

public class PODListUI extends MutableListUI<String> {
    private final JFileChooser fileChooser = new JFileChooser();
    
    public PODListUI() {
	super();
	final MutableListPanel<String> ui = super.getUi();
	ui.setBorder(new TitledBorder("PODfiles to Use"));
	ui.setElementFactory(elementFactory);
	
	fileChooser.setFileFilter(new FileFilter(){
		@Override
		public boolean accept(File file) {
		    return file.getName().toUpperCase().endsWith(".POD")||file.isDirectory();
		}

		@Override
		public String getDescription() {
		    return "Terminal Reality POD files";
		}});
    }
    
    private final Supplier<Optional<String>> elementFactory = new Supplier<>() {
	
	@Override
	public Optional<String> get() {
	    final int result = fileChooser.showDialog(PODListUI.this.getUi(), "Add");
	    if(result == JFileChooser.APPROVE_OPTION) {
		final File file = fileChooser.getSelectedFile();
		return Optional.of(file.getAbsolutePath());
	    }//end if(APPROVE)
	    
	    return Optional.empty();
	}//end get()
    };

    @Override
    public boolean isNeedingRestart() {
	return false;
    }
}//end PODListUI
