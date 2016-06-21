/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016 Chuck Ritola
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.Features;

public abstract class ConfigRootFeature<TARGET_CLASS> implements Feature<TARGET_CLASS> {
    public static final String CONFIG_SAVE_URI = "configSaveURI";
    private TARGET_CLASS target;
    @Override
    public void apply(TARGET_CLASS target){
	setTarget(target);
    }
    private String configSaveURI = null;
    public void saveConfigurations(){
	//First save data from all local configurators
	final FeatureTreeElement configurationTreeElement = new FeatureTreeElement.Default();
	saveConfigurationsOfTargetRecursive(getTarget(), configurationTreeElement);
	//TODO :Write to file
    }//end saveConfigurations()
    
    public void loadConfigurations(){
	final FeatureTreeElement root = null;//TODO: Load from file
	loadConfigurationsOfTargetRecursive(getTarget(), root);
    }//end loadConfigurations()
    
    protected void saveConfigurationsOfTargetRecursive(Object target, FeatureTreeElement element){
	final ArrayList<Feature> features = new ArrayList<Feature>();
	Features.getAllFeaturesOf(target, features);
	final ConfigRootFeature cmf = getConfigManagerFeature(features);
	if(target != getTarget() && cmf != null){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(cmf.getClass().getName());
	    subElement.setPropertiesMap(new HashMap<String,Object>());
	    cmf.notifyRecursiveSaveOperation(this,subElement.getPropertiesMap());
	    return;
	}
	for(Feature feature:features){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(feature.getClass().getName());
	    if(feature instanceof FeatureConfigurator){
		FeatureConfigurator configurator = (FeatureConfigurator)feature;
		subElement.setPropertiesMap(new HashMap<String,Object>());
		configurator.storeToMap(subElement.getPropertiesMap());
	    }//end if(FeatureConfigurator)
	    element.getSubFeatures().put(feature.getClass().getName(),subElement);
	    saveConfigurationsOfTargetRecursive(feature, subElement);
	}//end for(features)
    }//end saveConfigurationsOfTargetRecursive()
    
    public void notifyRecursiveSaveOperation(
	    ConfigRootFeature<TARGET_CLASS> configManagerFeature, Map<String,Object> propertiesMap) {
	propertiesMap.put(CONFIG_SAVE_URI, getConfigSaveURI());
    }
    
    public void notifyRecursiveLoadOperation(ConfigRootFeature<TARGET_CLASS> configManagerFeature, Map<String,Object> propertiesMap){
	setConfigSaveURI((String)propertiesMap.get(CONFIG_SAVE_URI));
    }

    public void loadConfigurationsOfTargetRecursive(Object target, FeatureTreeElement element){
	final ArrayList<Feature> features = new ArrayList<Feature>();
	Features.getAllFeaturesOf(target, features);
	/*
	final ConfigManagerFeature cmf = getConfigManagerFeature(features);
	if(target != getTarget() && cmf != null){
	    FeatureTreeElement subElement = new FeatureTreeElement.Default();
	    subElement.setFeatureClassName(cmf.getClass().getName());
	    subElement.setPropertiesMap(new HashMap<String,Object>());
	    cmf.notifyRecursiveSaveOperation(this,subElement.getPropertiesMap());
	    return;
	}*/
	for(Feature feature:features){
	    final String featureClassName = feature.getClass().getName();
	    FeatureTreeElement subElement = element.getSubFeatures().get(featureClassName);
	    //subElement.setFeatureClassName(feature.getClass().getName());
	    if(subElement != null){
		Map<String,Object> propertiesMap = subElement.getPropertiesMap();
		if(feature instanceof ConfigRootFeature){
		    ConfigRootFeature cmf = (ConfigRootFeature)feature;
		    cmf.notifyRecursiveLoadOperation(this, propertiesMap);
		} else if(feature instanceof FeatureConfigurator){
		    FeatureConfigurator configurator = (FeatureConfigurator)feature;
		    //subElement.setPropertiesMap(new HashMap<String,Object>());
		    configurator.applyFromMap(propertiesMap);
		}//end if(FeatureConfigurator)
		loadConfigurationsOfTargetRecursive(feature, subElement);
	    }//end if(subElement!=null)
	}//end for(features)
    }//end loadConfigurationsOfTargetRecursive
    
    protected ConfigRootFeature getConfigManagerFeature(Collection<Feature> features){
	for(Feature feature:features){
	    if(feature instanceof ConfigRootFeature)
		return (ConfigRootFeature)feature;
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
