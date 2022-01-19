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

import java.util.ArrayList;
import java.util.Collection;

import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.conf.ui.EnumComboBoxUI;
import org.jtrfp.trcl.conf.ui.TextFieldUI;
import org.jtrfp.trcl.flow.GameVersion;

import lombok.Getter;

@Getter
public class GameCampaignData {
    private String name = "[no name]";
    private Collection<String> podURIs = new ArrayList<>();
    private String voxURI;
    private GameVersion gameVersion = GameVersion.TV;
    
    @ConfigByUI(editorClass = TextFieldUI.class)
    public void setName(String name) {
	this.name = name;
    }
    @ConfigByUI(editorClass = PODListUI.class)
    public void setPodURIs(Collection<String> podURIs) {
	this.podURIs = podURIs;
    }
    @ConfigByUI(editorClass = TextFieldUI.class)
    public void setVoxURI(String voxURI) {
	this.voxURI = voxURI;
    }
    @ConfigByUI(editorClass = EnumComboBoxUI.class)
    public void setGameVersion(GameVersion gameVersion) {
	this.gameVersion = gameVersion;
    }
    @Override
    public String toString() {
	return name;
    }
}//end GameCampaignData
