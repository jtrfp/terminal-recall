/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012, 2013 Chuck Ritola.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the COPYING and CREDITS files for more details.
 * 
 * Contributors:
 *      chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.flow;

import java.io.File;
import java.io.PrintStream;
import java.util.Map.Entry;

import javax.media.opengl.GL3;

import org.jtrfp.trcl.GameSetup;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.dbg.Reporter;
import org.jtrfp.trcl.gpu.GPU;

public class RunMe{
	public static void main(String [] args){
		ensureJVMIsProperlyConfigured(args);
		System.out.println(
				"\t\t\t***TERMINAL RECALL***\n"+
				"	An unofficial engine remake for Terminal Velocity and Fury3.\n"+
				"	Copyright (c) 2012,2013 Chuck Ritola and contributors. See enclosed CREDITS file for details.\n\n"+
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
				"	Bring any issues to the Terminal Recall google code page.\n"
				);
		if(args.length>=2)
			{
			try {
				TR tr = new TR();
				tr.gatherSysInfo();
				for(int argI=0; argI<args.length-1; argI++)
					{tr.getResourceManager().registerPOD(new File(args[argI]));}
				new GameSetup(tr.getResourceManager().getLVL(args[args.length-1]),tr);
				}
			catch(Exception e) {e.printStackTrace();}
			}//end if(good)
		else
			{//fail
			System.out.println("USAGE: TerminalRecall [path_to_POD_file0] [path_to_POD_file1] [...] [level_name.LVL]");
			}
		}//end aspectMain
	
	
	
	private static void ensureJVMIsProperlyConfigured(String [] args)
		{
		if(!isAlreadyConfigured())
			{
			System.out.println("Overriding the settings passed to this JVM. If you wish to manually set the JVM settings, include the `-Dorg.jtrfp.trcl.bypassConfigure=true` flag in the java command.");
			String executable=new File("RunMe.jar").exists()?"-jar RunMe.jar":"-cp "+System.getProperty("java.class.path")+" org.jtrfp.trcl.flow.RunMe";
			String cmd="java -Xmx1024M -Dorg.jtrfp.trcl.bypassConfigure=true "+executable;
			for(String arg:args){cmd+=" "+arg;}
			try {
				System.out.println("Restarting JVM with: \n\t"+cmd);
				final Process proc = Runtime.getRuntime().exec(cmd);
			    Thread tOut=new Thread()
			    	{public void run()
			    		{
			    		int bytesRead;
			    		byte[] buffer = new byte[1024];
			    		try{
			    		 while ((bytesRead = proc.getInputStream().read(buffer)) != -1)
						    {System.out.write(buffer, 0, bytesRead);}
			    			}catch(Exception e){e.printStackTrace();}
			    		}//end run()
			    	};
		    	Thread tErr=new Thread()
			    	{public void run()
			    		{int bytesRead;
			    		byte[] buffer = new byte[1024];
			    		try{
			    		 while ((bytesRead = proc.getErrorStream().read(buffer)) != -1)
						    {System.err.write(buffer, 0, bytesRead);}
			    			}catch(Exception e){e.printStackTrace();}
			    		}//end run()
			    	};
				tOut.start();tErr.start();
				tOut.join();tErr.join();
				}//end try{}
			catch(Exception e){e.printStackTrace();}
			System.exit(0);
			}
		}//end Ensure...Configured(...)
	
	private static boolean isAlreadyConfigured()
		{return System.getProperty("org.jtrfp.trcl.bypassConfigure")!=null;}
	}//end Run
