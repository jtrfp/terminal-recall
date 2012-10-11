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
package com.ritolaaudio.trcl.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
	public static ThirdPartyParseable readFile(File f) throws FileNotFoundException, IOException
		{
		final String packagePath="com/ritolaaudio/tr/file";
		for(File classFile:new File(packagePath).listFiles())
			{
			try
				{
				Class classToTry = Class.forName("com.ritolaaudio.tr.file."+classFile.getName().substring(0,classFile.getName().length()-6));
				ThirdPartyParseable result =  
						Parser.readToNewBean(new EndianAwareDataInputStream(new DataInputStream(new BufferedInputStream(new FileInputStream(f)))), classToTry);
				if(result!=null)return result;
				}
			catch(UnrecognizedFormatException e){}
			catch(FileNotFoundException e){e.printStackTrace();}
			catch(IllegalAccessException e){e.printStackTrace();}
			catch(ClassNotFoundException e){e.printStackTrace();}
			}//end for(filesInPackage)
		throw new UnrecognizedFormatException();
		}//end readFile(...)
	
	public static void writeFile(ThirdPartyParseable bean,File f) throws IOException
		{
		if(!f.exists())f.createNewFile();
		Parser.writeBean(bean, new EndianAwareDataOutputStream(new DataOutputStream(new FileOutputStream(f))));
		}
	}//end TRParsers
