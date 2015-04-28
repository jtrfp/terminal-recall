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
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import org.jtrfp.trcl.pool.IndexPool.GrowthBehavior;


public class IndexList<E> {
    public static final String NUM_USED_INDICES   = IndexPool.NUM_USED_INDICES;
    public static final String NUM_UNUSED_INDICES = IndexPool.NUM_UNUSED_INDICES;
    
    private final PropertyChangeSupport pcs  = new PropertyChangeSupport(this);
    private final PropertyChangeListener pcl = new ILPropertyChangeListener();
    
    private final List<E>   delegate;
    private final IndexPool indexPool      = new IndexPool();
    private GrowthBehavior  growthBehavior = new GrowthBehavior(){
	@Override
	public int grow(int previousMaxCapacity) {
	    return previousMaxCapacity!=0?previousMaxCapacity*2:1;
	}
	@Override
	public int shrink(int minDesiredCapacity) {
	    return minDesiredCapacity;
	}};
    
    public IndexList(List<E> delegate){
	this.delegate=delegate;
	delegate.clear();
	indexPool.setGrowthBehavior(new WrappedGrowthBehavior());
	indexPool.addPropertyChangeListener(pcl);
    }
    
    public int pop(E element){
	final int index = indexPool.pop();
	assert index<delegate.size():"index="+index+" delegate.size()="+delegate.size();
	if(index!=-1)
	 delegate.set(index, element);
	return index;
    }//end pop(...)
    
    public void free(int index){
	indexPool.free(index);
	delegate.set(index, null);
    }
    
    private class ILPropertyChangeListener implements PropertyChangeListener{
	@Override
	public void propertyChange(PropertyChangeEvent event) {
	    pcs.firePropertyChange(event);
	}
    }//end ILPropertyChangeListener
    
    private class WrappedGrowthBehavior implements GrowthBehavior{
	@Override
	public int grow(int previousMaxCapacity) {
	    final int result    = growthBehavior.grow(previousMaxCapacity);
	    final int sizeDelta = result - delegate.size();
	    for(int i=sizeDelta; i>0; i--)
		delegate.add(null);
	    assert delegate.size()==result:" delegate.size()="+delegate.size()+" "+result;
	    return result;
	}
	@Override
	public int shrink(int minDesiredCapacity) {
	    if(minDesiredCapacity<0)
		throw new IllegalArgumentException("Min desired capacity intolerably negative: "+minDesiredCapacity);
	    final int result    = growthBehavior.shrink(minDesiredCapacity);
	    final int sizeDelta = delegate.size() - result;
	    assert delegate.size()>=sizeDelta:"sizeDelta>delegate.size() size="+delegate.size()+" delta="+sizeDelta;
	    if(!delegate.isEmpty())
	     for(int i=sizeDelta; i>0; i--)
		delegate.remove(delegate.size()-1);
	    assert delegate.size()==result:" delegate.size()="+delegate.size()+" "+result;
	    return result;
	}
    }//end WrappedGrowthBehavior

    /**
     * @return the growthBehavior
     */
    protected GrowthBehavior getGrowthBehavior() {
        return growthBehavior;
    }

    /**
     * @param growthBehavior the growthBehavior to set
     */
    protected void setGrowthBehavior(GrowthBehavior growthBehavior) {
        this.growthBehavior = growthBehavior;
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

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#compact()
     */
    public int compact() {
	final int result = indexPool.compact();
	final int newSize = indexPool.getMaxCapacity();
	truncate(newSize);
	return result;
    }
    
    private void truncate(int newSize){
	while(delegate.size()>newSize)
	    delegate.remove(newSize);
	assert delegate.size()==newSize;
    }//end truncate(...)

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getFreeIndices()
     */
    public PriorityBlockingQueue<Integer> getFreeIndices() {
	return indexPool.getFreeIndices();
    }

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getUsedIndices()
     */
    public PriorityBlockingQueue<Integer> getUsedIndices() {
	return indexPool.getUsedIndices();
    }

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getMaxCapacity()
     */
    public int getMaxCapacity() {
	return indexPool.getMaxCapacity();
    }

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getHardLimit()
     */
    public int getHardLimit() {
	return indexPool.getHardLimit();
    }

    /**
     * @param hardLimit
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#setHardLimit(int)
     */
    public IndexPool setHardLimit(int hardLimit) {
	return indexPool.setHardLimit(hardLimit);
    }

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getNumUnusedIndices()
     */
    public int getNumUnusedIndices() {
	return indexPool.getNumUnusedIndices();
    }

    /**
     * @return
     * @see org.jtrfp.trcl.pool.IndexPool#getNumUsedIndices()
     */
    public int getNumUsedIndices() {
	return indexPool.getNumUsedIndices();
    }
}//end IndexList
