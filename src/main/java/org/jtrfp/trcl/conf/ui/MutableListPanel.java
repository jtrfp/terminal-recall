/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2022 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.conf.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import lombok.Getter;
import lombok.Setter;

public class MutableListPanel<ELEMENT_TYPE> extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = -2552077995665118448L;
    private JList<ELEMENT_TYPE> list;
    private DefaultListModel<ELEMENT_TYPE> elementLM=new DefaultListModel<>();
    @Getter
    private Collection<ELEMENT_TYPE> elements = new ArrayList<>();
    @Getter @Setter
    private Supplier<Optional<ELEMENT_TYPE>> elementFactory;
    @Getter @Setter
    private UnaryOperator<ELEMENT_TYPE> editor;
    @Getter @Setter
    private Predicate<ELEMENT_TYPE> removalFilter;
    @Getter @Setter
    private Predicate<ELEMENT_TYPE> editFilter;
    
    public MutableListPanel() {
	initialize();
    }//end constructor

    private void initialize() {
	GridBagConstraints gbc_this = new GridBagConstraints();
	gbc_this.insets = new Insets(0, 0, 5, 0);
	gbc_this.fill = GridBagConstraints.BOTH;
	gbc_this.gridx = 0;
	gbc_this.gridy = 1;
	
	GridBagLayout gbl_this = new GridBagLayout();
	gbl_this.columnWidths = new int[]{272, 0};
	gbl_this.rowHeights = new int[]{76, 0, 0};
	gbl_this.columnWeights = new double[]{1.0, Double.MIN_VALUE};
	gbl_this.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
	this.setLayout(gbl_this);

	JPanel uriListPanel = new JPanel();
	GridBagConstraints gbc_uriListPanel = new GridBagConstraints();
	gbc_uriListPanel.insets = new Insets(0, 0, 5, 0);
	gbc_uriListPanel.fill = GridBagConstraints.BOTH;
	gbc_uriListPanel.gridx = 0;
	gbc_uriListPanel.gridy = 0;
	this.add(uriListPanel, gbc_uriListPanel);
	uriListPanel.setLayout(new BorderLayout(0, 0));

	JScrollPane uriListScrollPane = new JScrollPane();
	uriListPanel.add(uriListScrollPane, BorderLayout.CENTER);

	list = new JList<>(elementLM);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	uriListScrollPane.setViewportView(list);

	JPanel uriListOpButtonPanel = new JPanel();
	uriListOpButtonPanel.setBorder(null);
	GridBagConstraints gbc_uriListOpButtonPanel = new GridBagConstraints();
	gbc_uriListOpButtonPanel.anchor = GridBagConstraints.NORTH;
	gbc_uriListOpButtonPanel.gridx = 0;
	gbc_uriListOpButtonPanel.gridy = 1;
	this.add(uriListOpButtonPanel, gbc_uriListOpButtonPanel);
	FlowLayout flowLayout = (FlowLayout) uriListOpButtonPanel.getLayout();
	flowLayout.setAlignment(FlowLayout.LEFT);

	JButton addButton = new JButton("Add...");
	uriListOpButtonPanel.add(addButton);
	addButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		if( elementFactory != null) {
		    final Optional<ELEMENT_TYPE> newElement = elementFactory.get();
		    if( newElement.isPresent() ){
			ELEMENT_TYPE element = newElement.get();
			if( editor != null )
			    element = editor.apply(element);
			elements.add(newElement.get()); elementLM.addElement(newElement.get());
			}
		}
	    }});

	JButton removeButton = new JButton("Remove");
	uriListOpButtonPanel.add(removeButton);
	removeButton.addActionListener(new ActionListener(){
	    @SuppressWarnings("unchecked")
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		if(list.getSelectedIndex() < 0)
		    return;//Nothing to do.
		final Object value = list.getSelectedValue();
		if(removalFilter != null)
		    if(!removalFilter.test((ELEMENT_TYPE)value))
			return;
		elements.remove(value);
		SwingUtilities.invokeLater(()->{elementLM.remove(list.getSelectedIndex());});
	    }});

	JButton editButton = new JButton("Edit...");
	editButton.setIcon(null);
	uriListOpButtonPanel.add(editButton);
	editButton.addActionListener(new ActionListener(){
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		final ELEMENT_TYPE oldValue = (ELEMENT_TYPE)list.getSelectedValue();
		if(editFilter != null)
		    if(!editFilter.test((ELEMENT_TYPE)oldValue))
			return;
		if(oldValue == null)
		    return;
		final ELEMENT_TYPE newValue = editor.apply(oldValue);
		if(newValue != null) {
		    SwingUtilities.invokeLater(()->{elementLM.set(list.getSelectedIndex(), newValue);});
		}
	    }});
    }//end initialize()
    
    public static interface FileChecker {
	public boolean isFileValid(File f);
    }

    public void setElements(Collection<ELEMENT_TYPE> x) {
	elements = new ArrayList<>(x);
	refreshElements();
    }
    
    protected void refreshElements() {
	SwingUtilities.invokeLater(()->{elementLM.clear();elementLM.addAll(elements);});
    }
}//end MutableListPanel
