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

import com.ritolaaudio.jfdt1.ClassInclusion;
import com.ritolaaudio.jfdt1.FailureBehavior;
import com.ritolaaudio.jfdt1.Parser;
import com.ritolaaudio.jfdt1.ThirdPartyParseable;
import com.ritolaaudio.jfdt1.UnrecognizedFormatException;

public class NAVFile implements ThirdPartyParseable
	{
	int numNavigationPoints;
	NAVSubObject [] navObjects;
	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.stringEndingWith("\r\n", Parser.property("numNavigationPoints",int.class), false);
		for(int i=0; i<getNumNavigationPoints(); i++)
			{Parser.subParseProposedClasses(Parser.indexedProperty("navObjects",NAVSubObject.class,i),ClassInclusion.nestedClassesOf(NAVFile.class));}
		}//end describeFormat()
	
	public static abstract class NAVSubObject implements ThirdPartyParseable
		{
		//int type;
		Location3D locationOnMap;
		String description;
		/**
		 * @return the locationOnMap
		 */
		public Location3D getLocationOnMap()
			{
			return locationOnMap;
			}
		/**
		 * @param locationOnMap the locationOnMap to set
		 */
		public void setLocationOnMap(Location3D locationOnMap)
			{
			this.locationOnMap = locationOnMap;
			}
		/**
		 * @return the description
		 */
		public String getDescription()
			{
			return description;
			}
		/**
		 * @param description the description to set
		 */
		public void setDescription(String description)
			{
			this.description = description;
			}
		}//end NAVSubObject
	
	public static class START extends NAVSubObject
	{
	int pitch,roll,yaw;
	@Override
	public void describeFormat() throws UnrecognizedFormatException
		{
		Parser.expectString("6\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
		//Parser.subParse("locationOnMap", Location3D.class);
		Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
		Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
		Parser.stringCSVEndingWith("\r\n", int.class, false, "pitch","roll","yaw");
		}
	/**
	 * @return the pitch
	 */
	public int getPitch()
		{
		return pitch;
		}
	/**
	 * @param pitch the pitch to set
	 */
	public void setPitch(int pitch)
		{
		this.pitch = pitch;
		}
	/**
	 * @return the roll
	 */
	public int getRoll()
		{
		return roll;
		}
	/**
	 * @param roll the roll to set
	 */
	public void setRoll(int roll)
		{
		this.roll = roll;
		}
	/**
	 * @return the yaw
	 */
	public int getYaw()
		{
		return yaw;
		}
	/**
	 * @param yaw the yaw to set
	 */
	public void setYaw(int yaw)
		{
		this.yaw = yaw;
		}
	
	}//end CHK
	
	public static class BOS extends NAVSubObject
		{
		int bossIndex;
		String musicFile;
		String unused;
		//!NewH
		int numTargets;
		int [] targets;
		
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("5\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("bossIndex", int.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("musicFile", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("unused", String.class), false);
			Parser.expectString("!NewH", FailureBehavior.IGNORE);
			Parser.stringEndingWith("\r\n", Parser.property("numTargets", int.class), false);
			for(int i=0; i<getNumTargets();i++)
				{Parser.stringEndingWith("\r\n", Parser.indexedProperty("targets", int.class, i), false);}
			}

		/**
		 * @return the bossIndex
		 */
		public int getBossIndex()
			{
			return bossIndex;
			}

		/**
		 * @param bossIndex the bossIndex to set
		 */
		public void setBossIndex(int bossIndex)
			{
			this.bossIndex = bossIndex;
			}

		/**
		 * @return the musicFile
		 */
		public String getMusicFile()
			{
			return musicFile;
			}

		/**
		 * @param musicFile the musicFile to set
		 */
		public void setMusicFile(String musicFile)
			{
			this.musicFile = musicFile;
			}

		/**
		 * @return the unused
		 */
		public String getUnused()
			{
			return unused;
			}

		/**
		 * @param unused the unused to set
		 */
		public void setUnused(String unused)
			{
			this.unused = unused;
			}

		/**
		 * @return the numTargets
		 */
		public int getNumTargets()
			{
			return numTargets;
			}

		/**
		 * @param numTargets the numTargets to set
		 */
		public void setNumTargets(int numTargets)
			{
			this.numTargets = numTargets;
			}

		/**
		 * @return the targets
		 */
		public int[] getTargets()
			{
			return targets;
			}

		/**
		 * @param targets the targets to set
		 */
		public void setTargets(int[] targets)
			{
			this.targets = targets;
			}
		
		}//end CHK
	
	public static class XIT extends NAVSubObject
		{
		String unused1,unused2;
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("4\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("unused1", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("unused2", String.class), false);
			}
		/**
		 * @return the unused1
		 */
		public String getUnused1()
			{
			return unused1;
			}
		/**
		 * @param unused1 the unused1 to set
		 */
		public void setUnused1(String unused1)
			{
			this.unused1 = unused1;
			}
		/**
		 * @return the unused2
		 */
		public String getUnused2()
			{
			return unused2;
			}
		/**
		 * @param unused2 the unused2 to set
		 */
		public void setUnused2(String unused2)
			{
			this.unused2 = unused2;
			}
		}//end XIT
	
	public static class DUN extends NAVSubObject
		{
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("3\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			}
		
		}//end DUN
	
	public static class CHK extends NAVSubObject
		{
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("2\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			}
		
		}//end CHK
	
	public static class TUN extends NAVSubObject
		{
		String unused;
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("1\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("unused", String.class),false);
			}
		/**
		 * @return the unused
		 */
		public String getUnused()
			{
			return unused;
			}
		/**
		 * @param unused the unused to set
		 */
		public void setUnused(String unused)
			{
			this.unused = unused;
			}
		
		}//end TUN
	
	public static class TGT extends NAVSubObject
		{
		int numTargets;
		int [] targets;
		
		@Override
		public void describeFormat() throws UnrecognizedFormatException
			{
			Parser.expectString("0\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
			Parser.subParseProposedClasses(Parser.property("locationOnMap", Location3D.class), ClassInclusion.classOf(Location3D.class));
			Parser.stringEndingWith("\r\n", Parser.property("description", String.class), false);
			Parser.stringEndingWith("\r\n", Parser.property("numTargets",int.class), false);
			for(int i=0; i<getNumTargets(); i++)
				{Parser.stringEndingWith("\r\n", Parser.indexedProperty("targets", int.class, i), false);}
			
			}//end descriptionFormat()

		/**
		 * @return the numTargets
		 */
		public int getNumTargets()
			{
			return numTargets;
			}

		/**
		 * @param numTargets the numTargets to set
		 */
		public void setNumTargets(int numTargets)
			{
			this.numTargets = numTargets;
			}

		/**
		 * @return the targets
		 */
		public int[] getTargets()
			{
			return targets;
			}

		/**
		 * @param targets the targets to set
		 */
		public void setTargets(int[] targets)
			{
			this.targets = targets;
			}
		
		}//end NAVSubObject

	/**
	 * @return the numNavigationPoints
	 */
	public int getNumNavigationPoints()
		{
		return numNavigationPoints;
		}

	/**
	 * @param numNavigationPoints the numNavigationPoints to set
	 */
	public void setNumNavigationPoints(int numNavigationPoints)
		{
		this.numNavigationPoints = numNavigationPoints;
		}

	/**
	 * @return the navObjects
	 */
	public NAVSubObject[] getNavObjects()
		{
		return navObjects;
		}

	/**
	 * @param navObjects the navObjects to set
	 */
	public void setNavObjects(NAVSubObject[] navObjects)
		{
		this.navObjects = navObjects;
		}

	}//end NAVFile
