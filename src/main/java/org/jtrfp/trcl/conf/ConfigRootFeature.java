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

package org.jtrfp.trcl.conf;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;
import java.beans.ExceptionListener;
import java.beans.Statement;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;

import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.FeatureConfigurationPrivilegeData;
import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.FeatureConfigurationPrivileges;
import org.jtrfp.trcl.conf.FeatureConfigurationPrivilegesFactory.PropertyKey;
import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;

import lombok.Getter;
import lombok.Setter;

public abstract class ConfigRootFeature<TARGET_CLASS> implements Feature<TARGET_CLASS> {
    public static final String CONFIG_SAVE_URI = "configSaveURI";
    private TARGET_CLASS target;
    private String configSaveURI = null;
    @Getter @Setter
    private int privilegeLevel = 0;
    
    @Override
    public void apply(TARGET_CLASS target){
	setTarget(target);
    }
    
    public void saveConfigurations() throws IOException {
	saveConfigurations(new File(getConfigSaveURI()));
    }
    
    public void saveConfigurations(File destFile) throws IOException{
	//First save data from all local configurators
	final FeatureTreeElement configurationTreeElement = new FeatureTreeElement.Default();
	configurationTreeElement.setFeatureClassName(getTarget().getClass().getName());
	configurationTreeElement.setPropertiesMap(null);
	final TARGET_CLASS target = getTarget();
	    @SuppressWarnings("unchecked")
	    Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap = Collections.EMPTY_MAP;
	    final FeatureConfigurationPrivileges fcp = Features.get(target, FeatureConfigurationPrivileges.class);
	    if( fcp != null )
		privMap = fcp.getPrivilegeData();
	saveConfigurationsOfTargetRecursive(getTarget(), configurationTreeElement, privMap, getPrivilegeLevel());
	final File temp = File.createTempFile("org.jtrfp.trcl.", "config.xml");
	//for(Configurator conf:configurators)
	    //configurations.put(conf.getConfiguredClass(),conf.storeToMap(new HashMap<String,Object>()));
	
	    FileOutputStream os = new FileOutputStream(temp);
	    XMLEncoder xmlEnc   = new XMLEncoder(os);
	    xmlEnc.setExceptionListener(new ExceptionListener(){
		@Override
		public void exceptionThrown(Exception e) {
		    e.printStackTrace();
		}});
	    xmlEnc.setPersistenceDelegate(DefaultListModel.class,
		    new DefaultPersistenceDelegate() {
			protected void initialize(Class<?> clazz,
				Object oldInst, Object newInst,
				Encoder out) {
			    super.initialize(clazz, oldInst, newInst,
				    out);
			    DefaultListModel<?> oldLM = (DefaultListModel<?>) oldInst;
			    //DefaultListModel<?> newLM = (DefaultListModel) newInst;
			    for (int i = 0; i < oldLM.getSize(); i++){
				final Object value = oldLM.getElementAt(i);
				if(value!=null)//When a DLM is initialized it contains a single null element. )X
				    out.writeStatement(new Statement(oldInst,"addElement",
					    new Object[] { value }));
			    }//end for(elements)
			}//end DefaultPersistenceDelegate()
	    });
	    xmlEnc.writeObject(configurationTreeElement);
	    xmlEnc.close();
	    //System.out.println("Successfully wrote config to temp file "+temp.getAbsolutePath());

	    FileChannel srcCh = null, dstCh = null;
	    FileInputStream fis = null;
	    FileOutputStream fos = null;
	    try {
		srcCh = (fis = new FileInputStream(temp)).getChannel();
		dstCh = (fos = new FileOutputStream(destFile)).getChannel();
		dstCh.transferFrom(srcCh, 0, srcCh.size());
		//System.out.println("Successfully wrote configuration to "+getConfigSaveURI());
	    }catch(Exception e){e.printStackTrace();}
	    
	    finally{
		if(srcCh != null)
		    srcCh.close();
		if(dstCh != null)
		    dstCh.close();
		if(fis != null)
		    fis.close();
		if(fos != null)
		    fos.close();
	    }
    }//end saveConfigurations()
    
    public void loadConfigurations(){
	loadConfigurations(new File(getConfigSaveURI()),getPrivilegeLevel());
    }
    
