/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2015 Chuck Ritola
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

import java.util.AbstractList;
import java.util.List;

public class SubList<E> extends AbstractList<E> {
    private final List<E> delegate;
    private int startIndex=0, endIndex=0;
    
    public SubList(List<E> delegate, int startIndex, int endIndex){
	this.delegate  =delegate;
	this.startIndex=startIndex;
	this.endIndex  =endIndex;
    }

    @Override
    public E get(int index) {
	checkRangeExclusive(index);
	return delegate.get(index+startIndex);
    }
    
    @Override
    public E remove(int index){
	checkRange(index);
	final E result = delegate.remove(startIndex+index);
	endIndex--;
	return result;
    }
    
    @Override
    public void add(int index, E element){
	checkRange(index);
	delegate.add(startIndex+index, element);
	endIndex++;
    }
    
    @Override
    public E set(int index, E element){
	checkRange(index);
	return delegate.set(startIndex+index, element);
    }

    @Override
    public int size() {
	return endIndex-startIndex;
    }
    
    private void checkRangeExclusive(int index){
	final int size = size();
	if(index>=size)
	    throw new IndexOutOfBoundsException("Index="+index+" size="+size);
	if(index<0)
	    throw new IndexOutOfBoundsException("Index must be positive integer. Got "+index);
    }//end checkRange()
    
    private void checkRange(int index){
	final int size = size();
	if(index>size)
	    throw new IndexOutOfBoundsException("Index="+index+" size="+size);
	if(index<0)
	    throw new IndexOutOfBoundsException("Index must be positive integer. Got "+index);
    }//end checkRange()
    
    @Override
    public List<E> subList(int start, int end){
	if(start<0)
	    throw new IllegalArgumentException("Start index must be positive integer. Got "+start);
	if(start>=size())
	    throw new IllegalArgumentException("Start index must be less than total size. Got "+start);
	if(end<1)
	    throw new IllegalArgumentException("End index must be positive nonzero integer. Got "+end);
	if(end>delegate.size())
	    throw new IllegalArgumentException("End index must be less than total size. Got "+end);
	return new SubList<E>(delegate,startIndex+start,startIndex+end);
    }//end subList(...)
    
    @SuppressWarnings("unchecked")
    @Override
    public void clear(){
	if(delegate instanceof RangeClearable)
	    ((RangeClearable<E>)delegate).clearRange(startIndex,endIndex);
	else
	    super.clear();
    }//end clear()

}//end SubList<E>()
