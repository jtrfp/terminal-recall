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

import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class Location3D extends AbstractVector
	{
	public static class EndingWithComma extends Location3D
		{
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.stringEndingWith(",", Parser.property("x", Integer.class), false);
			Parser.stringEndingWith(",", Parser.property("y", Integer.class), false);
			Parser.stringEndingWith(",", Parser.property("z", Integer.class), false);
			}
		}//end EndingWithComma
	}//end Location3D
