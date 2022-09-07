/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2017 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.flow;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

public class ComboBoxPropertyBinding extends AbstractPropertyBinding<String> implements ItemListener {
    private final JComboBox<String> comboBox;
    public ComboBoxPropertyBinding(JComboBox<String> comboBox, Object bindingBean,String propertyName) {
	super(propertyName, bindingBean, String.class);
	comboBox.addItemListener(this);
	this.comboBox = comboBox;
    }

    private final CollectionActionSink comboBoxContents = new CollectionActionSink();

    private class CollectionActionSink implements Collection<String>{

	@Override
	public boolean add(final String element) {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    model.addElement(element);
		}});
	    return true;
	}

	@Override
	public boolean addAll(final Collection<? extends String> elements) {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    for(String element:elements)
		    model.addElement(element);
		}});
	    return true;
	}//end addAll(...)

	@Override
	public void clear() {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    model.removeAllElements();
		}});
	}

	@Override
	public boolean contains(Object arg0) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    return model.getSize() == 0;
	}

	@Override
	public Iterator<String> iterator() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final Object element) {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    model.removeElement(element);
		}});
	    return true;
	}

	@Override
	public boolean removeAll(final Collection<?> elements) {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    SwingUtilities.invokeLater(new Runnable(){
		@Override
		public void run() {
		    for(Object element:elements)
		    model.removeElement(element);
		}});
	    return true;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
	    throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
	    final DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)comboBox.getModel();
	    return model.getSize();
	}

	@Override
	public Object[] toArray() {
	    throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
	    throw new UnsupportedOperationException();
	}

    }//end CollectionActionSink

    public Collection<String> getComboBoxContents() {
	return comboBoxContents;
    }

    public JComboBox<String> getComboBox() {
        return comboBox;
    }

    @Override
    protected void setUIValue(final String newValue) {
	final JComboBox<String> comboBox = getComboBox();
	if(comboBox == null)
	    return;//Not ready
	SwingUtilities.invokeLater(new Runnable(){
	    @Override
	    public void run() {
		comboBox.setSelectedItem(newValue);
	    }});
    }

    @Override
    public void itemStateChanged(ItemEvent evt) {
	if(evt.getStateChange() == ItemEvent.SELECTED){
	    final Object itemObject = evt.getItem();
	    if(this.getPropertyType().isAssignableFrom(itemObject.getClass()))
		this.setPropertyValue((String)itemObject);
	}//end if(SELECTED)
    }
}//end ComboBoxPropertyBinding
