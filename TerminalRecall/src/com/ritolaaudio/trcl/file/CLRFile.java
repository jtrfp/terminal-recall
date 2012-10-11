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


public class CLRFile extends RAWFile
	{

	public CLRFile(InputStream inputStream) throws IllegalAccessException,
			IOException
		{
		super(inputStream);
		}

	}
