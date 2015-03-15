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
import java.util.List;

import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableList;

/**
 * Handles entries in partitions which are organized in a sequential fashion such that all entries (used or unused) in a Partition
 * are consecutive (i.e. they do not mix with entries from other partitions but may mix used/unused indices within each Partition)
 * The Pool will automatically scale and reorganized the Pool in accordance to changes in enclosed Partitions. 
 * Defragmentation (removal of unused indices) can be customized such that some degree of fragmentation is tolerated as it can result
 * in performance improvements. A partition with no fragmentation tolerance could require the entire global pool to be
 * shifted left or right to accommodate this change in size, depending on the fragmentation state of other said Partitions.
 * @author Chuck Ritola
 *
 * @param <STORED_TYPE>
 */
public interface PartitionedIndexPool<STORED_TYPE> {
    //// BEAN PROPERTIES
    public static final String TOT_UNUSED_INDICES     = "totalUnusedIndices",
	                       UNUSED_LIMIT_BEHAVIOR  = "unusedLimitBehavior",
	                       FLUSH_BEHAVIOR         = "flushBehavior";
    
    /**
     * Creates a new partition of zero size at an undefined index in the pool.
     * @return this
     * @since Mar 10, 2015
     */
    public Partition           <STORED_TYPE> newPartition();
    /**
     * Removes the specified partition (and all enclosed entires) from this pool.
     * @param toRemove Non-null partition to remove, belonging to this pool.
     * @return this
     * @throws NullPointerException If the specified partition is null.
     * @throws IllegalArgumentException If the specified partition is not a member of this pool.
     * @since Mar 10, 2015
     */
    public PartitionedIndexPool<STORED_TYPE> removePartition(Partition<STORED_TYPE> toRemove) throws NullPointerException, IllegalArgumentException;
    /**
     * Removes all partitions from this pool, if any.
     * @return this
     * @since Mar 10, 2015
     */
    public PartitionedIndexPool<STORED_TYPE> removeAllPartitions();
    /**
     * Auto-updated access to the valid partitions in this pool.
     * @return Read-only listenable collection representing the partitions of this pool.
     * @since Mar 10, 2015
     */
    public ListenableCollection<Partition<STORED_TYPE>> 
                                        getPartitions();
    /**
     * Auto-updated access to this entire pool's contents as a flat List of Entries. Unused locations are set to null.
     * @return a read-only ListenableList of Entries.
     * @since Mar 12, 2015
     */
    public ListenableList<STORED_TYPE>
                                        getFlatEntries();
    /**
     * Query quantity of free indices of the entire pool.<br>
     * @see org.jtrfp.trcl.pool.PartitionedIndexPool#TOT_UNUSED_INDICES
     * @return The total number of unused indices of all partitions within this pool.
     * @since Mar 10, 2015
     */
    public int                          getTotalUnusedIndices();
    /**
     * Compacts (or defragments) all partitions in this pool and resizes such that 
     * there are no more unused indices than the specified argument.
     * Defragmentation may recursively repeat until the total number of unused indices equals or is
     * less than the specified argument.
     * @param maxNumUnusedIndices The collective maximum tolerable number of unused indices in all partitions of this pool.
     * @return this
     * @throws IllegalArgumentException
     * @since Mar 10, 2015
     */
    public PartitionedIndexPool<STORED_TYPE>
                                        defragment(int maxNumUnusedIndices)         throws IllegalArgumentException;
    /**
     * Modify this pool's management of unused indices across all partitions belonging to this pool.
     * This behavior gets invoked when the total number of unused indices of this pool's partitions increases but 
     * may result in no action at all. Such a decision is by the behavior itself.
     * Partitions which have their own UnusedIndexLimitBehavior specified (!=null) may be defragmented even if their own behavior does
     * not require it. The decision as to which partitions are defragmented or or the number of total unused indices tolerated
     * is determined by the specified behavior. The initial value is null.
     * 
     * @see org.jtrfp.trcl.pool.PartitionedIndexPool#UNUSED_LIMIT_BEHAVIOR
     * @param behavior The new behavior to apply to this pool. If this behavior is null the result no management and it will never auto-defragment at the pool level.
     * When set, the behavior will immediately be invoked for possible defragmentation.
     * @return The previous UnusedIndexLimitBehavior for this pool, which may be null.
     * @since Mar 10, 2015
     */
    public UnusedIndexLimitBehavior     setTotalUnusedLimitBehavior(UnusedIndexLimitBehavior behavior);
    /**
     * Query the current UnusedIndexLimitBehavior of this pool.
     * @see org.jtrfp.trcl.pool.PartitionedIndexPool#UNUSED_LIMIT_BEHAVIOR
     * @return The current UnusedIndexLimitBehavior for this pool, which may be null.
     * @since Mar 10, 2015
     */
    public UnusedIndexLimitBehavior     getTotalUnusedLimitBehavior();
    
