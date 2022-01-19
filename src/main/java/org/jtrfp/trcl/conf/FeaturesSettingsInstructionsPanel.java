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

package org.jtrfp.trcl.conf;

import java.awt.GridLayout;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JTextPane;

public class FeaturesSettingsInstructionsPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 4952567738105924284L;

    public FeaturesSettingsInstructionsPanel() {
	initialize();
    }

    private void initialize() {
	try {
	    setLayout(new GridLayout(0,1));
	    final InputStream is = FeaturesSettingsInstructionsPanel.class.getResourceAsStream("/doc/featuresSettingsInstructions.html");
	    final String text = new String(is.readAllBytes());
	    is.close();
	    final JTextPane textPane = new JTextPane();
	    textPane.setContentType("text/html");
	    textPane.setText(text);
	    add(textPane);
	} catch(IOException e) {e.printStackTrace();}
    }//end initialize()
}//end FeaturesSettingsInstructionsPanel
