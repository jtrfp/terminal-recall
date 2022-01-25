/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.ext.tr;

import java.util.List;
import java.util.Objects;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jtrfp.trcl.conf.ui.CheckboxUI;
import org.jtrfp.trcl.conf.ui.ComboBoxUI;
import org.jtrfp.trcl.conf.ui.ConfigByUI;
import org.jtrfp.trcl.conf.ui.DoubleNormalizedSliderUI;
import org.jtrfp.trcl.conf.ui.ToolTip;
import org.jtrfp.trcl.conf.ui.TreeSelectionUI;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.ext.tr.GPUFactory.GPUFeature;
import org.jtrfp.trcl.snd.SoundSystem;
import org.jtrfp.trcl.snd.SoundSystemOutputConfig;
import org.jtrfp.trcl.tools.Util;
import org.springframework.stereotype.Component;

@Component
public class SoundSystemFactory implements FeatureFactory<TR> {

    public static class SoundSystemFeature extends SoundSystem implements Feature<TR> {
	private DefaultMutableTreeNode bufferSizeChoices = new DefaultMutableTreeNode();
	private DefaultMutableTreeNode bufferSize4096  = new DefaultMutableTreeNode("4096");
	private DefaultMutableTreeNode bufferSize8192  = new DefaultMutableTreeNode("8192");
	private DefaultMutableTreeNode bufferSize16384 = new DefaultMutableTreeNode("16384");

	public SoundSystemFeature() {
	    super();
	    bufferSizeChoices.add(bufferSize4096);
	    bufferSizeChoices.add(bufferSize8192);
	    bufferSizeChoices.add(bufferSize16384);
	}

	@Override
	public void apply(TR target) {
	    setTr(target);
	    setGpu(Features.get(target, GPUFeature.class));
	    initialize();
	}

	@Override
	public void destruct(TR target) {}
	
	@Override
	@ConfigByUI(editorClass = DoubleNormalizedSliderUI.class)
	public void setMusicVolume(Double width) {
	    super.setMusicVolume(width);
	}
	
	@Override
	@ConfigByUI(editorClass = DoubleNormalizedSliderUI.class)
	public void setSfxVolume(Double width) {
	    super.setSfxVolume(width);
	}
	
	@Override
	@ConfigByUI(editorClass = DoubleNormalizedSliderUI.class)
	public void setModStereoWidth(Double width) {
	    super.setModStereoWidth(width);
	}
	
	@ConfigByUI(editorClass = CheckboxUI.class)
	@ToolTip(text="Apply an extra buffer to improve performance for a doubling in sound latency.")
	public Boolean getBufferLag() {
	    return super.isBufferLag();
	}
	
	@ConfigByUI(editorClass = CheckboxUI.class)
	@ToolTip(text="Use the GPU's TMU to apply interpolative filtering 'for free.'")
	public Boolean getLinearFiltering() {
	    return super.isLinearFiltering();
	}
	
	@ConfigByUI(editorClass = ComboBoxUI.class)
	public DefaultMutableTreeNode getBufferSize() {
	    final String bufferSize = super.getBufferSizeFramesString();
	    return
		    Util.getLeaves(bufferSizeChoices).
		    stream().
		    filter(x->Objects.equals(x.getUserObject(),bufferSize)).
		    findAny().orElse(bufferSize4096);
	}//end getBufferSize()
	
	public void setBufferSize(DefaultMutableTreeNode bufferSize) {
	    final Object userObject = bufferSize.getUserObject();
	    if(userObject != null)
		super.setBufferSizeFrames(Integer.parseInt((String)userObject));
	}//end setBufferSize()
	
	@ConfigByUI(editorClass = TreeSelectionUI.class)
	@ToolTip(text="Method by which to send audio output, ordered by driver, port, and format.")
	public DefaultMutableTreeNode getSelectedOutput() {
	    return super.getOutputConfigNode();
	    /*
	    final DefaultMutableTreeNode root = super.getOutputConfigTree();
	    final SoundSystemOutputConfig config = super.getOutputConfig();
	    
	    List<DefaultMutableTreeNode> path = Util.nodePathFromUserObjectPath(root, config.getDriverByName(), config.getDeviceByName(), config.getPortByName(), config.getFormatByName());
	    return path.get(path.size()-1);
	    */
	}//end getConfiguration
	
	public void setSelectedOutput(DefaultMutableTreeNode configuration) {
	    super.setOutputConfigNode(configuration);
	    /*
	    if(configuration == null)
		return;
	    configuration = Util.getToStringApproximation(configuration, super.getOutputConfigTree(),x->x.toString());
	    final SoundSystemOutputConfig newConfig = new SoundSystemOutputConfig();
	    newConfig.setFormatByName((String)(configuration.getUserObject()));
	    configuration = (DefaultMutableTreeNode)configuration.getParent();
	    newConfig.setPortByName((String)(configuration.getUserObject()));
	    configuration = (DefaultMutableTreeNode)configuration.getParent();
	    newConfig.setDeviceByName((String)(configuration.getUserObject()));
	    configuration = (DefaultMutableTreeNode)configuration.getParent();
	    newConfig.setDriverByName((String)(configuration.getUserObject()));
	    System.out.println("Setting output config to "+newConfig);
	    super.setOutputConfig(newConfig);
	    System.out.println("Finished setting output config.");
	    */
	}//end setConfiguration
    }//end SoundSystemFeature

    @Override
    public Feature<TR> newInstance(TR target) {
	final SoundSystemFeature result = new SoundSystemFeature();
	Runtime.getRuntime().addShutdownHook(new Thread(){
	    @Override
	    public void run(){
		result.setPaused(true);
	    }
	});
	return result;
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return SoundSystemFeature.class;
    }

}//end SoundSystemFactory
