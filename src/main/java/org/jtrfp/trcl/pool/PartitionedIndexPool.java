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

import org.jtrfp.trcl.coll.ListActionDispatcher;
import com.ochafik.util.listenable.ListenableCollection;

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
    public static final String TOT_UNUSED_INDICES     = "totalUnusedIndices";
    
    /**
     * Creates a new partition of zero size at an undefined index in the pool.
     * @return this
     * @since Mar 10, 2015
     */
    public Partition           <STORED_TYPE> newPartition();
    /**
     * Auto-updated access to the valid partitions in this pool.
     * @return Read-only listenable collection representing the partitions of this pool.
     * @since Mar 10, 2015
     */
    public ListenableCollection<Partition<STORED_TYPE>> 
                                        getPartitions();
    /**
     * Auto-updated access to this entire pool's contents as a flat List of Entries. Unused locations are set to null.
     * @return a ListActionDispatcher of Entries.
     * @since Mar 12, 2015
     */
    public ListActionDispatcher<Entry<STORED_TYPE>>
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
    
    //// PROPERTY CHANGE SUPPORT
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PropertyChangeListener[]     getPropertyChangeListeners();
    public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
    public boolean                      hasListeners(String propertyName);
    
    /**
     * An auto-resizing subunit within the pool which may be invalidated (removed from the pool)
     * @author Chuck Ritola
     *
     * @param <STORED_TYPE> The object type of this Pool
     */
    public interface Partition<STORED_TYPE>{
	//// BEAN PROPERTIES
	public static final String GLOBAL_START_INDEX     ="globalStartIndex",
		                   LENGTH_INDICES         ="lengthInIndices",
		                   NUM_UNUSED_INDICES     ="numUnusedIndices",
			           NUM_USED_INDICES       ="numUsedIndices",
		                   VALID                  ="valid";
	
	/**
	 * Query this Partition's parent pool. This value is to remain constant through the life of the Partition.
	 * @return This partition's parent pool.
	 * @since Mar 11, 2015
	 */
	public PartitionedIndexPool<STORED_TYPE> getParent();
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
	 * Removes all Entries from this Partition, IFF this Partition is valid.
	 * @return this
	 * @throws IllegalStateException if the specified Entry or this Partition are invalid.
	 * @since Mar 11, 2015
	 */
	public Partition           <STORED_TYPE> removeAllEntries()                              throws IllegalStateException;
	/**
	 * Auto-updating query of all valid entries within this Partition.
	 * @return A ListActionDispatcher representing all entries of this Partition.
	 * @since Mar 11, 2015
	 */
	public ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>> 
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
	 * @return The number of unused indices removed from this Partition
	 * @throws IllegalStateException if this Partition is invalid (removed from PartitionedIndexPool).
	 * @throws IllegalArgumentException if maxNumUnusedIndices is not positive and of a value supported by the implementation.
	 * @since Mar 11, 2015
	 */
	public int       defragment(int maxNumUnusedIndices)              throws IllegalStateException, IllegalArgumentException;
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
	/**
	 * Query the current number of unused indices in this Partition, typically for defragmentation behavior.
	 * @return Zero or a positive number representing the number of unused indices in this Partition.
	 * @since Mar 31, 2015
	 */
	public int			    getNumUnusedIndices();
	public int                          getNumUsedIndices();

	//// PROPERTY CHANGE SUPPORT
	public PartitionedIndexPool.Partition<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool.Partition<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PartitionedIndexPool.Partition<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool.Partition<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
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
	public PartitionedIndexPool.Entry<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool.Entry<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PartitionedIndexPool.Entry<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
	public PartitionedIndexPool.Entry<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
	public PropertyChangeListener[]     getPropertyChangeListeners();
	public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
	public boolean                      hasListeners(String propertyName);
    }//end Entry
}//end PartitionedIndexPool
