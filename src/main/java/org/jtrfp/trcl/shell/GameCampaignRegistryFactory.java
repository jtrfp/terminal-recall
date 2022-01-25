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

import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.flow.GameVersion;
import org.jtrfp.trcl.shell.GameShellFactory.GameShell;
import org.springframework.stereotype.Component;

@Component
public class GameCampaignRegistryFactory implements FeatureFactory<GameShell> {
    
    public static class GameCampaignRegistry implements Feature<GameShell> {
	
	private final CollectionActionDispatcher<GameCampaignData> campaigns = new CollectionActionDispatcher<>(new ArrayList<>());
	
	public GameCampaignRegistry() {}

	private void ensureCampaignDataIsPopulated() { //Fill with default if empty
	    if(campaigns.isEmpty()) {
		final GameCampaignData f3 = new GameCampaignData();
		f3.setGameVersion(GameVersion.F3);
		f3.setName("Microsoft Fury3");
		final GameCampaignData fZone = new GameCampaignData();
		fZone.setGameVersion(GameVersion.FURYSE);
		fZone.setName("F!Zone");
		final GameCampaignData tv = new GameCampaignData();
		tv.setGameVersion(GameVersion.TV);
		tv.setName("Terminal Velocity");
		campaigns.add(f3);
		campaigns.add(fZone);
		campaigns.add(tv);
	    }
	}//end ensureCampaignDataIsPopulated
	
	@ConfigByUI(editorClass=GameCampaignListUI.class)
	public CollectionActionDispatcher<GameCampaignData> getCampaigns() {
	    ensureCampaignDataIsPopulated();
	    return campaigns;
	}

	@Override
	public void apply(GameShell target) {
	}//end apply()

	@Override
	public void destruct(GameShell target) {
	}
	
	public Collection<GameCampaignData> getCampaignEntriesCollection() {
	    return campaigns.getDelegate();
	}
	
	public void setCampaignEntriesCollection(Collection<GameCampaignData> entries) {
	    campaigns.clear();
	    campaigns.addAll(entries);
	}
	
    }//end GameResourceRegistry

    @Override
    public Feature<GameShell> newInstance(GameShell target)
	    throws FeatureNotApplicableException {
	return new GameCampaignRegistry();
    }

    @Override
    public Class<GameShell> getTargetClass() {
	return GameShell.class;
    }

    @Override
    public Class<? extends Feature<GameShell>> getFeatureClass() {
	return GameCampaignRegistry.class;
    }

}//end GameResourceRegistryFactory
