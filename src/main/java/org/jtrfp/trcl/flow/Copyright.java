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

package org.jtrfp.trcl.flow;

public class Copyright {
    private static String copyright;
    
    public static String getCopyright(){
	if(copyright==null)
	   copyright=  "\t\t\t***TERMINAL RECALL***\n"+
			"	An unofficial enhancement engine for Terminal Velocity and Fury3.\n"+
			"	Copyright (c) 2012-2015 Chuck Ritola and contributors. See enclosed CREDITS file for details.\n"+
			"	Part of the Java Terminal Reality File Parsers Project.\n\n"+
			"		This program is free software; you can redistribute it and/or modify\n"+
			"	it under the terms of the GNU General Public License as published by\n"+
			"	the Free Software Foundation; either version 3 of the License, or\n"+
			"	(at your option) any later version.\n" +
			"		This program is distributed in the hope that it will be useful,\n"+
			"	but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
			"	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"+
			"	GNU General Public License for more details.\n"+
			"   	You should have received a copy of the GNU General Public License\n"+
			"	along with this program; if not, write to the Free Software Foundation,\n"+
			"	Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA\n\n"+
			"		THIS PROGRAM IS NOT SUPPORTED BY OR AFFILIATED WITH MICROSOFT OR TERMINAL REALITY INC.\n"+
			"	Bring any issues to the Terminal Recall GitHub page.\n" +
			"	www.jtrfp.org\n";
	return copyright;
    }//end getCopyright()
    
    public static void printCopyright(){
	System.out.println(getCopyright());
    }//end printCopyright()

}//end Copyright
