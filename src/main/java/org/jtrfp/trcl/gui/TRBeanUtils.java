/*******************************************************************************
 * This file is part of TERMINAL RECALL
 * Copyright (c) 2012-2022 Chuck Ritola
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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;

public class TRBeanUtils {
    public static PropertyEditor getDefaultPropertyEditor(Object o){
	try{final PropertyEditorSupport pe = (PropertyEditorSupport)PropertyEditorManager.findEditor(o.getClass());
	pe.setSource(o);
	return pe;}
	catch(Exception e){e.printStackTrace();}
	return null;
    }//end getDefaultPropertyEditor(...)

    public static String camelCaseToSentence(String camelCase) {
	String sentence = "";
	final char [] camelChars = camelCase.toCharArray();
	for( int i = 0; i < camelChars.length; i++ ) {
	    boolean addSpace = ( Character.isUpperCase(camelChars[i]));

	    if( i+1 < camelChars.length) {
		if( Character.isUpperCase(camelChars[i+1]) || Character.isDigit(camelChars[i+1]) )
		    addSpace = false;
	    } else if( Character.isUpperCase(camelChars[i]) || Character.isDigit(camelChars[i]))
		addSpace = false;
	    if( i > 0 && Character.isLowerCase(camelChars[i-1]) && Character.isUpperCase(camelChars[i]))
		addSpace = true;
	    if( i == 0)
		addSpace = false;
	    if(addSpace)
		sentence += " ";
	    if( i == 0 )
		sentence += Character.toUpperCase(camelChars[i]);
	    else
		sentence += camelChars[i];
	}//end for(chars)
	return sentence;
    }//end camelCaseToSentence()
}//end TRBeanUtils
