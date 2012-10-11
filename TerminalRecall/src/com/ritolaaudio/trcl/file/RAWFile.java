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

import java.io.IOException;
import java.io.InputStream;

import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.SelfParsingFile;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class RAWFile extends SelfParsingFile
	{
	byte [] rawBytes;

	public RAWFile(InputStream inputStream) throws IllegalAccessException, IOException
		{
		super(inputStream);
		}

	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.bytesEndingWith(null,Parser.property("rawBytes",byte[].class),false);
		}

	/**
	 * @return the rawBytes
	 */
	public byte[] getRawBytes()
		{
		return rawBytes;
		}

	/**
	 * @param rawBytes the rawBytes to set
	 */
	public void setRawBytes(byte[] rawBytes)
		{
		this.rawBytes = rawBytes;
		}
	
	public int getSideLength()
		{
		return (int)Math.sqrt(rawBytes.length);
		}
	
	public int valueAt(int x,int y)
		{
		return (int)rawBytes[x+(y*getSideLength())] & 0xFF;
		}
	}//end RAWFile
