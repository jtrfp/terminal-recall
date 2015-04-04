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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.jtrfp.trcl.coll.CachedAdapter;
import org.jtrfp.trcl.coll.ListActionAdapter;
import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;

import com.ochafik.util.Adapter;
import com.ochafik.util.listenable.DefaultListenableCollection;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableCollections;

public class PartitionedIndexPoolImpl<STORED_TYPE> implements
	PartitionedIndexPool<STORED_TYPE> {
    private int totalUnusedIndices = 0;
    private final PartitionedList        <EntryBasedIndexPool.Entry<STORED_TYPE>> wrappedList;
    private final ListActionAdapter      <EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl> flatEntries;
    private final ListActionTelemetry    <EntryBasedIndexPool.Entry<STORED_TYPE>> telemetry;
    private final ListActionAdapter      <PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl> partitions;
    private final ListenableCollection<PartitionedIndexPool.Partition<STORED_TYPE>> partitionsRO = 
	    ListenableCollections.unmodifiableCollection(
		    new DefaultListenableCollection<PartitionedIndexPool.Partition<STORED_TYPE>>(
			    new ArrayList<PartitionedIndexPool.Partition<STORED_TYPE>>()));
    private final Adapter<PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl> partitionAdapter =
	     new CachedAdapter<PartitionedList <EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>(){
		@Override
		public PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl _adapt(
			PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition value) {
		    return new PartitionImpl(value);
		}
		@Override
		public PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl value) {
		    return value.getWrappedPartition();
		}};
    private final Adapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl> flatEntryAdapter =
	     new CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(){
		@Override
		public PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl _adapt(
			EntryBasedIndexPool.Entry<STORED_TYPE> value) {
		    throw new UnsupportedOperationException();
		}

		@Override
		public EntryBasedIndexPool.Entry<STORED_TYPE> _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl value) {
		    return value.getWrappedEntry();
		}};
		
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public PartitionedIndexPoolImpl(){
	telemetry         = new ListActionTelemetry<EntryBasedIndexPool.Entry<STORED_TYPE>>();
	wrappedList       = new PartitionedList    <EntryBasedIndexPool.Entry<STORED_TYPE>>(telemetry);
	partitions        = new ListActionAdapter  <PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>(partitionAdapter);
	flatEntries       = new ListActionAdapter  <EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(flatEntryAdapter);
	wrappedList.getPartitions().addTarget(partitions, true);
    }
	
    @Override
    public PartitionedIndexPool.Partition<STORED_TYPE> newPartition() {
	Partition<STORED_TYPE> result;
	result = partitionAdapter.adapt(wrappedList.newSubList());
	result.addPropertyChangeListener(TOT_UNUSED_INDICES, new PropertyChangeListener(){
	    @Override
	    public void propertyChange(PropertyChangeEvent arg0) {
		int delta = (Integer)arg0.getOldValue()-(Integer)arg0.getNewValue();
		unusedIndicesDelta(delta);
	    }});
	return result;
    }//end newPartition()

    @Override
    public ListenableCollection<PartitionedIndexPool.Partition<STORED_TYPE>> getPartitions() {
	return partitionsRO;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>> getFlatEntries() {
	final Object result = flatEntries.getOutput();
	return (ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>>)result;
    }
    
    protected void unusedIndicesDelta(int delta){
	setTotalUnusedIndices(getTotalUnusedIndices()+delta);
    }
    
    private void setTotalUnusedIndices(int newValue){
	pcs.firePropertyChange(TOT_UNUSED_INDICES, totalUnusedIndices, newValue);
    }

    @Override
    public int getTotalUnusedIndices() {
	return totalUnusedIndices;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)
	    throws IllegalArgumentException {//TODO: Unfinished.
	for(PartitionedIndexPool.Partition<STORED_TYPE> part:partitionsRO)
	    part.defragment(0);
	return this;
    }//end defragment()

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> addPropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	pcs.addPropertyChangeListener(propertyName, l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
	return this;
    }

    @Override
    public PartitionedIndexPool<STORED_TYPE> removePropertyChangeListener(
	    String propertyName, PropertyChangeListener l) {
	pcs.removePropertyChangeListener(propertyName, l);
	return this;
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    @Override
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }
    
    ///////////////////////////////
    ///////////////////////////////
    
    private class PartitionImpl implements Partition<STORED_TYPE>, PropertyChangeListener{
	private final EntryBasedIndexPool<STORED_TYPE> ebip;
	private final PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition partitionedList;
	private final ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>> entries;
	private volatile boolean valid        = true;
	private volatile int numUnusedIndices = 0;
	private volatile int numUsedIndices   = 0;
	private volatile int lengthInIndices  = 0;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>
	    partitionEntryAdapter = new CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(){
		@Override
		protected PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl _adapt(
			EntryBasedIndexPool.Entry<STORED_TYPE> value) {
		    EntryImpl result = new EntryImpl(value);
		    return result;
		}//end _adapt()

		@Override
		protected EntryBasedIndexPool.Entry<STORED_TYPE> _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl value) {
		    return value.getWrappedEntry();
		}};
	
	public PartitionImpl(PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition list) {
	    ebip = new EntryBasedIndexPool<STORED_TYPE>();
	    this.partitionedList = list;
	    ebip.getListActionDispatcher().addTarget(list, true);
	    entries = new ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>>();
	    ebip           .addPropertyChangeListener(this);
	    partitionedList.addPropertyChangeListener(this);
	}//end constructor
	
	// PROPERTY CHANGE HANDLING
	@Override
	public void propertyChange(PropertyChangeEvent evt){
	    if(evt.getPropertyName().contentEquals(EntryBasedIndexPool.NUM_USED_INDICES))
		setNumUsedIndices((Integer)evt.getNewValue());
	    else if(evt.getPropertyName().contentEquals(EntryBasedIndexPool.NUM_UNUSED_INDICES))
		setNumUnusedIndices((Integer)evt.getNewValue());
	    else if(evt.getPropertyName().contentEquals(PartitionedList.Partition.START_INDEX))
		pcs.firePropertyChange(Partition.GLOBAL_START_INDEX, evt.getOldValue(), evt.getNewValue());
	}//end propertyChange
	
	private void updateLengthInIndices(){
	    setLengthInIndices(getNumUnusedIndices()+getNumUsedIndices());
	}
	
	/**
	 * @param lengthInIndices the lengthInIndices to set
	 */
	private void setLengthInIndices(int lengthInIndices) {
	    pcs.firePropertyChange(LENGTH_INDICES, this.lengthInIndices, lengthInIndices);
	    this.lengthInIndices = lengthInIndices;
	}
	
	public PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition getWrappedPartition() {
	    return partitionedList;
	}
	
	private void setNumUnusedIndices(int newValue){
	    pcs.firePropertyChange(Partition.NUM_UNUSED_INDICES, numUnusedIndices, newValue);
	    numUnusedIndices=newValue;
	    updateLengthInIndices();
	}

	private void invalidate(){
	    pcs.firePropertyChange(Partition.VALID, this.valid, false);
	    valid = false;
	    for(PropertyChangeListener l : pcs.getPropertyChangeListeners())
		pcs.removePropertyChangeListener(l);
	    ebip           .removePropertyChangeListener(this);
	    partitionedList.removePropertyChangeListener(this);
	}//end invalidate()

	@Override
	public PartitionedIndexPool<STORED_TYPE> getParent() {
	    return PartitionedIndexPoolImpl.this;
	}

	@Override
	public PartitionedIndexPool.Entry<STORED_TYPE> newEntry(
		STORED_TYPE value) throws IllegalStateException,
		NullPointerException {
	    PartitionedIndexPool.Entry<STORED_TYPE> result = partitionEntryAdapter.adapt(ebip.popEntry(value));
	    return result;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> remove()
		throws IllegalStateException {
	    PartitionedIndexPoolImpl.this.wrappedList.removeSubList(partitionedList);
	    invalidate();
	    return this;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> removeAllEntries()
		throws IllegalStateException {
	    for(EntryBasedIndexPool.Entry<STORED_TYPE> entry: ebip.getListActionDispatcher())
		entry.free();
	    return this;
	}

	@Override
	public ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>> getEntries() {
	    return entries;
	}

	@Override
	public int getGlobalStartIndex() {
	    return partitionedList.getStartIndex();
	}

	@Override
	public int getLengthInIndices() {
	    return lengthInIndices;
	}

	@Override
	public int defragment(int maxNumUnusedIndices)
		throws IllegalStateException, IllegalArgumentException {
	    ebip.defragment();
	    return 0;
	}

	@Override
	public boolean isValid() {
	    return valid;
	}

	@Override
	public PartitionedIndexPool.Partition<STORED_TYPE> addPropertyChangeListener(
		PropertyChangeListener l) {
	    pcs.addPropertyChangeListener(l);
	    return this;
	}

	@Override
	public PartitionedIndexPool.Partition<STORED_TYPE> addPropertyChangeListener(
		String propertyName, PropertyChangeListener l) {
	    pcs.addPropertyChangeListener(propertyName, l);
	    return this;
	}

	@Override
	public PartitionedIndexPool.Partition<STORED_TYPE> removePropertyChangeListener(
		PropertyChangeListener l) {
	    pcs.removePropertyChangeListener(l);
	    return this;
	}

	@Override
	public PartitionedIndexPool.Partition<STORED_TYPE> removePropertyChangeListener(
		String propertyName, PropertyChangeListener l) {
	    pcs.removePropertyChangeListener(propertyName, l);
	    return this;
	}

	@Override
	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	@Override
	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	@Override
	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}
	
	@Override
	public int getNumUnusedIndices() {
	    return ebip.getNumUnusedIndices();
	}

	@Override
	public int getNumUsedIndices() {
	    return numUsedIndices;
	}

	/**
	 * @param numUsedIndices the numUsedIndices to set
	 */
	public void setNumUsedIndices(int numUsedIndices) {
	    pcs.firePropertyChange(NUM_USED_INDICES, this.numUsedIndices, numUsedIndices);
	    this.numUsedIndices = numUsedIndices;
	    updateLengthInIndices();
	}
	
	/////////////////////////////////////
	/////////////////////////////////////
	
	protected class EntryImpl implements Entry<STORED_TYPE>, PropertyChangeListener{
	    private final EntryBasedIndexPool.Entry<STORED_TYPE> wrappedEntry;
	    private final STORED_TYPE storedObject;
	    private volatile int      globalIndex = 0;
	    private volatile int      localIndex  = 0;
		public EntryImpl(
		    EntryBasedIndexPool.Entry<STORED_TYPE> poppedEntry) {
		this.wrappedEntry = poppedEntry;
		this.storedObject = poppedEntry.getContained();
		wrappedEntry.addPropertyChangeListener(this);
	    }//end constructor()

		// PROPERTY CHANGE HANDLING
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
		    if(evt.getPropertyName().contentEquals(EntryBasedIndexPool.Entry.POOL_INDEX)){
			pcs.firePropertyChange(Entry.LOCAL_INDEX, evt.getOldValue(),evt.getNewValue());
			setLocalIndex((Integer)evt.getNewValue());
		    }//end if(POOL_INDEX)
		    else if(evt.getPropertyName().contentEquals(Partition.GLOBAL_START_INDEX))
			updateGlobalIndex();
		}//end propertyChange(...)

		@Override
		public PartitionedIndexPool.Partition<STORED_TYPE> getParent() {
		    return PartitionedIndexPoolImpl.PartitionImpl.this;
		}

		@Override
		public STORED_TYPE get() {
		    return storedObject;
		}

		@Override
		public int getLocalIndex() throws IllegalStateException {
		    return localIndex;
		}

		@Override
		public int getGlobalIndex() throws IllegalStateException {
		    return globalIndex;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> remove()
			throws IllegalStateException {
		    wrappedEntry.free();
		    invalidate();
		    return this;
		}
		
		private void invalidate(){
		    pcs.firePropertyChange(VALID, valid, false);
		    valid=false;
		    for(PropertyChangeListener l : pcs.getPropertyChangeListeners())
			pcs.removePropertyChangeListener(l);
		    wrappedEntry.removePropertyChangeListener(this);
		}

		@Override
		public boolean isValid() {
		    return valid;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> addPropertyChangeListener(
			PropertyChangeListener l) {
		    pcs.addPropertyChangeListener(l);
		    return this;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> addPropertyChangeListener(
			String propertyName, PropertyChangeListener l) {
		    pcs.addPropertyChangeListener(propertyName, l);
		    return this;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> removePropertyChangeListener(
			PropertyChangeListener l) {
		    pcs.removePropertyChangeListener(l);
		    return this;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> removePropertyChangeListener(
			String propertyName, PropertyChangeListener l) {
		    pcs.removePropertyChangeListener(propertyName, l);
		    return this;
		}

		@Override
		public PropertyChangeListener[] getPropertyChangeListeners() {
		    return pcs.getPropertyChangeListeners();
		}

		@Override
		public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		    return pcs.getPropertyChangeListeners(propertyName);
		}

		@Override
		public boolean hasListeners(String propertyName) {
		    return pcs.hasListeners(propertyName);
		}

		/**
		 * @return the wrappedEntry
		 */
		public EntryBasedIndexPool.Entry<STORED_TYPE> getWrappedEntry() {
		    return wrappedEntry;
		}
		
		private void updateGlobalIndex(){
		    setGlobalIndex(getLocalIndex()+PartitionImpl.this.getGlobalStartIndex());
		}

		/**
		 * @param globalIndex the globalIndex to set
		 */
		private void setGlobalIndex(int globalIndex) {
		    pcs.firePropertyChange(Entry.GLOBAL_INDEX, this.globalIndex, globalIndex);
		    this.globalIndex = globalIndex;
		}

		/**
		 * @param localIndex the localIndex to set
		 */
		public void setLocalIndex(int localIndex) {
		    pcs.firePropertyChange(Entry.LOCAL_INDEX, this.localIndex, localIndex);
		    this.localIndex = localIndex;
		    updateGlobalIndex();
		}
	    }//end EntryImpl
    }//end PartitionImpl
}//end PartitionedIndexPool
