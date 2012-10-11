/*******************************************************************************
 * Copyright (c) 2012 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.io.File;

public class RunMe
	{
	
	public static void main(String [] args)
		{
		System.out.println(
				"\t\t\t***TERMINAL RECALL***\n"+
				"	An unofficial engine remake for Terminal Velocity and Fury3.\n"+
				"	Copyright (c) 2012 Chuck Ritola and contributors. See enclosed CREDITS file for details.\n\n"+
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
				"	THIS PROGRAM IS NOT SUPPORTED BY OR AFFILIATED WITH MICROSOFT OR TERMINAL REALITY INC.\n"+
				"	Bring any issues to the Terminal Recall google code page.\n"
				);
		if(args.length==2)
			{
			try
				{
				System.out.println("Loading POD: "+args[0]+" Loading level: "+args[1]);
				TR.wakeUp(new File(args[0]));
				new OverworldGame(TR.resources.getLVL(args[1]));
				}
			catch(Exception e) {e.printStackTrace();}
			}//end if(good)
		else
			{//fail
			System.out.println("USAGE: TerminalRecall [path_to_POD_file] [level_name.LVL]");
			}
		}
	}//end Run
