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

package org.jtrfp.trcl.gui;

import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jtrfp.trcl.flow.AbstractPropertyBinding;

public class LabelPropertyBinding<PROPERTY_TYPE>
extends AbstractPropertyBinding<PROPERTY_TYPE> {
    private final JLabel label;
    public LabelPropertyBinding(String propertyName, Object bindingBean, JLabel label, Class<PROPERTY_TYPE> propertyType){
	super(propertyName, bindingBean, propertyType);
	this.label = label;
    }//end constructor

    @Override
    protected void setUIValue(final PROPERTY_TYPE newValue) {
	SwingUtilities.invokeLater(new Runnable(){
	    @Override
	    public void run() {
		if(label != null)
		    label.setText(newValue.toString());
	    }});
    }//end setUIValue
}//end LabelPropertyBinding
