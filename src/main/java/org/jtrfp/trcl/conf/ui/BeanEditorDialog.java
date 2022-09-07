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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jtrfp.trcl.gui.TRBeanUtils;

import lombok.Getter;
import lombok.Setter;

public class BeanEditorDialog extends JDialog {
    private static final long serialVersionUID = -242311278263112643L;
    private final JPanel rootPanel = new JPanel(), bottomButtonPanel = new JPanel();
    private final JButton applyButton = new JButton("Apply And Close"), revertButton = new JButton("Revert"), cancelButton = new JButton("Cancel");
    private BeanEditor beanEditor = new BeanEditor();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    JFrame testFrame = new JFrame();
		    BeanEditorDialog prototype = new BeanEditorDialog(testFrame, new TestBean(), Collections.emptySet());
		    testFrame.setVisible(true);
		    prototype.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }//end main()

    /**
     * Create the application.
     */
    public BeanEditorDialog(Frame owner, Object target, Set<Annotation> annotations) {
	super(owner, true);
	rootPanel.setLayout(new BorderLayout());
	getContentPane().add(rootPanel, BorderLayout.CENTER);
	initEditor(owner, target, annotations);
	initBottomButtons();
	setLocation(owner.getLocation());
	pack();
    }
    
    private void initBottomButtons() {
	rootPanel.add(bottomButtonPanel, BorderLayout.SOUTH);
	bottomButtonPanel.add(applyButton);
	bottomButtonPanel.add(revertButton);
	bottomButtonPanel.add(cancelButton);
	
	applyButton.addActionListener((ActionEvent evt)->{beanEditor.proposeApplySettings(); this.dispose();});
	revertButton.addActionListener((ActionEvent evt)->{beanEditor.proposeRevertSettings(System.currentTimeMillis());});
	cancelButton.addActionListener((ActionEvent evt)->{this.dispose();});
    }//end initBottomButtons
    
    @Getter @Setter
    private static final class TestBean {
	@Getter(onMethod=@__({@ConfigByUI(editorClass=TextFieldUI.class)}))
	private String mutableStringProperty = "Name Here";
    }//end TestBean

    /**
     * Initialize the contents of the frame.
     */
    private void initEditor(Frame owner, Object target, Set<Annotation> annotations) {
	beanEditor.configure((x)->{}, ()->target, annotations, null);
	rootPanel.add(beanEditor.getUIComponent(), BorderLayout.CENTER);
	setSize(new Dimension(300,500));
	setTitle("Editor: "+TRBeanUtils.camelCaseToSentence(target.getClass().getSimpleName()));
	beanEditor.proposeRevertSettings(System.currentTimeMillis());
    }//end initialize()

}//end BeanEditorDialog
