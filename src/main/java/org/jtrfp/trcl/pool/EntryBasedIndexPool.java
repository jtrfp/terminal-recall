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
import java.util.Collections;
import java.util.Iterator;

import org.jtrfp.trcl.coll.ListActionDispatcher;
import org.jtrfp.trcl.pool.EntryBasedIndexPool.Entry.DeadEntry;
import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;

public class EntryBasedIndexPool<CONTAINED_TYPE> implements PropertyChangeListener {
    //// BEAN PROPERTIES
    public static final String    NUM_UNUSED_INDICES = "numUnusedIndices",
	                          NUM_USED_INDICES   = "numUsedIndices";
    
    private final IndexPool
    indexPool = new IndexPool();
    private final ListActionDispatcher<Entry<CONTAINED_TYPE>> 
    listActionDispatcher = new ListActionDispatcher<Entry<CONTAINED_TYPE>>(new ArrayList<Entry<CONTAINED_TYPE>>());
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public EntryBasedIndexPool(){
	indexPool.addPropertyChangeListener(this);
    }//end constructor
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
	if(evt.getPropertyName()==IndexPool.NUM_UNUSED_INDICES)
	 {pcs.firePropertyChange(EntryBasedIndexPool.NUM_UNUSED_INDICES, evt.getOldValue(), evt.getNewValue());}
	else if(evt.getPropertyName()==IndexPool.NUM_USED_INDICES)
	 {pcs.firePropertyChange(EntryBasedIndexPool.NUM_USED_INDICES, evt.getOldValue(), evt.getNewValue());}
    }
    
    /**
     * 
     * @param immutableValue
     * @return The new popped Entry or null if no more are available.
     * @since Mar 23, 2015
     */
    public Entry<CONTAINED_TYPE> popEntry(CONTAINED_TYPE immutableValue) {
	final Entry<CONTAINED_TYPE> result = new Entry<CONTAINED_TYPE>(this, immutableValue);
	final int index = indexPool.pop();
	if(index!=-1)
	    return result.setPoolIndex(index);
	return null;
    }

    private EntryBasedIndexPool<CONTAINED_TYPE> setInDispatcher(int index, Entry<CONTAINED_TYPE> entry){
	while(listActionDispatcher.size()<index)
	    listActionDispatcher.add(null);
	if(listActionDispatcher.size()==index)
	    listActionDispatcher.add(entry);
	else
	    listActionDispatcher.set(index, entry);
	return this;
    }//end setInDispatcher(...)

    private void truncateDispatcher(int newSize){
	while(listActionDispatcher.size()>newSize)
	    listActionDispatcher.remove(newSize);
	assert listActionDispatcher.size()==newSize;
    }//end truncateDispatcher(...)

    public void defragment(){
	//Used Indices, descending w/ Iterator
	ArrayList<Integer> used = new ArrayList<Integer>();
	for(Integer i:indexPool.getUsedIndices())
	    used.add(i);
	Collections.sort(used,Collections.reverseOrder());
	final Iterator<Integer> usedIterator = used.iterator();
	
	//Loop until Unused Index > Used Index.
	while(usedIterator.hasNext())//TODO: Do not need to go through entire list
	    defragmentEntry(usedIterator.next());

	/*Assuming defragmentation was properly executed, compaction should 
       discard an optimal number of unused indices.*/
	indexPool.compact();
	assert indexPool.getNumUnusedIndices()==0;
	final int newSize = indexPool.getUsedIndices().size();
	truncateDispatcher(newSize);
    }

    private void defragmentEntry(int index){
	assert index>=0:"Index is intolerably negative";
	assert index<listActionDispatcher.size():"index "+index+" exceeds size of entry list "+listActionDispatcher.size();
	Entry<CONTAINED_TYPE> entry = listActionDispatcher.get(index);
	assert entry!=null:"entry at index"+index+" intolerably null";
	indexPool.free(index);
	final int newIndex = indexPool.pop();
	entry.setPoolIndex(newIndex);
    }

    public void setGrowthBehavior(GrowthBehavior gb){
	indexPool.setGrowthBehavior(gb);
    }
    public void setHardLimit(int hardLimit){
	indexPool.setHardLimit(hardLimit);
    }
    
    public void freeAll() {
	for(Entry<CONTAINED_TYPE> entry:listActionDispatcher){
	    if(entry!=null)/*{entry.setValid(false);entry.setPoolIndex(-1);}*/entry.free();}
	//listActionDispatcher.clear();
	//indexPool.freeAll();
    }

    public static class Entry<CONTAINED_TYPE>{
	//// BEAN PROPERTIES
	public static final String VALID     ="valid",
		                   POOL_INDEX="poolIndex";

	private final EntryBasedIndexPool<CONTAINED_TYPE> parent;
	private final CONTAINED_TYPE        contained;
	private int                         poolIndex=-1;
	private boolean                     valid=true;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	protected Entry(EntryBasedIndexPool<CONTAINED_TYPE> parent, CONTAINED_TYPE contained){
	    this.parent   =parent;
	    this.contained=contained;
	}

	public void free() throws IllegalStateException{
	    illegalIfInvalid();
	    setValid(false);
	    getParent().indexPool.free(getPoolIndex());
	    setPoolIndex(-1);
	}

	private void illegalIfInvalid(){
	    if(!isValid())
		throw new IllegalStateException("Cannot perform this operation on an freed Entry");
	}

	public EntryBasedIndexPool<CONTAINED_TYPE> getParent(){
	    return parent;
	}

	protected Entry<CONTAINED_TYPE> setPoolIndex(int poolIndex){
	    pcs.firePropertyChange(POOL_INDEX, this.poolIndex, poolIndex);
	    if(this.poolIndex!=-1){
	     getParent().setInDispatcher(this.poolIndex,null);}//Remove old
	    this.poolIndex=poolIndex;
	    if(poolIndex!=-1)
	     getParent().setInDispatcher(poolIndex,this);
	    return this;
	}

	public int getPoolIndex(){
	    return poolIndex;
	}

	public CONTAINED_TYPE getContained(){
	    return contained;
	}

	@Override
	public void finalize(){
	    if(isValid())
		free();
	}

	/**
	 * @return true if this Entry is valid, else false if invalid (removed from pool)
	 */
	public boolean isValid() {
	    return valid;
	}

	/**
	 * @param valid the valid to set
	 */
	protected void setValid(boolean valid) {
	    pcs.firePropertyChange(VALID, this.valid, valid);
	    this.valid = valid;
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
	    return pcs.getPropertyChangeListeners();
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
		String propertyName) {
	    return pcs.getPropertyChangeListeners(propertyName);
	}

	/**
	 * @param propertyName
	 * @return
	 * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
	 */
	public boolean hasListeners(String propertyName) {
	    return pcs.hasListeners(propertyName);
	}

	/**
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(listener);
	}

	/**
	 * @param propertyName
	 * @param listener
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(String propertyName,
		PropertyChangeListener listener) {
	    pcs.removePropertyChangeListener(propertyName, listener);
	}
	
	public class DeadEntry extends Entry{
	    protected DeadEntry(EntryBasedIndexPool parent) {
		super(parent, null);
	    }//end constructor
	    @Override
	    public boolean isValid(){
		return false;
	    }
	}
    }//end Entry

    /**
     * @return the laDispatcher
     */
    public ListActionDispatcher<Entry<CONTAINED_TYPE>> getListActionDispatcher() {
	return listActionDispatcher;
    }

    /**
     * Query the number of unused indices in this pool.
     * @return Zero or a positive number representing the number of unused indices in this pool.
     * @since Mar 31, 2015
     */
    public int getNumUnusedIndices() {
	return indexPool.getNumUnusedIndices();
    }
    
    /**
     * Query the number of used indices in this pool.
     * @return Zero or a positive number representing the number of unused indices in this pool.
     * @since Mar 31, 2015
     */
    public int getNumUsedIndices() {
	return indexPool.getNumUsedIndices();
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void addPropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners()
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
	return pcs.getPropertyChangeListeners();
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#getPropertyChangeListeners(java.lang.String)
     */
    public PropertyChangeListener[] getPropertyChangeListeners(
	    String propertyName) {
	return pcs.getPropertyChangeListeners(propertyName);
    }

    /**
     * @param propertyName
     * @return
     * @see java.beans.PropertyChangeSupport#hasListeners(java.lang.String)
     */
    public boolean hasListeners(String propertyName) {
	return pcs.hasListeners(propertyName);
    }

    /**
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(listener);
    }

    /**
     * @param propertyName
     * @param listener
     * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.lang.String, java.beans.PropertyChangeListener)
     */
    public void removePropertyChangeListener(String propertyName,
	    PropertyChangeListener listener) {
	pcs.removePropertyChangeListener(propertyName, listener);
    }
}//end EntryBasedIndexPool