    //// PROPERTY CHANGE SUPPORT
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PropertyChangeListener[]     getPropertyChangeListeners();
    public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
    public boolean                      hasListeners(String propertyName);
    
    /**
     * Implementation determines when it is appropriate to defragment. May be shared between pools.
     * @author Chuck Ritola
     *
     */
    public static interface UnusedIndexLimitBehavior{
	/**
	 * Notifies the implementation to check if the specified PartitionedIndexPool is ready to be flushed and
	 * invokes that PartitionedIndexPool's flush() method if so.
	 * @param poolToCheck The non-null pool for which to perform the check and potential flush.
	 * @throws NullPointerException if the passed pool is null.
	 * @since Mar 12, 2015
	 */
	public void proposeDefragmentation(PartitionedIndexPool<?> poolToCheck)                throws NullPointerException;
    }//end UnusedIndexLimitBehavior
    
    /**
     * An auto-resizing subunit within the pool which may be invalidated (removed from the pool)
     * @author Chuck Ritola
     *
     * @param <STORED_TYPE> The object type of this Pool
     */
    public interface Partition<STORED_TYPE>{
	//// BEAN PROPERTIES
	public static final String GLOBAL_START_INDEX     ="globalStartIndex",
		                   UNUSED_LIMIT_BEHAVIOR  ="unusedLimitBehavior",
		                   LENGTH_INDICES         ="lengthInIndices",
		                   VALID                  ="valid";
	
	/**
	 * Query this Parition's parent pool. This value is to remain constant through the life of the Partition.
	 * @return This partition's parent pool.
	 * @since Mar 11, 2015
	 */
	public PartitionedIndexPool<STORED_TYPE> 
	                                         getParent();
	/**
	 * Create a new Entry from this partition.
	 * @param value Immutable intended non-null value
	 * @return A new Entry from this partition.
	 * @throws IllegalStateException if this partition is no longer valid. (it was removed from the parent pool)
	 * @throws NullPointerException if passed value is null.
	 * @since Mar 11, 2015
	 */
	public Entry               <STORED_TYPE> newEntry(STORED_TYPE value) throws IllegalStateException, NullPointerException;
	/**
	 * Removes all entries from this partition, removes this partition from the parent pool, and 
	 * invalidates the partition such that isValid() returns false.
	 * @return this
	 * @throws IllegalStateException if this partition is no longer valid. (it was removed from the parent pool)
	 * @since Mar 11, 2015
	 */
	public Partition           <STORED_TYPE> remove()                                        throws IllegalStateException;
	/**
	 * Remove and invalidate an entry from this Partition and mark its index as unused.
	 * @param entry The non-null entry to invalidate and remove from this Partition.
	 * @return The removed Entry.
	 * @throws IllegalArgumentException if the specified Entry is not a member of this Partition.
	 * @throws IllegalStateException if the specified Entry or this Partition are invalid. (removed)
	 * @throws NullPointerException if the specified Entry is intolerably null.
	 * @since Mar 11, 2015
	 */
	public Entry               <STORED_TYPE> removeEntry(Entry<STORED_TYPE> entry)           throws IllegalArgumentException, IllegalStateException, NullPointerException;
	/**
	 * Removes all Entries from this Partition, IFF this Partition is valid.
	 * @return this
	 * @throws IllegalStateException if the specified Entry or this Partition are invalid.
	 * @since Mar 11, 2015
	 */
	public Partition           <STORED_TYPE> removeAllEntries()                              throws IllegalStateException;
	/**
	 * Auto-updating query of all valid entries within this Partition.
	 * @return A read-only listenable collection representing all entries of this Partition.
	 * @since Mar 11, 2015
	 */
	public ListenableCollection<Entry<STORED_TYPE>> 
	                                    getEntries();
	/**
	 * Query this Parittion's global start index.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#GLOBAL_START_INDEX
	 * @return This Partition's global start index.
	 * @since Mar 11, 2015
	 */
	public int                          getGlobalStartIndex();
	/**
	 * Query this Partition's length in indices.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#LENGTH_INDICES
	 * @return This Partition's length in indices, including free indices.
	 * @since Mar 11, 2015
	 */
	public int                          getLengthInIndices();
	/**
	 * Executes a compacting/defragmentation of this Partition.
	 * @param maxNumUnusedIndices The maximum tolerable threshold of unused indices in this Partition by the time defragmentation completes.
	 * @return this
	 * @throws IllegalStateException if this Partition is invalid (removed from PartitionedIndexPool).
	 * @throws IllegalArgumentException if maxNumUnusedIndices is not positive and of a value supported by the implementation.
	 * @since Mar 11, 2015
	 */
	public Partition<STORED_TYPE>       defragment(int maxNumUnusedIndices)              throws IllegalStateException, IllegalArgumentException;
	/**
	 * Modify this Partition's management of unused indices.
	 * This behavior gets invoked when the total number of unused indices of this Partition increases but 
	 * may result in no action at all. Such a decision is by the behavior itself.
	 * The initial value is null.
	 * 
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#UNUSED_LIMIT_BEHAVIOR
	 * @param behavior The new behavior to apply to this pool. If this behavior is null the result no management and it will never auto-defragment at the pool level.
	 * When set, the behavior will immediately be invoked for possible defragmentation.
	 * @return The previous UnusedIndexLimitBehavior for this pool, which may be null.
	 * @since Mar 10, 2015
	 */
	public UnusedIndexLimitBehavior     setUnusedLimitBehavior(UnusedIndexLimitBehavior mode);
	/**
	 * Query the current UnusedIndexLimitBehavior of this Partition.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#UNUSED_LIMIT_BEHAVIOR
	 * @return The current UnusedIndexLimitBehavior for this partition, which may be null.
	 * @since Mar 10, 2015
	 */
	public UnusedIndexLimitBehavior     getUnusedLimitBehavior();
	/**
	 * Query if this Partition is still valid. A Partition is valid until it is invalidated by removal through
	 * remove(), or the parent pools remove(Partition) method.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#remove()
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool#removePartition(Partition)
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Partition#VALID
	 * @return true if this Partition is valid, else false
	 * @since Mar 11, 2015
	 */
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
    
