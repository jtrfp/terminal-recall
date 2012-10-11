/*******************************************************************************
 * Copyright (c) 2012 chuck.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package com.ritolaaudio.trcl;

import java.nio.ByteBuffer;

public class Index extends GLElement
	{
	int index;
	
	public Index(){}
	
	public Index(int index){this.index=index;}
	
	@Override
	public void writeToBuffer(ByteBuffer buf)
		{
		//System.out.println("index: "+index);
		buf.putInt(index);
		}

	@Override
	public void buildFrom(ByteBuffer buf)
		{
		index=buf.getInt();
		}

	@Override
	public int getElementSizeInBytes()
		{
		return 4;
		}

	/**
	 * @return the index
	 */
	public int getIndex()
		{
		return index;
		}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index)
		{
		this.index = index;
		}

	}//end Index
