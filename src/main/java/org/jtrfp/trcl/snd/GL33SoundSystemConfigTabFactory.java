/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.snd;

import java.util.concurrent.Executor;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.core.ThreadManager;
import org.jtrfp.trcl.gui.ConfigWindowFactory.ConfigWindow;
import org.jtrfp.trcl.gui.ConfigurationTab;
import org.springframework.stereotype.Component;

@Component
public class GL33SoundSystemConfigTabFactory
	implements FeatureFactory<SoundSystem> {

    @Override//XXX This must not be made a Feature for ConfigWindow because it will cause a dependency loop!
    public Feature<SoundSystem> newInstance(SoundSystem target)
	    throws FeatureNotApplicableException {
	final GL33SoundSystemConfigTab result      = new GL33SoundSystemConfigTab();
	final TR                       tr          = Features.get(Features.getSingleton(), TR.class);
	final SoundSystem              soundSystem = Features.get(tr, SoundSystem.class);
	final Executor                 executor    = Features.get(tr, ThreadManager.class).threadPool;
	if(soundSystem == null)
	    throw new NullPointerException("SoundSystem intolerably null.");
	result.setSoundSystem(soundSystem);
	result.setExecutor(executor);
	return result;
    }

    @Override
    public Class<SoundSystem> getTargetClass() {
	return SoundSystem.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return GL33SoundSystemConfigTab.class;
    }
    
    public static class GL33SoundSystemConfigTab implements ConfigurationTab, Feature<SoundSystem> {
	private GL33SoundSystemConfigPanel configPanel;
	private SoundSystem soundSystem;
	private ConfigWindow configWindow;
	private Executor executor;

	@Override
	public void apply(SoundSystem target) {
	    setSoundSystem(target);
	    final TR tr = target.getTr();
	    final ConfigWindow configWindow = Features.get(tr, ConfigWindow.class);
	    setConfigWindow(configWindow);
	    configWindow.registerConfigTab(this);
	}

	@Override
	public void destruct(SoundSystem target) {
	    // TODO Auto-generated method stub
	    
	}

	@Override
	public String getTabName() {
	    return "Sound";
	}

	@Override
	public JComponent getContent() {
	    if(configPanel == null){
		configPanel = new GL33SoundSystemConfigPanel();
		configPanel.setExecutor(getExecutor());
		configPanel.setSoundSystem (getSoundSystem ());
		configPanel.setConfigWindow(configWindow);
		}
	    return configPanel;
	}

	@Override
	public ImageIcon getTabIcon() {
	    return new ImageIcon(SoundSystem.class.getResource("/org/freedesktop/tango/22x22/devices/audio-card.png"));
	}

	public ConfigWindow getConfigWindow() {
	    return configWindow;
	}

	public void setConfigWindow(ConfigWindow configWindow) {
	    this.configWindow = configWindow;
	}

	public SoundSystem getSoundSystem() {
	    return soundSystem;
	}

	public void setSoundSystem(SoundSystem soundSystem) {
	    this.soundSystem = soundSystem;
	}

	protected Executor getExecutor() {
	    return executor;
	}

	protected void setExecutor(Executor executor) {
	    this.executor = executor;
	}
	
    }//end SoundSystemConfigTab

}//end SoundSystemConfigTabFactory