    public void loadConfigurations(final File fp, int privilegeLevel){
	FeatureTreeElement root = null;
	    if(fp.exists()){
		try{FileInputStream is = new FileInputStream(fp);
		XMLDecoder xmlDec = new XMLDecoder(is);
		xmlDec.setExceptionListener(new ExceptionListener(){
		    @Override
		    public void exceptionThrown(Exception ex) {
			ex.printStackTrace();
		    }});
		Object deserializedObject;
		try{deserializedObject = xmlDec.readObject();}
		catch(Exception e){deserializedObject = new FileEmptyOrMalformed();}
		//System.out.println("deserializedObject="+deserializedObject);
		if(deserializedObject instanceof FeatureTreeElement)
		    root = (FeatureTreeElement)deserializedObject;
		else {
		    System.err.println("WARNING: Loaded expected configuration map is of intolerable type "+deserializedObject.getClass().getName()+".");
		    System.err.println("Creating a new configuration instead. This could happen if there is a change in config parsing. If this happens repeatedly there may be a bug.");
		}//end if(invalid)
		xmlDec.close();
		is.close();
		}catch(Exception e){e.printStackTrace();}
	    }//end if(exists)
	if(root != null){
	    //System.out.println("loadConfigurations()");
	    final TARGET_CLASS target = getTarget();
	    @SuppressWarnings("unchecked")
	    Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap = Collections.EMPTY_MAP;
	    final FeatureConfigurationPrivileges fcp = Features.get(target, FeatureConfigurationPrivileges.class);
	    if( fcp != null )
		privMap = fcp.getPrivilegeData();
	 loadConfigurationsOfTargetRecursive(getTarget(), root, privMap, privilegeLevel);
	 }
    }//end loadConfigurations()
    
    private static class FileEmptyOrMalformed{}
    
    protected void saveConfigurationsOfTargetRecursive(Object target, FeatureTreeElement element, Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap, int privilegeLevel){
	final Set<Feature<TARGET_CLASS>> features = new HashSet<Feature<TARGET_CLASS>>();
	Features.getAllFeaturesOf(target, features);
	final ConfigRootFeature<TARGET_CLASS> configRootFeature = getConfigRootFeature(features);
	//This is a config root. Stop branching here.
	if(target != getTarget() && configRootFeature != null){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(configRootFeature.getClass().getName());
	    subElement.setPropertiesMap(new HashMap<String,Object>());
	    configRootFeature.notifyRecursiveSaveOperation(this,subElement.getPropertiesMap());
	    element.getSubFeatures().put(configRootFeature.getClass().getName(), subElement);
	    return;
	}
	for(Feature<?> feature:features){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(feature.getClass().getName());
	    if(feature instanceof FeatureConfigurator){
		@SuppressWarnings("unchecked")
		FeatureConfigurator<TARGET_CLASS> configurator = (FeatureConfigurator<TARGET_CLASS>)feature;
		subElement.setPropertiesMap(new HashMap<String,Object>());
		configurator.storeToMap(subElement.getPropertiesMap(), privMap, privilegeLevel);
	    }//end if(FeatureConfigurator)
	    element.getSubFeatures().put(feature.getClass().getName(),subElement);
	    saveConfigurationsOfTargetRecursive(feature, subElement, privMap, privilegeLevel);
	}//end for(features)
    }//end saveConfigurationsOfTargetRecursive()
    
    public void notifyRecursiveSaveOperation(
	    ConfigRootFeature<TARGET_CLASS> configManagerFeature, Map<String,Object> propertiesMap) {
	final String newConfigSaveURI = getConfigSaveURI();
	propertiesMap.put(CONFIG_SAVE_URI, newConfigSaveURI);
    }
    
    public void notifyRecursiveLoadOperation(ConfigRootFeature<TARGET_CLASS> configManagerFeature, Map<String,Object> propertiesMap){
	if(propertiesMap == null)
	    throw new IllegalStateException("propertiesMap intolerably null.");
	final String newSaveURI = (String)propertiesMap.get(CONFIG_SAVE_URI);
	System.out.println("loading new config save URI "+newSaveURI);
	setConfigSaveURI(newSaveURI);
    }

