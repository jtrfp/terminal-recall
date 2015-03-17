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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.NoSuchElementException;

import com.ochafik.util.listenable.DefaultListenableCollection;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableCollections;
import com.ochafik.util.listenable.ListenableList;

public class PartitionedIndexPoolImpl<STORED_TYPE> implements
	PartitionedIndexPool<STORED_TYPE> {
	private final ListenableList<Partition<STORED_TYPE>> partitions 
	    = ListenableCollections.asList(new DefaultListenableCollection<Partition<STORED_TYPE>>(new ArrayList<Partition<STORED_TYPE>>()));
	private final ListenableCollection<Partition<STORED_TYPE>> partitionsRO = ListenableCollections.unmodifiableCollection(partitions);
	private final ListenableList<Entry<STORED_TYPE>> flatEntries 
	    = ListenableCollections.asList(new DefaultListenableCollection<Entry<STORED_TYPE>>(new ArrayList<Entry<STORED_TYPE>>()));
	private final ListenableList<Entry<STORED_TYPE>> flatEntriesRO = ListenableCollections.unmodifiableList(flatEntries);
	private final Deque<Integer>unusedIndices = new ArrayDeque<Integer>();
	private int numUsedIndices=0;
	
    @Override
    public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> newPartition() {
	Partition<STORED_TYPE> result;
	//result = new PartitionImpl<STORED_TYPE>(this);//TODO
	result = null;
	partitions.add(result);
	return result;
    }
    
    private int newIndex(){
	int result = 0;
	try{result = unusedIndices.removeFirst();}
	catch(NoSuchElementException e){
	    result = numUsedIndices;
	}
	numUsedIndices++;
	return result;
    }//end newIndex()
    
    private void releaseIndex(int index){
	unusedIndices.add(index);
	numUsedIndices--;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePartition(
	    org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> toRemove)
	    throws NullPointerException, IllegalArgumentException {
	if(toRemove==null)
	    throw new NullPointerException();
	if(!partitions.contains(toRemove))
	    throw new IllegalArgumentException();
	toRemove.removeAllEntries();
	partitions.remove(toRemove);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removeAllPartitions() {
	for(Partition<STORED_TYPE> partition: partitions)
	    partition.remove();
	return this;
    }

    @Override
    public ListenableCollection<org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE>> getPartitions() {
	return partitionsRO;
    }

    @Override
    public ListenableList<Entry<STORED_TYPE>> getFlatEntries() {
	return flatEntriesRO;
    }

    @Override
    public int getTotalUnusedIndices() {
	return unusedIndices.size();
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)
	    throws IllegalArgumentException {
	return this;//TODO
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
