/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.conf.TRConfigurationFactory.TRConfiguration;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.core.GraphStabilizationListener;
import org.jtrfp.trcl.core.PODRegistry;
import org.jtrfp.trcl.core.TRConfigRootFactory.TRConfigRoot;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.file.VOXFile;
import org.jtrfp.trcl.snd.SoundSystem;
import org.springframework.stereotype.Component;

@Component
public class ConfigWindowFactory implements FeatureFactory<TR>{
    
/*
    public static void main(String [] args){
	new ConfigWindow().setVisible(true);
    }//end main()
    */

    public class ConfigWindow extends JFrame implements GraphStabilizationListener, Feature<TR>{
	//private TRConfiguration config;
	//private JComboBox audioBufferSizeCB;
	//private JSlider modStereoWidthSlider;
	private JList podList,missionList;
	private DefaultListModel podLM=new DefaultListModel(), missionLM=new DefaultListModel();
	private boolean needRestart=false;
	private final JFileChooser fileChooser = new JFileChooser();
	
	private final Collection<ConfigurationTab> tabs;
	//private final TRConfigRoot cMgr;
	private final JTabbedPane tabbedPane;
	private       PODRegistry podRegistry;
	private       PodCollectionListener podCollectionListener = new PodCollectionListener();
	private TR              tr;
	private TRConfigRoot    trConfigRoot;
	private TRConfiguration trConfiguration;
	private JLabel lblConfigpath = new JLabel("[config path not set]");

	public ConfigWindow(){
	    this(new ArrayList<ConfigurationTab>());
	}
	
