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
import java.util.List;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.jtrfp.trcl.coll.BidiReferenceMap;
import org.jtrfp.trcl.coll.CachedAdapter;
import org.jtrfp.trcl.coll.ListActionAdapter;
import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.coll.ListActionTelemetry;
import org.jtrfp.trcl.coll.PartitionedList;

import com.ochafik.util.Adapter;
import com.ochafik.util.listenable.ListenableCollection;
import com.ochafik.util.listenable.ListenableCollections;
import com.ochafik.util.listenable.ListenableList;

public class PartitionedIndexPoolImpl<STORED_TYPE> implements
	PartitionedIndexPool<STORED_TYPE>, PropertyChangeListener {
    private int totalUnusedIndices = 0;
    private final BidiReferenceMap<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl> sharedEntryCache = 
	    new BidiReferenceMap<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(ReferenceStrength.WEAK);
    private final BidiReferenceMap<EntryBasedIndexPool<STORED_TYPE>, PartitionImpl>   ebip2PartitionMap = new BidiReferenceMap<EntryBasedIndexPool<STORED_TYPE>, PartitionImpl>(ReferenceStrength.WEAK);
    private final PartitionedList        <EntryBasedIndexPool.Entry<STORED_TYPE>>     wrappedList;
    private final ListActionTelemetry    <EntryBasedIndexPool.Entry<STORED_TYPE>>     telemetry;
    private final ListActionDispatcher   <EntryBasedIndexPool.Entry<STORED_TYPE>>     wrappedEntries;
    private final ListenableList<PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl> partitionsList = ListenableCollections.listenableList(new ArrayList<PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>());
    private final ListActionAdapter      <EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>                  flatEntries;
    private final ListActionAdapter      <PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl> partitions;
    private final Adapter<PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>                 partitionAdapter =
	     new CachedAdapter<PartitionedList <EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>(){
		@Override
		public PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl _adapt(
			PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition value) {
		    return new PartitionImpl(value);
		}
		@Override
		public PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl value) {
		    final PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition result = value.getWrappedPartition();
		    assert result!=null;
		    return result;
		}}.setTolerateNull(true);
    private final Adapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl> flatEntryAdapter =
	     new CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(sharedEntryCache){
		@Override
		public PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl _adapt(
			EntryBasedIndexPool.Entry<STORED_TYPE> value) {
		    PartitionImpl partition = getPartitionFromEntryBasedIndexPool(value.getParent());
		    return partition.generateEntry(value);
		}

		@Override
		public EntryBasedIndexPool.Entry<STORED_TYPE> _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl value) {
		    final EntryBasedIndexPool.Entry<STORED_TYPE> result = value.getWrappedEntry();
		    return result;
		}}.setTolerateNull(true);
		
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public PartitionedIndexPoolImpl(){//TODO: ListActionDispatcher may not be dispatching remove(index)?
	wrappedEntries    = new ListActionDispatcher<EntryBasedIndexPool.Entry<STORED_TYPE>>();
	telemetry         = new ListActionTelemetry<EntryBasedIndexPool.Entry<STORED_TYPE>>();
	wrappedList       = new PartitionedList    <EntryBasedIndexPool.Entry<STORED_TYPE>>(wrappedEntries);
	partitions        = new ListActionAdapter  <PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl>(partitionAdapter);
	flatEntries       = new ListActionAdapter  <EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(flatEntryAdapter);
	wrappedEntries    .addTarget(telemetry, true);
	wrappedEntries    .addTarget(flatEntries, true);
	partitions.getOutput().addTarget(partitionsList, true);
	wrappedList       .getPartitions().addTarget(partitions, true);
    }
	
    @Override
    public PartitionedIndexPool.Partition<STORED_TYPE> newPartition() {
	Partition<STORED_TYPE> result;
	result = partitionAdapter.adapt(wrappedList.newSubList());
	result.addPropertyChangeListener(Partition.NUM_UNUSED_INDICES, this);
	return result;
    }//end newPartition()
    
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
	if(arg0.getPropertyName()==Partition.NUM_UNUSED_INDICES){
	    int delta = (Integer)arg0.getNewValue()-(Integer)arg0.getOldValue();
	    //System.out.println("PartitionedIndexPoolImp.propertyChange NUM_UNUSED_INDICES delta="+delta);
	    unusedIndicesDelta(delta);
	}
    }//end propertyChange()
    
    private PartitionImpl getPartitionFromEntryBasedIndexPool(EntryBasedIndexPool<STORED_TYPE> k){
	return ebip2PartitionMap.get(k);
    }

    @Override
    public ListenableCollection<PartitionedIndexPool.Partition<STORED_TYPE>> getPartitions() {
	return (ListenableCollection)ListenableCollections.unmodifiableList(partitionsList);
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
    
    private void setTotalUnusedIndices(int totalUnusedIndices){
	//System.out.println("PartitionedIndexPoolImpl.setTotalUnusedIndices()" +totalUnusedIndices);
	pcs.firePropertyChange(TOT_UNUSED_INDICES, this.totalUnusedIndices, totalUnusedIndices);
	this.totalUnusedIndices=totalUnusedIndices;
    }

    @Override
    public int getTotalUnusedIndices() {
	int referenceValue = 0;
	for(Partition p:partitionsList)
	    referenceValue += p.getNumUnusedIndices();
	assert totalUnusedIndices==referenceValue:"Mismatch: cached value="+totalUnusedIndices+" reference="+referenceValue;
	return totalUnusedIndices;
    }

  //TODO: Unfinished but should work. Need to accommodate maxNumUnusedIndices
    @Override
    public PartitionedIndexPool<STORED_TYPE> defragment(int maxNumUnusedIndices)
	    throws IllegalArgumentException {
	//System.out.println("PartitionedIndexPoolImpl.defragment()");
	for(PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl part:partitionsList)
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
    
    class PartitionImpl implements Partition<STORED_TYPE>, PropertyChangeListener{
	private final EntryBasedIndexPool<STORED_TYPE>                                  ebip;
	private final PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition listPartition;
	private final ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>>     entries;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private volatile boolean valid          = true;
	private volatile int numUnusedIndices   = 0;
	private volatile int numUsedIndices     = 0;
	private volatile int lengthInIndices    = 0;
	private final CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>
	    partitionEntryAdapter = new CachedAdapter<EntryBasedIndexPool.Entry<STORED_TYPE>,PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl>(sharedEntryCache){
		@Override
		protected PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl _adapt(
			EntryBasedIndexPool.Entry<STORED_TYPE> value) {
		    if(value==null)
			throw new NullPointerException("Passed value intolerably null.");
		    //PartitionImpl partition = getPartitionFromEntryBasedIndexPool(value.getParent());
		    //return partition.generateEntry(value);
		    throw new UnsupportedOperationException();
		}//end _adapt()

		@Override
		protected EntryBasedIndexPool.Entry<STORED_TYPE> _reAdapt(
			PartitionedIndexPoolImpl<STORED_TYPE>.PartitionImpl.EntryImpl value) {
		    EntryBasedIndexPool.Entry<STORED_TYPE> result = value.getWrappedEntry();
		    assert result!=null;
		    return result;
		}}.setTolerateNull(true);
	
	public PartitionImpl(PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition listPartition) {
	    ebip = new EntryBasedIndexPool<STORED_TYPE>();
	    this.listPartition = listPartition;
	    ebip2PartitionMap.put(ebip, this);
	    ebip.getListActionDispatcher().addTarget(listPartition, true);
	    entries = new ListActionDispatcher<PartitionedIndexPool.Entry<STORED_TYPE>>();
	    ebip           .addPropertyChangeListener(this);
	    listPartition  .addPropertyChangeListener(this);
	}//end constructor
	
	public EntryImpl generateEntry(
		EntryBasedIndexPool.Entry<STORED_TYPE> value) {
	    return new EntryImpl(value);
	}

	// PROPERTY CHANGE HANDLING
	@Override
	public void propertyChange(PropertyChangeEvent evt){
	    if(evt.getPropertyName()==EntryBasedIndexPool.NUM_USED_INDICES)
		setNumUsedIndices((Integer)evt.getNewValue());
	    else if(evt.getPropertyName()==EntryBasedIndexPool.NUM_UNUSED_INDICES)
		setNumUnusedIndices((Integer)evt.getNewValue());
	    else if(evt.getPropertyName()==PartitionedList.Partition.START_INDEX)
		pcs.firePropertyChange(Partition.GLOBAL_START_INDEX, evt.getOldValue(), evt.getNewValue());
	}//end propertyChange
	
	private void updateLengthInIndices(){
	    //System.out.println("PartitionedIndexPoolImpl.updateLengthInIndices. numUnusedIndices="+getNumUnusedIndices()+" numUsedIndices="+getNumUsedIndices()+"\n\tTotal="+(getNumUnusedIndices()+getNumUsedIndices()));
	    setLengthInIndices(getNumUnusedIndices()+getNumUsedIndices());
	}
	
	/**
	 * @param lengthInIndices the lengthInIndices to set
	 */
	private void setLengthInIndices(int lengthInIndices) {
	    //System.out.println("PartitionedIndexPoolImpl.setLengthIndices "+lengthInIndices);
	    pcs.firePropertyChange(LENGTH_INDICES, this.lengthInIndices, lengthInIndices);
	    this.lengthInIndices = lengthInIndices;
	}
	
	public PartitionedList<EntryBasedIndexPool.Entry<STORED_TYPE>>.Partition getWrappedPartition() {
	    return listPartition;
	}
	
	private void setNumUnusedIndices(int newValue){
	    //System.out.println("PartitionedIndexPoolImpl.PartitionImpl.setNumUnusedIndices "+newValue);
	    pcs.firePropertyChange(Partition.NUM_UNUSED_INDICES, numUnusedIndices, newValue);
	    numUnusedIndices=newValue;
	    updateLengthInIndices();
	}

	private void invalidate(){
	    pcs.firePropertyChange(Partition.VALID, this.valid, false);
	    valid = false;
	    for(PropertyChangeListener l : pcs.getPropertyChangeListeners())
		pcs.removePropertyChangeListener(l);
	    ebip         .removePropertyChangeListener(this);
	    listPartition.removePropertyChangeListener(this);
	}//end invalidate()

	@Override
	public PartitionedIndexPool<STORED_TYPE> getParent() {
	    return PartitionedIndexPoolImpl.this;
	}

	@Override
	public PartitionedIndexPool.Entry<STORED_TYPE> newEntry(
		STORED_TYPE value) throws IllegalStateException,
		NullPointerException {
	    if(!isValid())
		throw new IllegalStateException("Cannot create Entry in Partition which has been invalidated (removed).");
	    EntryBasedIndexPool.Entry<STORED_TYPE> entry = ebip.popEntry(value);
	    assert entry!=null:"popped entry intolerably null.";
	    PartitionedIndexPool.Entry<STORED_TYPE> result = partitionEntryAdapter.adapt(entry);
	    entries.add(result);
	    return result;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> remove()
		throws IllegalStateException {
	    if(!isValid())
		throw new IllegalStateException("Cannot remove Partition which has already been invalidated (removed).");
	    removeAllEntries();
	    defragment(0);//Clean up.
	    PartitionedIndexPoolImpl.this.wrappedList.removeSubList(listPartition);
	    invalidate();
	    return this;
	}

	@Override
	public org.jtrfp.trcl.pool.PartitionedIndexPool.Partition<STORED_TYPE> removeAllEntries()
		throws IllegalStateException {
	    if(!isValid())
		throw new IllegalStateException("Cannot remove all Entries of Partition which has been invalidated (removed).");
	    List<Entry<STORED_TYPE>> entries = getEntries();
	    entries.clear();
	    ebip.freeAll();
	    return this;
	}

	@Override
	public ListActionDispatcher<org.jtrfp.trcl.pool.PartitionedIndexPool.Entry<STORED_TYPE>> getEntries()
	      throws IllegalStateException {
	    if(!isValid())
		throw new IllegalStateException("Cannot get entries of Partition which has been invalidated (removed).");
	    return entries;
	}

	@Override
	public int getGlobalStartIndex() {
	    if(!isValid())
		throw new IllegalStateException("Cannot get start index of Partition which has been invalidated (removed).");
	    return listPartition.getStartIndex();
	}

	@Override
	public int getLengthInIndices() {
	    if(!isValid())
		throw new IllegalStateException("Cannot get length of Partition which has been invalidated (removed).");
	    return lengthInIndices;
	}

	@Override
	public int defragment(int maxNumUnusedIndices)
		throws IllegalStateException, IllegalArgumentException {
	    //System.out.println("PartitionedIndexPoolImpl.Partition.defragment()");
	    if(!isValid())
		throw new IllegalStateException("Cannot defragment Partition which has been invalidated (removed).");
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
	    if(!isValid())
		throw new IllegalStateException("Cannot defragment Partition which has been invalidated (removed).");
	    //return ebip.getNumUnusedIndices();
	    return numUnusedIndices;
	}

	@Override
	public int getNumUsedIndices() {
	    if(!isValid())
		throw new IllegalStateException("Cannot defragment Partition which has been invalidated (removed).");
	    return numUsedIndices;
	}

	/**
	 * @param numUsedIndices the numUsedIndices to set
	 */
	private void setNumUsedIndices(int numUsedIndices) {
	    pcs.firePropertyChange(NUM_USED_INDICES, this.numUsedIndices, numUsedIndices);
	    this.numUsedIndices = numUsedIndices;
	    updateLengthInIndices();
	}
	
	/////////////////////////////////////
	/////////////////////////////////////
	
	protected class EntryImpl implements Entry<STORED_TYPE>, PropertyChangeListener{
	    private final EntryBasedIndexPool.Entry<STORED_TYPE> wrappedEntry;
	    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	    private final STORED_TYPE storedObject;
	    private volatile int      globalIndex = 0;
	    private volatile int      localIndex  = 0;
	    private volatile boolean  valid       = true;
		public EntryImpl(
		    EntryBasedIndexPool.Entry<STORED_TYPE> poppedEntry) {
		this.wrappedEntry = poppedEntry;
		this.storedObject = poppedEntry.getContained();
		wrappedEntry.addPropertyChangeListener(this);
		setLocalIndex(poppedEntry.getPoolIndex());
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
		    else if(evt.getPropertyName().contentEquals(EntryBasedIndexPool.Entry.VALID) && evt.getNewValue()==Boolean.FALSE && isValid())
			invalidate();
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
		    if(!isValid())
			throw new IllegalStateException("Cannot get local index of Entry which has been invalidated (removed).");
		    return localIndex;
		}

		@Override
		public int getGlobalIndex() throws IllegalStateException {
		    if(!isValid())
			throw new IllegalStateException("Cannot get local index of Entry which has been invalidated (removed).");
		    return globalIndex;
		}

		@Override
		public PartitionedIndexPool.Entry<STORED_TYPE> remove()
			throws IllegalStateException {
		    if(!isValid())
			throw new IllegalStateException("Cannot remove Entry which has been invalidated (removed).");
		    ((PartitionedIndexPoolImpl.PartitionImpl)getParent()).entries.remove(this);
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
		
		@Override
		public String toString(){
		    return "E["+this.hashCode()+" value="+this.get()+
			    " globalIdx="+this.getGlobalIndex()+" poolIdx="+this.getLocalIndex()+"]";
		}
	    }//end EntryImpl
    }//end PartitionImpl
}//end PartitionedIndexPool
