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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jtrfp.trcl.WeakValueHashMap;
import org.jtrfp.trcl.pool.ObjectFactory;

import com.ochafik.util.Adapter;

public class ListActionAdapter<IN,OUT> implements List<IN> {
    protected final Adapter<IN,OUT>           adapter;
    protected final ListActionDispatcher<OUT> output;
    protected final List<IN>                  input;
    protected final Map<IN,OUT>               inOutMap = new WeakValueHashMap<IN,OUT>();
    
    private final ObjectFactory<IN,OUT>       objectFactory;
    
    public ListActionDispatcher<OUT> getOutput(){
	return output;
    }
    
    public ListActionAdapter(Adapter<IN,OUT>adapter) {
	if(adapter==null)
	    throw new NullPointerException("Supplied Adapter is intolerably null.");
	this.adapter = adapter;
	this.output  = new ListActionDispatcher<OUT>();
	this.input   = new ArrayList<IN>();
	objectFactory= new ObjectFactory<IN,OUT>(inOutMap, adapter);
    }//end constructor()

    @Override
    public boolean add(IN in) {
	input.add(in);
	return output.add(objectFactory.get(in));
    }

    @Override
    public void add(int index, IN in) {
	input.add(index,in);
	output.add(index, objectFactory.get(in));
    }

    @Override
    public boolean addAll(Collection<? extends IN> c) {
	input.addAll(c);
	return addAll(0,c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends IN> c) {
	input.addAll(index,c);
	final ArrayList<OUT> out = new ArrayList<OUT>(c.size());
	List<IN> buffer = new ArrayList<IN>(c.size());
	buffer.addAll(c);
	while(!buffer.isEmpty())
	    out.add(index, objectFactory.get(buffer.remove(buffer.size()-1)));
	
	return output.addAll(index,out);
    }

    @Override
    public void clear() {
	input   .clear();
	inOutMap.clear();
	output  .clear();
    }

    @Override
    public boolean contains(Object o) {
	return input.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	return input.containsAll(c);
    }

    @Override
    public IN get(int index) {
	return input.get(index);
    }

    @Override
    public int indexOf(Object o) {
	return input.indexOf(o);
    }

    @Override
    public boolean isEmpty() {
	return input.isEmpty();
    }

    @Override
    public Iterator<IN> iterator() {
	return input.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
	return input.lastIndexOf(o);
    }

    @Override
    public ListIterator<IN> listIterator() {
	return input.listIterator();
    }

    @Override
    public ListIterator<IN> listIterator(int index) {
	return input.listIterator(index);
    }

    @Override
    public boolean remove(Object o) {
	final int index      = input.indexOf(o);
	final boolean result = index!=-1;
	if(result)
	  remove(index);
	return result;
    }

    @Override
    public IN remove(int index) {
	final IN result = input.get(index);
	input.remove(index);
	output.remove(index);
	return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	boolean result = false;
	for(Object o:c)
	    while(input.contains(o))
	     result |= remove(o);
	return result;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	final ArrayList<Integer> toRetain = new ArrayList<Integer>();
	int index;
	for(Object o:c)
	    if((index = input.indexOf(o)) != -1)
		toRetain.add(index);
	final ArrayList<IN> retainIn  = new ArrayList<IN>();
	final ArrayList<OUT>retainOut = new ArrayList<OUT>();
	for(Integer i:toRetain){
	    retainIn .add(input.get(i));
	    retainOut.add(output.get(i));
	}
	       input .retainAll(retainIn);
	return output.retainAll(retainOut);
    }//end retainAll()

    @Override
    public IN set(int index, IN element) {
	final OUT out = objectFactory.get(element);
	output.set(index, out);
	return input.set(index, element);
    }

    @Override
    public int size() {
	return output.size();
    }

    @Override
    public List<IN> subList(int fromIndex, int toIndex) {
	return input.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
	return input.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	return input.toArray(a);
    }
}//end ListActionAdapter
