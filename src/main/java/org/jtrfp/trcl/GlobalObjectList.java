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
package org.jtrfp.trcl;

import org.jtrfp.trcl.core.RenderList;
import org.jtrfp.trcl.gpu.GlobalDynamicTextureBuffer;

public class GlobalObjectList
	{
	private static final IndirectObject<Integer> arrayOffset = new IndirectObject<Integer>();
	public static void poke(){}//Triggers static initialization above. TODO: Program this a bit cleaner. This is kludgy.
	public static final int OBJECT_LIST_SIZE_BYTES_PER_PASS = RenderList.NUM_BLOCKS_PER_PASS*4;//uint is 4 bytes
	
	static {GlobalDynamicTextureBuffer.addAllocationToFinalize(GlobalObjectList.class);}
	
	public static void finalizeAllocation()
		{
		int bytesToAllocate = OBJECT_LIST_SIZE_BYTES_PER_PASS*RenderList.NUM_RENDER_PASSES;
		System.out.println("ObjectList: Allocating "+bytesToAllocate+" bytes of GPU resident RAM.");
		arrayOffset.set(GlobalDynamicTextureBuffer.requestAllocation(bytesToAllocate));
		}
	public static int getArrayOffsetInBytes(){return arrayOffset.get();}
	}//end GlobalObjectList
