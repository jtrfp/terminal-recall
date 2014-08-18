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

public abstract class BINFile implements ThirdPartyParseable {

    public static class Model extends SelfParsingFile {
	int scale;
	int unknown1, unknown2;
	int numVertices;
	ArrayList<Vertex> vertices;
	ArrayList<ThirdPartyParseable> dataBlocks;

	public Model(InputStream is) throws IllegalAccessException, IOException {
	    super(is);
	}

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.littleEndian();
	    prs.expectBytes(new byte[] { 0x00, 0x00, 0x00, 0x14 },
		    FailureBehavior.UNRECOGNIZED_FORMAT);

	    prs.int4s(prs.property("scale", int.class));
	    prs.int4s(prs.property("unknown1", int.class));
	    prs.int4s(prs.property("unknown2", int.class));
	    prs.int4s(prs.property("numVertices", int.class));
	    for (int i = 0; i < getNumVertices(); i++) {
		prs.subParseProposedClasses(
			prs.indexedProperty("vertices", Vertex.class, i),
			ClassInclusion.classOf(Vertex.class));
	    }
	    try {
		int i = 0;
		do {
		    prs.subParseProposedClasses(prs.indexedProperty(
			    "dataBlocks", ThirdPartyParseable.class, i),
			    ClassInclusion.nestedClassesOf(DataBlock.class));
		    // if(getDataBlocks()[i++] instanceof
		    // DataBlock.EOFBlock)break;
		} while (!(getDataBlocks().get(i++) instanceof DataBlock.EOFBlock));
	    } catch (UnrecognizedFormatException e) {
	    }
	}// end describeFormat(Parser prs)

	public static class DataBlock {
	    /**
	     * Added 12/21/2012
	     * 
	     * @author Chuck Ritola
	     * 
	     */
	    public static class LineSegmentBlock implements ThirdPartyParseable {
		private int color;
		private int vertexID1;
		private int vertexID2;

		@Override
		public void describeFormat(Parser prs) {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x16 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("color", int.class));
		    prs.int4s(prs.property("vertexID1", int.class));
		    prs.int4s(prs.property("vertexID2", int.class));
		}

		/**
		 * @return the color
		 */
		public int getColor() {
		    return color;
		}

		/**
		 * @param color
		 *            the color to set
		 */
		public void setColor(int color) {
		    this.color = color;
		}

		/**
		 * @return the vertexID1
		 */
		public int getVertexID1() {
		    return vertexID1;
		}

		/**
		 * @param vertexID1
		 *            the vertexID1 to set
		 */
		public void setVertexID1(int vertexID1) {
		    this.vertexID1 = vertexID1;
		}

		/**
		 * @return the vertexID2
		 */
		public int getVertexID2() {
		    return vertexID2;
		}

		/**
		 * @param vertexID2
		 *            the vertexID2 to set
		 */
		public void setVertexID2(int vertexID2) {
		    this.vertexID2 = vertexID2;
		}
	    }// end LineSegmentBlock

	    /**
	     * Added 1/17/2013 Suspected to be textured face for live radar map
	     * 
	     * @author Chuck Ritola
	     */

	    public static class FaceBlock05 extends NoUVFaceBlock {
		@Override
		protected byte getBlockID() {
		    return 0x05;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertex.class;
		}
	    }// end FaceBlock05

	    /**
	     * Added 10/12/2012 as "Unknown1" Suspected to be double sided
	     * faceblock.
	     * 
	     * @author Chuck Ritola
	     * 
	     */
	    public static class DoubleSidedFaceBlock extends FaceBlock {
		@Override
		protected byte getBlockID() {
		    return 0x22;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}
	    }// end DoubleSidedFaceBlock

	    public static class ArenaFaceBlock extends FaceBlock {
		@Override
		protected byte getBlockID() {
		    return 0x34;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}

	    }// end FaceBlockXXX

	    public static class MTM2TransparentZeroFaceBlock extends FaceBlock {

		@Override
		protected byte getBlockID() {
		    return 0x33;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}

	    }// end FaceBlockXXX

	    public static class OpaqueZeroShinyFaceBlock extends FaceBlock {

		@Override
		protected byte getBlockID() {
		    return 0x29;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}

	    }// end FaceBlockXXX

	    public static class OpaqueZeroFaceBlock extends FaceBlock {

