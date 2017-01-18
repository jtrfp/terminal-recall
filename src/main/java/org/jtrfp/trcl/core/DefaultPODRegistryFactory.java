/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2016-2017 Chuck Ritola
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.coll.BidiReferenceMap;
import org.jtrfp.trcl.coll.CachedAdapter;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionPrinter;
import org.jtrfp.trcl.core.TRFactory.TR;
import org.jtrfp.trcl.gui.RootWindowFactory.RootWindow;
import org.springframework.stereotype.Component;

@Component
public class DefaultPODRegistryFactory implements FeatureFactory<TR> {
    public static class DefaultPODRegistry implements PODRegistry, Feature<TR> {
	private final CollectionActionDispatcher<String> podCollection = new CollectionActionDispatcher<String>( new HashSet<String>());
	private final CachedAdapter<String,IPodData> podCache;
	private RootWindow rootWindow;
	private final CollectionActionPrinter<String> podCollectionPrinter = new CollectionActionPrinter<String>("podCollection "+hashCode()+" ", new ArrayList<String>());
	private TR target;

	public DefaultPODRegistry(){
	    podCache = new CachedAdapter<String,IPodData>(new BidiReferenceMap<String,IPodData>(ReferenceStrength.SOFT,ReferenceStrength.SOFT, 64,.75f,true)){

		@Override
		protected IPodData _adapt(String path)
			throws UnsupportedOperationException {
		    final PodFile podFile = new PodFile(new File(path));
		    try{return podFile.getData();}
		    catch (FileLoadException e) {
			final String message = "Failed to parse (understand) POD file/path " + path;
			final RootWindow rootWindow = getRootWindow();
			if( rootWindow == null ){
			    System.err.println(message);
			    throw new UnsupportedOperationException();
			}
			JOptionPane.showMessageDialog(getRootWindow(),
				"Failed to parse (understand) POD file " + path,
				"Parsing failure", JOptionPane.ERROR_MESSAGE);
			throw new UnsupportedOperationException();
		    }//end catch(...)
		}//end _adapt()

		@Override
		protected String _reAdapt(IPodData value)
			throws UnsupportedOperationException {
		    throw new UnsupportedOperationException();
		}};
		
		podCollection.addTarget(podCollectionPrinter, false);
	}//end constructor
	
	@Override
	public void apply(TR target) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void destruct(TR target) {
	    // TODO Auto-generated method stub

	}

	public CollectionActionDispatcher<String> getPodCollection() {
	    return podCollection;
	}

	public String [] getPodsAsArray(){
	    return podCollection.toArray( new String [podCollection.size()] );
	}

	public void setPodsAsArray(String [] newPodsAsArray){
	    podCollection.clear();
	    for(String newPod : newPodsAsArray)
		podCollection.add(newPod);
	}//end setPodsAsArray

	@Override
	public IPodData getPodData(String path) {
	    return podCache.adapt(path);
	}

	public RootWindow getRootWindow() {
	    if( rootWindow == null )
		rootWindow = Features.get(getTarget(),RootWindow.class);
	    return rootWindow;
	}

	public void setRootWindow(RootWindow rootWindow) {
	    this.rootWindow = rootWindow;
	}

	public TR getTarget() {
	    return target;
	}

	public void setTarget(TR target) {
	    this.target = target;
	}

    }//end PODRegistry

    @Override
    public Feature<TR> newInstance(TR target) throws FeatureNotApplicableException {
	final DefaultPODRegistry result = new DefaultPODRegistry();
	result.setTarget(target);
	return result;
    }

    @Override
    public Class<TR> getTargetClass() {
	return TR.class;
    }

    @Override
    public Class<? extends Feature> getFeatureClass() {
	return DefaultPODRegistry.class;
    }
}//end PODRegistryFactory