    public void loadConfigurationsOfTargetRecursive(Object target, FeatureTreeElement element, Map<PropertyKey, FeatureConfigurationPrivilegeData> privMap, int privilegeLevel){
	final Set<Feature<TARGET_CLASS>> features = new HashSet<Feature<TARGET_CLASS>>();
	Features.getAllFeaturesOf(target, features);
	//System.out.println("loadConfigurationsOfTargetRecursive "+target.getClass().getName());
	/*
	final ConfigManagerFeature cmf = getConfigManagerFeature(features);
	if(target != getTarget() && cmf != null){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(cmf.getClass().getName());
	    subElement.setPropertiesMap(new HashMap<String,Object>());
	    cmf.notifyRecursiveSaveOperation(this,subElement.getPropertiesMap());
	    return;
	}*/
	//System.out.println("FEATURE LIST: ");
	//for(Feature feature:features)
	//    System.out.println("\t"+feature.getClass().getName()+" "+feature.hashCode());
	for(Feature<TARGET_CLASS> feature:features){
	    final String featureClassName = feature.getClass().getName();
	    FeatureTreeElement subFeature = element.getSubFeatures().get(featureClassName);
	    //System.out.println("featureClassName="+featureClassName+" elementFeatureClassName="+element.getFeatureClassName());
	    //subElement.setFeatureClassName(feature.getClass().getName());
	    if(subFeature != null){
		Map<String,Object> propertiesMap = subFeature.getPropertiesMap();
		if(feature instanceof ConfigRootFeature){
		    //System.out.println(".... is ConfigRootFeature.");
		    ConfigRootFeature<TARGET_CLASS> configRootFeature = (ConfigRootFeature<TARGET_CLASS>)feature;
		    if(propertiesMap != null)
		     configRootFeature.notifyRecursiveLoadOperation(this, propertiesMap);
		} else if(feature instanceof FeatureConfigurator){
		    FeatureConfigurator<TARGET_CLASS> configurator = (FeatureConfigurator<TARGET_CLASS>)feature;
		    //subElement.setPropertiesMap(new HashMap<String,Object>());
		    //System.out.println("Feature is a Configurator. Applying map:");
		    //for(Entry<String,Object> entry : propertiesMap.entrySet())
			//System.out.println("\t"+entry.getKey()+" "+entry.getValue());
		    configurator.applyFromMap(propertiesMap, privMap, privilegeLevel);
		}//end if(FeatureConfigurator)
		loadConfigurationsOfTargetRecursive(feature, subFeature, privMap, privilegeLevel);
	    }//end if(subElement!=null)
	}//end for(features)
	//System.out.println("loadConfigOfTargetRecursive() traversing down from "+target.getClass().getName());
    }//end loadConfigurationsOfTargetRecursive
    
    protected ConfigRootFeature<TARGET_CLASS> getConfigRootFeature(Collection<Feature<TARGET_CLASS>> features){
	for(Feature<TARGET_CLASS> feature:features){
	    if(feature instanceof ConfigRootFeature)
		return (ConfigRootFeature<TARGET_CLASS>)feature;
	}
	return null;
    }//end containsConfigManagerFeature()
    
    public String getConfigSaveURI() {
	if(configSaveURI == null)
	    setConfigSaveURI(getDefaultSaveURI());
        return configSaveURI;
    }
    public void setConfigSaveURI(String configSaveURI) {
        this.configSaveURI = configSaveURI;
    }
    
    protected abstract String getDefaultSaveURI();
    public TARGET_CLASS getTarget() {
        return target;
    }
    public void setTarget(TARGET_CLASS target) {
        this.target = target;
    }
    
   public static interface FeatureTreeElement {
       public String getFeatureClassName();
       public void   setFeatureClassName(String featureClassName);
       public Map<String, Object> getPropertiesMap();
       public void   setPropertiesMap(Map<String, Object> propertiesMap);
       public Map<String,FeatureTreeElement> getSubFeatures();
       public void   setSubFeatures(Map<String,FeatureTreeElement> subFeatures);
       
       public static class Default implements FeatureTreeElement{
	   private String featureClassName;
	   private Map<String,Object> propertiesMap;
	   private Map<String,FeatureTreeElement> subFeatures;
	@Override
	public String getFeatureClassName() {
	    return featureClassName;
	}
	@Override
	public void setFeatureClassName(String featureClassName) {
	    this.featureClassName = featureClassName;
	}
	@Override
	public Map<String, Object> getPropertiesMap() {
	    return propertiesMap;
	}
	@Override
	public void setPropertiesMap(Map<String, Object> propertiesMap) {
	    this.propertiesMap = propertiesMap;
	}
	@Override
	public Map<String,FeatureTreeElement> getSubFeatures() {
	    if(subFeatures == null)
		subFeatures = new HashMap<String,FeatureTreeElement>();
	    return subFeatures;
	}
	@Override
	public void setSubFeatures(Map<String,FeatureTreeElement> subFeatures) {
	    this.subFeatures = subFeatures;
	}
       }//end Default
   }//end FeatureTreeElement
}//end ConfigManagerFeature