		@Override
		protected byte getBlockID() {
		    return 0x18;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}

	    }// end FaceBlockXXX

	    public static class MTM1TransparentZeroFaceBlock extends FaceBlock {

		@Override
		protected byte getBlockID() {
		    return 0x11;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}

	    }// end FaceBlockXXX

	    public static class FaceBlockOE extends FaceBlock {

		@Override
		protected byte getBlockID() {
		    return 0x0E;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;//TODO: Not really known.
		}

	    }// end FaceBlockXXX

	    public static class FaceBlock19 extends FaceBlock {
/*
		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x19 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("numVertices", int.class));
		    prs.int4s(prs.property("normalX", int.class));
		    prs.int4s(prs.property("normalY", int.class));
		    prs.int4s(prs.property("normalZ", int.class));
		    prs.int4s(prs.property("magic", int.class));
		    for (int i = 0; i < getNumVertices(); i++) {
			prs.subParseProposedClasses(prs.indexedProperty(
				"vertices", ShortFaceBlockVertex.class, i),
				ClassInclusion
					.classOf(ShortFaceBlockVertex.class));
		    }

		}// end describeFormat(Parser prs)
		*/
		@Override
		protected byte getBlockID() {
		    return 0x19;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertex.class;
		}
	    }// end FaceBlock19

