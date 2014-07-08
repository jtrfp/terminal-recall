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

package org.jtrfp.trcl;

import java.util.ArrayList;
import java.util.Scanner;

import org.jtrfp.trcl.core.TR;
import org.jtrfp.trcl.obj.PositionedRenderable;

public class CharAreaDisplay extends RenderableSpacePartitioningGrid {
    private final int 		heightChars;
    private final CharLineDisplay[]lines;
    private final double 	FONT_SIZE=.07;
    private ArrayList<String>	lineStrings = new ArrayList<String>();
    private double 		startString;
    private double		positionX,positionY,positionZ;
    
    public CharAreaDisplay(SpacePartitioningGrid<PositionedRenderable> parent, int widthChars, int heightChars, TR tr, GLFont font) {
	super(parent);
	this.heightChars=heightChars;
	lines = new CharLineDisplay[heightChars];
	for(int i=0; i<heightChars; i++){
	    lines[i]=new CharLineDisplay(tr, this, FONT_SIZE, widthChars, font);
	}//end for(i)
	updatePositions();
    }//end constructor
    
    public CharAreaDisplay setContent(String content){
	final Scanner s = new Scanner(content);
	lineStrings.clear();
	while(s.hasNext()){
	    final String line = s.nextLine();
	    lineStrings.add(line);
	}
	s.close();
	updateStrings();
	return this;
    }//end setContent()
    
    public void setScollPosition(double newScrollPos){
	startString=newScrollPos;
	updatePositions();
	updateStrings();
    }//end setScrollPosition(...)
    
    public void setPosition(double x, double y, double z){
	positionX=x;
	positionY=y;
	positionZ=z;
	updatePositions();
    }//end setPosition(...)

    private void updateStrings() {
	for (int i = 0; i < heightChars; i++) {
	    final int index = (int) Math.floor(startString) + i - heightChars;
	    if (lineStrings.size() > index&&index>=0) {
		final String line = lineStrings.get(index);
		if (line != null)
		    lines[i].setContent(line);
	    }// end if(lineStrings.size())
	    else lines[i].setContent("");
	}//end end(i)
    }// end updateStrings()
    
    private void updatePositions(){
	for(int i=0; i<heightChars; i++){
	    lines[i].setPosition(positionX, positionY-(i-(startString%1.))*FONT_SIZE, positionZ);
	}//end for(i)
    }//end updatePositions()
}//end CharAreaDisplay