	public ConfigWindow(Collection<ConfigurationTab> tabs){
	    setTitle("Settings");
	    setSize(340,540);
	    final TR tr = getTr();
	    //this.cMgr = Features.get(tr, TRConfigRoot.class);
	    this.tabs = tabs;
	    //config = Features.get(tr, TRConfiguration.class);
	    tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	    getContentPane().add(tabbedPane, BorderLayout.CENTER);

	    JPanel generalTab = new JPanel();
	    tabbedPane.addTab("General", new ImageIcon(ConfigWindow.class.getResource("/org/freedesktop/tango/22x22/mimetypes/application-x-executable.png")), generalTab, null);
	    GridBagLayout gbl_generalTab = new GridBagLayout();
	    gbl_generalTab.columnWidths = new int[]{0, 0};
	    gbl_generalTab.rowHeights = new int[]{0, 188, 222, 0};
	    gbl_generalTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gbl_generalTab.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
	    generalTab.setLayout(gbl_generalTab);

	    JPanel settingsLoadSavePanel = new JPanel();
	    GridBagConstraints gbc_settingsLoadSavePanel = new GridBagConstraints();
	    gbc_settingsLoadSavePanel.insets = new Insets(0, 0, 5, 0);
	    gbc_settingsLoadSavePanel.anchor = GridBagConstraints.WEST;
	    gbc_settingsLoadSavePanel.gridx = 0;
	    gbc_settingsLoadSavePanel.gridy = 0;
	    generalTab.add(settingsLoadSavePanel, gbc_settingsLoadSavePanel);
	    settingsLoadSavePanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Overall Settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	    FlowLayout flowLayout_1 = (FlowLayout) settingsLoadSavePanel.getLayout();
	    flowLayout_1.setAlignment(FlowLayout.LEFT);

	    JButton btnSave = new JButton("Export...");
	    btnSave.setToolTipText("Export these settings to an external file");
	    settingsLoadSavePanel.add(btnSave);
	    btnSave.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    exportSettings();
		}});

	    JButton btnLoad = new JButton("Import...");
	    btnLoad.setToolTipText("Import an external settings file");
	    settingsLoadSavePanel.add(btnLoad);
	    btnLoad.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    importSettings();
		}});

	    JButton btnConfigReset = new JButton("Reset");
	    btnConfigReset.setToolTipText("Reset all settings to defaults");
	    settingsLoadSavePanel.add(btnConfigReset);
	    btnConfigReset.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    defaultSettings();
		}});

	    JPanel registeredPODsPanel = new JPanel();
	    registeredPODsPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Registered PODs", TitledBorder.LEFT, TitledBorder.TOP, null, null));
	    GridBagConstraints gbc_registeredPODsPanel = new GridBagConstraints();
	    gbc_registeredPODsPanel.insets = new Insets(0, 0, 5, 0);
	    gbc_registeredPODsPanel.fill = GridBagConstraints.BOTH;
	    gbc_registeredPODsPanel.gridx = 0;
	    gbc_registeredPODsPanel.gridy = 1;
	    generalTab.add(registeredPODsPanel, gbc_registeredPODsPanel);
	    GridBagLayout gbl_registeredPODsPanel = new GridBagLayout();
	    gbl_registeredPODsPanel.columnWidths = new int[]{272, 0};
	    gbl_registeredPODsPanel.rowHeights = new int[]{76, 0, 0};
	    gbl_registeredPODsPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gbl_registeredPODsPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
	    registeredPODsPanel.setLayout(gbl_registeredPODsPanel);

	    JPanel podListPanel = new JPanel();
	    GridBagConstraints gbc_podListPanel = new GridBagConstraints();
	    gbc_podListPanel.insets = new Insets(0, 0, 5, 0);
	    gbc_podListPanel.fill = GridBagConstraints.BOTH;
	    gbc_podListPanel.gridx = 0;
	    gbc_podListPanel.gridy = 0;
	    registeredPODsPanel.add(podListPanel, gbc_podListPanel);
	    podListPanel.setLayout(new BorderLayout(0, 0));

	    JScrollPane podListScrollPane = new JScrollPane();
	    podListPanel.add(podListScrollPane, BorderLayout.CENTER);

	    podList = new JList(podLM);
	    podList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    podListScrollPane.setViewportView(podList);

	    JPanel podListOpButtonPanel = new JPanel();
	    podListOpButtonPanel.setBorder(null);
	    GridBagConstraints gbc_podListOpButtonPanel = new GridBagConstraints();
	    gbc_podListOpButtonPanel.anchor = GridBagConstraints.NORTH;
	    gbc_podListOpButtonPanel.gridx = 0;
	    gbc_podListOpButtonPanel.gridy = 1;
	    registeredPODsPanel.add(podListOpButtonPanel, gbc_podListOpButtonPanel);
	    FlowLayout flowLayout = (FlowLayout) podListOpButtonPanel.getLayout();
	    flowLayout.setAlignment(FlowLayout.LEFT);

	    JButton addPodButton = new JButton("Add...");
	    addPodButton.setIcon(new ImageIcon(ConfigWindow.class.getResource("/org/freedesktop/tango/16x16/actions/list-add.png")));
	    addPodButton.setToolTipText("Add a POD to the registry to be considered when running a game.");
	    podListOpButtonPanel.add(addPodButton);
	    addPodButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    notifyNeedRestart();
		    addPOD();
		}});

	    JButton removePodButton = new JButton("Remove");
	    removePodButton.setIcon(new ImageIcon(ConfigWindow.class.getResource("/org/freedesktop/tango/16x16/actions/list-remove.png")));
	    removePodButton.setToolTipText("Remove a POD file from being considered when playing a game");
	    podListOpButtonPanel.add(removePodButton);
	    removePodButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    //podLM.removeElement(podList.getSelectedValue());
		    notifyNeedRestart();
		    getPodRegistry().getPodCollection().remove(podList.getSelectedValue());
		}});


	    JButton podEditButton = new JButton("Edit...");
	    podEditButton.setIcon(null);
	    podEditButton.setToolTipText("Edit the selected POD path");
	    podListOpButtonPanel.add(podEditButton);
	    podEditButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    notifyNeedRestart();
		    editPODPath();
		}});

	    JPanel missionPanel = new JPanel();
	    GridBagConstraints gbc_missionPanel = new GridBagConstraints();
	    gbc_missionPanel.fill = GridBagConstraints.BOTH;
	    gbc_missionPanel.gridx = 0;
	    gbc_missionPanel.gridy = 2;
	    generalTab.add(missionPanel, gbc_missionPanel);
	    missionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Missions", TitledBorder.LEADING, TitledBorder.TOP, null, null));
	    GridBagLayout gbl_missionPanel = new GridBagLayout();
	    gbl_missionPanel.columnWidths = new int[]{0, 0};
	    gbl_missionPanel.rowHeights = new int[]{0, 0, 0};
	    gbl_missionPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	    gbl_missionPanel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
	    missionPanel.setLayout(gbl_missionPanel);

	    JScrollPane scrollPane = new JScrollPane();
	    GridBagConstraints gbc_scrollPane = new GridBagConstraints();
	    gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
	    gbc_scrollPane.fill = GridBagConstraints.BOTH;
	    gbc_scrollPane.gridx = 0;
	    gbc_scrollPane.gridy = 0;
	    missionPanel.add(scrollPane, gbc_scrollPane);

	    missionList = new JList(missionLM);
	    missionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    scrollPane.setViewportView(missionList);

	    JPanel missionListOpButtonPanel = new JPanel();
	    GridBagConstraints gbc_missionListOpButtonPanel = new GridBagConstraints();
	    gbc_missionListOpButtonPanel.anchor = GridBagConstraints.NORTH;
	    gbc_missionListOpButtonPanel.gridx = 0;
	    gbc_missionListOpButtonPanel.gridy = 1;
	    missionPanel.add(missionListOpButtonPanel, gbc_missionListOpButtonPanel);

	    JButton addVOXButton = new JButton("Add...");
	    addVOXButton.setIcon(new ImageIcon(ConfigWindow.class.getResource("/org/freedesktop/tango/16x16/actions/list-add.png")));
	    addVOXButton.setToolTipText("Add an external VOX file as a mission");
	    missionListOpButtonPanel.add(addVOXButton);
	    addVOXButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    addVOX();
		}});

	    final JButton removeVOXButton = new JButton("Remove");
	    removeVOXButton.setIcon(new ImageIcon(ConfigWindow.class.getResource("/org/freedesktop/tango/16x16/actions/list-remove.png")));
	    removeVOXButton.setToolTipText("Remove the selected mission");
	    missionListOpButtonPanel.add(removeVOXButton);
	    removeVOXButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    //TODO:
		    missionLM.remove(missionList.getSelectedIndex());
		}});

	    final JButton editVOXButton = new JButton("Edit...");
	    editVOXButton.setToolTipText("Edit the selected Mission's VOX path");
	    missionListOpButtonPanel.add(editVOXButton);
	    editVOXButton.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    editVOXPath();
		}});

	    missionList.addListSelectionListener(new ListSelectionListener(){
		@Override
		public void valueChanged(ListSelectionEvent evt) {
		    final String val = (String)missionList.getSelectedValue();
		    if(val == null)
			missionList.setSelectedIndex(0);
		    else if(isBuiltinVOX(val)){
			removeVOXButton.setEnabled(false);
			editVOXButton.setEnabled(false);
		    }else{
			removeVOXButton.setEnabled(true);
			editVOXButton.setEnabled(true);
		    }
		}});
	    
	    JPanel okCancelPanel = new JPanel();
	    getContentPane().add(okCancelPanel, BorderLayout.SOUTH);
	    okCancelPanel.setLayout(new BorderLayout(0, 0));

	    JButton btnOk = new JButton("OK");
	    btnOk.setToolTipText("Apply these settings and close the window");
	    okCancelPanel.add(btnOk, BorderLayout.WEST);
	    btnOk.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    //Keep settings, save to disk
		    try               {getTrConfigRoot().saveConfigurations();}
		    catch(Exception e){e.printStackTrace();}
		    applySettingsEDT();
		    ConfigWindow.this.setVisible(false);
		}
	    });

	    JButton btnCancel = new JButton("Cancel");
	    btnCancel.setToolTipText("Close the window without applying settings");
	    okCancelPanel.add(btnCancel, BorderLayout.EAST);
	    //final TRConfigRoot cfgRoot = getTrConfigRoot();
	    //final String cFilePath = cfgRoot.getConfigSaveURI();
	    //JLabel lblConfigpath = new JLabel(cFilePath);
	    lblConfigpath.setIcon(null);
	    lblConfigpath.setToolTipText("Default config file path");
	    lblConfigpath.setHorizontalAlignment(SwingConstants.CENTER);
	    lblConfigpath.setFont(new Font("Dialog", Font.BOLD, 6));
	    okCancelPanel.add(lblConfigpath, BorderLayout.CENTER);
	    btnCancel.addActionListener(new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent arg0) {
		    //dispatchComponentConfigs();
		    //Revert to original
		    getTrConfigRoot().loadConfigurations();
		    ConfigWindow.this.setVisible(false);
		}});
	}//end constructor
	
	@Override
	public void setVisible(boolean isVisible){
	    if(isVisible)
		try               {getTrConfigRoot().saveConfigurations();}
	        catch(Exception e){e.printStackTrace();}
	    super.setVisible(isVisible);
	}//end setVisible(...)

	private void applySettingsEDT(){
	    final TRConfigRoot      configRoot = getTrConfigRoot();
	    final TRConfiguration   config     = getTrConfiguration();
	    config.setVoxFile((String)missionList.getSelectedValue());
	    //config.setModStereoWidth((double)modStereoWidthSlider.getValue()/100.);
	    //config.setAudioLinearFiltering(chckbxLinearInterpolation.isSelected());
	    //config.setAudioBufferLag(chckbxBufferLag.isSelected());
	    {HashSet<String>pList=new HashSet<String>();
	    //for(int i=0; i<podLM.getSize();i++)
	//	pList.add((String)podLM.getElementAt(i));
	    //podLM.clear();//Clear so we don't get a double-copy when the dispatcher populates it.
	    //final Collection<String> podCollection = getPodRegistry().getPodCollection();
	    //podCollection.clear();
	    //for(String pod:pList)
		//podCollection.add(pod);

	    HashSet<String>vxList=new HashSet<String>();
	    for(int i=0; i<missionLM.getSize();i++)
		vxList.add((String)missionLM.getElementAt(i));
	    config.setMissionList(vxList);}
	    //soundOutputSelectorGUI.applySettings(config);
	    try{configRoot.saveConfigurations();}
	    catch(Exception e){e.printStackTrace();}
	    //writeSettingsTo(cMgr.getConfigFilePath());
	    if(needRestart)
		notifyOfRestart();
	    //final AudioBufferSize abs = (AudioBufferSize)audioBufferSizeCB.getSelectedItem();
	    //config.setAudioBufferSize(abs.getSizeInFrames());
	    //Apply the component configs
	    //gatherComponentConfigs();
	}//end applySettings()
