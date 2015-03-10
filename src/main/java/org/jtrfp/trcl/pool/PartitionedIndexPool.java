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

public interface PartitionedIndexPool<STORED_TYPE> {
    //// BEAN PROPERTIES
    public static final String TOT_UNUSED_INDICES     = "totalUnusedIndices",
	                       UNUSED_LIMIT_BEHAVIOR  = "unusedLimitBehavior",
	                       BACKEND                = "backend";
    
    public Partition           <STORED_TYPE> newPartition();
    public PartitionedIndexPool<STORED_TYPE> removePartition(Partition<STORED_TYPE> toRemove);
    public PartitionedIndexPool<STORED_TYPE> removeAllPartitions();
    public ListenableCollection<Partition<STORED_TYPE>> 
                                        getPartitions();
    public int                          getTotalUnusedIndices();
    public PartitionedIndexPool<STORED_TYPE>
                                        defragment(int maxNumUnusedIndices)         throws IllegalArgumentException;
    public UnusedIndexLimitBehavior     setTotalUnusedLimitBehavior(UnusedIndexLimitBehavior mode) throws NullPointerException;
    public UnusedIndexLimitBehavior     getTotalUnusedLimitBehavior();
    public Backend<STORED_TYPE>         setBackend(Backend<STORED_TYPE> newBackend) throws NullPointerException;
    
    //// PROPERTY CHANGE SUPPORT
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PropertyChangeListener[]     getPropertyChangeListeners();
    public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
    public boolean                      hasListeners(String propertyName);
    
    public static interface UnusedIndexLimitBehavior{
	public void proposeDefragmentation(PartitionedIndexPool<?> poolToCheck)                throws NullPointerException;
	public void proposeDefragmentation(PartitionedIndexPool.Partition<?> partitionToCheck) throws NullPointerException;
    }//end UnusedIndexLimitBehavior
    
    public interface Partition<STORED_TYPE>{
	//// BEAN PROPERTIES
	public static final String GLOBAL_START_INDEX     ="globalStartIndex",
		                   UNUSED_LIMIT_BEHAVIOR  ="unusedLimitBehavior",
		                   LENGTH_INDICES         ="lengthInIndices",
		                   VALID                  ="valid";
	
	public PartitionedIndexPool<STORED_TYPE> 
	                                    getParent();
	public Entry               <STORED_TYPE> newEntry(STORED_TYPE value);
	public Partition           <STORED_TYPE> remove();
	public Entry               <STORED_TYPE> removeEntry(Entry<STORED_TYPE> entry)           throws NullPointerException;
	public Partition           <STORED_TYPE> removeAllEntries();
	public ListenableCollection<Entry<STORED_TYPE>> 
	                                    getEntries();
	public int                          getGlobalStartIndex();
	public int                          getLengthInIndices();
	public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)              throws IllegalArgumentException;
	public UnusedIndexLimitBehavior     setUnusedLimitBehavior(UnusedIndexLimitBehavior mode) throws NullPointerException;
	public UnusedIndexLimitBehavior     getUnusedLimitBehavior();
	public boolean                      isValid();
	
	//// PROPERTY CHANGE SUPPORT
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PropertyChangeListener[]     getPropertyChangeListeners();
	public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
	public boolean                      hasListeners(String propertyName);
    }//end Partition
    
    public interface Entry<STORED_TYPE>{
	//// BEAN PROPERTIES
	public static final String LOCAL_INDEX = "localIndex",
		                   GLOBAL_INDEX= "globalIndex",
		                   VALID       = "valid";
	
	public Partition<STORED_TYPE> getParent();
	public STORED_TYPE get();
	public int getLocalIndex()  throws IllegalStateException;
	public int getGlobalIndex() throws IllegalStateException;
	public Partition<STORED_TYPE> remove() throws IllegalStateException;
	public boolean isValid();
	
	//// PROPERTY CHANGE SUPPORT
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PropertyChangeListener[]     getPropertyChangeListeners();
	public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
	public boolean                      hasListeners(String propertyName);
    }//end Entry
    
    public interface Backend<STORED_TYPE>{
	public void set   (int index, STORED_TYPE object) throws IndexOutOfBoundsException;
	public void resize(int newSizeInElements)         throws IndexOutOfBoundsException;
    }//end Backend
}//end PartitionedPool
