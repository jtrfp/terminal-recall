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

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;
/**
 * A bidirectional binding between an AbstractButton (JCheckbox, JRadioButton, etc) and 
 * a bean property.
 * @author Chuck Ritola
 *
 */
public class CheckboxPropertyBinding extends AbstractPropertyBinding<Boolean> implements ItemListener {
    private final AbstractButton button;
    
    public CheckboxPropertyBinding(AbstractButton button, Object bindingBean, String propertyName){
	super(propertyName, bindingBean, Boolean.class);
	this.button = button;
	button.addItemListener(this);
    }//end constructor

    @Override
    public void itemStateChanged(ItemEvent evt) {
	final int stateChange = evt.getStateChange();
	try{switch(stateChange){
	    case ItemEvent.SELECTED:
		setPropertyValue(Boolean.TRUE);
		break;
	    case ItemEvent.DESELECTED:
		setPropertyValue(Boolean.FALSE);
		break;
	    default://Do nothing
	    }
	}catch(Exception e){throw new RuntimeException(e);}
    }//end itemStateChanged(...)

    @Override
    protected void setUIValue(final Boolean newValue) {
	final AbstractButton button = getButton();
	if( button == null )
	    return;// Not ready.
	SwingUtilities.invokeLater(new Runnable(){
	    @Override
	    public void run() {
		System.out.println(getPropertyName()+" setUIValue: "+newValue);
		button.setSelected(newValue);
	    }});
    }

    public AbstractButton getButton() {
        return button;
    }

}//end CheckboxPropertyBinding
