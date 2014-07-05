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

import java.util.ArrayList;
import java.util.List;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;

public class TNLFile implements ThirdPartyParseable {
    int numSegments;
    ArrayList<Segment> segments;

    private static void Int(String targetProperty, Parser parser) {
	parser.stringEndingWith("\r\n",
		parser.property(targetProperty, int.class), false);
    }

    @Override
    public void describeFormat(Parser parser)
	    throws UnrecognizedFormatException {
	parser.stringEndingWith("\r\n",
		parser.property("numSegments", int.class), false);
	parser.arrayOf(getNumSegments(), "segments", Segment.class);
    }

    public static class Segment implements ThirdPartyParseable {
	int startX, startY, endX, endY, numPolygons;
	int startAngle1, startAngle2, rotationSpeed;
	int startWidth, startHeight;
	int endAngle1, endAngle2, endWidth, endHeight;
	int unknown1;
	int lightPolygon;
	boolean cutout;
	Obstacle obstacle;
	int obstacleTextureIndex;
	ArrayList<Integer> polyTextureIndices;
	// int [] polyTextureIndices;
	FlickerLightType flickerLightType;
	int flickerLightStrength, ambientLight;

	@Override
	public void describeFormat(Parser p) throws UnrecognizedFormatException {
	    p.stringCSVEndingWith("\r\n", int.class, false, "startX", "startY",
		    "endX", "endY", "numPolygons");
	    p.stringCSVEndingWith("\r\n", int.class, false, "startAngle1",
		    "startAngle2", "rotationSpeed");
	    p.stringCSVEndingWith("\r\n", int.class, false, "startWidth",
		    "startHeight");
	    p.stringCSVEndingWith("\r\n", int.class, false, "endAngle1",
		    "endAngle2");
	    p.stringCSVEndingWith("\r\n", int.class, false, "endWidth",
		    "endHeight");
	    // Int("endAngle1");Int("endAngle2");Int("endWidth");Int("endHeight");
	    Int("unknown1", p);
	    Int("lightPolygon", p);
	    p.stringEndingWith("\r\n", p.property("cutout", Boolean.class),
		    false);
	    p.stringEndingWith("\r\n", p.property("obstacle", Obstacle.class),
		    false);
	    Int("obstacleTextureIndex", p);
	    for (int i = 0; i < getNumPolygons(); i++) {
		p.stringEndingWith("\r\n",
			p.indexedProperty("polyTextureIndices", int.class, i),
			false);
	    }

	    p.stringEndingWith("\r\n",
		    p.property("flickerLightType", FlickerLightType.class),
		    false);
	    Int("flickerLightStrength", p);
	    Int("ambientLight", p);
	}// end describeFormat()

	public static enum Obstacle {
	    none0, doorway, closedDoor, blownOpenDoor, wallUpSTUB, wallDownSTUB, wallLeftSTUB, wallRightSTUB, movingWallLeft, movingWallRight, movingWallDown, movingWallUp, wallUp, wallDown, wallLeft, wallRight, rotatingHalfWall, rotating34Wall, fan, jawsVertical, jawsHorizontal, metalBeamUp, metalBeamDown, metalBeamLeft, metalBeamRight, forceField, invisibleWallUp, invisibleWallDown, invisibleWallLeft, invisibleWallRight, iris;
	}

	public static enum FlickerLightType {
	    noLight, on1Sec, on1p5Sec, off1p5Sec, toggleEveryFrame;
	}

	/**
	 * @return the startX
	 */
	public int getStartX() {
	    return startX;
	}

	/**
	 * @param startX
	 *            the startX to set
	 */
	public void setStartX(int startX) {
	    this.startX = startX;
	}

	/**
	 * @return the startY
	 */
	public int getStartY() {
	    return startY;
	}

	/**
	 * @param startY
	 *            the startY to set
	 */
	public void setStartY(int startY) {
	    this.startY = startY;
	}

	/**
	 * @return the endX
	 */
	public int getEndX() {
	    return endX;
	}

	/**
	 * @param endX
	 *            the endX to set
	 */
	public void setEndX(int endX) {
	    this.endX = endX;
	}

	/**
	 * @return the endY
	 */
	public int getEndY() {
	    return endY;
	}

	/**
	 * @param endY
	 *            the endY to set
	 */
	public void setEndY(int endY) {
	    this.endY = endY;
	}

	/**
	 * @return the numPolygons
	 */
	public int getNumPolygons() {
	    return numPolygons;
	}

	/**
	 * @param numPolygons
	 *            the numPolygons to set
	 */
	public void setNumPolygons(int numPolygons) {
	    this.numPolygons = numPolygons;
	}

	/**
	 * @return the startAngle1
	 */
	public int getStartAngle1() {
	    return startAngle1;
	}

	/**
	 * @param startAngle1
	 *            the startAngle1 to set
	 */
	public void setStartAngle1(int startAngle1) {
	    this.startAngle1 = startAngle1;
	}

	/**
	 * @return the startAngle2
	 */
	public int getStartAngle2() {
	    return startAngle2;
	}

	/**
	 * @param startAngle2
	 *            the startAngle2 to set
	 */
	public void setStartAngle2(int startAngle2) {
	    this.startAngle2 = startAngle2;
	}

	/**
	 * @return the rotationSpeed
	 */
	public int getRotationSpeed() {
	    return rotationSpeed;
	}

