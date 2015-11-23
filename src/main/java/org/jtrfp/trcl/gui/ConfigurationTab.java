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

import javax.swing.ImageIcon;
import javax.swing.JComponent;

public interface ConfigurationTab<CONFIG_BEAN> {
 public String     getTabName();
 public JComponent getContent();
 public ImageIcon  getTabIcon();
 public Class<CONFIG_BEAN>   getConfigBeanClass();
 public void                 setConfigBean(CONFIG_BEAN cfg);
 public CONFIG_BEAN          getConfigBean();
}//end configurationTab
