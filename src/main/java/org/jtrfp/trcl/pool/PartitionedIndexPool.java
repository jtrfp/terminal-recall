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
	                       BACKEND                = "backend",
	                       FLUSH_BEHAVIOR         = "flushBehavior";
    
    /**
     * Creates are new partition of zero size at an undefined index in the pool.
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
    /**
     * Set the Backend used by this pool to handle 'set' and 'resize' events. 
     * If the specified Backend is non-null and old.equals(new)==false, the new backend will be considered
     * completely stale and may be subject to a full flush, depending on the specified flush behavior of this pool.
     * @see org.jtrfp.trcl.pool.PartitionedIndexPool#BACKEND
     * @param newBackend New Backend for this pool. May be null.
     * @return Original Backend prior to the specified Backend. May be null.
     * @throws NullPointerException
     * @since Mar 11, 2015
     */
    public Backend<STORED_TYPE>         setBackend(Backend<STORED_TYPE> newBackend);
    /**
     * Query the currently-used backend.
     * @return The currently-used backend or null if none was specified.
     * @since Mar 11, 2015
     */
    public Backend<STORED_TYPE>		getBackend();
    
    /**
     * Sets the new FlushBehavior of this pool. The appropriate FlushBehavior methods will then be immediately 
     * invoked to propose flush operations, though none may necessarily occur.
     * @param behavior New FlushBehavior of this pool. 
     * @return The previously-used FlushBehavior of this pool or null if there was none.
     * @since Mar 11, 2015
     */
    public FlushBehavior<STORED_TYPE>   setFlushBehavior(FlushBehavior<STORED_TYPE> behavior);
    /**
     * Query the currently-used FlushBehavior.
     * @return The currently-used FlushBehavior or null if there is none.
     * @since Mar 11, 2015
     */
    public FlushBehavior<STORED_TYPE>   getFlushBehavior();
    /**
     * Perform a flush of all partitions of this pool, notifying the currently-specified Backend of changes. 
     * If the current Backend is null, this operation is ignored.
     * @return The total number of entries flushed.
     * @since Mar 11, 2015
     */
    public int				flush();
    
    //// PROPERTY CHANGE SUPPORT
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(PropertyChangeListener l);
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    public PropertyChangeListener[]     getPropertyChangeListeners();
    public PropertyChangeListener[]     getPropertyChangeListeners(String propertyName);
    public boolean                      hasListeners(String propertyName);
    
    /**
     * FlushBehavior is responsible for all calls to the pool's currently active Backend.
     * @author Chuck Ritola
     *
     * @param <STORED_TYPE> The stored type of the PartitionedIndexPool for which this FlushBehavior is to interact
     */
    public static interface FlushBehavior<STORED_TYPE>{
	/**
	 * Notify this FlushBehavior that an element is requested to be set..
	 * @param pool The non-null originating pool for this notification.
	 * @param object The value to apply to the specified globalIndex. May be null.
	 * @param globalIndex The global (pool-level) index to which the object is to be applied.
	 * @return this
	 * @throws NullPointerException if pool is null
	 * @throws IndexOutOfBoundsException if globalIndex is less than zero or otherwise not supported by the implementation.
	 * @since Mar 12, 2015
	 */
	public FlushBehavior<STORED_TYPE> notifySet   (PartitionedIndexPool<STORED_TYPE> pool, STORED_TYPE object, int globalIndex) throws NullPointerException, IndexOutOfBoundsException;
	/**
	 * Notify this FlushBehavior that a resize request has been made. 
	 * @param pool The non-null originating pool for this notification.
	 * @param newSize The requested new global size for this pool in elements. 
	 * @return this
	 * @throws NullPointerException if pool is null.
	 * @throws IndexOutOfBoundsException if size is less than zero or unsupported by the implementation.
	 * @since Mar 12, 2015
	 */
	public FlushBehavior<STORED_TYPE> notifyResize(PartitionedIndexPool<STORED_TYPE> pool, int newSize)                         throws NullPointerException, IndexOutOfBoundsException;
	/**
	 * Forces a flush regardless of the FlushBehavior implementation's volition.
	 * @return this
	 * @since Mar 12, 2015
	 */
	public FlushBehavior<STORED_TYPE> forceFlush();
    }//end FlushBehavior
    
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
	
	/**
	 * Query this Parition's parent pool. This value is to remain constant through the life of the Partition.
	 * @return This partition's parent pool.
	 * @since Mar 11, 2015
	 */
	public PartitionedIndexPool<STORED_TYPE> 
	                                         getParent();
	/**
	 * Create a new Entry from this partition.
	 * @param value
	 * @return A new Entry from this partition.
	 * @throws IllegalStateException if this partition is no longer valid. (it was removed from the parent pool)
	 * @since Mar 11, 2015
	 */
	public Entry               <STORED_TYPE> newEntry(STORED_TYPE value) throws IllegalStateException;
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
    
    public interface Backend<STORED_TYPE>{
	/**
	 * Requests this Backend to set an object at a given location.
	 * @param index Global index at which to set this STORED_TYPE object.
	 * @param object The STORED_TYPE to apply at the given index. May be null.
	 * @throws IndexOutOfBoundsException If the index is outside the range of 0<=index<size
	 * @since Mar 11, 2015
	 */
	public void set   (int index, STORED_TYPE object) throws IndexOutOfBoundsException;
	/**
	 * Request that the underlying implementation resize itself, discarding out-of-range entries.
	 * @param newSizeInElements A positive integer representing the requested size.
	 * @throws IndexOutOfBoundsException if the index is negative or otherwise incompatible with the implementation.
	 * @since Mar 11, 2015
	 */
	public void resize(int newSizeInElements)         throws IndexOutOfBoundsException;
    }//end Backend
}//end PartitionedPool
