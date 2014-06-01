package org.jtrfp.trcl;
/*******************************************************************************
 * This file is part of TERMINAL RECALL 
 * Copyright (c) 2012-2014 Chuck Ritola.
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class OutputDump {
    private final PrintStream htmlOutput;
   public OutputDump() throws FileNotFoundException{
       htmlOutput = new PrintStream(new File("log.html"));
       System.setOut(new PrintStream(new HTMLConsole(htmlOutput,System.out,"<FONT COLOR='black'>","</FONT>")));
       System.setErr(new PrintStream(new HTMLConsole(htmlOutput,System.err,"<FONT COLOR='red'>","</FONT>")));
   }//end constructor
   
   private static class HTMLConsole extends OutputStream {
       private final PrintStream 	output;
       private final String 		prefix,suffix;
       private final PrintStream 	tee;
       private StringBuilder 		sb 		= new StringBuilder();
       public HTMLConsole(PrintStream output,PrintStream tee, String prefix, String suffix){
	   this.output=output;
	   this.prefix=prefix;
	   this.suffix=suffix;
	   this.tee=tee;
       }//end constructor
    @Override
    public void write(int b) throws IOException {
	boolean newLine = b=='\n';
	if(newLine){
	    final String o = sb.toString();
	    tee.println(o);
	    output.println(prefix+o.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;")+suffix+"<BR>");
	    sb=new StringBuilder();
	}else sb.append((char)b);
    }//end write()
   }//end HTMLConsole
}//end OutputDump
