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
package com.ritolaaudio.trcl.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ritolaaudio.jfdt1.EndianAwareDataInputStream;
import com.ritolaaudio.jfdt1.EndianAwareDataOutputStream;
import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.ThirdPartyParseable;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class TRParsers
	{
	/**
	 * Untested.
	 * @param f
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @since Sep 16, 2012
	 */
	public static ThirdPartyParseable read(InputStream is) throws FileNotFoundException, IOException
		{
		final String packagePath="com/ritolaaudio/trcl/file";
		for(File classFile:new File(packagePath).listFiles())
			{
			try
				{
				Class classToTry = Class.forName("com.ritolaaudio.trcl.file."+classFile.getName().substring(0,classFile.getName().length()-6));
				ThirdPartyParseable result =  
						Parser.readToNewBean(new EndianAwareDataInputStream(new DataInputStream(new BufferedInputStream(is))), classToTry);
				if(result!=null)return result;
				}
			catch(UnrecognizedFormatException e){}
			catch(IllegalAccessException e){e.printStackTrace();}
			catch(ClassNotFoundException e){e.printStackTrace();}
			}//end for(filesInPackage)
		throw new UnrecognizedFormatException();
		}//end readFile(...)
	
	public static void write(ThirdPartyParseable bean,OutputStream os) throws IOException
		{
		Parser.writeBean(bean, new EndianAwareDataOutputStream(new DataOutputStream(os)));
		}
	}//end TRParsers
