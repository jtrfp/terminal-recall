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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A List actor which combines the effects of several mutable sub-lists to a single destination List in a partitioned
 * fashion such that resizing of a partition shifts the positions of all partitions to the right of resized partition.
 * @author Chuck Ritola
 *
 * @param <T>
 */
public class PartitionedList<T> {
    private final List<T>                         dest;
    private final ListActionDispatcher<Partition> partitions = new ListActionDispatcher<Partition>();
    
    public PartitionedList(List<T> dest) {
	this.dest=dest;
    }
    
    /**
     * Sub-lists are in the order of their creation.
     * @return
     * @since Mar 18, 2015
     */
    public Partition newSubList(){
	Partition result = new Partition(this);
	partitions.add(result);
	return result;
    }
    
    public ListActionDispatcher<Partition> getPartitions(){
	return partitions;
    }
    
    public void removeSubList(Partition l) throws IllegalArgumentException, IllegalStateException{
	if(!partitions.contains(l))
	    if(l.getParent()==this)
	     throw new IllegalStateException("This partition was already removed.");
	    else
	     throw new IllegalArgumentException("This partition is not a member of this List.");
	//Remove from dest
	final int start = l.getStartIndex();
	dest.removeAll(dest.subList(start, start+l.size()));
	//Clear from record.
	l.setValid(false);
	partitions.remove(l);
	notifySizeAdjust();
    }
    
    protected void notifySizeAdjust(){
	for(Partition p:partitions)
	    p.staleSubList();
    }
    
    protected int getStartIndex(Partition l){
	if(!partitions.contains(l))
	    if(l.getParent()==this)
	     throw new IllegalStateException("This partition was removed.");
	    else
	     throw new IllegalArgumentException("This partition is not a member of this List.");
	final int index = partitions.indexOf(l);
	if(index==0)
	    return 0;
	Partition left = partitions.get(index-1);
	return left.getStartIndex()+left.size();
    }
    
    public class Partition implements List<T>{
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
	
	public Partition(PartitionedList<T> parent){
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
	    size += amt;
	    assert size>=0:"Size has shrunk to less than zero. Suspect excess removals without checking.";
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
	    if(c==null)
		throw new NullPointerException("Passed Collection is intolerably null.");
	    getSubList().addAll(c);
	    adjustSize(c.size());
	    return false;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
	    if(c==null)
		throw new NullPointerException("Passed Collection is intolerably null.");
	    if(index<0)
		throw new IllegalArgumentException("Passed index is intolerably negative.");
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
	    if(index<0)
		throw new IllegalArgumentException("Passed index is intolerably negative.");
	    final int size  = this.size;
	    final int offset = index;
	    return new ListIterator<T>(){
		int index = offset-1;//TODO: Check for bugs
		@Override
		public void add(T arg0) {
		    Partition.this.add(index,arg0);
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
		    return Partition.this.get(++index);
		}

		@Override
		public int nextIndex() {
		    return index+1;
		}

		@Override
		public T previous() {
		    return Partition.this.get(--index);
		}

		@Override
		public int previousIndex() {
		    return index-1;
		}

		@Override
		public void remove() {
		    Partition.this.remove(index);
		}

		@Override
		public void set(T element) {
		    Partition.this.set(index,element);
		}};
	}

	@Override
	public boolean remove(Object o) {
	    final boolean result = getSubList().remove(o);
	    if(result)adjustSize(-1);
	    return result;
	}

	@Override
	public T remove(int index) {
	    final T result = getSubList().remove(index);
	    adjustSize(-1);
	    return result;
	}

	@Deprecated
	@Override
	public boolean removeAll(Collection<?> c) {
	    throw new UnsupportedOperationException();
	}

	@Deprecated
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
