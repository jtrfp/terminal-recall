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

import java.util.Arrays;

import org.jtrfp.trcl.core.TR;

public class CharLineDisplay {
    private char[] 		content;
    private final CharDisplay[] displays;
    double[] 			position 
    	= new double[] { 0, 0, 0 };
    private GLFont 		font;
    private double 		glSize;
    private double 		totGlLen = 0;
    private boolean 		centered = false;

    public CharLineDisplay(TR tr, RenderableSpacePartitioningGrid grid,
	    double glSize, int lengthInChars, GLFont font) {
	content = new char[lengthInChars];
	displays = new CharDisplay[lengthInChars];
	this.font = font;
	for (int i = 0; i < lengthInChars; i++) {
	    final CharDisplay charDisplay = new CharDisplay(tr, grid, glSize, font);
	    content[i] = 'X';
	    charDisplay.setVisible(false);
	    charDisplay.setChar('X');
	    charDisplay.setImmuneToOpaqueDepthTest(true);
	    grid.add(charDisplay);
	    displays[i] = charDisplay;
	}// end for(lengthInChars)

	this.glSize = glSize;
	updatePositions();
    }// end LineDisplay(...)

    public void setContent(String content) {
	for (int i = 0; i < this.content.length; i++) {
	    char newContent=0;
	    if (i < content.length()) {
		final char c = content.charAt(i);
		if(c>31 && c<127)
		    newContent = c;
	    }
	    this.content[i] = newContent;
	    displays[i].setChar(newContent);
	}// end for(length)
	updatePositions();
    }// end setContent(...)

    private void updatePositions() {
	final double[] charPosition = Arrays.copyOf(position, 3);
	totGlLen = 0;
	// Determine total length;
	for (int i = 0; i < displays.length; i++) {
	    char _content = content[i];
	    if (_content != 0) {
		final double progress = ((double) glSize)
			* font.glWidthOf(_content) * 1.1;// 1.1 fudge factor for
							 // space between
							 // letters
		totGlLen += progress;
	    }
	}// end for(displays)
	if (centered)
	    charPosition[0] -= totGlLen / 2.;
	for (int i = 0; i < displays.length; i++) {
	    final double[] dispPos = displays[i].getPosition();
	    if(content[i]!=0){
		 dispPos[0] = charPosition[0];
		 dispPos[1] = charPosition[1];
		 dispPos[2] = charPosition[2];
	    }else{
		displays[i].setVisible(false);
		dispPos[0]= Math.random();
		dispPos[1]= Math.random();
		dispPos[2]= Math.random();
	    }
	    displays[i].notifyPositionChange();
	    char _content = content[i];
	    final double progress = ((double) glSize)
		    * font.glWidthOf(_content) * 1.1;// 1.1 fudge factor for
						     // space between letters
	    charPosition[0] += progress;
	}// end for(displays)
    }// end updatePositions

    public void setPosition(double[] location) {
	this.position = location;
	updatePositions();
    }// end setPosition(...)

    public void setPosition(double x, double y, double z) {
	position[0] = x;
	position[1] = y;
	position[2] = z;
	updatePositions();
    }

    /**
     * @return the displays
     */
    public CharDisplay[] getDisplays() {
	return displays;
    }

    public GLFont getFont() {
	return font;
    }

    /**
     * @return the centered
     */
    public boolean isCentered() {
	return centered;
    }

    /**
     * @param centered
     *            the centered to set
     */
    public void setCentered(boolean centered) {
	this.centered = centered;
    }

    public void setVisible(boolean b) {
	for (int i = 0; i<displays.length; i++) {
	    if(content[i]!=0)displays[i].setVisible(b);
	    else displays[i].setVisible(false);
	}// end for(displays)
    }// end setVisible(...)

    public void setFontSize(double glSize) {
	this.glSize=glSize;
	for(CharDisplay disp : displays){
	    disp.setFontSize(glSize);
	}
    }
}// end LineDisplay
