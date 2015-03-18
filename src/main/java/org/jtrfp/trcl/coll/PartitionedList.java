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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.collections.ListUtils;

/**
 * A List actor which combines the effects of several mutable sub-lists to a single destination List in a partitioned
 * fashion such that resizing of a partition shifts the positions of all partitions to the right of resized partition.
 * @author Chuck Ritola
 *
 * @param <T>
 */
public class PartitionedList<T> {
    private final List<T>                  dest;
    private final ArrayList<ListPartition> partitionLists   = new ArrayList<ListPartition>();
    private final List<ListPartition>      partitionListsRO = ListUtils.unmodifiableList(partitionLists);
    
    public PartitionedList(List<T> dest) {
	this.dest=dest;
    }
    
    /**
     * Sub-lists are in the order of their creation.
     * @return
     * @since Mar 18, 2015
     */
    public ListPartition newSubList(){
	ListPartition result = new ListPartition(this);
	partitionLists.add(result);
	return result;
    }
    
    public List<ListPartition> getPartitionLists(){
	return partitionListsRO;
    }
    
    public void removeSubList(ListPartition l) throws IllegalArgumentException, IllegalStateException{
	if(!partitionLists.contains(l))
	    if(l.getParent()==this)
	     throw new IllegalStateException("This partition was already removed.");
	    else
	     throw new IllegalArgumentException("This partition is not a member of this List.");
	//Remove from dest
	final int start = l.getStartIndex();
	dest.removeAll(dest.subList(start, start+l.size()));
	//Clear from record.
	l.setValid(false);
	partitionLists.remove(l);
	notifySizeAdjust();
    }
    
    protected void notifySizeAdjust(){
	for(ListPartition p:partitionLists)
	    p.staleSubList();
    }
    
    protected int getStartIndex(ListPartition l){
	final int index = partitionLists.indexOf(l);
	if(index==0)
	    return 0;
	ListPartition left = partitionLists.get(index-1);
	return left.getStartIndex()+left.size();
    }
    
    public class ListPartition implements List<T>{
	//// BEAN PROPERTIES
	public static final String SIZE        = "size",
		                   VALID       = "valid",
		                   START_INDEX = "startIndex";
	
	private int                      size=0;
	private List<T>                  subList;
	private final PartitionedList<T> parent;
	private boolean                  valid = true;
	private int                      startIndex=0;
	
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	protected PartitionedList<T> getParent(){
	    return parent;
	}
	
	public ListPartition(PartitionedList<T> parent){
	    this.parent=parent;
	}
	
	protected List<T> getSubList(){
	    if(!isValid())
		throw new IllegalStateException("Attempting to perform List operation on an invalid (removed) Partition");
	    if(subList==null){
		final int start = getStartIndex();
		subList = dest.subList(getStartIndex(), size+start);
		}
	    return subList;
	}//getSubList()
	
	protected void staleSubList(){
	    subList=null;
	}
	
	protected void adjustSize(int amt){
	    pcs.firePropertyChange(SIZE, size, size+amt);
	    size+=amt;
	    parent.notifySizeAdjust();
	}
	
	public int getStartIndex(){
	    final int newIndex = parent.getStartIndex(this);
	    pcs.firePropertyChange(START_INDEX, startIndex, newIndex);
	    startIndex = newIndex;
	    return newIndex;
	}
	
	@Override
	public boolean add(T element) {
	    getSubList().add(element);
	    adjustSize(1);
	    return true;
	}

	@Override
	public void add(int index, T element) {
	    getSubList().add(index,element);
	    adjustSize(1);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
	    getSubList().addAll(c);
	    adjustSize(c.size());
	    return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
	    getSubList().addAll(index,c);
	    adjustSize(c.size());
	    return false;
	}

	@Override
	public void clear() {
	    final int prevSize = size;
	    getSubList().clear();
	    adjustSize(-prevSize);
	}

	@Override
	public boolean contains(Object o) {
	    return getSubList().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
	    return getSubList().containsAll(c);
	}

	@Override
	public T get(int index) {
	    return getSubList().get(index);
	}

	@Override
	public int indexOf(Object o) {
	    return getSubList().indexOf(o);
	}

	@Override
	public boolean isEmpty() {
	    return size==0;
	}

	@Override
	public Iterator<T> iterator() {
	    return getSubList().iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
	    return getSubList().lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
	    return listIterator(0);
	}

	@Override
	public ListIterator<T> listIterator(int index) {
	    final int size  = this.size;
	    final int offset = index;
	    return new ListIterator<T>(){
		int index = offset-1;
		@Override
		public void add(T arg0) {
		    ListPartition.this.add(index,arg0);
		}

		@Override
		public boolean hasNext() {
		    return index<size-1;
		}

		@Override
		public boolean hasPrevious() {
		    return index>0;
		}

		@Override
		public T next() {
		    return ListPartition.this.get(++index);
		}

		@Override
		public int nextIndex() {
		    return index+1;
		}

		@Override
		public T previous() {
		    return ListPartition.this.get(--index);
		}

		@Override
		public int previousIndex() {
		    return index-1;
		}

		@Override
		public void remove() {
		    ListPartition.this.remove(index);
		}

		@Override
		public void set(T element) {
		    ListPartition.this.set(index,element);
		}};
	}

	@Override
	public boolean remove(Object o) {
	    final boolean result = getSubList().remove(o);
	    adjustSize(-1);
	    return result;
	}

	@Override
	public T remove(int index) {
	    final T result = getSubList().remove(index);
	    adjustSize(-1);
	    return result;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public T set(int index, T element) {
	    return getSubList().set(index, element);
	}

	@Override
	public int size() {
	    return size;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
	    return getSubList().subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
	    return toArray(new Object[]{});
	}

	@Override
	public <TYPE> TYPE[] toArray(TYPE[] a) {
	    return getSubList().toArray(a);
	}

	/**
	 * @return the valid
	 */
	public boolean isValid() {
	    return valid;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(boolean valid) {
	    pcs.firePropertyChange(VALID, this.valid, valid);
	    this.valid = valid;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
	    return size;
	}

	/**
	 * @param size the size to set
	 */
	protected void setSize(int size) {
	    this.size = size;
	}

	/**
	 * @param arg0
	 * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void addPropertyChangeListener(PropertyChangeListener arg0) {
	    pcs.addPropertyChangeListener(arg0);
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
	 * @param arg0
	 * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
	 */
	public void removePropertyChangeListener(PropertyChangeListener arg0) {
	    pcs.removePropertyChangeListener(arg0);
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
    }//end PartitionList
}//end LsitenablePartitionedList
