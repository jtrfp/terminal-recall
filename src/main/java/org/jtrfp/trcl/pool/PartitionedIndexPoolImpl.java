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

package org.jtrfp.trcl.pool;

import java.beans.PropertyChangeListener;

import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableList;

public class PartitionedIndexPoolImpl<STORED_TYPE> implements
	PartitionedIndexPool<STORED_TYPE> {

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> newPartition() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePartition(
	    org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> toRemove)
	    throws NullPointerException, IllegalArgumentException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removeAllPartitions() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ListenableCollection<org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE>> getPartitions() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ListenableList<STORED_TYPE> getFlatEntries() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int getTotalUnusedIndices() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)
	    throws IllegalArgumentException {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior setTotalUnusedLimitBehavior(
	    org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior behavior) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.UnusedIndexLimitBehavior getTotalUnusedLimitBehavior() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.FlushBehavior<STORED_TYPE> setFlushBehavior(
	    org.jtrfp.trcl.pool.PartitionedIndexPool.FlushBehavior<STORED_TYPE> behavior) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.FlushBehavior<STORED_TYPE> getFlushBehavior() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public int flush() {
	// TODO Auto-generated method stub
	return 0;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    PropertyChangeListener l) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    PropertyChangeListener l) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean hasListeners(String propertyName) {
	// TODO Auto-generated method stub
	return false;
    }

}
