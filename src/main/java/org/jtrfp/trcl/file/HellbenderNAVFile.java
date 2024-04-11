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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jtrfp.jfdt.ClassInclusion;
import org.jtrfp.jfdt.FailureBehavior;
import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.SelfParsingFile;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class HellbenderNAVFile extends SelfParsingFile implements NAVData {
    int numNavigationPoints;
    List<? extends NAVSubObjectData> navObjects;
    
    public HellbenderNAVFile(InputStream sourceStream) throws IOException, IllegalAccessException {
	super(new BufferedInputStream(sourceStream,1024));
    }
    
    public HellbenderNAVFile() {}

    @Override
    public void describeFormat(Parser prs) throws UnrecognizedFormatException {
	prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		prs.property("numNavigationPoints", int.class), false);
	for (int i = 0; i < getNumNavigationPoints(); i++) {
	    prs.subParseProposedClasses(
		    prs.indexedProperty("nativeNavObjects", NAVSubObject.class, i),
		    ClassInclusion.nestedClassesOf(HellbenderNAVFile.class));
	    prs.expectString("-------------------------------------------------\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
	}
    }// end describeFormat()

    public static abstract class NAVSubObject implements ThirdPartyParseable, NAVSubObjectData {
	Location3D locationOnMap;
	int priority, time;
	String completionSoundFile, completionText, proximitySoundFile;
	String description;
	
	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.expectString(getNAVNumber()+"\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
	    prs.subParseProposedClasses(
		    prs.property("locationOnMap", Location3D.class),
		    ClassInclusion.classOf(Location3D.class));
	    
	    try {//Hellbender stuff //TODO: Consider skipping writing this if not applicable
		prs.expectString("!priority,time\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringCSVEndingWith("\r\n", int.class, false, "priority","time");
	    }catch(UnrecognizedFormatException e) {}
	    
	    try {//Hellbender stuff //TODO: Consider skipping writing this if not applicable
		prs.expectString("@Completion sound & completion text (39 chars max)\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			    prs.property("completionSoundFile", String.class), false);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			    prs.property("completionText", String.class), false);
	    }catch(UnrecognizedFormatException e) {}
	    
	    try {//Hellbender stuff //TODO: Consider skipping writing this if not applicable
		prs.expectString("; Proximity Sound file\r\n", FailureBehavior.UNRECOGNIZED_FORMAT);
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			    prs.property("proximitySoundFile", String.class), false);
	    }catch(UnrecognizedFormatException e) {}
	    
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("description", String.class), false);
	}//end describeFormat(...)
	
	protected abstract int getNAVNumber();

	/**
	 * @return the locationOnMap
	 */
	public Location3D getLocationOnMap() {
	    return locationOnMap;
	}

	/**
	 * @param locationOnMap
	 *            the locationOnMap to set
	 */
	public void setLocationOnMap(Location3D locationOnMap) {
	    this.locationOnMap = locationOnMap;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
	    return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
	    this.description = description;
	}
	
	@Override
	public String toString() {
	    return super.toString()+" loc="+locationOnMap+" desc="+description;
	}

	public String getCompletionSoundFile() {
	    return completionSoundFile;
	}

	public void setCompletionSoundFile(String completionSoundFile) {
	    this.completionSoundFile = completionSoundFile;
	}

	public String getProximitySoundFile() {
	    return proximitySoundFile;
	}

	public void setProximitySoundFile(String proximitySoundFile) {
	    this.proximitySoundFile = proximitySoundFile;
	}

	public String getCompletionText() {
	    return completionText;
	}

	public void setCompletionText(String completionText) {
	    this.completionText = completionText;
	}

	public int getPriority() {
	    return priority;
	}

	public void setPriority(int priority) {
	    this.priority = priority;
	}

	public int getTime() {
	    return time;
	}

	public void setTime(int time) {
	    this.time = time;
	}
    }// end NAVSubObject

    public static class Nyx extends NAVSubObject implements NAVData.Nyx {
	private int nyxIndex;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("nyxIndex", int.class), false);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 14;
	}

	public int getNyxIndex() {
	    return nyxIndex;
	}

	public void setNyxIndex(int nyxIdx) {
	    this.nyxIndex = nyxIdx;
	}

    }// end Nyx
    
    public static class MessagePod extends NAVSubObject implements NAVData.PickupMessagePod {
	private int messagePodIdx;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("messagePodIdx", int.class), false);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 13;
	}

	public int getMessagePodIdx() {
	    return messagePodIdx;
	}

	public void setMessagePodIdx(int messagePodIdx) {
	    this.messagePodIdx = messagePodIdx;
	}

    }// end MessagePod
    
    public static class EscortObject extends NAVSubObject implements NAVData.EscortObject {
	private int escortedObjectIdx;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("escortedObjectIdx", int.class), false);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 12;
	}

	public int getEscortedObjectIdx() {
	    return escortedObjectIdx;
	}

	public void setEscortedObjectIdx(int escortedObjectIdx) {
	    this.escortedObjectIdx = escortedObjectIdx;
	}

    }// end EscortObject
    
    public static class NAVListEnd extends NAVSubObject implements NAVData.NAVListEnd {

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 9;
	}

    }// end NAVListEnd
    
    public static class DropBeacon extends NAVSubObject implements NAVData.DropBeacon {

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 8;
	}

    }// end DropBeacon

    public static class SynchronizationPoint extends NAVSubObject implements NAVData.SynchronizationPoint {

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	}
	
	@Override
	protected int getNAVNumber() {
	    return 7;
	}

    }// end SynchronizationPoint
    
    public static class START extends NAVSubObject implements NAVData.StartingPoint {
	int pitch, roll, yaw;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringCSVEndingWith("\r\n", int.class, false, "pitch", "roll",
		    "yaw");
	}

	/**
	 * @return the pitch
	 */
	public int getPitch() {
	    return pitch;
	}

	/**
	 * @param pitch
	 *            the pitch to set
	 */
	public void setPitch(int pitch) {
	    this.pitch = pitch;
	}

	/**
	 * @return the roll
	 */
	public int getRoll() {
	    return roll;
	}

	/**
	 * @param roll
	 *            the roll to set
	 */
	public void setRoll(int roll) {
	    this.roll = roll;
	}

	/**
	 * @return the yaw
	 */
	public int getYaw() {
	    return yaw;
	}

	/**
	 * @param yaw
	 *            the yaw to set
	 */
	public void setYaw(int yaw) {
	    this.yaw = yaw;
	}

	@Override
	protected int getNAVNumber() {
	    return 6;
	}

    }// end CHK

    public static class BOS extends NAVSubObject implements NAVData.Boss {
	int bossIndex;
	String musicFile;
	String unused;
	// !NewH
	int numTargets;
	int[] targets;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("bossIndex", int.class), false);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
		    prs.property("musicFile", String.class), false);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("unused", String.class),
		    false);
	    prs.expectString("!NewH\r\n", FailureBehavior.IGNORE);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("numTargets", int.class),
		    false);
	    for (int i = 0; i < getNumTargets(); i++) {
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.indexedProperty("targets", int.class, i), false);
	    }
	}

	/**
	 * @return the bossIndex
	 */
	public int getBossIndex() {
	    return bossIndex;
	}

	/**
	 * @param bossIndex
	 *            the bossIndex to set
	 */
	public void setBossIndex(int bossIndex) {
	    this.bossIndex = bossIndex;
	}

	/**
	 * @return the musicFile
	 */
	public String getMusicFile() {
	    return musicFile;
	}

	/**
	 * @param musicFile
	 *            the musicFile to set
	 */
	public void setMusicFile(String musicFile) {
	    this.musicFile = musicFile;
	}

	/**
	 * @return the unused
	 */
	public String getUnused() {
	    return unused;
	}

	/**
	 * @param unused
	 *            the unused to set
	 */
	public void setUnused(String unused) {
	    this.unused = unused;
	}

	/**
	 * @return the numTargets
	 */
	public int getNumTargets() {
	    return numTargets;
	}

	/**
	 * @param numTargets
	 *            the numTargets to set
	 */
	public void setNumTargets(int numTargets) {
	    this.numTargets = numTargets;
	}

	/**
	 * @return the targets
	 */
	public int[] getTargets() {
	    return targets;
	}

	/**
	 * @param targets
	 *            the targets to set
	 */
	public void setTargets(int[] targets) {
	    this.targets = targets;
	}

	@Override
	protected int getNAVNumber() {
	    return 5;
	}

    }// end CHK

    public static class XIT extends NAVSubObject implements NAVData.ExitTunnel {
	String unused1, unused2;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    /*
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("unused1", String.class),
		    false);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("unused2", String.class),
		    false);
	    */
	}

	/**
	 * @return the unused1
	 */
	public String getUnused1() {
	    return unused1;
	}

	/**
	 * @param unused1
	 *            the unused1 to set
	 */
	public void setUnused1(String unused1) {
	    this.unused1 = unused1;
	}

	/**
	 * @return the unused2
	 */
	public String getUnused2() {
	    return unused2;
	}

	/**
	 * @param unused2
	 *            the unused2 to set
	 */
	public void setUnused2(String unused2) {
	    this.unused2 = unused2;
	}

	@Override
	protected int getNAVNumber() {
	    return 4;
	}
    }// end XIT

    public static class DUN extends NAVSubObject implements NAVData.Jumpzone {

	@Override
	protected int getNAVNumber() {
	    return 3;
	}

    }// end DUN

    public static class CHK extends NAVSubObject implements NAVData.Checkpoint {
	
	@Override
	protected int getNAVNumber() {
	    return 2;
	}

    }// end CHK
    
    public static class TUN extends NAVSubObject implements NAVData.EnterTunnel {
	private String unused;
	private String tunnelFileName;

	/**
	 * @return the unused
	 */
	public String getUnused() {
	    return unused;
	}

	/**
	 * @param unused
	 *            the unused to set
	 */
	public void setUnused(String unused) {
	    this.unused = unused;
	}

	/**
	 * @return the tunnelFileName
	 */
	public String getTunnelFileName() {
	    return tunnelFileName;
	}

	/**
	 * @param tunnelFileName
	 *            the tunnelFileName to set
	 */
	public void setTunnelFileName(String tunnelFileName) {
	    this.tunnelFileName = tunnelFileName;
	}

	@Override
	protected int getNAVNumber() {
	    return 1;
	}

    }// end TUN

    public static class TGT extends NAVSubObject implements NAVData.DestroyTarget {
	int numTargets;
	int[] targets;

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    super.describeFormat(prs);
	    prs.stringEndingWith(TRParsers.LINE_DELIMITERS, prs.property("numTargets", int.class),
		    false);
	    for (int i = 0; i < getNumTargets(); i++) {
		prs.stringEndingWith(TRParsers.LINE_DELIMITERS,
			prs.indexedProperty("targets", int.class, i), false);
	    }
	}// end describeFormat()

	/**
	 * @return the numTargets
	 */
	public int getNumTargets() {
	    return numTargets;
	}

	/**
	 * @param numTargets
	 *            the numTargets to set
	 */
	public void setNumTargets(int numTargets) {
	    this.numTargets = numTargets;
	}

	/**
	 * @return the targets
	 */
	public int[] getTargets() {
	    return targets;
	}

	/**
	 * @param targets
	 *            the targets to set
	 */
	public void setTargets(int[] targets) {
	    this.targets = targets;
	}

	public String getCompletionSoundFile() {
	    return completionSoundFile;
	}

	public void setCompletionSoundFile(String completionSoundFile) {
	    this.completionSoundFile = completionSoundFile;
	}

	public String getProximitySoundFile() {
	    return proximitySoundFile;
	}

	public void setProximitySoundFile(String proximitySoundFile) {
	    this.proximitySoundFile = proximitySoundFile;
	}

	@Override
	protected int getNAVNumber() {
	    return 0;
	}

    }// end TGT

    /**
     * @return the numNavigationPoints
     */
    public int getNumNavigationPoints() {
	return numNavigationPoints;
    }

    /**
     * @param numNavigationPoints
     *            the numNavigationPoints to set
     */
    public void setNumNavigationPoints(int numNavigationPoints) {
	this.numNavigationPoints = numNavigationPoints;
    }

    /**
     * @return the navObjects
     */
    public List<? extends NAVSubObjectData> getNavObjects() {
	return navObjects;
    }

    /**
     * @param navObjects
     *            the navObjects to set
     */
    public void setNavObjects(List<? extends NAVSubObjectData> navObjects) {
	this.navObjects = navObjects;
    }
    
    public void setNativeNavObjects(ArrayList<NAVSubObject> navObjects) {
	this.navObjects = navObjects;
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<NAVSubObject> getNativeNavObjects() {
	return (ArrayList<NAVSubObject>) this.navObjects;
    }

}// end NAVFile
