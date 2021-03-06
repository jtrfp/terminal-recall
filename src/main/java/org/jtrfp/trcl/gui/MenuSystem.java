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

import java.awt.event.ActionListener;

public interface MenuSystem {
 public static double BEGINNING = 0, END = 1, MIDDLE = Utils.between(BEGINNING,END);
 
 public void setMenuPosition(double position, String ... path);
 public void addMenuItem   (double position, String ... path) throws IllegalArgumentException;
 public void removeMenuItem(String ... path) throws IllegalArgumentException;
 public void addMenuItemListener  (ActionListener l, String ... path) throws IllegalArgumentException;
 public void removeMenuItemListener(ActionListener l, String ... path) throws IllegalArgumentException;
 public void setMenuItemEnabled(boolean enabled, String ... path)     throws IllegalArgumentException;
 
 public abstract class Utils {
     public static double between(double first, double second){
	 return (first+second) / 2.;
     }//end between(...)
 }//end Utils
}//end MenuSystem
