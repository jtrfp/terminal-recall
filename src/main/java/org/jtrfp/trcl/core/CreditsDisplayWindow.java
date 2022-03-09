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

package org.jtrfp.trcl.core;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

public class CreditsDisplayWindow extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 4921828042869209099L;
    private JPanel contentPane;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    CreditsDisplayWindow frame = new CreditsDisplayWindow();
		    frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the frame.
     */
    public CreditsDisplayWindow() {
    	setTitle("About Terminal Recall");
	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	setBounds(100, 100, 450, 500);
	contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	contentPane.setLayout(new BorderLayout(0, 0));
	setContentPane(contentPane);
	
	JScrollPane scrollPane = new JScrollPane();
	contentPane.add(scrollPane, BorderLayout.CENTER);
	
	JTextArea creditsDisplay = new JTextArea();
	scrollPane.setViewportView(creditsDisplay);

	try {
	    final BufferedReader creditsReader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/credits.txt")));
	    creditsDisplay.read(creditsReader, "About Terminal Recall");
	} catch(Exception e) {e.printStackTrace();}
    }

}
