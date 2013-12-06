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
package org.jtrfp.trcl.tools;

import java.io.File;

import org.jtrfp.common.FileLoadException;
import org.jtrfp.common.pod.IPodFileEntry;
import org.jtrfp.common.pod.PodFile;

public class PodDump
	{

	/**
	 * @param args
	 * @since Apr 28, 2013
	 */
	public static void main(String[] args)
		{
		new PodDump(args);
		}

	public PodDump(String [] args)
		{
		if(args.length!=2)
			{failure();}
		File podFile = new File(args[0]);
		File dest = new File(args[1]);
		if(!dest.isDirectory())
			{System.err.println("Destination must be a directory.");System.exit(1);}
		PodFile pod = new PodFile(podFile);
		try {
			for(IPodFileEntry ent:pod.getData().getEntries())
				{try
					{
					File destFile=new File(dest,ent.getPath());
					System.out.println(""+destFile.getName());
					ent.toFile(destFile);
					}
				catch(Exception e)
					{e.printStackTrace();}
				}//end for(files)
			}
		catch(FileLoadException e){e.printStackTrace();}
		}//end PodDump(...)
	
	private void failure()
		{System.err.println("USAGE: PodDump [Path to POD file] [Destination Directory]");
		System.exit(1);}
	}//end PodDump
