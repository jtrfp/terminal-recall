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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class JVM {
    private String []args;
    private static final boolean runningFromJar = new File("RunMe.jar").exists();

    /**
     * @return the args
     */
    public String []getArgs() {
        return args;
    }

    /**
     * @param args the args to set
     */
    public JVM setArgs(String []args) {
        this.args = args;
        return this;
    }
    
    public JVM ensureProperlyConfigured() {//Skip if using 32-bit.
	final boolean is64Bit = System.getProperty("os.arch").contains("64");
	final boolean isServerVM = System.getProperty("java.vm.name").toLowerCase().contains("server");
	if (!isAlreadyConfigured()) {
	    //http://stackoverflow.com/questions/13029915/how-to-programmatically-test-if-assertions-are-enabled
	    //Seems to work better than the official way of querying assertion ability.
	    boolean useAssertions = false;
	    assert useAssertions = true;
	    System.out
		    .println("Overriding the default JVM settings. If you wish to manually set the JVM settings, include the `-Dorg.jtrfp.trcl.bypassConfigure=true` flag in the java command.");
	    String executable = isRunningFromJar() ? "-jar RunMe.jar"
		    : "-cp " + System.getProperty("java.class.path")
			    + " org.jtrfp.trcl.flow.RunMe";
	    StringBuilder commandBuilder = new StringBuilder();
	    
	    commandBuilder.append("java ");
	    
	    commandBuilder.append(isServerVM?"-server ":"-client ");
	    commandBuilder.append("-Dorg.jtrfp.trcl.bypassConfigure=true ");
	    commandBuilder.append("-Dcom.sun.management.jmxremote ");
	    
	    if( is64Bit && isServerVM) {
		commandBuilder.append("-XX:+UnlockExperimentalVMOptions ");
		commandBuilder.append("-XX:+DoEscapeAnalysis ");
		commandBuilder.append("-XX:+UseFastAccessorMethods ");
		commandBuilder.append("-XX:+UseParNewGC ");

		commandBuilder.append("-XX:+UseConcMarkSweepGC ");
		commandBuilder.append("-XX:MaxGCPauseMillis=5 ");
		commandBuilder.append("-XX:+AggressiveOpts ");
		//commandBuilder.append("");

		commandBuilder.append("-XX:+UseBiasedLocking ");
		commandBuilder.append("-XX:+AlwaysPreTouch ");
		commandBuilder.append("-XX:ParallelGCThreads=4 ");
		commandBuilder.append("-XX:+UseCompressedOops ");
		
		commandBuilder.append("-XX:MaxDirectMemorySize=32m ");
	    }//end if( 64 & server)
	    
	    //UNIVERSAL OPTS
	    commandBuilder.append("-Xms512m ");
	    commandBuilder.append("-Xmx512m ");

	    if(useAssertions)
		commandBuilder.append("-ea ");
	    for (Entry<Object,Object> property:System.getProperties().entrySet()){
		if(property.getKey().toString().startsWith("org.jtrfp")&&!property.getKey().toString().toLowerCase().contains("org.jtrfp.trcl.bypassconfigure"))
		    commandBuilder.append(" -D"+property.getKey()+"="+property.getValue()+" ");
	    }//end for(properties)
	    commandBuilder.append(executable);
	    for (String arg : args) {
		commandBuilder.append(" " + arg);
	    }
	    try {
		final String cmd = commandBuilder.toString();
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
	}//end if(already configured)
	return this;
    }// end Ensure...Configured(...)

    public static void printStartupMode(){
	if(JVM.isRunningFromJar())
	    System.out.println("Running in JAR mode.");
	else
	    System.out.println("Running in IDE mode.");
    }//end printStartupMode()
    
    private boolean isAlreadyConfigured() {
   	return System.getProperty("org.jtrfp.trcl.bypassConfigure") != null?System.getProperty("org.jtrfp.trcl.bypassConfigure").toUpperCase().contentEquals("TRUE"):false;
       }//end isAlreadyConfigured()
    
    public static boolean isRunningFromJar(){
	return runningFromJar;
    }//end isRunningFromJar
    
    public File getTempDir() throws IOException{
	final File temp = new File("DeleteMe");
	if(temp.exists()){
	    if(temp.isDirectory()){
		return temp;
	    }else temp.delete();
	}//end if(exists)
	temp.mkdir();
	temp.deleteOnExit();
	return temp;
    }//end getTempDir()
    
    public File loadFromJarToFile(String resourcePath) throws IOException{
	if(resourcePath==null)
	    throw new NullPointerException("Passed resourcePath intolerably null.");
	if(!resourcePath.startsWith("/"))
	    throw new IllegalArgumentException("Path must start with forward slash. '/' Got "+resourcePath);
	if(resourcePath.length()==0)
	    throw new IllegalArgumentException("Empty resource path.");
	
	String [] pathSegments = resourcePath.split("/");
	final String simpleName = pathSegments[Math.max(0,pathSegments.length-1)];
	
	final File temporary = new File(getTempDir()+File.separator+simpleName);
	if(temporary.exists())
	    temporary.delete();
	temporary.createNewFile();
	temporary.deleteOnExit();
	
	if(!temporary.exists())
	    throw new IOException("Failed to create temporary file "+temporary.getName());
	
	final int bufferSize = 4096;
	final byte [] buffer = new byte[bufferSize];
	final InputStream inputStream = JVM.class.getResourceAsStream(resourcePath);
	final OutputStream outputStream = new FileOutputStream(temporary);
	int bytesRead;
	try{
	    while((bytesRead = inputStream.read(buffer)) != -1)
		outputStream.write(buffer, 0, bytesRead);
	    }
	finally{
	    outputStream.close();
	    inputStream .close();
	    }
	
	return temporary;
    }//end loadNativeFromJarToFile
    
    public void loadNativeFromJar(String resourcePath) throws IOException{
	final File temporary = loadFromJarToFile(resourcePath);
	System.load(temporary.getAbsolutePath());
    }//end loadNativeFromJar
}//end JVMConfigurator
