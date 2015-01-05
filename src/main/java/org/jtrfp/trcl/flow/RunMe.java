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
package org.jtrfp.trcl.flow;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map.Entry;

import org.jtrfp.jtrfp.pod.PodFile;
import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.core.TRConfiguration;
import org.jtrfp.trcl.file.VOXFile;

public class RunMe{
	public static void main(String [] args){
		ensureJVMIsProperlyConfigured(args);
		System.out.println(
				"\t\t\t***TERMINAL RECALL***\n"+
				"	An unofficial enhancement engine for Terminal Velocity and Fury3.\n"+
				"	Copyright (c) 2012-2014 Chuck Ritola and contributors. See enclosed CREDITS file for details.\n"+
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
				"	www.jtrfp.org\n"
				);
		new TR();
		}//end main()
	
    private static void ensureJVMIsProperlyConfigured(String[] args) {
	if (!isAlreadyConfigured()) {
	    System.out
		    .println("Overriding the default JVM settings. If you wish to manually set the JVM settings, include the `-Dorg.jtrfp.trcl.bypassConfigure=true` flag in the java command.");
	    String executable = new File("RunMe.jar").exists() ? "-jar RunMe.jar"
		    : "-cp " + System.getProperty("java.class.path")
			    + " org.jtrfp.trcl.flow.RunMe";
	    String cmd = "java -Xmx1024M -Dorg.jtrfp.trcl.bypassConfigure=true -Dcom.sun.management.jmxremote "
		    + "-XX:+UnlockExperimentalVMOptions -XX:+DoEscapeAnalysis -XX:+UseFastAccessorMethods "
		    + "-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:MaxGCPauseMillis=5 -XX:+AggressiveOpts "
		    + "-XX:+UseBiasedLocking -XX:+AlwaysPreTouch -XX:ParallelGCThreads=4 -Xms512m -Xmx768m ";
	    for (Entry<Object,Object> property:System.getProperties().entrySet()){
		if(property.getKey().toString().startsWith("org.jtrfp")&&!property.getKey().toString().toLowerCase().contains("org.jtrfp.trcl.bypassconfigure"))
		    cmd += " -D"+property.getKey()+"="+property.getValue()+" ";
	    }//end for(properties)
	    cmd		+= executable;
	    for (String arg : args) {
		cmd += " " + arg;
	    }
	    try {
		System.out.println("Restarting JVM with: \n\t" + cmd);
		final Process proc = Runtime.getRuntime().exec(cmd);
		Thread tOut = new Thread() {
		    public void run() {
			int bytesRead;
			byte[] buffer = new byte[1024];
			try {
			    while ((bytesRead = proc.getInputStream().read(
				    buffer)) != -1) {
				System.out.write(buffer, 0, bytesRead);
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }// end run()
		};
		Thread tErr = new Thread() {
		    public void run() {
			int bytesRead;
			byte[] buffer = new byte[1024];
			try {
			    while ((bytesRead = proc.getErrorStream().read(
				    buffer)) != -1) {
				System.err.write(buffer, 0, bytesRead);
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			}
		    }// end run()
		};
		tOut.start();
		tErr.start();
		tOut.join();
		tErr.join();
	    }// end try{}
	    catch (Exception e) {
		e.printStackTrace();
	    }
	    System.exit(0);
	}
    }// end Ensure...Configured(...)

    private static boolean isAlreadyConfigured() {
	return System.getProperty("org.jtrfp.trcl.bypassConfigure") != null?System.getProperty("org.jtrfp.trcl.bypassConfigure").toUpperCase().contentEquals("TRUE"):false;
    }
}// end RunMe
