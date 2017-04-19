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

import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JSliderPropertyBinding extends AbstractPropertyBinding<Double> implements ChangeListener {
    private final JSlider slider;
    private final double scalar;

    public JSliderPropertyBinding(JSlider slider, double scalar, Object bindingBean,
	    String propertyName, Class<Double> propertyType) {
	super(propertyName, bindingBean, propertyType);
	if( slider == null )
	    throw new NullPointerException("Supplied JSlider intolerably null.");
	this.slider = slider;
	this.scalar = scalar;
	slider.addChangeListener(this);
	setUIValue(super.getPropertyValue());//The auto-set doesn't work because slider is null.
    }

    @Override
    protected void setUIValue(final Double newValue) {
	final JSlider slider = this.slider;
	final double  scalar = this.scalar;
	if( slider == null )
	    return;
	SwingUtilities.invokeLater(new Runnable(){
	    @Override
	    public void run() {
		double v = (newValue != null? newValue: 1);
		slider.setValue((int)(v / scalar));
	    }});
    }

    @Override
    public void stateChanged(ChangeEvent event) {
	setPropertyValue((float)slider.getValue() * scalar);
    }

}//end JSliderPropertyBinding
