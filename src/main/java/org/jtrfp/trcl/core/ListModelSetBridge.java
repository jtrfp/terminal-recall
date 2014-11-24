/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2014 Chuck Ritola
 * Part of the jTRFP.org project
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/

package org.jtrfp.trcl.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ListModelSetBridge<E> {
    private final ListModel<E> listModel;
    private final Set<E> checkerSet = new HashSet<E>();
    private final SetModelListener<E> setML;
    private final ArrayList<E> list = new ArrayList<E>();
    public ListModelSetBridge(ListModel<E> _listModel, SetModelListener<E> _set){
	this.listModel=_listModel;
	this.setML=_set;
	//Initial setup
	for(int i=0; i<listModel.getSize();i++){
	    final E element = listModel.getElementAt(i);
	    list.add(element);
	    checkerSet.add(element);
	}//end for(i)
	for(E element:checkerSet)
	    setML.added(element);
	listModel.addListDataListener(new ListDataListener(){
	    @Override
	    public void contentsChanged(ListDataEvent evt) {
		for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++){
		    E newE = listModel.getElementAt(i);
		    E oldE = list.set(i, newE);
		    if(!list.contains(oldE)){
			if(checkerSet.remove(oldE)){
			    setML.removed(oldE);}
			}
		    if(checkerSet.add(newE))
			setML.added(newE);
		}//end for(i)
	    }//end contentsChanged

	    @Override
	    public void intervalAdded(ListDataEvent evt) {
		for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++){
		    E newE = listModel.getElementAt(i);
		    list.add(newE);
		    if(checkerSet.add(newE))
		     setML.added(newE);
		}//end for(indices)
	    }//end intervalAdded()

	    @Override
	    public void intervalRemoved(ListDataEvent evt) {
		for(int i=evt.getIndex0(); i<=evt.getIndex1(); i++){
		    E oldE = list.remove(evt.getIndex0());
		    setML.removed(oldE);
		    if(!list.contains(oldE))
			checkerSet.remove(oldE);
		}//end for(i)
	    }});
    }//end constructor
}//end ListModelSetBridge
