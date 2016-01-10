/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.core;

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
import java.util.Map;

import javax.swing.DefaultListModel;

import org.jtrfp.trcl.conf.Configurator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
    private TRConfiguration config;
    private Collection<Configurator> configurators = Collections.EMPTY_LIST;
    private final Map<Class,Map<String,Object>> configurations = new HashMap<Class,Map<String,Object>>(); 
    private TRConfiguration trConfiguration;

    public TRConfiguration getConfig(){
	if(config==null)
	    config=getConfigurations();
	return config;
    }//end getConfig()
    
    @Autowired
    public void setConfigurators(Collection<Configurator> configurators){
	this.configurators=configurators;
    }//end setPersistents
    

    public File getConfigFilePath(){
	String homeProperty = System.getProperty("user.home");
	if(homeProperty==null)homeProperty="";
	return new File(homeProperty+File.separator+"settings.config.trcl.xml");
    }//end getConfigFilePath()
    
    public void saveConfigurations() throws IOException {
	saveConfigurations(getConfigFilePath());
    }//end saveConfigurations()
    
    public TRConfiguration getConfigurations() {
	File fp = getConfigFilePath();
	configurations.clear();
	if(fp.exists()){
	    try{FileInputStream is = new FileInputStream(fp);
	    XMLDecoder xmlDec = new XMLDecoder(is);
	    final Object deserializedObject = xmlDec.readObject();
	    if(deserializedObject instanceof Map)
	     configurations.putAll((Map<Class,Map<String,Object>>)deserializedObject);
	    else {
		System.err.println("WARNING: Loaded expected configuration map is of intolerable type "+deserializedObject.getClass().getName()+".");
		System.err.println("Creating a new configuration instead. This could happen if there is a change in config parsing. If this happens repeatedly there may be a bug.");
		}//end if(invalid)
	    xmlDec.close();
	    is.close();
	    }catch(Exception e){e.printStackTrace();}
	}//end if(exists)
	//Apply configurations
	System.out.println("Iterate between "+configurators.size()+" configurators...");
	for(Configurator configurator : configurators ){
	    final Class configuredClass = configurator.getConfiguredClass();
	    final Map<String,Object> properties = configurations.get(configuredClass);
	    System.out.println("configurator for "+configurator.getConfiguredClass().getName()+" properties = "+properties);
	    if(properties != null)
	     configurator.applyFromMap(properties);
	    }//end for(configurators)
	return getTrConfiguration();
    }//end getConfigurations()
    
    public void saveConfigurations(File finalDest) throws IOException {
	final File temp = File.createTempFile("temp.org.trcl.", "config.xml");
	for(Configurator conf:configurators)
	    configurations.put(conf.getConfiguredClass(),conf.storeToMap(new HashMap<String,Object>()));
	
	    FileOutputStream os = new FileOutputStream(temp);
	    XMLEncoder xmlEnc   = new XMLEncoder(os);
	    xmlEnc.setExceptionListener(new ExceptionListener(){
		@Override
		public void exceptionThrown(Exception e) {
		    e.printStackTrace();
		}});
	    xmlEnc.setPersistenceDelegate(DefaultListModel.class,
		    new DefaultPersistenceDelegate() {
			protected void initialize(Class clazz,
				Object oldInst, Object newInst,
				Encoder out) {
			    super.initialize(clazz, oldInst, newInst,
				    out);
			    DefaultListModel oldLM = (DefaultListModel) oldInst;
			    DefaultListModel newLM = (DefaultListModel) newInst;
			    for (int i = 0; i < oldLM.getSize(); i++){
				final Object value = oldLM.getElementAt(i);
			    	if(value!=null)//When a DLM is initialized it contains a single null element. )X
				 out.writeStatement(new Statement(oldInst,"addElement",
					new Object[] { value }));
			    }//end for(elements)
			}//end DefaultPersistenceDelegate()
		    });
	    xmlEnc.writeObject(configurations);
	    xmlEnc.close();
	    
	    FileChannel srcCh = null, dstCh = null;
	    try {
	        srcCh = new FileInputStream(temp).getChannel();
	        dstCh = new FileOutputStream(finalDest).getChannel();
	        dstCh.transferFrom(srcCh, 0, srcCh.size());
	       }catch(Exception e){e.printStackTrace();}
	    	finally{
	           srcCh.close();
	           dstCh.close();
	       }
    }//end saveConfiguration()
    
    public void setConfiguration(Class clazz, Map<String,Object> properties){
	configurations.put(clazz, properties);
    }

    public TRConfiguration getTrConfiguration() {
        return trConfiguration;
    }

    @Autowired
    public void setTrConfiguration(TRConfiguration trConfiguration) {
        this.trConfiguration = trConfiguration;
    }
}//end ConfigManager
