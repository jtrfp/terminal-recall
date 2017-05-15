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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.Executor;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jtrfp.trcl.flow.CheckboxPropertyBinding;
import org.jtrfp.trcl.flow.ComboBoxPropertyBinding;
import org.jtrfp.trcl.flow.JSliderPropertyBinding;
import org.jtrfp.trcl.gui.ConfigWindowFactory.ConfigWindow;
import org.jtrfp.trcl.gui.LabelPropertyBinding;
import org.jtrfp.trcl.gui.SoundOutputSelectorGUI;
import org.jtrfp.trcl.tools.Util;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class GL33SoundSystemConfigPanel extends JPanel {
    private JCheckBox chckbxLinearInterpolation, chckbxBufferLag;
    private final SoundOutputSelectorGUI soundOutputSelectorGUI;
    private JComboBox<String> audioBufferSizeCB;
	private JSlider modStereoWidthSlider, musicVolumeSlider, sfxVolumeSlider;
    private ConfigWindow configWindow;
    private SoundSystem soundSystem;
    private boolean initialized = false;
    private volatile boolean isModifying = false;
    private Executor executor;
    final JLabel modStereoWidthLbl = new JLabel("NN%"), musicVolumeLbl = new JLabel("NN%"), sfxVolumeLbl = new JLabel("NN%");
    protected static final String [] BUFFER_SIZES = {
	    "8192","4096","2048","1024","512","256"
    };
    private JPanel musicVolumePanel, sfxVolumePanel;
    /*
    enum AudioBufferSize{
	SAMPLES_8192(8192),
	SAMPLES_4096(4096),
	SAMPLES_2048(2048),
	SAMPLES_1024(1024),
	SAMPLES_512(512),
	SAMPLES_256(256);

	private final int sizeInFrames;
	AudioBufferSize(int sizeInFrames){
	    this.sizeInFrames=sizeInFrames;
	}
	@Override
	public String toString(){
	    return sizeInFrames+" frames";
	}

	public int getSizeInFrames(){
	    return sizeInFrames;
	}
    }//end AudioBufferSize
*/
    /**
     * Create the panel.
     */
    public GL33SoundSystemConfigPanel() {
	GridBagLayout gbl_soundTab = new GridBagLayout();
	    gbl_soundTab.columnWidths = new int[]{0, 0};
	    gbl_soundTab.rowHeights = new int[]{65, 51, 0, 0, 70, 132, 0, 0, 0};
	    gbl_soundTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gbl_soundTab.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
	    setLayout(gbl_soundTab);

	    JPanel checkboxPanel = new JPanel();
	    GridBagConstraints gbc_checkboxPanel = new GridBagConstraints();
	    gbc_checkboxPanel.insets = new Insets(0, 0, 5, 5);
	    gbc_checkboxPanel.fill = GridBagConstraints.BOTH;
	    gbc_checkboxPanel.gridx = 0;
	    gbc_checkboxPanel.gridy = 0;
	    add(checkboxPanel, gbc_checkboxPanel);

	    chckbxLinearInterpolation = new JCheckBox("Linear Filtering");
	    chckbxLinearInterpolation.setToolTipText("Use the GPU's TMU to smooth playback of low-rate samples.");
	    chckbxLinearInterpolation.setHorizontalAlignment(SwingConstants.LEFT);
	    checkboxPanel.add(chckbxLinearInterpolation);

	    chckbxLinearInterpolation.addItemListener(new ItemListener(){
		@Override
		public void itemStateChanged(ItemEvent e) {
		    getConfigWindow().notifyNeedRestart();
		}});
	    chckbxBufferLag = new JCheckBox("Buffer Lag");
	    chckbxBufferLag.setToolTipText("Improves efficiency, doubles latency.");
	    checkboxPanel.add(chckbxBufferLag);
	    
	    JPanel modStereoWidthPanel = new JPanel();
	    FlowLayout flowLayout_2 = (FlowLayout) modStereoWidthPanel.getLayout();
	    flowLayout_2.setAlignment(FlowLayout.LEFT);
	    modStereoWidthPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "MOD Stereo Width", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	    GridBagConstraints gbc_modStereoWidthPanel = new GridBagConstraints();
	    gbc_modStereoWidthPanel.anchor = GridBagConstraints.NORTH;
	    gbc_modStereoWidthPanel.insets = new Insets(0, 0, 5, 5);
	    gbc_modStereoWidthPanel.fill = GridBagConstraints.HORIZONTAL;
	    gbc_modStereoWidthPanel.gridx = 0;
	    gbc_modStereoWidthPanel.gridy = 1;
	    add(modStereoWidthPanel, gbc_modStereoWidthPanel);

	    modStereoWidthSlider = new JSlider();
	    modStereoWidthSlider.setPaintTicks(true);
	    modStereoWidthSlider.setMinorTickSpacing(25);
	    modStereoWidthPanel.add(modStereoWidthSlider);
	    
	    modStereoWidthPanel.add(modStereoWidthLbl);
	    
	    modStereoWidthSlider.addChangeListener(new ChangeListener(){
		@Override
		public void stateChanged(ChangeEvent arg0) {
		    updateModStereoWidthProperty();
		}});
	    
	    //MUSIC
	    musicVolumePanel = new JPanel();
	    musicVolumePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Music Volume", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
	    GridBagConstraints gbc_musicVolumePanel = new GridBagConstraints();
	    gbc_musicVolumePanel.gridwidth = 0;
	    gbc_musicVolumePanel.anchor = GridBagConstraints.NORTH;
	    gbc_musicVolumePanel.insets = new Insets(0, 0, 5, 0);
	    gbc_musicVolumePanel.fill = GridBagConstraints.HORIZONTAL;
	    gbc_musicVolumePanel.gridx = 0;
	    gbc_musicVolumePanel.gridy = 2;
	    add(musicVolumePanel, gbc_musicVolumePanel);
	    musicVolumePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
	    
	    musicVolumeSlider = new JSlider();
	    musicVolumeSlider.setPaintTicks(true);
	    musicVolumeSlider.setMinorTickSpacing(25);
	    musicVolumePanel.add(musicVolumeSlider);
	    musicVolumePanel.add(musicVolumeLbl);
	    
	    //SFX
	    sfxVolumePanel = new JPanel();
	    sfxVolumePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "SFX Volume", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
	    GridBagConstraints gbc_sfxVolumePanel = new GridBagConstraints();
	    gbc_sfxVolumePanel.anchor = GridBagConstraints.NORTH;
	    gbc_sfxVolumePanel.fill = GridBagConstraints.HORIZONTAL;
	    gbc_sfxVolumePanel.insets = new Insets(0, 0, 5, 5);
	    gbc_sfxVolumePanel.gridx = 0;
	    gbc_sfxVolumePanel.gridy = 3;
	    add(sfxVolumePanel, gbc_sfxVolumePanel);
	    sfxVolumePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
	    
	    sfxVolumeSlider = new JSlider();
	    sfxVolumeSlider.setPaintTicks(true);
	    sfxVolumeSlider.setMinorTickSpacing(25);
	    sfxVolumePanel.add(sfxVolumeSlider);
	    sfxVolumePanel.add(sfxVolumeLbl);
	    
	    //TODO:
	    JPanel bufferSizePanel = new JPanel();
	    FlowLayout flowLayout_3 = (FlowLayout) bufferSizePanel.getLayout();
	    flowLayout_3.setAlignment(FlowLayout.LEFT);
	    bufferSizePanel.setBorder(new TitledBorder(null, "Buffer Size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	    GridBagConstraints gbc_bufferSizePanel = new GridBagConstraints();
	    gbc_bufferSizePanel.anchor = GridBagConstraints.NORTH;
	    gbc_bufferSizePanel.insets = new Insets(0, 0, 5, 5);
	    gbc_bufferSizePanel.fill = GridBagConstraints.HORIZONTAL;
	    gbc_bufferSizePanel.gridx = 0;
	    gbc_bufferSizePanel.gridy = 4;
	    add(bufferSizePanel, gbc_bufferSizePanel);
	    audioBufferSizeCB = new JComboBox();
	    audioBufferSizeCB.setModel(new DefaultComboBoxModel<String>(BUFFER_SIZES));
	    bufferSizePanel.add(audioBufferSizeCB);
	    
	    soundOutputSelectorGUI = new SoundOutputSelectorGUI();
	    soundOutputSelectorGUI.setBorder(new TitledBorder(null, "Output Driver", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	    GridBagConstraints gbc_soundOutputSelectorGUI = new GridBagConstraints();
	    gbc_soundOutputSelectorGUI.anchor = GridBagConstraints.NORTH;
	    gbc_soundOutputSelectorGUI.insets = new Insets(0, 0, 5, 5);
	    gbc_soundOutputSelectorGUI.fill = GridBagConstraints.HORIZONTAL;
	    gbc_soundOutputSelectorGUI.gridx = 0;
	    gbc_soundOutputSelectorGUI.gridy = 5;
	    add(soundOutputSelectorGUI, gbc_soundOutputSelectorGUI);
    }//end constructor
    
    protected void proposeInit(){
	if(initialized)
	    return;
	try{Util.assertPropertiesNotNull(this, "soundSystem");}
	catch(Exception e){e.printStackTrace();return;}
	initialized = true;
	init();
    }
    
    protected void init(){
	System.out.println("init called");
	final SoundSystem soundSystem = getSoundSystem();
	new CheckboxPropertyBinding(chckbxBufferLag          , soundSystem, SoundSystem.BUFFER_LAG                 ).setExecutor(getExecutor());
	new CheckboxPropertyBinding(chckbxLinearInterpolation, soundSystem, SoundSystem.LINEAR_FILTERING           ).setExecutor(getExecutor());
	new ComboBoxPropertyBinding(audioBufferSizeCB        , soundSystem, SoundSystem.BUFFER_SIZE_FRAMES_STRING  ).setExecutor(getExecutor());
	new LabelPropertyBinding<Double>(SoundSystem.MOD_STEREO_WIDTH, soundSystem, modStereoWidthLbl, Double.class).setExecutor(getExecutor());
	new LabelPropertyBinding<Double>(SoundSystem.MUSIC_VOLUME, soundSystem, musicVolumeLbl, Double.class       ).setExecutor(getExecutor());
	new LabelPropertyBinding<Double>(SoundSystem.SFX_VOLUME,   soundSystem,   sfxVolumeLbl, Double.class       ).setExecutor(getExecutor());
	new JSliderPropertyBinding(musicVolumeSlider, .01, soundSystem, SoundSystem.MUSIC_VOLUME, Double.class     ).setExecutor(executor);
	new JSliderPropertyBinding(sfxVolumeSlider,   .01, soundSystem, SoundSystem.SFX_VOLUME,   Double.class     ).setExecutor(executor);
	soundOutputSelectorGUI.setExecutor(getExecutor());
	soundOutputSelectorGUI.init(soundSystem);
	soundSystem.addPropertyChangeListener(SoundSystem.MOD_STEREO_WIDTH, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent evt) {
		updateModStereoWidthSlider();
	    }});
	updateModStereoWidthSlider();
    }//end init()
    
    private void updateModStereoWidthSlider(){
	if(isModifying)
	    return;
	isModifying = true;
	final int newValue = (int)Math.rint(((Double)getSoundSystem().getModStereoWidth())*100.);
	//modStereoWidthLbl.setText(newValue+"%");
	modStereoWidthSlider.setValue(newValue);
	isModifying = false;
    }
    
    private void updateModStereoWidthProperty(){
	if(isModifying)
	    return;
	isModifying = true;
	final int newValue = modStereoWidthSlider.getValue();
	getSoundSystem().setModStereoWidth((double)newValue / 100.);
	//modStereoWidthLbl.setText(newValue+"%");
	//ConfigWindow.this.getTrConfiguration().setModStereoWidth(((double)modStereoWidthSlider.getValue())/100.);
	getConfigWindow().notifyNeedRestart();
	isModifying = false;
    }

    public ConfigWindow getConfigWindow() {
        return configWindow;
    }

    public void setConfigWindow(ConfigWindow configWindow) {
        this.configWindow = configWindow;
        proposeInit();
    }

    public SoundSystem getSoundSystem() {
        return soundSystem;
    }

    public void setSoundSystem(SoundSystem soundSystem) {
        this.soundSystem = soundSystem;
        proposeInit();
    }

    protected Executor getExecutor() {
        return executor;
    }

    protected void setExecutor(Executor transientExecutor) {
        this.executor = transientExecutor;
    }

}//end GL33SoundSystemConfigPanel
