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
package org.jtrfp.trcl.mem;

import java.nio.ByteBuffer;

/**
 * ByteBuffer cannot (legitimately) be extended so a new interface must be constructed to create what's effectively
 * a crude ByteBuffer. This is used for utilities like the PagingByteBuffer, however other buffers may implement this 
 * interface for interoperability.
 * @author Chuck Ritola
 *
 */

public interface IByteBuffer {
    public IByteBuffer putShort(int indexInBytes,short val);
    public IByteBuffer putFloat(int indexInBytes,float val);
    public IByteBuffer putInt(int indexInBytes,int val);
    public IByteBuffer putInts(int indexInBytes,int []vals);
    public IByteBuffer put(int indexInBytes,byte val);
    public IByteBuffer put(int startIndexInBytes, ByteBuffer src);
    public byte get(int indexInBytes);
    public short getShort(int indexInBytes);
    public int logical2PhysicalAddressBytes(int logicalAddressInBytes);
    public double getFloat(int posInBytes);
    public Integer getInt(int posInBytes);
}//end IByteBuffer
