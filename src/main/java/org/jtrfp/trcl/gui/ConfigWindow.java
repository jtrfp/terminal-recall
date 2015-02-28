/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
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
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.beanutils.BeanUtils;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.file.VOXFile;

public class ConfigWindow extends JFrame {
    private TRConfiguration config;
    private JCheckBox chckbxLinearInterpolation;
    private JSlider modStereoWidthSlider;
    private JList podList,missionList;
    private DefaultListModel<String> podLM=new DefaultListModel<String>(), missionLM=new DefaultListModel<String>();
    private boolean needRestart=false;
    private final JFileChooser fileChooser = new JFileChooser();
    private final SoundOutputSelectorGUI soundOutputSelectorGUI;
    
    public static void main(String [] args){
	new ConfigWindow().setVisible(true);
    }//end main()
    
 public ConfigWindow(TRConfiguration config){
     this();
     this.config=config;
     readSettingsToPanel();
 }
 public ConfigWindow(){
 	setTitle("Settings");
 	setSize(340,540);
 	if(config==null)
 	    config=new TRConfiguration();
 	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 	getContentPane().add(tabbedPane, BorderLayout.CENTER);
 	
 	JPanel generalTab = new JPanel();
 	tabbedPane.addTab("General", null, generalTab, null);
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
 	addPodButton.setToolTipText("Add a POD to the registry to be considered when running a game.");
 	podListOpButtonPanel.add(addPodButton);
 	addPodButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		addPOD();
	    }});
 	
 	JButton removePodButton = new JButton("Remove");
 	removePodButton.setToolTipText("Remove a POD file from being considered when playing a game");
 	podListOpButtonPanel.add(removePodButton);
 	removePodButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		podLM.removeElement(podList.getSelectedValue());
	    }});
 	
 	
 	JButton podEditButton = new JButton("Edit...");
 	podEditButton.setToolTipText("Edit the selected POD path");
 	podListOpButtonPanel.add(podEditButton);
 	podEditButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
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
 	addVOXButton.setToolTipText("Add an external VOX file as a mission");
 	missionListOpButtonPanel.add(addVOXButton);
 	addVOXButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		addVOX();
	    }});
 	
 	final JButton removeVOXButton = new JButton("Remove");
 	removeVOXButton.setToolTipText("Remove the selected mission");
 	missionListOpButtonPanel.add(removeVOXButton);
 	removeVOXButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
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
		if(isBuiltinVOX(val)){
		    removeVOXButton.setEnabled(false);
		    editVOXButton.setEnabled(false);
		}else{
		    removeVOXButton.setEnabled(true);
		    editVOXButton.setEnabled(true);
		}
	    }});
 	
 	JPanel soundTab = new JPanel();
 	tabbedPane.addTab("Sound", null, soundTab, null);
 	GridBagLayout gbl_soundTab = new GridBagLayout();
 	gbl_soundTab.columnWidths = new int[]{0, 0};
 	gbl_soundTab.rowHeights = new int[]{0, 51, 132, 0, 0};
 	gbl_soundTab.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 	gbl_soundTab.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 	soundTab.setLayout(gbl_soundTab);
 	
 	chckbxLinearInterpolation = new JCheckBox("Linear Filtering");
 	GridBagConstraints gbc_chckbxLinearInterpolation = new GridBagConstraints();
 	gbc_chckbxLinearInterpolation.anchor = GridBagConstraints.WEST;
 	gbc_chckbxLinearInterpolation.insets = new Insets(0, 0, 5, 0);
 	gbc_chckbxLinearInterpolation.gridx = 0;
 	gbc_chckbxLinearInterpolation.gridy = 0;
 	soundTab.add(chckbxLinearInterpolation, gbc_chckbxLinearInterpolation);
 	chckbxLinearInterpolation.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent action) {
		needRestart=true;
	    }});
 	
 	JPanel modStereoWidthPanel = new JPanel();
 	FlowLayout flowLayout_2 = (FlowLayout) modStereoWidthPanel.getLayout();
 	flowLayout_2.setAlignment(FlowLayout.LEFT);
 	modStereoWidthPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "MOD Stereo Width", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 	GridBagConstraints gbc_modStereoWidthPanel = new GridBagConstraints();
 	gbc_modStereoWidthPanel.anchor = GridBagConstraints.NORTH;
 	gbc_modStereoWidthPanel.insets = new Insets(0, 0, 5, 0);
 	gbc_modStereoWidthPanel.fill = GridBagConstraints.HORIZONTAL;
 	gbc_modStereoWidthPanel.gridx = 0;
 	gbc_modStereoWidthPanel.gridy = 1;
 	soundTab.add(modStereoWidthPanel, gbc_modStereoWidthPanel);
 	
 	modStereoWidthSlider = new JSlider();
 	modStereoWidthSlider.setPaintTicks(true);
 	modStereoWidthSlider.setMinorTickSpacing(25);
 	modStereoWidthPanel.add(modStereoWidthSlider);
 	
 	final JLabel modStereoWidthLbl = new JLabel("NN%");
 	modStereoWidthPanel.add(modStereoWidthLbl);
 	
 	soundOutputSelectorGUI = new SoundOutputSelectorGUI();
 	soundOutputSelectorGUI.setBorder(new TitledBorder(null, "Output Driver", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 	GridBagConstraints gbc_soundOutputSelectorGUI = new GridBagConstraints();
 	gbc_soundOutputSelectorGUI.anchor = GridBagConstraints.NORTH;
 	gbc_soundOutputSelectorGUI.insets = new Insets(0, 0, 5, 0);
 	gbc_soundOutputSelectorGUI.fill = GridBagConstraints.HORIZONTAL;
 	gbc_soundOutputSelectorGUI.gridx = 0;
 	gbc_soundOutputSelectorGUI.gridy = 2;
 	soundTab.add(soundOutputSelectorGUI, gbc_soundOutputSelectorGUI);
 	
 	modStereoWidthSlider.addChangeListener(new ChangeListener(){
	    @Override
	    public void stateChanged(ChangeEvent arg0) {
		modStereoWidthLbl.setText(modStereoWidthSlider.getValue()+"%");
		needRestart=true;
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
		applySettings();
		ConfigWindow.this.setVisible(false);
	    }
 	});
 	
 	JButton btnCancel = new JButton("Cancel");
 	btnCancel.setToolTipText("Close the window without applying settings");
 	okCancelPanel.add(btnCancel, BorderLayout.EAST);
 	
 	JLabel lblConfigpath = new JLabel(TRConfiguration.getConfigFilePath().getAbsolutePath());
 	lblConfigpath.setIcon(null);
 	lblConfigpath.setToolTipText("Default config file path");
 	lblConfigpath.setHorizontalAlignment(SwingConstants.CENTER);
 	lblConfigpath.setFont(new Font("Dialog", Font.BOLD, 6));
 	okCancelPanel.add(lblConfigpath, BorderLayout.CENTER);
 	btnCancel.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		ConfigWindow.this.setVisible(false);
	    }});
 	}//end constructor
 
 private void applySettings(){
     config.setVoxFile((String)missionList.getSelectedValue());
     config.setModStereoWidth((double)modStereoWidthSlider.getValue()/100.);
     config.setAudioLinearFiltering(chckbxLinearInterpolation.isSelected());
     {HashSet<String>pList=new HashSet<String>();
     for(int i=0; i<podLM.getSize();i++)
	 pList.add((String)podLM.getElementAt(i));
     config.getPodList().clear();
     for(String pod:pList)
	 config.getPodList().addElement(pod);
     
     HashSet<String>vxList=new HashSet<String>();
     for(int i=0; i<missionLM.getSize();i++)
	 vxList.add((String)missionLM.getElementAt(i));
     config.setMissionList(vxList);}
     soundOutputSelectorGUI.applySettings(config);
     writeSettingsTo(TRConfiguration.getConfigFilePath());
     if(needRestart)
	 notifyOfRestart();
 }//end applySettings()
 
 private void readSettingsToPanel(){
     modStereoWidthSlider.setValue((int)(config.getModStereoWidth()*100.));
     chckbxLinearInterpolation.setSelected(config.isAudioLinearFiltering());
     
     missionLM.removeAllElements();
     for(String vox:config.getMissionList()){
	 if(isBuiltinVOX(vox))
	     missionLM.addElement(vox);
	 else if(checkVOX(new File(vox)))
	     missionLM.addElement(vox);
     }//end for(vox)
     String missionSelection = config.getVoxFile();
     for(int i=0; i<missionLM.getSize(); i++){
	 if(missionLM.get(i).contentEquals(missionSelection))missionList.setSelectedIndex(i);}
     
     podLM.removeAllElements();
     final DefaultListModel<String>podList = config.getPodList();
     for(int i=0; i<podList.size();i++){
	 final String pod = podList.get(i);
	 if(pod!=null)
	  if(checkPOD(new File(pod)))
	   podLM.addElement(pod);}
     soundOutputSelectorGUI.readToPanel(config);
 }//end readSettings()
 
 private boolean isBuiltinVOX(String vox){
     return vox.contentEquals(TRConfiguration.AUTO_DETECT) || 
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
     podLM.addElement(file.getAbsolutePath());
 }//end addPOD()
 
    private void addVOX() {
	fileChooser.setFileFilter(new FileFilter() {
	    @Override
	    public boolean accept(File file) {
		return file.getName().toUpperCase().endsWith(".VOX")||file.isDirectory();
	    }

	    @Override
	    public String getDescription() {
		return "Terminal Reality .POD files";
	    }
	});
	if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
	    return;
	final File file = fileChooser.getSelectedFile();
	if(file!=null)
 	  missionLM.addElement(file.getAbsolutePath());
    }//end addVOX()
    
    private boolean writeSettingsTo(File f){
	try{
	    config.saveConfig(f);
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
	final String result = JOptionPane.showInputDialog(this, "Edit VOX Path", missionLM.get(missionList.getSelectedIndex()));
	if(checkVOX(new File(result)))
	    missionLM.set(missionList.getSelectedIndex(), result);
    }//end editVOXPath()
    
    private void editPODPath(){
	final String result = JOptionPane.showInputDialog(this, "Edit POD Path", podLM.get(missionList.getSelectedIndex()));
	if(checkPOD(new File(result)))
	    podLM.set(podList.getSelectedIndex(), result);
    }//end editVOXPath()
    
    private boolean readSettingsFromFile(File f){
	try{FileInputStream is = new FileInputStream(f);
	    XMLDecoder xmlDec = new XMLDecoder(is);
	    xmlDec.setExceptionListener(new ExceptionListener(){
		@Override
		public void exceptionThrown(Exception e) {
		    e.printStackTrace();
		}});
	    TRConfiguration src =(TRConfiguration)xmlDec.readObject();
	    xmlDec.close();
	    if(config!=null)
		BeanUtils.copyProperties(config, src);
	    else config=src;
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
	 try{BeanUtils.copyProperties(config, new TRConfiguration());}
	catch(Exception e){e.printStackTrace();}
	readSettingsToPanel();
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
}//end ConfigWindow
