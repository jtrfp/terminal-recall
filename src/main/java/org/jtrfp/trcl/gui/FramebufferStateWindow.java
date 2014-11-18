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

package org.jtrfp.trcl.gui;

import java.awt.Component;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import org.jtrfp.trcl.core.Renderer;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.gpu.GLFrameBuffer;

public class FramebufferStateWindow extends JFrame {
    private final JPanel rootPanel = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane(rootPanel);
    private boolean initialized=false;
    private final TR tr;

    /**
     * 
     */
    private static final long serialVersionUID = -3261505603514572783L;
    
    public FramebufferStateWindow(TR tr){
	setTitle("Framebuffer States");
	setSize(280,600);
	rootPanel.setLayout(new BoxLayout(rootPanel,BoxLayout.PAGE_AXIS));
	scrollPane.setAlignmentX(LEFT_ALIGNMENT);
	add(scrollPane);
	this.setDefaultCloseOperation(HIDE_ON_CLOSE);
	this.tr=tr;
	}//end for(descriptors)
	
    private void initialize() {
	initialized=true;
	// Sound frameBuffer
	final GLFrameBuffer sfb = tr.soundSystem.get()
		.getSoundOutputFrameBuffer();
	if (sfb.getAttached2DDrawTextures().size() > 0) {
	    final PropertyEditor pe = TRBeanUtils.getDefaultPropertyEditor(sfb);
	    final Component ed = pe.getCustomEditor();
	    final JPanel subPanel = new JPanel();
	    subPanel.add(ed);
	    Border border = new TitledBorder(new SoftBevelBorder(
		    SoftBevelBorder.RAISED), "SoundFrameBuffer");
	    subPanel.setBorder(border);
	    rootPanel.add(subPanel);
	}// end if(size>0)
	final Renderer renderer = tr.renderer.get();
	BeanInfo rendererBeanInfo = null;
	try {
	    rendererBeanInfo = Introspector.getBeanInfo(Renderer.class);
	} catch (IntrospectionException e) {
	    tr.showStopper(e);
	}
	final PropertyDescriptor[] pDescs = rendererBeanInfo
		.getPropertyDescriptors();
	for (PropertyDescriptor desc : pDescs) {
	    if (desc.getPropertyType() == GLFrameBuffer.class) {
		try{
		   final GLFrameBuffer fb = (GLFrameBuffer)desc.getReadMethod().invoke(renderer);
		    if (fb.getAttached2DDrawTextures().size() > 0) {
			final PropertyEditor pe = TRBeanUtils
				.getDefaultPropertyEditor(fb);
			final Component ed = pe.getCustomEditor();
			final JPanel subPanel = new JPanel();
			subPanel.add(ed);
			Border border = new TitledBorder(new SoftBevelBorder(
				SoftBevelBorder.RAISED), desc.getDisplayName());
			subPanel.setBorder(border);
			rootPanel.add(subPanel);
		    }//end if(size>0)
		   }//end try{}
		catch(Exception e){tr.showStopper(e);}
	    }// end if(GLFrameBuffer)
	}//end for(propertyDescriptors)
    }//end initialize()
    
    @Override
    public void setVisible(boolean visible){
	if(visible){
	    if(!initialized)
		initialize();
	}//end if(visible)
	super.setVisible(visible);
    }//end setVisible(...)

}//end FrameBufferStateWindow