	    public static abstract class FaceBlock implements
		    ThirdPartyParseable {
		int numVertices, normalX, normalY, normalZ, magic;
		public static final int BACKDROP_MAGIC = 0x80000000;

		protected abstract byte getBlockID();

		ArrayList<FaceBlockVertex> vertices;

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, getBlockID() },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("numVertices", int.class));
		    prs.int4s(prs.property("normalX", int.class));
		    prs.int4s(prs.property("normalY", int.class));
		    prs.int4s(prs.property("normalZ", int.class));
		    prs.int4s(prs.property("magic", int.class));
		    for (int i = 0; i < getNumVertices(); i++) {
			prs.subParseProposedClasses(prs.indexedProperty(
				"vertices", getFaceBlockVertexType(), i),
				ClassInclusion.classOf(getFaceBlockVertexType()));
		    }
		}// end describeFormat(Parser prs)
		
		protected abstract Class<? extends FaceBlockVertex> getFaceBlockVertexType();
		
		public static class FaceBlockVertexWithUV extends FaceBlockVertex implements ThirdPartyParseable{
		    int textureCoordinateU, textureCoordinateV;
		    @Override
		    public void describeFormat(Parser prs) 
			    throws UnrecognizedFormatException{
			super.describeFormat(prs);
			prs.int4s(prs.property("textureCoordinateU", int.class));
			prs.int4s(prs.property("textureCoordinateV", int.class));
		    }//end describeFormat()
		    
		    /**
		     * @return the textureCoordinateU
		     */
		    public int getTextureCoordinateU() {
			return textureCoordinateU;
		    }

		    /**
		     * @param textureCoordinateU
		     *            the textureCoordinateU to set
		     */
		    public void setTextureCoordinateU(int textureCoordinateU) {
			this.textureCoordinateU = textureCoordinateU;
		    }

		    /**
		     * @return the textureCoordinateV
		     */
		    public int getTextureCoordinateV() {
			return textureCoordinateV;
		    }

		    /**
		     * @param textureCoordinateV
		     *            the textureCoordinateV to set
		     */
		    public void setTextureCoordinateV(int textureCoordinateV) {
			this.textureCoordinateV = textureCoordinateV;
		    }
		}//end FaceBlockVertexWithUV

		public static class FaceBlockVertex implements
			ThirdPartyParseable {
		    int vertexIndex;

		    @Override
		    public void describeFormat(Parser prs)
			    throws UnrecognizedFormatException {
			prs.int4s(prs.property("vertexIndex", int.class));
		    }

		    /**
		     * @return the vertexIndex
		     */
		    public int getVertexIndex() {
			return vertexIndex;
		    }

		    /**
		     * @param vertexIndex
		     *            the vertexIndex to set
		     */
		    public void setVertexIndex(int vertexIndex) {
			this.vertexIndex = vertexIndex;
		    }
		}// end FaceBlockVertex

		/**
		 * @return the numVertices
		 */
		public int getNumVertices() {
		    return numVertices;
		}

		/**
		 * @param numVertices
		 *            the numVertices to set
		 */
		public void setNumVertices(int numVertices) {
		    this.numVertices = numVertices;
		}

		/**
		 * @return the normalX
		 */
		public int getNormalX() {
		    return normalX;
		}

		/**
		 * @param normalX
		 *            the normalX to set
		 */
		public void setNormalX(int normalX) {
		    this.normalX = normalX;
		}

		/**
		 * @return the normalY
		 */
		public int getNormalY() {
		    return normalY;
		}

		/**
		 * @param normalY
		 *            the normalY to set
		 */
		public void setNormalY(int normalY) {
		    this.normalY = normalY;
		}

		/**
		 * @return the normalZ
		 */
		public int getNormalZ() {
		    return normalZ;
		}

		/**
		 * @param normalZ
		 *            the normalZ to set
		 */
		public void setNormalZ(int normalZ) {
		    this.normalZ = normalZ;
		}

		/**
		 * @return the magic
		 */
		public int getMagic() {
		    return magic;
		}

		/**
		 * @param magic
		 *            the magic to set
		 */
		public void setMagic(int magic) {
		    this.magic = magic;
		}

		/**
		 * @return the vertices
		 */
		public List<FaceBlockVertex> getVertices() {
		    return vertices;
		}

		/**
		 * @param vertices
		 *            the vertices to set
		 */
		public void setVertices(ArrayList<FaceBlockVertex> vertices) {
		    this.vertices = vertices;
		}

	    }// end FaceBlockXXX

	    public static class Unknown0C implements ThirdPartyParseable {
		byte[] unknown = new byte[24];

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x0C },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.bytesOfCount(24, prs.property("unknown", byte.class));
		}

		/**
		 * @return the unknown
		 */
		public byte[] getUnknown() {
		    return unknown;
		}

		/**
		 * @param unknown
		 *            the unknown to set
		 */
		public void setUnknown(byte[] unknown) {
		    this.unknown = unknown;
		}
	    }// end Unknown0C

	    public static class Unknown12 implements ThirdPartyParseable {
		byte[] unknown = new byte[4];

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x12 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.bytesOfCount(4, prs.property("unknown", byte.class));
		}

		/**
		 * @return the unknown
		 */
		public byte[] getUnknown() {
		    return unknown;
		}

		/**
		 * @param unknown
		 *            the unknown to set
		 */
		public void setUnknown(byte[] unknown) {
		    this.unknown = unknown;
		}
	    }// end Unknown12

	    public static class TextureBlock implements ThirdPartyParseable {
		int unknown;
		String textureFileName;

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x0D },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("unknown", int.class));
		    prs.stringOfLength(16,
			    prs.property("textureFileName", String.class));
		}

		/**
		 * @return the unknown
		 */
		public int getUnknown() {
		    return unknown;
		}

		/**
		 * @param unknown
		 *            the unknown to set
		 */
		public void setUnknown(int unknown) {
		    this.unknown = unknown;
		}

		/**
		 * @return the textureFileName
		 */
		public String getTextureFileName() {
		    return textureFileName;
		}

		/**
		 * @param textureFileName
		 *            the textureFileName to set
		 */
		public void setTextureFileName(String textureFileName) {
		    this.textureFileName = textureFileName;
		}

	    }// end TextureBlock

	    public static class ColorBlock implements ThirdPartyParseable {
		byte[] bytes = new byte[4];

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x0A },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.bytesOfCount(4, prs.property("bytes", byte.class));
		}

		/**
		 * @return the bytes
		 */
		public byte[] getBytes() {
		    return bytes;
		}

		/**
		 * @param bytes
		 *            the bytes to set
		 */
		public void setBytes(byte[] bytes) {
		    this.bytes = bytes;
		}
	    }// end ColorBlock

	    public static abstract class NoUVFaceBlock extends FaceBlock {
		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, getBlockID() },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("numVertices", int.class));
		    prs.int4s(prs.property("normalX", int.class));
		    prs.int4s(prs.property("normalY", int.class));
		    prs.int4s(prs.property("normalZ", int.class));
		    prs.int4s(prs.property("magic", int.class));
		    for (int i = 0; i < getNumVertices(); i++) {
			prs.subParseProposedClasses(prs.indexedProperty(
				"vertices", ShortFaceBlockVertex.class, i),
				ClassInclusion
					.classOf(ShortFaceBlockVertex.class));
		    }//end for(vertices)
		}// end describeFormat(Parser prs)

		protected abstract byte getBlockID();

		public static class ShortFaceBlockVertex extends
			FaceBlockVertex {
		    @Override
		    public void describeFormat(Parser prs)
			    throws UnrecognizedFormatException {
			prs.int4s(prs.property("vertexIndex", int.class));
		    }
		}// end ShortFaceBlockVertex
	    }// end NoUVFaceBlock

	    public static class BillboardSprite0x0F extends NoUVFaceBlock {
		@Override
		protected byte getBlockID() {
		    return 0x0F;
		}

		@Override
		protected Class<? extends FaceBlockVertex> getFaceBlockVertexType() {
		    return FaceBlockVertexWithUV.class;
		}
	    }// end Unknown 0x0F

	    public static class BillboardTexCoords0x04 implements
		    ThirdPartyParseable {
		private int numVertices;
		private List<UVCoordinate> vertices = new ArrayList<UVCoordinate>();

		@Override
		public void describeFormat(Parser prs) {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x04 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.expectBytes(new byte[] { 0, 0, 0, 0 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("numVertices", int.class));
		    for (int i = 0; i < getNumVertices(); i++) {
			prs.subParseProposedClasses(prs.indexedProperty("vertices", UVCoordinate.class,
				i),ClassInclusion
				.classOf(UVCoordinate.class));
		    }
		}// end describeFormat(..)
		
		public static class UVCoordinate implements
			ThirdPartyParseable {
		    int textureCoordinateU, textureCoordinateV;
		    
		    @Override
		    public void describeFormat(Parser prs)
			    throws UnrecognizedFormatException {
			prs.int4s(prs.property("textureCoordinateU", int.class));
			prs.int4s(prs.property("textureCoordinateV", int.class));
		    }

		    /**
		     * @return the textureCoordinateU
		     */
		    public int getTextureCoordinateU() {
		        return textureCoordinateU;
		    }

		    /**
		     * @param textureCoordinateU the textureCoordinateU to set
		     */
		    public void setTextureCoordinateU(int textureCoordinateU) {
		        this.textureCoordinateU = textureCoordinateU;
		    }

		    /**
		     * @return the textureCoordinateV
		     */
		    public int getTextureCoordinateV() {
		        return textureCoordinateV;
		    }

		    /**
		     * @param textureCoordinateV the textureCoordinateV to set
		     */
		    public void setTextureCoordinateV(int textureCoordinateV) {
		        this.textureCoordinateV = textureCoordinateV;
		    }
		}//end UVCoordinate

		/**
		 * @return the numVertices
		 */
		public int getNumVertices() {
		    return numVertices;
		}

		/**
		 * @param numVertices
		 *            the numVertices to set
		 */
		public void setNumVertices(int numVertices) {
		    this.numVertices = numVertices;
		}

		/**
		 * @return the vertices
		 */
		public List<UVCoordinate> getVertices() {
		    return vertices;
		}

		/**
		 * @param vertices
		 *            the vertices to set
		 */
		public void setVertices(List<UVCoordinate> vertices) {
		    this.vertices = vertices;
		}
	    }// end Unknown 0x04

	    public static class AnimatedTextureBlock implements
		    ThirdPartyParseable {
		int unknown1, numTextures, unknown2, delay, unknown3, unknown4;
		// String [] frameNames;
		ArrayList<String> frameNames;

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x1D },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("unknown1", int.class));
		    prs.int4s(prs.property("numTextures", int.class));
		    prs.int4s(prs.property("unknown2", int.class));
		    prs.int4s(prs.property("delay", int.class));
		    prs.int4s(prs.property("unknown3", int.class));
		    prs.int4s(prs.property("unknown4", int.class));
		    if (getNumTextures() > 100 || getNumTextures() < 0) {
			System.out.println("Unreasonable number of textures: "
				+ getNumTextures());
			System.err.println("Trouble ahead...");
			prs.dumpState();
		    }
		    for (int i = 0; i < getNumTextures(); i++) {
			prs.stringOfLength(32, prs.indexedProperty(
				"frameNames", String.class, i));
		    }
		}// end describeFormat(Parser prs)

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
		 * @return the numTextures
		 */
		public int getNumTextures() {
		    return numTextures;
		}

		/**
		 * @param numTextures
		 *            the numTextures to set
		 */
		public void setNumTextures(int numTextures) {
		    this.numTextures = numTextures;
		}

		/**
		 * @return the unknown2
		 */
		public int getUnknown2() {
		    return unknown2;
		}

		/**
		 * @param unknown2
		 *            the unknown2 to set
		 */
		public void setUnknown2(int unknown2) {
		    this.unknown2 = unknown2;
		}

		/**
		 * @return Time between frames in 65535ths of a second.
		 *         (return=65535/frames per second)
		 */
		public int getDelay() {
		    return delay;
		}

		/**
		 * @param delay
		 *            the delay to set
		 */
		public void setDelay(int delay) {
		    this.delay = delay;
		}

		/**
		 * @return the unknown3
		 */
		public int getUnknown3() {
		    return unknown3;
		}

		/**
		 * @param unknown3
		 *            the unknown3 to set
		 */
		public void setUnknown3(int unknown3) {
		    this.unknown3 = unknown3;
		}

		/**
		 * @return the unknown4
		 */
		public int getUnknown4() {
		    return unknown4;
		}

		/**
		 * @param unknown4
		 *            the unknown4 to set
		 */
		public void setUnknown4(int unknown4) {
		    this.unknown4 = unknown4;
		}

		/**
		 * @return the frameNames
		 */
		public List<String> getFrameNames() {
		    return frameNames;
		}

		/**
		 * @param frameNames
		 *            the frameNames to set
		 */
		public void setFrameNames(ArrayList<String> frameNames) {
		    this.frameNames = frameNames;
		}

	    }// end AnimatedTextureBlock

	    public static class Unknown17 implements ThirdPartyParseable {
		int unknown1, unknown2;

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.littleEndian();
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x17 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("unknown1", int.class));
		    prs.int4s(prs.property("unknown2", int.class));
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
		 * @return the unknown2
		 */
		public int getUnknown2() {
		    return unknown2;
		}

		/**
		 * @param unknown2
		 *            the unknown2 to set
		 */
		public void setUnknown2(int unknown2) {
		    this.unknown2 = unknown2;
		}

	    }// end Unknown0

	    public static class VertexNormal implements ThirdPartyParseable {
		int x, y, z;

		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {
		    prs.expectBytes(new byte[] { 0, 0, 0, 0x03 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		    prs.int4s(prs.property("x", int.class));
		    prs.int4s(prs.property("y", int.class));
		    prs.int4s(prs.property("z", int.class));
		}

		/**
		 * @return the x
		 */
		public int getX() {
		    return x;
		}

		/**
		 * @param x
		 *            the x to set
		 */
		public void setX(int x) {
		    this.x = x;
		}

		/**
		 * @return the y
		 */
		public int getY() {
		    return y;
		}

		/**
		 * @param y
		 *            the y to set
		 */
		public void setY(int y) {
		    this.y = y;
		}

		/**
		 * @return the z
		 */
		public int getZ() {
		    return z;
		}

		/**
		 * @param z
		 *            the z to set
		 */
		public void setZ(int z) {
		    this.z = z;
		}
	    }// end VertexNormal

	    public static class EOFBlock implements ThirdPartyParseable {
		@Override
		public void describeFormat(Parser prs)
			throws UnrecognizedFormatException {// End of file is
							    // all zeroes
		    prs.expectBytes(new byte[] { 0x00, 0x00, 0x00, 0x00 },
			    FailureBehavior.UNRECOGNIZED_FORMAT);
		}
	    }// end EOFBlock
	}// DataBlock

	public static class Vertex implements ThirdPartyParseable {
	    int x, y, z;

	    @Override
	    public void describeFormat(Parser prs)
		    throws UnrecognizedFormatException {
		prs.int4s(prs.property("x", int.class));
		prs.int4s(prs.property("y", int.class));
		prs.int4s(prs.property("z", int.class));
	    }

	    /**
	     * @return the x
	     */
	    public int getX() {
		return x;
	    }

	    /**
	     * @param x
	     *            the x to set
	     */
	    public void setX(int x) {
		this.x = x;
	    }

	    /**
	     * @return the y
	     */
	    public int getY() {
		return y;
	    }

	    /**
	     * @param y
	     *            the y to set
	     */
	    public void setY(int y) {
		this.y = y;
	    }

	    /**
	     * @return the z
	     */
	    public int getZ() {
		return z;
	    }

	    /**
	     * @param z
	     *            the z to set
	     */
	    public void setZ(int z) {
		this.z = z;
	    }

	}// end Vertex

	/**
	 * @return the scale
	 */
	public int getScale() {
	    return scale;
	}

	/**
	 * @param scale
	 *            the scale to set
	 */
	public void setScale(int scale) {
	    this.scale = scale;
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
	 * @return the unknown2
	 */
	public int getUnknown2() {
	    return unknown2;
	}

	/**
	 * @param unknown2
	 *            the unknown2 to set
	 */
	public void setUnknown2(int unknown2) {
	    this.unknown2 = unknown2;
	}

	/**
	 * @return the numVertices
	 */
	public int getNumVertices() {
	    return numVertices;
	}

	/**
	 * @param numVertices
	 *            the numVertices to set
	 */
	public void setNumVertices(int numVertices) {
	    this.numVertices = numVertices;
	}

	/**
	 * @return the vertices
	 */
	public ArrayList<Vertex> getVertices() {
	    return vertices;
	}

	/**
	 * @param vertices
	 *            the vertices to set
	 */
	public void setVertices(ArrayList<Vertex> vertices) {
	    this.vertices = vertices;
	}

	/**
	 * @return the dataBlocks
	 */
	public List<ThirdPartyParseable> getDataBlocks() {
	    return dataBlocks;
	}

	/**
	 * @param dataBlocks
	 *            the dataBlocks to set
	 */
	public void setDataBlocks(ArrayList<ThirdPartyParseable> dataBlocks) {
	    this.dataBlocks = dataBlocks;
	}
    }// end Model

    public static class AnimationControl extends SelfParsingFile {
	int unknown1, numFrames, delay, unknown2, unknown3;
	// String [] binFiles;
	ArrayList<String> binFiles;

	public AnimationControl(InputStream is) throws IllegalAccessException,
		IOException {
	    super(is);
	}

	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.littleEndian();
	    prs.expectBytes(new byte[] { 0x00, 0x00, 0x00, 0x20 },
		    FailureBehavior.UNRECOGNIZED_FORMAT);
	    prs.int4s(prs.property("unknown1", int.class));
	    prs.int4s(prs.property("numFrames", int.class));
	    prs.int4s(prs.property("delay", int.class));
	    prs.int4s(prs.property("unknown2", int.class));
	    prs.int4s(prs.property("unknown3", int.class));
	    for (int i = 0; i < getNumFrames(); i++) {
		prs.stringOfLength(16,
			prs.indexedProperty("binFiles", String.class, i));
	    }
	}// end describeFormat(Parser prs)

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
	 * @return the numFrames
	 */
	public int getNumFrames() {
	    return numFrames;
	}

	/**
	 * @param numFrames
	 *            the numFrames to set
	 */
	public void setNumFrames(int numFrames) {
	    this.numFrames = numFrames;
	}

	/**
	 * @return the delay
	 */
	public int getDelay() {
	    return delay;
	}

	/**
	 * @param delay
	 *            the delay to set
	 */
	public void setDelay(int delay) {
	    this.delay = delay;
	}

	/**
	 * @return the unknown2
	 */
	public int getUnknown2() {
	    return unknown2;
	}

	/**
	 * @param unknown2
	 *            the unknown2 to set
	 */
	public void setUnknown2(int unknown2) {
	    this.unknown2 = unknown2;
	}

	/**
	 * @return the unknown3
	 */
	public int getUnknown3() {
	    return unknown3;
	}

	/**
	 * @param unknown3
	 *            the unknown3 to set
	 */
	public void setUnknown3(int unknown3) {
	    this.unknown3 = unknown3;
	}

	/**
	 * @return the binFiles
	 */
	public List<String> getBinFiles() {
	    return binFiles;
	}

	/**
	 * @param binFiles
	 *            the binFiles to set
	 */
	public void setBinFiles(ArrayList<String> binFiles) {
	    this.binFiles = binFiles;
	}

    }// end AnimationControl
}// end BINFile(...)
