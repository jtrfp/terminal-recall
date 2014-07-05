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
package org.jtrfp.trcl.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jtrfp.jfdt.EndianAwareDataInputStream;
import org.jtrfp.jfdt.EndianAwareDataOutputStream;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class TRParsers {
    /**
     * Untested.
     * 
     * @param f
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @since Sep 16, 2012
     */
    public static ThirdPartyParseable read(InputStream is)
	    throws FileNotFoundException, IOException {
	final String packagePath = "com/ritolaaudio/trcl/file";
	for (File classFile : new File(packagePath).listFiles()) {
	    try {
		final Parser p = new Parser();
		Class classToTry = Class.forName("com.ritolaaudio.trcl.file."
			+ classFile.getName().substring(0,
				classFile.getName().length() - 6));
		ThirdPartyParseable result = p.readToNewBean(
			new EndianAwareDataInputStream(new DataInputStream(
				new BufferedInputStream(is))), classToTry);
		if (result != null)
		    return result;
	    } catch (UnrecognizedFormatException e) {
	    } catch (IllegalAccessException e) {
		e.printStackTrace();
	    } catch (ClassNotFoundException e) {
		e.printStackTrace();
	    }
	}// end for(filesInPackage)
	throw new UnrecognizedFormatException();
    }// end readFile(...)

    public static void write(ThirdPartyParseable bean, OutputStream os)
	    throws IOException {
	final Parser p = new Parser();
	p.writeBean(bean, new EndianAwareDataOutputStream(new DataOutputStream(
		os)));
    }
}// end TRParsers