	/**
	 * @param rotationSpeed
	 *            the rotationSpeed to set
	 */
	public void setRotationSpeed(int rotationSpeed) {
	    this.rotationSpeed = rotationSpeed;
	}

	/**
	 * @return the startWidth
	 */
	public int getStartWidth() {
	    return startWidth;
	}

	/**
	 * @param startWidth
	 *            the startWidth to set
	 */
	public void setStartWidth(int startWidth) {
	    this.startWidth = startWidth;
	}

	/**
	 * @return the startHeight
	 */
	public int getStartHeight() {
	    return startHeight;
	}

	/**
	 * @param startHeight
	 *            the startHeight to set
	 */
	public void setStartHeight(int startHeight) {
	    this.startHeight = startHeight;
	}

	/**
	 * @return the endAngle1
	 */
	public int getEndAngle1() {
	    return endAngle1;
	}

	/**
	 * @param endAngle1
	 *            the endAngle1 to set
	 */
	public void setEndAngle1(int endAngle1) {
	    this.endAngle1 = endAngle1;
	}

	/**
	 * @return the endAngle2
	 */
	public int getEndAngle2() {
	    return endAngle2;
	}

	/**
	 * @param endAngle2
	 *            the endAngle2 to set
	 */
	public void setEndAngle2(int endAngle2) {
	    this.endAngle2 = endAngle2;
	}

	/**
	 * @return the endWidth
	 */
	public int getEndWidth() {
	    return endWidth;
	}

	/**
	 * @param endWidth
	 *            the endWidth to set
	 */
	public void setEndWidth(int endWidth) {
	    this.endWidth = endWidth;
	}

	/**
	 * @return the endHeight
	 */
	public int getEndHeight() {
	    return endHeight;
	}

	/**
	 * @param endHeight
	 *            the endHeight to set
	 */
	public void setEndHeight(int endHeight) {
	    this.endHeight = endHeight;
	}

	/**
	 * @return the unknown1
	 */
	public int getUnknown1() {
	    return unknown1;
	}

	/**
	 * @param unknown1
	 *            the unknown1 to set
	 */
	public void setUnknown1(int unknown1) {
	    this.unknown1 = unknown1;
	}

	/**
	 * @return the lightPolygon
	 */
	public int getLightPolygon() {
	    return lightPolygon;
	}

	/**
	 * @param lightPolygon
	 *            the lightPolygon to set
	 */
	public void setLightPolygon(int lightPolygon) {
	    this.lightPolygon = lightPolygon;
	}

	/**
	 * @return the cutout
	 */
	public boolean isCutout() {
	    return cutout;
	}

	/**
	 * @param cutout
	 *            the cutout to set
	 */
	public void setCutout(boolean cutout) {
	    this.cutout = cutout;
	}

	/**
	 * @return the obstacle
	 */
	public Obstacle getObstacle() {
	    return obstacle;
	}

	/**
	 * @param obstacle
	 *            the obstacle to set
	 */
	public void setObstacle(Obstacle obstacle) {
	    this.obstacle = obstacle;
	}

	/**
	 * @return the obstacleTextureIndex
	 */
	public int getObstacleTextureIndex() {
	    return obstacleTextureIndex;
	}

	/**
	 * @param obstacleTextureIndex
	 *            the obstacleTextureIndex to set
	 */
	public void setObstacleTextureIndex(int obstacleTextureIndex) {
	    this.obstacleTextureIndex = obstacleTextureIndex;
	}

	/**
	 * @return the polyTextureIndices
	 */
	public List<Integer> getPolyTextureIndices() {
	    return polyTextureIndices;
	}

	/**
	 * @param polyTextureIndices
	 *            the polyTextureIndices to set
	 */
	public void setPolyTextureIndices(ArrayList<Integer> polyTextureIndices) {
	    this.polyTextureIndices = polyTextureIndices;
	}

	/**
	 * @return the flickerLightType
	 */
	public FlickerLightType getFlickerLightType() {
	    return flickerLightType;
	}

	/**
	 * @param flickerLightType
	 *            the flickerLightType to set
	 */
	public void setFlickerLightType(FlickerLightType flickerLightType) {
	    this.flickerLightType = flickerLightType;
	}

	/**
	 * @return the flickerLightStrength
	 */
	public int getFlickerLightStrength() {
	    return flickerLightStrength;
	}

	/**
	 * @param flickerLightStrength
	 *            the flickerLightStrength to set
	 */
	public void setFlickerLightStrength(int flickerLightStrength) {
	    this.flickerLightStrength = flickerLightStrength;
	}

	/**
	 * @return the ambientLight
	 */
	public int getAmbientLight() {
	    return ambientLight;
	}

	/**
	 * @param ambientLight
	 *            the ambientLight to set
	 */
	public void setAmbientLight(int ambientLight) {
	    this.ambientLight = ambientLight;
	}
    }// end Segment

    /**
     * @return the numSegments
     */
    public int getNumSegments() {
	return numSegments;
    }

    /**
     * @param numSegments
     *            the numSegments to set
     */
    public void setNumSegments(int numSegments) {
	this.numSegments = numSegments;
    }

    /**
     * @return the segments
     */
    public List<Segment> getSegments() {
	return segments;
    }

    /**
     * @param segments
     *            the segments to set
     */
    public void setSegments(ArrayList<Segment> segments) {
	this.segments = segments;
    }

}// end TNLFile