    /**
     * Immutable-contents element representing an Entry in the pool whose local and global indices may be changed
     * by the pool. Entries are created by Partition.newEntry(...) and removed by Partition.remove() or Entry.remove()
     * @author Chuck Ritola
     *
     * @param <STORED_TYPE>
     */
    public interface Entry<STORED_TYPE>{
	//// BEAN PROPERTIES
	public static final String LOCAL_INDEX = "localIndex",
		                   GLOBAL_INDEX= "globalIndex",
		                   VALID       = "valid";
	
	/**
	 * Query this Entry's immutable parent Partition.
	 * @return This Entry's parent Partition.
	 * @since Mar 11, 2015
	 */
	public Partition<STORED_TYPE> getParent();
	/**
	 * Query this Entry's immutable stored value (object), not to be confused with its index (location in the pool).
	 * @return This Entry's stored value.
	 * @since Mar 11, 2015
	 */
	public STORED_TYPE get();
	/**
	 * Query this Entry's local index.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Entry#LOCAL_INDEX
	 * @return This Entry's global index.
	 * @since Mar 11, 2015
	 */
	public int getLocalIndex()  throws IllegalStateException;
	/**
	 * Query this Entry's global index.
	 * @see org.jtrfp.trcl.pool.PartitionedIndexPool.Entry#GLOBAL_INDEX
	 * @return This Entry's global index.
	 * @since Mar 11, 2015
	 */
	public int getGlobalIndex() throws IllegalStateException;
	/**
	 * Remove (invalidate) this Entry from its parent Partition.
	 * @return this
	 * @throws IllegalStateException if this Entry was already removed (invalidated).
	 * @since Mar 11, 2015
	 */
	public Entry<STORED_TYPE> remove() throws IllegalStateException;
	/**
	 * Query if this entry is valid (not removed) and that its parent Partition is valid.
	 * @return true if this Entry and its containing Partition are valid, else false
	 * @since Mar 11, 2015
	 */
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
}//end PartitionedIndexPool