/*
	private void gatherComponentConfigs(){
	    Map<String,Object> configs = config.getComponentConfigs();
	    for(ConfigurationTab tab:tabs){
		System.out.println("Putting tab "+tab.getTabName()+" With entries:");
		System.out.print("\t");
		ControllerConfigTabConf conf = (ControllerConfigTabConf)tab.getConfigBean();
		for(Entry ent: conf.getControllerConfigurations().entrySet()){
		    System.out.print(" "+ent.getKey());
		} System.out.println();
		configs.put(tab.getConfigBeanClass().getName(), tab.getConfigBean());}
	}//end gatherComponentConfigs()
	*/
/*
	private void dispatchComponentConfigs(){
	    Map<String,Object> configs = config.getComponentConfigs();
	    for(ConfigurationTab tab:tabs)
		tab.setConfigBean(configs.get(tab.getConfigBeanClass().getName()));
	}//end dispatchComponentConfigs()
*/
	private void readSettingsToPanel(){
	    final TRConfiguration config = getTrConfiguration();
	    //Initial settings - These do not listen to the config states!
	    final SoundSystem soundSystem = Features.get(getTr(), SoundSystem.class);
	    //modStereoWidthSlider.setValue((int)(config.getModStereoWidth()*100.));
	    //chckbxLinearInterpolation.setSelected(soundSystem.isLinearFiltering());
	    //chckbxBufferLag.setSelected(soundSystem.isBufferLag());
	    /*final int bSize = config.getAudioBufferSize();
	    for(AudioBufferSize abs:AudioBufferSize.values())
		if(abs.getSizeInFrames()==bSize)
		    audioBufferSizeCB.setSelectedItem(abs);
*/
	    missionLM.removeAllElements();
	    for(String vox:config.getMissionList()){
		if(isBuiltinVOX(vox))
		    missionLM.addElement(vox);
		else if(checkVOX(new File(vox)))
		    missionLM.addElement(vox);
	    }//end for(vox)
	    String missionSelection = config.getVoxFile();
	    for(int i=0; i<missionLM.getSize(); i++){
		if(((String)missionLM.get(i)).contentEquals(missionSelection))missionList.setSelectedIndex(i);}
/*
	    podLM.removeAllElements();
	    final Collection<String> podRegistryCollection = getPodRegistry().getPodCollection();
	    //final DefaultListModel podList = config.getPodList();
	    for(String path : podRegistryCollection){
		if(path!=null)
		    if(checkPOD(new File(path)))
			podLM.addElement(path);}
	    */
	    //soundOutputSelectorGUI.readToPanel(config);
	    //Undo any flags set by the listeners.
	    needRestart=false;
	}//end readSettings()

	private boolean isBuiltinVOX(String vox){
	    return  vox.contentEquals(TRConfiguration.AUTO_DETECT) || 
		    vox.contentEquals("Fury3") || vox.contentEquals("TV") || 
		    vox.contentEquals("FurySE");
	}//end isBuiltinVOX

	private void notifyOfRestart(){
	    JOptionPane.showMessageDialog(this, "Some changes won't take effect until the program is restarted.");
	    needRestart=false;
	}

	private void addPOD(){
	    fileChooser.setFileFilter(new FileFilter(){
		@Override
		public boolean accept(File file) {
		    return file.getName().toUpperCase().endsWith(".POD")||file.isDirectory();
		}

		@Override
		public String getDescription() {
		    return "Terminal Reality .POD files";
		}});
	    if(fileChooser.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)
		return;
	    final File file = fileChooser.getSelectedFile();
	    if(!checkPOD(file))
		return;
	    //podLM.addElement(file.getAbsolutePath());
	    getPodRegistry().getPodCollection().add(file.getAbsolutePath());
	}//end addPOD()

	private void addVOX() {
	    fileChooser.setFileFilter(new FileFilter() {
		@Override
		public boolean accept(File file) {
		    return file.getName().toUpperCase().endsWith(".VOX")||file.isDirectory();
		}

		@Override
		public String getDescription() {
		    return "Terminal Reality .VOX files";
		}
	    });
	    if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
		return;
	    final File file = fileChooser.getSelectedFile();
	    if(file!=null)
		missionLM.addElement(file.getAbsolutePath());
	}//end addVOX()
	
	private boolean writeSettingsToNormalConf(){
	    try{
		//cMgr.saveConfigurations(f);
		final TRConfigRoot configRoot = getTrConfigRoot();
		//configRoot.setConfigSaveURI(f.getPath());
		configRoot.saveConfigurations();
		
		return true;
	    }catch(Exception e){JOptionPane.showMessageDialog(
		    this,
		    "Failed to write the config file.\n"
			    + e.getLocalizedMessage()+"\n"+e.getClass().getName(),
			    "File write failure", JOptionPane.ERROR_MESSAGE);
	    return false;}
	}//end writeSettings()

	private boolean writeSettingsTo(File f){
	    try{/*
		//cMgr.saveConfigurations(f);
		final TRConfigRoot configRoot = getTrConfigRoot();
		configRoot.setConfigSaveURI(f.getPath());
		configRoot.saveConfigurations();
		*/
		getTrConfigRoot().saveConfigurations(f);
		return true;
	    }catch(Exception e){JOptionPane.showMessageDialog(
		    this,
		    "Failed to write the config file.\n"
			    + e.getLocalizedMessage()+"\n"+e.getClass().getName(),
			    "File write failure", JOptionPane.ERROR_MESSAGE);
	    return false;}
	}//end writeSettingsTo(...)

	private void exportSettings(){
	    fileChooser.setFileFilter(new FileFilter(){
		@Override
		public boolean accept(File file) {
		    return file.getName().toLowerCase().endsWith(".config.trcl.xml")||file.isDirectory();
		}

		@Override
		public String getDescription() {
		    return "Terminal Recall Config Files";
		}});
	    fileChooser.showSaveDialog(this);
	    File f = fileChooser.getSelectedFile();
	    if(f==null)return;
	    writeSettingsTo(f);
	}//end exportSettings()

	private void editVOXPath(){
	    JOptionPane.showMessageDialog(this, "Not yet implemented.");
	    //TODO
	    /*
	    final String result = JOptionPane.showInputDialog(this, "Edit VOX Path", missionLM.get(missionList.getSelectedIndex()));
	    if(result==null)// Clicked Cancel
		return;// Do nothing
	    if(checkVOX(new File(result)))
		missionLM.set(missionList.getSelectedIndex(), result);
	    */
	}//end editVOXPath()

	private void editPODPath(){
	    JOptionPane.showMessageDialog(this, "Not yet implemented.");
	    //TODO
	    /*
	    final String result = JOptionPane.showInputDialog(this, "Edit POD Path", podLM.get(missionList.getSelectedIndex()));
	    if(result==null)// Clicked Cancel
		return;// Do nothing
	    if(checkPOD(new File(result)))
		podLM.set(podList.getSelectedIndex(), result);
	    */
	}//end editPODPath()

	private boolean readSettingsFromFile(File f){
	    try{/*
		FileInputStream is = new FileInputStream(f);
	    XMLDecoder xmlDec = new XMLDecoder(is);
	    xmlDec.setExceptionListener(new ExceptionListener(){
		@Override
		public void exceptionThrown(Exception e) {
		    e.printStackTrace();
		}});
	    TRConfiguration src =(TRConfiguration)xmlDec.readObject();
	    xmlDec.close();
	    TRConfiguration config = getTrConfiguration();
	    */
	    this.getTrConfigRoot().loadConfigurations(f);
	    /*if(config!=null)
		BeanUtils.copyProperties(config, src);
	    else setTrConfiguration(config = src);*/
	    }catch(Exception e){JOptionPane.showMessageDialog(
		    this,
		    "Failed to read the specified file:\n"
			    + e.getLocalizedMessage(),
			    "File read failure", JOptionPane.ERROR_MESSAGE);return false;}
	    return true;
	}//end readSettingsFromFile(...)

	private void importSettings(){
	    fileChooser.setFileFilter(new FileFilter(){
		@Override
		public boolean accept(File file) {
		    return file.getName().toLowerCase().endsWith(".config.trcl.xml")||file.isDirectory();
		}

		@Override
		public String getDescription() {
		    return "Terminal Recall Config Files";
		}});
	    fileChooser.showOpenDialog(this);
	    File f = fileChooser.getSelectedFile();
	    if(f==null)return;
	    readSettingsFromFile(f);
	    readSettingsToPanel();
	}//end exportSettings()

	private void defaultSettings(){
	    JOptionPane.showMessageDialog(this, "Not yet implemented.");
	    //TODO
	    /*
	    try{BeanUtils.copyProperties(config, new TRConfiguration());}
	    catch(Exception e){e.printStackTrace();}
	    readSettingsToPanel();
	    */
	}

	private boolean checkFile(File f){
	    if(f.exists())
		return true;
	    JOptionPane.showMessageDialog(this, "The specified path could not be opened from disk:\n"+f.getAbsolutePath(), "File Not Found", JOptionPane.ERROR_MESSAGE);
	    return false;
	}//end checkFile

	private boolean checkPOD(File file){
	    if(!checkFile(file))
		return false;
	    try{new PodFile(file).getData();}
	    catch(FileLoadException e){
		JOptionPane.showMessageDialog(this, "Failed to parse the specified POD: \n"+e.getLocalizedMessage(), "POD failed format check.", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		return false;}
	    return true;
	}//end checkPOD(...)

	private boolean checkVOX(File file){
	    if (!checkFile(file))
		return false;
	    try {
		FileInputStream fis = new FileInputStream(file);
		new Parser().readToNewBean(fis, VOXFile.class);
		fis.close();
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(
			this,
			"Failed to parse the specified VOX: \n"
				+ e.getLocalizedMessage(),
				"VOX failed format check", JOptionPane.ERROR_MESSAGE);
		e.printStackTrace();
		return false;
	    }
	    return true;
	}//end checkVOX(...)

	@Override
	public void apply(TR target) {
	    setPodRegistry(Features.get(target, PODRegistry.class));
	    final TR tr;
	    setTr             (tr = Features.get(Features.getSingleton(), TR.class));
	    setTrConfigRoot   (Features.get(Features.getSingleton(), TRConfigRoot.class   ));
	    setTrConfiguration(Features.get(tr,                      TRConfiguration.class));
	    
	    final CollectionActionDispatcher<String> podRegistryCollection = getPodRegistry().getPodCollection();
	    podRegistryCollection.addTarget(podCollectionListener, true);
	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub

	}

	public void registerConfigTab(final ConfigurationTab configTab) {
	    final String     name    = configTab.getTabName();
	    final ImageIcon  icon    = configTab.getTabIcon();
	    final JComponent content = configTab.getContent();
	    try{
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    tabbedPane.addTab(name,icon,content);
		}});
	    }catch(Exception e){e.printStackTrace();}
	    //dispatchComponentConfigs();
	}//end registerConfigTab(...)

	public PODRegistry getPodRegistry() {
	    return podRegistry;
	}

	public void setPodRegistry(PODRegistry podRegistry) {
	    this.podRegistry = podRegistry;
	}
	
	private class PodCollectionListener implements List<String> {

	    @Override
	    public boolean add(final String element) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.addElement(element);
		    }});
		
		return true;//Meh.
	    }

	    @Override
	    public void add(final int index, final String element) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.add(index, element);
		    }});
	    }

	    @Override
	    public boolean addAll(final Collection<? extends String> arg0) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			for(String s : arg0 )
		    podLM.addElement(s);
		    }});
		
		return true;//Meh.
	    }

	    @Override
	    public boolean addAll(int arg0, Collection<? extends String> arg1) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public void clear() {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.clear();
		    }});
	    }

	    @Override
	    public boolean contains(Object arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public boolean containsAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public String get(int arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public int indexOf(Object arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public boolean isEmpty() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Iterator<String> iterator() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public int lastIndexOf(Object arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public ListIterator<String> listIterator() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public ListIterator<String> listIterator(int arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public boolean remove(final Object elementToRemove) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.removeElement(elementToRemove);
		    }});
		return true; //Meh.
	    }

	    @Override
	    public String remove(final int indexOfElementToRemove) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.remove(indexOfElementToRemove);
		    }});
		return null;//Meh.
	    }

	    @Override
	    public boolean removeAll(final Collection<?> elementsToRemove) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			boolean result = false;
		for(Object o : elementsToRemove)
		    result |= podLM.removeElement(o);
		    }});
		
		return true;//Meh.
	    }

	    @Override
	    public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public String set(final int index, final String element) {
		SwingUtilities.invokeLater(new Runnable(){
		    @Override
		    public void run() {
			podLM.set(index, element);
		    }});
		return null;//Meh.
	    }

	    @Override
	    public int size() {
		return podLM.size();
	    }

	    @Override
	    public List<String> subList(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Object[] toArray() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	    }
	    
	}//end PodCollectionListener

	public TR getTr() {
	    return tr;
	}

	public void setTr(TR tr) {
	    this.tr = tr;
	}

	public TRConfigRoot getTrConfigRoot() {
	    return trConfigRoot;
	}

	public void setTrConfigRoot(TRConfigRoot trConfigRoot) {
	    this.trConfigRoot = trConfigRoot;
	    final String cFilePath = trConfigRoot.getConfigSaveURI();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    lblConfigpath.setText(cFilePath);
		}});
	}//end setTrConfigRoot(...)

	public TRConfiguration getTrConfiguration() {
	    return trConfiguration;
	}

	public void setTrConfiguration(TRConfiguration trConfiguration) {
	    this.trConfiguration = trConfiguration;
	}
	
	public void notifyNeedRestart(){
	    needRestart = true;
	}

	@Override
	public void graphStabilized(Object target) {
	    if(getTrConfiguration() != null)
		readSettingsToPanel();
	}
    }//end ConfigWindow

@Override
public Feature<TR> newInstance(TR target) {
    final ConfigWindow result = new ConfigWindow();
    return result;
}

@Override
public Class<TR> getTargetClass() {
    return TR.class;
}

@Override
public Class<? extends Feature> getFeatureClass() {
    return ConfigWindow.class;
}
}//end ConfigWindowFactory