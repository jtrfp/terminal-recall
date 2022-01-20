/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
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

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.jtrfp.jtrfp.FileLoadException;
import org.jtrfp.jtrfp.pod.IPodData;
import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.coll.BidiReferenceMap;
import org.jtrfp.trcl.coll.CachedAdapter;
import org.jtrfp.trcl.coll.CollectionActionDispatcher;
import org.jtrfp.trcl.coll.CollectionActionPrinter;
import org.jtrfp.trcl.shell.GameCampaignData;

public class GameResourcePODRegistry implements PODRegistry {
    private CollectionActionDispatcher<String> podCAD = new CollectionActionDispatcher<>(new HashSet<>());
    private final CachedAdapter<String,IPodData> podCache;
    private final CollectionActionPrinter<String> podCollectionPrinter = new CollectionActionPrinter<String>("podCollection "+hashCode()+" ", new ArrayList<String>());
    
    public GameResourcePODRegistry(GameCampaignData data) {
	podCAD.addAll(data.getPodURIs());
	podCache = new CachedAdapter<String,IPodData>(new BidiReferenceMap<String,IPodData>(ReferenceStrength.SOFT,ReferenceStrength.SOFT, 64,.75f,true)){

		@Override
		protected IPodData _adapt(String path)
			throws UnsupportedOperationException {
		    final PodFile podFile = new PodFile(new File(path));
		    try{return podFile.getData();}
		    catch (FileLoadException e) {
			final String message = "Failed to parse (understand) POD file/path " + path;
			System.err.println(message);
			throw new UnsupportedOperationException();
		    }//end catch(...)
		}//end _adapt()

		@Override
		protected String _reAdapt(IPodData value)
			throws UnsupportedOperationException {
		    throw new UnsupportedOperationException();
		}};
		
		podCAD.addTarget(podCollectionPrinter, false);
    }//end constructor

    @Override
    public CollectionActionDispatcher<String> getPodCollection() {
	return podCAD;
    }

    @Override
    public IPodData getPodData(String path) {
	return podCache.adapt(path);
    }

}//end GameResourcePODRegistry
