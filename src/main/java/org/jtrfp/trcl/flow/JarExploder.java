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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * UNFINISHED
 * This is to aid in exploding JAR-packed external classpath libs at startup.
 * @author Chuck Ritola
 *
 */
public class JarExploder {
    
    public static void deployNativeLibs() {
	if (isRunningFromJAR()) {
	    System.out.println("Detected run from JAR");
	    deployNativeFilesFromJar(getRunningPath());
	}// end if(jar)
    }// end deployNativeLibs()

    public static boolean isRunningFromJAR() {
	return JarExploder.class.getResource("JARNativeLibDeployment.class").toString()
		.startsWith("jar:");
    }
    
    public static File getRunningPath(){
	try{return new File(JarExploder.class
		.getProtectionDomain().getCodeSource().getLocation().toURI());}
	catch(URISyntaxException e){e.printStackTrace();return null;}
    }
    
    public static File getCWD(){
	return new File(System.getProperty("user.dir"));
    }
    
    public static void deployNativeFilesFromJar(File jar){
	//JarFile jf = null;
	try(JarFile jf = new JarFile(jar)) {
	    byte [] buffer = new byte[1024];
	    int read=0;
	    Enumeration<JarEntry> entries = jf.entries();
	    JarEntry entry;
	    while(entries.hasMoreElements()){
		entry = entries.nextElement();
		final String rawName = entry.getName();
		final String name = rawName.toLowerCase();
		if(name.endsWith(".so")||name.endsWith(".dll")){
		    System.out.println("entry="+entry.getName());
		    int delimiterIndex=rawName.lastIndexOf("\\");
		    delimiterIndex=delimiterIndex!=-1?delimiterIndex:0;
		    delimiterIndex=Math.max(delimiterIndex, rawName.lastIndexOf("/"));
		    String outFileName = entry.getName().substring(delimiterIndex);
		    File f = new File(getCWD().getAbsolutePath()+System.getProperty("file.separator")+outFileName);
		    System.out.println(""+f.getAbsolutePath());
		    if(!f.exists()){f.createNewFile();f.deleteOnExit();}
		    InputStream is = jf.getInputStream(entry);
		    FileOutputStream os = new FileOutputStream(f);
		    while((read=is.read(buffer))!=-1){
			os.write(buffer, 0, read);
		    }//end while(...)
		    os.close();
		}//end if(endsWith)
	    }//end while(entries)
	}catch(Exception e){e.printStackTrace();}
    }//end getNativeFilesFromJar(...)

    public static void main(String[] args) {
	System.out.println(isRunningFromJAR());
    }

    public static void explodeLibFiles() {
	// TODO Auto-generated method stub
	
    }
}// end JARNativeLibDeployment
