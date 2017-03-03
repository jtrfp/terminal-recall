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

package org.jtrfp.trcl.ctl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jtrfp.trcl.core.Feature;
import org.jtrfp.trcl.core.FeatureFactory;
import org.jtrfp.trcl.core.FeatureNotApplicableException;
import org.jtrfp.trcl.core.Features;
import org.jtrfp.trcl.ctl.ControllerMapperFactory.ControllerMapper;
import org.jtrfp.trcl.ctl.ControllerSinksFactory.ControllerSinks;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration;
import org.jtrfp.trcl.gui.ControllerInputDevicePanel.ControllerConfiguration.ConfEntry;
import org.springframework.stereotype.Component;

@Component
public class ControllerConfigSupportImplFactory
	implements FeatureFactory<ControllerMapper> {

    @Override
    public Feature<ControllerMapper> newInstance(ControllerMapper target)
	    throws FeatureNotApplicableException {
	return new ControllerConfigSupportImpl();
    }

    @Override
    public Class<ControllerMapper> getTargetClass() {
	return ControllerMapper.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return ControllerConfigSupportImpl.class;
    }
    
    public static class ControllerConfigSupportImpl implements Feature<ControllerMapper>{
	//// PROPERTIES
	public static final String CONFIG_BEANS = "configBeans";//TODO: Move this to an interface
	
	private ControllerMapper target;

	@Override
	public void apply(ControllerMapper target) {
	    setTarget(target);
	    //This makes sure defaults are loaded even if no config is set
	    setConfigBeans(new ArrayList<ControllerMapBean>());
	}

	@Override
	public void destruct(ControllerMapper target) {
	    this.target = null;
	}
	
	public Collection<ControllerMapBean> getConfigBeans(){
	    final ControllerMapper target = getTarget(); //Cache
	    if(target == null)
		return null;//If this is being used as a default, target will be null.
	    System.out.println("getConfigBeans()");
	    final Map<ControllerSource, ControllerMapping> routingMap = target.getRoutingMap();
	    final Collection<InputDevice>    inputDevs = target.getInputDevices();
	    final Collection<ControllerMapBean> result = new ArrayList<>(60);
	    for( InputDevice dev:inputDevs )
		for( ControllerSource src:dev.getControllerSources() ){
		    final ControllerMapping controllerMapping = routingMap.get(src);
		    if( controllerMapping != null )
		        result.add(new ControllerMapBean(controllerMapping));
		    }//end for(getControllerSources())
	    return result;
	}//end getConfigBeans()
	
	public void setConfigBeans(Collection<ControllerMapBean> beans){
	    removeAllMappings();
	    final ControllerMapper                target = getTarget(); //Cache
	    final ControllerSinks        controllerSinks = getControllerSinks();
	    final Collection<String> registeredSinkNames = controllerSinks.getSinkNames();
	    final Collection<InputDevice>      inputDevs = target.getInputDevices();
	    final Set<String>          beanSourceDevices = new HashSet<String>();
	    for(ControllerMapBean bean:beans)
		beanSourceDevices.add(bean.getSourceDevice());
	    System.out.println("setConfigBeans "+beans.size()+" beanSourceDevices="+beanSourceDevices.size()+" inputDevs="+inputDevs.size());
	    //Apply saved conf, default confs or fallback if not available
	    for( InputDevice inputDev : inputDevs ){
		final ControllerConfiguration defaultConfig = target.getRecommendedDefaultConfiguration(inputDev);
		for( ControllerSource controllerSource : inputDev.getControllerSources() ){
		    //Check for an entry in the bean
		    final ControllerMapBean mapBean = getMapBeanForSource(controllerSource, beans);
		    System.out.println("mapBean for "+controllerSource.getName()+" "+mapBean+" defaultConfig="+defaultConfig);
		    if(mapBean == null){
			//Skip
		    } else{ //mapBean != null  (we have a saved config)
			target.mapControllerSourceToInput(
				    controllerSource, 
				    controllerSinks.getSink(mapBean.getSinkName()), 
				    mapBean.getScalar(),
				    mapBean.getOffset());
			}//end saved-config mapping
		}//end for( controllerSources )
	    }//end for(inputDevs)
	}//end setConfigBeans()
	
	private static ControllerMapBean getMapBeanForSource(ControllerSource controllerSource, Collection<ControllerMapBean> beans){
	    final String inputDevName = controllerSource.getInputDevice().getName();
	    final String inputName    = controllerSource.getName();
	    for(ControllerMapBean controllerMapBean : beans){
		if(     controllerMapBean.getSourceDevice().equals(inputDevName) &&
			controllerMapBean.getSourceName().equals(inputName))
		    return controllerMapBean;
	    }//end for(beans)
	    return null;
	}//end getMapBeanForSource()
	
	private ControllerSinks getControllerSinks(){
	    return Features.get(target, ControllerSinks.class);
	}//end getControllerSinks()
	
	private void removeAllMappings(){
	    final ControllerMapper         target = getTarget(); //Cache
	    final Set<Entry<ControllerSource,ControllerMapping>> entrySet = target.getRoutingMap().entrySet();
	    final ArrayList<Entry<ControllerSource,ControllerMapping>> entryList = new ArrayList<>(entrySet);
	    for(Entry<ControllerSource,ControllerMapping> entry : entryList)
		target.unmapControllerSource(entry.getKey());
	}//end removeAllMappings()
	
	public static class ControllerMapBean {
	    private String  sourceDevice, sourceName, sinkName;
	    private double  scalar, offset;
	    private Integer hashCode;
	    
	    public ControllerMapBean(){}
	    public ControllerMapBean(ControllerMapping mapping){
		ControllerSink  sink = mapping.getControllerSink();
		ControllerSource src = mapping.getControllerSource();
		setSinkName    (sink.getName());
		setSourceDevice(src .getInputDevice().getName());
		setSourceName  (src .getName());
		setScalar(mapping.getScale());
		setOffset(mapping.getOffset());
	    }//end constructor
	    
	    public String getSourceDevice() {
	        return sourceDevice;
	    }
	    public void setSourceDevice(String sourceDevice) {
	        this.sourceDevice = sourceDevice;
	    }
	    public String getSourceName() {
	        return sourceName;
	    }
	    public void setSourceName(String sourceName) {
	        this.sourceName = sourceName;
	    }
	    public String getSinkName() {
	        return sinkName;
	    }
	    public void setSinkName(String sinkName) {
	        this.sinkName = sinkName;
	    }
	    public double getScalar() {
	        return scalar;
	    }
	    public void setScalar(double scalar) {
	        this.scalar = scalar;
	    }
	    public double getOffset() {
	        return offset;
	    }
	    public void setOffset(double offset) {
	        this.offset = offset;
	    }
	    
	    @Override
	    public int hashCode(){
		if(hashCode == null)
		    hashCode = 
		        sourceDevice.hashCode() +
		        sourceName.hashCode() >> 8 +
		        sinkName.hashCode() >> 16 +
		        (int)(scalar*127) >> 24 +
		        (int)(offset*127);
		return hashCode;
	    }//end hashCode
	}//end ControllerMapBean

	public ControllerMapper getTarget() {
	    return target;
	}

	public void setTarget(ControllerMapper target) {
	    this.target = target;
	}
	
    }//end ControllerMApperSerializationSupport
}//end ControllerMapperSerializationSupportFactory
