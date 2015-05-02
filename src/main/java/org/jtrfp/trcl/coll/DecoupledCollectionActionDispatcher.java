/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2015 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.coll;

import java.util.Collection;
import java.util.concurrent.Executor;

public class DecoupledCollectionActionDispatcher<E> extends
	CollectionActionDispatcher<E> {
    private final Executor executor;
    public DecoupledCollectionActionDispatcher(Collection<E> cache,Executor executor){
	super(cache);
	this.executor=executor;
    }
    
    public DecoupledCollectionActionDispatcher(Executor executor){
	super();
	this.executor=executor;
    }
    
    @Override
    public boolean addTarget(Collection<E> target, boolean prefill){
	return super.addTarget(new CollectionThreadDecoupler<E>(target, executor), prefill);
    }
    @Override
    public boolean removeTarget(Collection<E> targetToRemove, boolean removeAll){
	Collection<E> toRemove = null;
	for(Collection<E> thisTarget:targets)
	    if(((CollectionThreadDecoupler<E>)thisTarget).getDelegate().equals(targetToRemove))
		toRemove = thisTarget;
	if(toRemove!=null)
	 return super.removeTarget(toRemove, removeAll);
	return false;
    }//end removeTarget()

    public Executor getExecutor() {
	return executor;
    }

}//end DecoupledCollectionActionDispatcher
