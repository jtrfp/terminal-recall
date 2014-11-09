/*******************************************************************************
 * This file is part of jTRFP
 * Copyright (c) 2012-2014. See commit history for copyright owners.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     chuck - initial API and implementation
 ******************************************************************************/
package org.jtrfp.trcl.file;

import org.jtrfp.jfdt.Parser;
import org.jtrfp.jfdt.ThirdPartyParseable;
import org.jtrfp.jfdt.UnrecognizedFormatException;
import org.jtrfp.jtrfp.Vertex3f;

/**
 * Abstract implementation of a Terminal Reality heading vector consisting of a triplet of signed ints 
 * followed by a Windows-style carriage-return newline, or a comma if using EndingWithComma nested class.
 * Ending with a comma is useful if the triplet is part of a longer list of comma-separated values.
 * @author Chuck Ritola
 *
 */
public class AbstractTriplet implements ThirdPartyParseable {
    int x, y, z;

    @Override
    public void describeFormat(Parser prs) throws UnrecognizedFormatException {
	prs.stringEndingWith(",", prs.property("x", Integer.class), false);
	prs.stringEndingWith(",", prs.property("y", Integer.class), false);
	prs.stringEndingWith("\r\n", prs.property("z", Integer.class), false);
    }

    public static class EndingWithComma extends AbstractTriplet {
	@Override
	public void describeFormat(Parser prs)
		throws UnrecognizedFormatException {
	    prs.stringEndingWith(",", prs.property("x", Integer.class), false);
	    prs.stringEndingWith(",", prs.property("y", Integer.class), false);
	    prs.stringEndingWith(",", prs.property("z", Integer.class), false);
	}
    }// end EndingWithComma

    /**
     * @return legacy X component of this vector.
     */
    public int getX() {
	return x;
    }

    /**
     * @param Legacy X component for this vector.
     */
    public void setX(int x) {
	this.x = x;
    }

    /**
     * @return Legacy Y component of this vector.
     */
    public int getY() {
	return y;
    }

    /**
     * @param Legacy Y component for this vector.
     */
    public void setY(int y) {
	this.y = y;
    }

    /**
     * @return Legacy Z component of this vector.
     */
    public int getZ() {
	return z;
    }

    /**
     * @param Legacy Z component for this vector.
     */
    public void setZ(int z) {
	this.z = z;
    }

    @Override
    public String toString() {
	return this.getClass().getSimpleName() + " x=" + x + " y=" + y + " z="
		+ z + "\n";
    }
    
    /**
     * Returns an immutable representation of this triplet as a Vertex3f.
     * The returned Vertex3f will not reflect changes to this object.
     * @return A vertex3f of the values at the time of invocation.
     * @since Nov 9, 2014
     */
    public Vertex3f asVertex3f(){
	return new Vertex3f((float)x,(float)y,(float)z);
    }
    
    /**
     * Imports contents of a vertex3f into this object.
     * Note that float-int conversion may result in lost precision and range.
     * @return this object
     * @since Nov 9, 2014
     */
    public AbstractTriplet importFromVertex3f(Vertex3f importFrom){
	setX((int)importFrom.getX());
	setY((int)importFrom.getY());
	setZ((int)importFrom.getZ());
	return this;
    }
}// end AbstractVector
