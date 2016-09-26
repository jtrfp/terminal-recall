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
package org.jtrfp.trcl.core;

public class TriangleVertex2FlatDoubleWindow implements FlatDoubleWindow {
    private final 	TriangleVertexWindow 	parentWindow;
    private final 	int 			NUM_ELEMENTS = Variable.values().length;
    
    public TriangleVertex2FlatDoubleWindow(TriangleVertexWindow parentWindow){
	this.parentWindow=parentWindow;
    }//end constructor
    
    public static enum Variable{
	X(0),
	Y(1),
	Z(2),
	U(3),
	V(4),
	NORMX(5),
	NORMY(6),
	NORMZ(7);
	private int idOffset;
	private Variable(int idOffset){
	    this.idOffset=idOffset;
	}
	protected int getIDOff(){return idOffset;}
	public int getFlatID(int vertexID){
	    return vertexID*Variable.values().length+getIDOff();
	}
    }//end enum Variable
    
    @Override
    public void set(int id, double val){
	final int vid=id/NUM_ELEMENTS;
	id%=NUM_ELEMENTS;
	switch (id){
	    case 0:
		parentWindow.x.set(vid,(short)val);
		break;
	    case 1:
		parentWindow.y.set(vid,(short)val);
		break;
	    case 2:
		parentWindow.z.set(vid,(short)val);
		break;
	    case 3:
		parentWindow.u.set(vid,(short)val);
		break;
	    case 4:
		parentWindow.v.set(vid,(short)val);
		break;
	    case 5:
		parentWindow.normX.set(vid, (byte)val);
		break;
	    case 6:
		parentWindow.normY.set(vid, (byte)val);
		break;
	    case 7:
		parentWindow.normZ.set(vid, (byte)val);
		break;
	}//end switch(...)
    }//end set(...)

    @Override
    public void flush() {
	parentWindow.flush();
    }
}//end Triangle...Window
