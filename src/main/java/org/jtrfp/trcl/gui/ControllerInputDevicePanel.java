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
package org.jtrfp.trcl.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jtrfp.trcl.core.ControllerInput;
import org.jtrfp.trcl.core.ControllerInputs;
import org.jtrfp.trcl.core.ControllerMapper;
import org.jtrfp.trcl.core.ControllerMapping;
import org.jtrfp.trcl.core.ControllerSource;
import org.jtrfp.trcl.core.InputDevice;
import org.jtrfp.trcl.core.MappingListener;

public class ControllerInputDevicePanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 4252247402423635792L;
    private final InputDevice                  inputDevice;
    private static final String                NONE = "[none]";
    private final ControllerInputs controllerInputs;
    private JComboBox<String>      destBox;
    private JTable table;
    private ControllerMapper       controllerMapper;
    private ArrayList<Object[]> rowData;
    private volatile boolean dispatching = false;
    
    private enum Columns{
	SOURCE("Source"),
	VALUE("Value"),
	DEST("Destination"),
	SCALAR("Scalar"),
	OFFSET("Offset");
	
	private String title;
	Columns(String title){
	    this.title=title;
	}
	public String getTitle(){
	    return title;
	}
    }//end Columns
    
    private class MonitorCollection implements Collection<String>{

	@Override
	public int size() {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public boolean isEmpty() {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public boolean contains(Object o) {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public Iterator<String> iterator() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Object[] toArray() {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public <T> T[] toArray(T[] a) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public boolean add(String item) {
		destBox.addItem(item);
	    return true;
	}//end add(...)

	@Override
	public boolean remove(Object item) {
		destBox.removeItem(item);
	    return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public boolean addAll(Collection<? extends String> c) {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	    // TODO Auto-generated method stub
	    return false;
	}

	@Override
	public void clear() {
		destBox.removeAllItems();
		destBox.addItem(NONE);
	}//end clear()
    }//end MonitorCollection
    
    private class ControllerTableModelListener implements TableModelListener{
	@Override
	public void tableChanged(TableModelEvent e) {
	    if(isDispatching())
		return;
	    final int row = e.getFirstRow();
	    if(e.getType()==TableModelEvent.UPDATE && e.getSource() != ControllerInputDevicePanel.this && e.getColumn() != Columns.VALUE.ordinal()){
		final TableModel model = table.getModel();
		final String inputString = (String)model.getValueAt(row,Columns.DEST.ordinal());
		final String srcString   = (String)model.getValueAt(row, Columns.SOURCE.ordinal());
		final double scale  = Double.parseDouble((String)model.getValueAt(row, Columns.SCALAR.ordinal()));
		final double offset = Double.parseDouble((String)model.getValueAt(row, Columns.OFFSET.ordinal()));
		final ControllerSource controllerSource = inputDevice.getSourceByName(srcString);
		final ControllerInput  controllerInput  = controllerInputs.getControllerInput(inputString);
		setDispatching(true);
		controllerMapper.unmapControllerSource(controllerSource);
		controllerMapper.mapControllerSourceToInput(controllerSource, controllerInput, scale, offset);
		setDispatching(false);
	    }
	}//end tableChanged(...)
    }//end ControllerTableModelListener
    
    private class ControllerMappingListener implements MappingListener<ControllerSource,ControllerMapping>{
	@Override
	public void mapped(ControllerSource key, ControllerMapping value) {
	    fireControllerSourceMapped(key,value);
	}

	@Override
	public void unmapped(ControllerSource key) {
	    fireControllerSourceUnmapped(key);
	}
    }//end ControllerMappingListener

    private void fireControllerSourceUnmapped(ControllerSource cSource){
	//Check for relevance to this panel
	final int row = getRowFor(cSource);
	if(row==-1)
	    return;//Ignore
	final TableModel model = table.getModel();
	//Set destination
	model.setValueAt(NONE, row, Columns.DEST.ordinal());
	//Set scalar
	model.setValueAt("1.0", row, Columns.SCALAR.ordinal());
	//Set offset
	model.setValueAt("0.0", row, Columns.OFFSET.ordinal());
    }//end fireControllerSourceUnmapped()
    
    private void fireControllerSourceMapped(ControllerSource cSource, ControllerMapping value){
	//Check for relevance to this panel
	final int row = getRowFor(cSource);
	if(row==-1)
	    return;//Ignore
	final TableModel model = table.getModel();
	//Set destination
	model.setValueAt(value.getControllerInput().getName(), row, Columns.DEST.ordinal());
	//Set scalar
	model.setValueAt(value.getScale()+"", row, Columns.SCALAR.ordinal());
	//Set offset
	model.setValueAt(value.getOffset()+"", row, Columns.OFFSET.ordinal());
    }//end fireControllerSourceMapped(...)
    
    private int getRowFor(ControllerSource cSource){
	//Check for relevance to this panel
	if(cSource.getInputDevice() != inputDevice)
	    return -1;
	final int col = Columns.SOURCE.ordinal();
	final String sourceString = cSource.getName();
	int row = -1;
	final TableModel model = table.getModel();
	//Find the row containing this sourceString
	for(int i=0; i<model.getRowCount(); i++){
	    if(((String)model.getValueAt(i, col)).contentEquals(sourceString))
		row=i;
	}//end for(model rows)
	if(row==-1)
	    return -1; //Not found in this table. Ignore.
	return row;
    }//end getRowFor(...)
    
    private class InputStateFeedbackMonitor implements PropertyChangeListener {
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	    final TableModel model = table.getModel();
	    final int row = getRowFor((ControllerSource)evt.getSource());
	    if(row!=-1)
	      model.setValueAt(evt.getNewValue()+"", row, Columns.VALUE.ordinal());
	}
    }//end InputStateFeedbackMonitor
    
    private final Collection<String> monitoringCollection = new MonitorCollection();
    private final InputStateFeedbackMonitor inputStateFeedbackMonitor = new InputStateFeedbackMonitor();

    public ControllerInputDevicePanel(InputDevice id, ControllerInputs ci, ControllerMapper mapper) {
	this.inputDevice = id;
	this.controllerInputs = ci;
	this.controllerMapper = mapper;
	this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	rowData = new ArrayList<Object[]>(id.getControllerSources().size());
	for(ControllerSource cs: id.getControllerSources()){
	    rowData.add(new String[]{cs.getName(),"?",NONE,"1.0","0.0"});
	    cs.addPropertyChangeListener(inputStateFeedbackMonitor);}
	final String [] columns = new String[Columns.values().length];
	for(int i=0; i<columns.length; i++)
	    columns[i]=Columns.values()[i].getTitle();
	table   = new JTable(rowData.toArray(new Object[rowData.size()][]),columns);
	destBox = new JComboBox<String>();
	destBox.addItem(NONE);
	final TableColumnModel cModel = table.getColumnModel();
	cModel.getColumn(Columns.DEST  .ordinal()).setCellEditor(new DefaultCellEditor(destBox));
	cModel.getColumn(Columns.VALUE .ordinal()).setPreferredWidth(20);
	cModel.getColumn(Columns.SCALAR.ordinal()).setPreferredWidth(20);
	cModel.getColumn(Columns.OFFSET.ordinal()).setPreferredWidth(20);
	table.getModel().addTableModelListener(new ControllerTableModelListener());
	mapper.addMappingListener(new ControllerMappingListener(), true);
	JScrollPane tableScrollPane = new JScrollPane(table);
	table.setFillsViewportHeight(true);
	this.add(tableScrollPane);
	ci.getInputNames().addTarget(monitoringCollection, true);
    }//end ControllerInputDevicePanel
    
    public boolean isDispatching() {
        return dispatching;
    }

    public void setDispatching(boolean dispatching) {
        this.dispatching = dispatching;
    }
    
}//end ControllerInputDevicePanel
