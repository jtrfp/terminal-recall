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

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ochafik.util.listenable.Adapter;
import com.ochafik.util.listenable.Pair;

public class PropertyBasedTaggerTest {
    private final Adapter<TaggingElement,Integer> subjectAdapter = new Adapter<TaggingElement,Integer>(){

	@Override
	public Integer adapt(TaggingElement value) {
	    return value.getValue().length();
	}};
    private ArrayList<Pair<Integer,TaggingElement>> delegate;
    private PropertyBasedTagger<TaggingElement,Integer,String> subject;
    private final TaggingElement [] elements = new TaggingElement[3];

    @Before
    public void setUp() throws Exception {
	delegate = new ArrayList<Pair<Integer,TaggingElement>>(); 
	subject  = new PropertyBasedTagger<TaggingElement,Integer,String>(delegate, subjectAdapter, TaggingElement.VALUE);
	elements[0] = new TaggingElement("zero");
	elements[1] = new TaggingElement("one");
	elements[2] = new TaggingElement("two");
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAdd() {
	assertTrue(subject.add(elements[0]));
    }

    @Test
    public void testAddAll() {
	assertTrue(subject.addAll(Arrays.asList(elements)));
	assertEquals(3,subject.size());
    }

    @Test
    public void testClear() {
	subject.add(elements[0]);
	subject.clear();
	assertEquals(0,subject.size());
    }

    @Test
    public void testContains() {
	subject.add(elements[0]);
	assertTrue(subject.contains(elements[0]));
    }

    @Test
    public void testContainsAll() {
	subject.add(elements[0]);
	subject.add(elements[1]);
	assertTrue(subject.containsAll(Arrays.asList(elements[0],elements[1])));
	assertFalse(subject.containsAll(Arrays.asList(elements[0],elements[1],elements[2])));
    }

    @Test
    public void testIsEmpty() {
	assertTrue(subject.isEmpty());
	subject.add(elements[0]);
	assertFalse(subject.isEmpty());
    }

    @Test
    public void testEmptyIterator() {
	Iterator<TaggingElement> iterator = subject.iterator();
	assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testFilledIterator() {
	subject.add(elements[0]);
	Iterator<TaggingElement> iterator = subject.iterator();
	assertTrue(iterator.hasNext());
	assertEquals(elements[0],iterator.next());
	assertFalse(iterator.hasNext());
    }
    
    @Test
    public void testRemovalIterator() {
	subject.add(elements[0]);
	Iterator<TaggingElement> iterator = subject.iterator();
	iterator.next();
	try{iterator.remove();fail("Removal failed to throw UnsupportedOperationException.");}
	catch(UnsupportedOperationException e){}
    }

    @Test
    public void testRemove() {
	subject.add(elements[0]);
	subject.add(elements[1]);
	assertTrue(subject.remove(elements[0]));
	assertEquals(1,subject.size());
    }

    @Test
    public void testRemoveAll() {
	subject.add(elements[0]);
	subject.add(elements[1]);
	subject.add(elements[2]);
	assertTrue(subject.removeAll(Arrays.asList(elements[0],elements[1])));
	assertEquals(1,subject.size());
    }

    @Test
    public void testRetainAll() {
	subject.add(elements[0]);
	subject.add(elements[1]);
	subject.add(elements[2]);
	assertTrue(subject.retainAll(Arrays.asList(elements[0],elements[1])));
	assertEquals(2,subject.size());
    }

    @Test
    public void testSize() {
	assertEquals(0,subject.size());
	subject.add(elements[0]);
	assertEquals(1,subject.size());
    }
    
    private static final class TaggingElement implements PropertyListenable{
	public static final String VALUE       = "value";
	private final PropertyChangeSupport pcs= new PropertyChangeSupport(this);
	private String value = "";
	
	public TaggingElement(String value){
	    setValue(value);
	}
	
	private void setValue(String newValue){
	    if(newValue==null)
		throw new NullPointerException("newValue intolerably null.");
	    pcs.firePropertyChange(VALUE, value, newValue);
	    this.value=newValue;
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

	/**
	 * @return the value
	 */
	public String getValue() {
	    return value;
	}
    }//end TaggingElement

}//end PropertyBasedTagger
